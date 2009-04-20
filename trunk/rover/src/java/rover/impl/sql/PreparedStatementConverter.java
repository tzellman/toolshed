/* =============================================================================
 * This file is part of Rover
 * =============================================================================
 * (C) Copyright 2009, Tom Zellman, tzellman@gmail.com
 *
 * Rover is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package rover.impl.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jester.ConverterRegistry;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.SerializationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rover.IDatabaseContext;
import rover.IFieldInfo;
import rover.ITableInfo;
import rover.QueryConstants;
import rover.RoverUtils;
import rover.SQLTypeConverter;

/**
 * Converter that turns an Object into a {@link PreparedStatement}.
 * 
 * The object (of table fields/values) must conform to a {@link Map} interface,
 * and the hints map is required to provide the table name. An optional "where"
 * clause for a database UPDATE can also be provided in the hints map.
 * 
 * @author tzellman
 * 
 */
public class PreparedStatementConverter extends
        ConverterRegistry<Object, PreparedStatement>
{
    private static final Log log = LogFactory
            .getLog(PreparedStatementConverter.class);

    public static final String HINT_FIELD_MAP = "fieldMap";

    public static final String HINT_TABLE_NAME = "tableName";

    public static final String HINT_WHERE = "where";

    public static final String HINT_CONNECTION = "connection";

    public static final String HINT_RETURN_SQL = "returnSQL";

    private IDatabaseContext databaseContext;

    public PreparedStatementConverter(IDatabaseContext databaseContext)
    {
        this.databaseContext = databaseContext;
    }

    protected Object toSQLObject(ITableInfo tableInfo, String field,
            String value) throws SerializationException
    {
        IFieldInfo fieldInfo = tableInfo.getFields().get(field.toUpperCase());
        if (fieldInfo == null)
            throw new SerializationException("Invalid table field: " + field);

        Integer sqlType = QueryConstants.SQL_TYPE_NAMES.get(fieldInfo
                .getSQLType());

        // add a hint for the dispatcher
        Map hints = new HashMap();
        hints.put(SQLTypeConverter.HINT_SQL_TYPE, sqlType);

        // turn the String value into an Object for the query
        return databaseContext.getSQLTypeConverter().convert(value, hints);
    }

    @Override
    public Object defaultConvert(Object from, Class<? extends Object> toClass,
            Map hints) throws SerializationException
    {
        if (from != null && Map.class.isAssignableFrom(from.getClass()))
        {
            Map map = (Map) from;

            if (map.isEmpty())
                throw new SerializationException("Missing fields");

            if (hints == null || !hints.containsKey(HINT_TABLE_NAME))
                throw new SerializationException(
                        "Missing table name in the hints map");

            String tableName = ObjectUtils.toString(hints.get(HINT_TABLE_NAME));
            ITableInfo tableInfo;
            try
            {
                tableInfo = databaseContext.getDatabaseInfo().getTableInfo(
                        tableName);
            }
            catch (Exception e)
            {
                throw new SerializationException(e);
            }

            List<String> fields = new LinkedList<String>();
            List<Object> values = new LinkedList<Object>();
            for (Object key : map.keySet())
            {
                // this could be dangerous... maybe we should force Strings
                String sKey = ObjectUtils.toString(key);
                String sVal = ObjectUtils.toString(map.get(key));
                fields.add(sKey);
                values.add(toSQLObject(tableInfo, sKey, sVal));
            }

            StringBuffer buf = new StringBuffer();
            Object where = hints.get(HINT_WHERE);
            if (where != null && Map.class.isAssignableFrom(where.getClass()))
            {
                // update

                Map whereMap = (Map) where;

                if (map.isEmpty())
                    throw new SerializationException("Missing where clause");

                List<String> whereFields = new LinkedList<String>();
                for (Object key : whereMap.keySet())
                {
                    // this could be dangerous... maybe we should force Strings
                    String sKey = ObjectUtils.toString(key);
                    String sVal = ObjectUtils.toString(map.get(key));
                    whereFields.add(sKey);
                    values.add(toSQLObject(tableInfo, sKey, sVal));
                }

                buf.append("UPDATE ");
                buf.append(tableName);
                buf.append(" SET ");

                for (Iterator<String> iterator = fields.iterator(); iterator
                        .hasNext();)
                {
                    String field = iterator.next();
                    buf.append(field);
                    buf.append("=?");
                    if (iterator.hasNext())
                        buf.append(", ");
                }
                buf.append(" WHERE ");

                for (Iterator<String> iterator = whereFields.iterator(); iterator
                        .hasNext();)
                {
                    String field = iterator.next();
                    buf.append(field);
                    buf.append("=?");
                    if (iterator.hasNext())
                        buf.append(" AND ");
                }
            }
            else
            {
                // insert
                buf.append("INSERT INTO ");
                buf.append(tableName);

                buf.append(" (");
                for (Iterator<String> iterator = fields.iterator(); iterator
                        .hasNext();)
                {
                    String field = iterator.next();
                    buf.append(field);
                    if (iterator.hasNext())
                        buf.append(", ");
                }

                buf.append(") VALUES (");
                for (Iterator<String> iterator = fields.iterator(); iterator
                        .hasNext();)
                {
                    iterator.next();
                    buf.append("?");
                    if (iterator.hasNext())
                        buf.append(", ");
                }
                buf.append(")");
            }
            String sql = buf.toString();
            log.info("SQL Statement: " + sql);
            hints.put(HINT_RETURN_SQL, sql);

            // create a PreparedStatement
            try
            {
                // use the provided connection, or get a new one
                Connection connection = (Connection) hints.get(HINT_CONNECTION);
                if (connection == null)
                {
                    connection = databaseContext.getConnectionProvider()
                            .newConnection();
                }

                PreparedStatement ps = connection.prepareStatement(sql);
                int i = 1;
                for (Object value : values)
                {
                    RoverUtils.setPreparedStatementField(ps, i++, value);
                }
                return ps;
            }
            catch (Exception e)
            {
                throw new SerializationException(e);
            }
        }
        throw new SerializationException("Unable to convert input object");
    }
}
