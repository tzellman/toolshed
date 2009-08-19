/*
 * =============================================================================
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package rover.impl.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaBeanMapDecorator;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.commons.beanutils.MutableDynaClass;
import org.apache.commons.beanutils.ResultSetDynaClass;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rover.IQueryContext;
import rover.IQueryResultSet;
import rover.QueryConstants;
import rover.RoverUtils;
import rover.impl.FilterInput;
import rover.impl.sql.SQLQueryInput.QueryData;

/**
 * Implementation of {@link IQueryResultSet}.
 * 
 * @author tzellman
 * 
 */
public class SQLQueryResultSet implements IQueryResultSet
{
    private static final Log log = LogFactory.getLog(SQLQueryResultSet.class);

    protected IQueryContext context;

    protected SQLQueryInput queryInput;

    protected Set<String> selectFields;

    public SQLQueryResultSet(String tableName, IQueryContext context)
            throws Exception
    {
        this.context = context;
        queryInput = new SQLQueryInput(tableName, context);
        selectFields = new HashSet<String>();
    }

    public SQLQueryResultSet(SQLQueryResultSet dolly) throws Exception
    {
        this.context = dolly.context;
        this.queryInput = new SQLQueryInput(dolly.queryInput);
        this.selectFields = new HashSet<String>(dolly.selectFields);
    }

    public int count() throws Exception
    {
        // clone the input
        SQLQueryInput dolly = new SQLQueryInput(queryInput);
        String databaseTypeName = context.getDatabaseInfo().getDatabaseType();

        String countVar = "count";
        String countStmt = "COUNT(*) AS " + countVar;
        if (StringUtils.equalsIgnoreCase(databaseTypeName,
                QueryConstants.DATABASE_HSQLDB))
        {
            countStmt = String.format("COUNT(*) \"%s\"", countVar);
        }

        // only select the count
        selectFields.clear();
        selectFields.add(countStmt);

        List<Object> results = execute(dolly, 0, 0);

        Object count = ((Map) results.get(0)).get(countVar);
        // one of these two better work
        if (count instanceof Number)
            return ((Number) count).intValue();
        throw new Exception("Unable to get count");
    }

    public IQueryResultSet distinct() throws Exception
    {
        SQLQueryResultSet dolly = new SQLQueryResultSet(this);
        dolly.queryInput.setDistinct(true);
        return dolly;
    }

    public IQueryResultSet filter(String... clauses) throws Exception
    {
        SQLQueryResultSet dolly = new SQLQueryResultSet(this);
        FilterInput.parseFilters(dolly.queryInput, clauses);
        return dolly;
    }

    public IQueryResultSet orderBy(String... fields) throws Exception
    {
        SQLQueryResultSet dolly = new SQLQueryResultSet(this);
        dolly.queryInput.addOrderBy(fields);
        return dolly;
    }

    // currently, getting spanned table values back is not possible
    // only values from the top table specified
    public IQueryResultSet values(String... fields) throws Exception
    {
        SQLQueryResultSet dolly = new SQLQueryResultSet(this);
        dolly.selectFields.addAll(Arrays.asList(fields));
        return dolly;
    }

    public List<Object> list() throws Exception
    {
        return list(0, 0);
    }

    public List<Object> list(int limit) throws Exception
    {
        return list(0, limit);
    }

    public List<Object> list(int offset, int limit) throws Exception
    {
        return execute(queryInput, offset, limit);
    }

    protected Map<String, Object> normalizeBean(DynaBean bean,
            Map<String, String> aliasMap)
    {
        DynaBeanMapDecorator beanMap = new DynaBeanMapDecorator(bean);

        // first, set up our DynaClasses
        Map<String, Object> map = new HashMap<String, Object>();
        for (Object key : beanMap.keySet())
        {
            Object value = beanMap.get(key);
            String alias = key.toString();

            String sKey = aliasMap.containsKey(alias) ? aliasMap.get(alias)
                    : alias;
            
            Deque<String> parts = new LinkedList<String>(Arrays
                    .asList(StringUtils.splitByWholeSeparator(sKey, "__")));

            if (parts.size() >= 2)
            {
                String objName = parts.pop();
                String fieldName = parts.pollLast();

                if (!map.containsKey(objName))
                    map.put(objName, new LazyDynaBean());

                DynaBean dynaBean = (DynaBean) map.get(objName);
                while (!parts.isEmpty())
                {
                    String name = parts.pop();
                    try
                    {
                        Object object = dynaBean.get(name);
                        // watch out for collision from FK fields...
                        if (!(object instanceof DynaBean))
                        {
                            DynaClass dynaClass = dynaBean.getDynaClass();
                            if (dynaClass instanceof MutableDynaClass)
                                ((MutableDynaClass) dynaClass).remove(name);
                            dynaBean.remove(name, null);
                            throw new IllegalArgumentException();
                        }
                    }
                    catch (IllegalArgumentException e)
                    {
                        dynaBean.set(name, new LazyDynaBean());
                    }
                    dynaBean = (DynaBean) dynaBean.get(name);
                }

                Object object = dynaBean.get(fieldName);
                if (object == null)
                {
                    // this guards against dynabean collisions
                    dynaBean.set(fieldName, value);
                    log.debug(String.format("Set field: [%s] = [%s]",
                            fieldName, value));
                }
            }
            else
            {
                // just put the simple value
                map.put(sKey, value);
            }
        }
        return map;
    }

    protected List<Object> execute(SQLQueryInput input, int offset, int limit)
            throws Exception
    {
        List<Object> results = new LinkedList<Object>();

        QueryData queryData = input.getQuery(offset, limit, selectFields);
        log.debug(queryData.query);

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        Connection connection = null;
        try
        {
            connection = context.getConnectionProvider().newConnection();
            preparedStatement = connection.prepareStatement(queryData.query);

            // bind the data to the statement
            if (queryData.values != null)
            {
                int i = 1;
                for (Object value : queryData.values)
                {
                    RoverUtils.setPreparedStatementField(preparedStatement,
                            i++, value);
                }
            }
            preparedStatement.execute();
            resultSet = preparedStatement.getResultSet();

            ResultSetDynaClass dynaClass = new ResultSetDynaClass(resultSet,
                    true);
            Iterator dynaIterator = dynaClass.iterator();
            while (dynaIterator.hasNext())
            {
                DynaBean rowBean = (DynaBean) dynaIterator.next();
                Map<String, Object> normalized = normalizeBean(rowBean,
                        queryData.selectAliases);
                results.add(normalized);

                // we can't use the SQL rowBean after the Statement closes, so
                // this requires us to create our own
                // DynaBeanCloner cloner = new DynaBeanCloner(rowBean);
                // results.add(cloner.newInstance());
            }
        }
        finally
        {
            if (resultSet != null)
                resultSet.close();
            if (preparedStatement != null)
                preparedStatement.close();
            if (connection != null)
                connection.close();
        }

        return results;
    }

    // public IQueryResultSet selectRelated(int depth) throws Exception
    // {
    // SQLQueryResultSet dolly = new SQLQueryResultSet(this);
    // dolly.queryInput.setSelectRelatedDepth(depth);
    // return dolly;
    // }

}
