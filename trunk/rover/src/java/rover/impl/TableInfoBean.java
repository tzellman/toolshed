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
package rover.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import rover.IFieldInfo;
import rover.ITableInfo;
import rover.QueryConstants;

/**
 * Bean that holds database table metadata information. Implements
 * {@link ITableInfo}.
 * 
 * @author tzellman
 */
public class TableInfoBean implements ITableInfo
{

    protected String name;

    protected Map<String, IFieldInfo> fields;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Map<String, IFieldInfo> getFields()
    {
        return fields;
    }

    public void setFields(Map<String, IFieldInfo> fields)
    {
        this.fields = fields;
    }

    /**
     * Returns a new TableInfo object
     * 
     * @param tableName
     * @return
     * @throws Exception
     */
    public static TableInfoBean getTableInfo(String tableName,
            Connection connection) throws Exception
    {
        TableInfoBean tableInfo = new TableInfoBean();
        tableInfo.setName(tableName.toUpperCase());
        Map<String, IFieldInfo> infos = new TreeMap<String, IFieldInfo>();

        DatabaseMetaData metaData = connection.getMetaData();

        ResultSet rsCols = metaData.getColumns(null, null, tableName, null);

        // map foreign key columns
        Map<String, ForeignKeyInfoBean> fkInfoMap = new HashMap<String, ForeignKeyInfoBean>();
        ResultSet rsFKs = metaData.getImportedKeys(null, null, tableName
                .toUpperCase());
        while (rsFKs.next())
        {
            String fkColumnName = rsFKs
                    .getString(QueryConstants.FKCOLUMN_NAME_FIELD);
            String pkTableName = rsFKs
                    .getString(QueryConstants.PKTABLE_NAME_FIELD);
            String pkColumnName = rsFKs
                    .getString(QueryConstants.PKCOLUMN_NAME_FIELD);

            ForeignKeyInfoBean fkInfo = new ForeignKeyInfoBean();
            fkInfo.setTable(pkTableName);
            fkInfo.setColumn(pkColumnName);

            fkInfoMap.put(fkColumnName, fkInfo);
        }
        rsFKs.close();

        // get column info
        Statement stmt = connection.createStatement();
        for (int j = 0; rsCols.next(); ++j)
        {
            String columnName = rsCols
                    .getString(QueryConstants.COLUMN_NAME_FIELD);
            String columnType = rsCols
                    .getString(QueryConstants.COLUMN_TYPE_FIELD);

            FieldInfoBean info = new FieldInfoBean();
            info.setTable(tableName.toUpperCase());
            info.setName(columnName);
            info.setSQLType(columnType);
            // info.setSqlType(QueryConstants.SQL_TYPE_NAMES.get(columnType));

            // if it's a FK column
            if (fkInfoMap.containsKey(columnName))
                info.setForeignKeyInfo(fkInfoMap.get(columnName));
            infos.put(columnName.toUpperCase(), info);
        }
        stmt.close();
        rsCols.close();

        tableInfo.setFields(infos);
        return tableInfo;
    }
}
