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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
import rover.impl.QueryInput.QueryData;

public class QueryResultSet implements IQueryResultSet
{
    private static final Log log = LogFactory.getLog(QueryResultSet.class);

    protected IQueryContext context;

    protected QueryInput queryInput;

    public QueryResultSet(String tableName, IQueryContext context)
            throws Exception
    {
        this.context = context;
        queryInput = new QueryInput(tableName, context);
    }

    public QueryResultSet(QueryResultSet dolly) throws Exception
    {
        this.context = dolly.context;
        this.queryInput = new QueryInput(dolly.queryInput);
    }

    public int count() throws Exception
    {
        // clone the input
        QueryInput dolly = new QueryInput(queryInput);
        String databaseTypeName = context.getDatabaseInfo().getDatabaseType();

        String countVar = "count";
        String countStmt = "COUNT(*) AS " + countVar;
        if (StringUtils.equalsIgnoreCase(databaseTypeName,
                QueryConstants.DATABASE_HSQLDB))
        {
            countStmt = String.format("COUNT(*) \"%s\"", countVar);
        }
        List<Map<String, ? extends Object>> results = execute(dolly, 0, 0,
                new HashSet<String>(Arrays.asList(new String[] { countStmt })));

        Object count = results.get(0).get(countVar);
        // one of these two better work
        if (count instanceof Number)
            return ((Number) count).intValue();
        throw new Exception("Unable to get count");
    }

    public IQueryResultSet distinct() throws Exception
    {
        QueryResultSet dolly = new QueryResultSet(this);
        dolly.queryInput.setDistinct(true);
        return dolly;
    }

    public IQueryResultSet filter(String... clauses) throws Exception
    {
        QueryResultSet dolly = new QueryResultSet(this);
        dolly.parseFilters(clauses);
        return dolly;
    }

    public IQueryResultSet orderBy(String... fields) throws Exception
    {
        QueryResultSet dolly = new QueryResultSet(this);
        dolly.queryInput.addOrderBy(fields);
        return dolly;
    }

    public List<Map<String, ? extends Object>> list() throws Exception
    {
        return list(0, 0);
    }

    public List<Map<String, ? extends Object>> list(int limit) throws Exception
    {
        return list(0, limit);
    }

    public List<Map<String, ? extends Object>> list(int offset, int limit)
            throws Exception
    {
        return execute(queryInput, offset, limit, null);
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
                    .toLowerCase() : alias;

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

    protected List<Map<String, ? extends Object>> execute(QueryInput input,
            int offset, int limit, Set<String> selectFields) throws Exception
    {
        List<Map<String, ? extends Object>> results = new LinkedList<Map<String, ? extends Object>>();

        QueryData queryData = input.getQuery(offset, limit, selectFields);
        log.debug(queryData.query);

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try
        {
            Connection connection = context.getConnectionProvider()
                    .getConnection();
            preparedStatement = connection.prepareStatement(queryData.query);

            // bind the data to the statement
            if (queryData.values != null)
            {
                int i = 1;
                for (Object value : queryData.values)
                {
                    setPreparedStatementField(preparedStatement, i++, value);
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
        }

        return results;
    }

    public IQueryResultSet selectRelated(int depth) throws Exception
    {
        QueryResultSet dolly = new QueryResultSet(this);
        dolly.queryInput.setSelectRelatedDepth(depth);
        return dolly;
    }

    protected static class FilterInput
    {
        Deque<String> fields;

        SQLOp op;

        String value;
    }

    protected static List<FilterInput> transformFilters(String... clauses)
            throws Exception
    {
        List<FilterInput> filterInputs = new LinkedList<FilterInput>();
        for (String clause : clauses)
        {
            String[] parts = StringUtils.split(clause, "=", 2);
            String fieldString = null, valueString = null;
            fieldString = parts[0];
            if (parts.length == 2)
                valueString = parts[1];

            String[] fields = StringUtils.splitByWholeSeparator(fieldString,
                    QueryConstants.QUERY_SEP);
            Deque<String> fieldList = new LinkedList<String>();

            // filter out empty fields
            for (String f : fields)
            {
                if (StringUtils.isEmpty(f))
                    throw new Exception("Invalid field expression");
                fieldList.add(f.toUpperCase());
            }

            // check to see if they provided a SQL op
            SQLOp op = SQLOp.EXACT;
            int numFields = fieldList.size();
            if (numFields > 1)
            {
                String lastField = fieldList.peekLast();
                op = SQLOp.getOp(lastField);
                if (op != null)
                    fieldList.pollLast();
                else
                    op = SQLOp.EXACT;
            }

            if (valueString == null && !op.isUnary())
                throw new Exception(
                        "Invalid field expression: must have value for binary operation");
            if (valueString != null && op.isUnary())
                throw new Exception(
                        "Invalid field expression: unary operation does not take a value");

            FilterInput filterInput = new FilterInput();
            filterInput.fields = fieldList;
            filterInput.op = op;
            filterInput.value = valueString;
            filterInputs.add(filterInput);
        }
        return filterInputs;
    }

    protected void parseFilters(String... clauses) throws Exception
    {
        List<FilterInput> filterClauses = transformFilters(clauses);
        for (FilterInput filterInput : filterClauses)
        {
            Deque<String> fieldList = filterInput.fields;
            if (!fieldList.isEmpty())
            {
                String lastField = fieldList.pollLast();
                while (!fieldList.isEmpty())
                {
                    String field = fieldList.pop();
                    queryInput.addFK(field);
                }
                queryInput.addWhere(lastField, filterInput.op,
                        filterInput.value);
            }
        }
    }

    /**
     * Set the correct (typed) field in a PreparedStatement
     * 
     * @param ps
     * @param field
     * @param i
     * @param object
     * @throws SQLException
     */
    protected void setPreparedStatementField(PreparedStatement ps, int i,
            Object object) throws SQLException
    {
        if (object instanceof Integer)
            ps.setInt(i, (Integer) object);
        else if (object instanceof Float)
            ps.setFloat(i, (Float) object);
        else if (object instanceof Double)
            ps.setDouble(i, (Double) object);
        else if (object instanceof Date)
            ps.setDate(i, (Date) object);
        else if (object instanceof Timestamp)
            ps.setTimestamp(i, (Timestamp) object);
        else
            ps.setString(i, object.toString());
    }

}
