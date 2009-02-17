package rover;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.ResultSetDynaClass;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import rover.hood.DynaBeanCloner;
import rover.hood.FieldInfoBean;
import rover.hood.ForeignKeyInfoBean;
import rover.hood.QueryClause;
import rover.hood.QueryInput;
import rover.hood.SQLOp;
import rover.hood.TableInfoBean;

public class QuerySet
{
    private static final Log log = LogFactory.getLog(QuerySet.class);

    protected String tableName;

    protected QueryContext context;

    protected QueryInput queryInput;

    public QuerySet(String tableName, QueryContext context)
    {
        this.tableName = tableName;
        this.context = context;

        queryInput = new QueryInput();
        queryInput.addTables(tableName);
    }

    public QuerySet filter(String... clauses) throws Exception
    {
        QuerySet dolly = new QuerySet(this);
        dolly.evaluate(parseClauses(clauses));
        return dolly;
    }

    public QuerySet(QuerySet dolly)
    {
        this.tableName = dolly.tableName;
        this.context = dolly.context;
        this.queryInput = new QueryInput(dolly.queryInput);
    }

    public int count() throws Exception
    {
        // clone the input
        QueryInput input = new QueryInput(queryInput);
        input.setDatabaseTypeName(context.getConnection().getMetaData()
                .getDatabaseProductName());
        Set<String> select = input.getSelect();
        if (select != null)
            select.clear();
        input.addSelect("COUNT(*) AS count");
        List<DynaBean> results = execute(input, 0, 0);
        Object count = results.get(0).get("count");
        // one of these two better work
        if (count instanceof Number)
            return ((Number) count).intValue();
        throw new Exception("Unable to get count");
    }

    public QuerySet distinct()
    {
        QuerySet dolly = new QuerySet(this);
        dolly.queryInput.setDistinct(true);
        return dolly;
    }

    public List<DynaBean> list() throws Exception
    {
        return list(0);
    }

    public List<DynaBean> list(int limit) throws Exception
    {
        return list(0, limit);
    }

    public List<DynaBean> list(int offset, int limit) throws Exception
    {
        // clone the input
        QueryInput input = new QueryInput(queryInput);
        input.setDatabaseTypeName(context.getConnection().getMetaData()
                .getDatabaseProductName());
        return execute(input, offset, limit);
    }

    protected List<DynaBean> execute(QueryInput input, int offset, int limit)
            throws Exception
    {
        // by default, add all fields of the table to the select clause
        // if nothing was selected
        if (input.getSelect() == null || input.getSelect().isEmpty())
        {
            TableInfoBean tableInfo = context.getCache().getTableInfo(
                    tableName, context.getConnection());
            // add all fields from the table
            Set<String> selectFields = new HashSet<String>();
            for (FieldInfoBean info : tableInfo.getFields().values())
                selectFields.add(info.getName());
            input.setSelect(selectFields);
        }

        List<DynaBean> results = new LinkedList<DynaBean>();

        String query = null;
        query = input.getQueryString(offset, limit);
        log.info(query);

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try
        {
            preparedStatement = context.getConnection().prepareStatement(query);

            // bind the data to the statement
            List<Object> values = input.getValues();
            List<FieldInfoBean> valueFields = input.getValueFields();
            if (values != null && valueFields != null)
            {
                for (int i = 0, size = values.size(); i < size; ++i)
                {
                    setPreparedStatementField(preparedStatement, valueFields
                            .get(i), i + 1, values.get(i));
                }
            }
            preparedStatement.execute();
            resultSet = preparedStatement.getResultSet();

            ResultSetDynaClass dynaClass = new ResultSetDynaClass(resultSet);
            Iterator dynaIterator = dynaClass.iterator();
            while (dynaIterator.hasNext())
            {
                DynaBean rowBean = (DynaBean) dynaIterator.next();

                // we can't use the SQL rowBean after the Statement closes, so
                // this requires us to create our own
                DynaBeanCloner cloner = new DynaBeanCloner(rowBean);
                results.add(cloner.newInstance());
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

    public void evaluate(List<QueryClause> clauses) throws Exception
    {
        String tableName = this.tableName.toUpperCase();

        for (QueryClause q : clauses)
        {
            Set<String> qTables = q.getTables();
            if (qTables != null)
                queryInput.addTables(qTables.toArray(new String[0]));

            List<String> qWhere = q.getWhere();
            if (qWhere != null)
                queryInput.addWhere(qWhere.toArray(new String[0]));

            FieldInfoBean field = q.getField();
            if (field != null)
            {
                SQLOp sqlOp = q.getSqlOp();
                String opString = String.format("%s.%s %s", field.getTable(),
                        field.getName(), sqlOp.getOperator());
                if (!sqlOp.isUnary())
                {
                    opString += " ?";
                    queryInput.addValueFields(field);
                    queryInput.addValues(q.getValue());
                }
                queryInput.addWhere(opString);
            }
        }
    }

    protected List<QueryClause> parseClauses(String... clauses)
            throws Exception
    {
        String tableName = this.tableName.toUpperCase();

        List<QueryClause> queryClauses = new LinkedList<QueryClause>();
        for (String clause : clauses)
        {
            String[] parts = StringUtils.split(clause, "=", 2);
            String fieldString = null, valueString = null;
            fieldString = parts[0];
            if (parts.length == 2)
                valueString = parts[1];

            QueryClause q = new QueryClause();

            String[] fields = StringUtils.splitByWholeSeparator(fieldString,
                    QueryConstants.QUERY_SEP);
            Deque<String> fieldList = new LinkedList<String>();

            // filter out empty fields
            for (String f : fields)
            {
                if (StringUtils.isEmpty(f))
                    throw new Exception("Invalid field expression");
                fieldList.add(f);
            }

            // check to see if they provided a SQL op
            int numFields = fieldList.size();
            if (numFields > 1)
            {
                String lastField = fieldList.peekLast();
                SQLOp op = SQLOp.getOp(lastField);
                if (op != null)
                    fieldList.pollLast();
                else
                    op = SQLOp.EXACT; // default
                q.setSqlOp(op);
            }
            else
            {
                q.setSqlOp(SQLOp.EXACT); // default
            }

            if (valueString == null && !q.getSqlOp().isUnary())
                throw new Exception(
                        "Invalid field expression: must have value for binary operation");
            if (valueString != null && q.getSqlOp().isUnary())
                throw new Exception(
                        "Invalid field expression: unary operation does not take a value");

            Set<String> tables = new HashSet<String>();
            tables.add(tableName);

            if (!fieldList.isEmpty())
            {
                String lastField = fieldList.pollLast();

                List<String> whereList = new LinkedList<String>();
                String currentTableName = tableName;
                TableInfoBean currentTableInfo = null;
                Connection connection = context.getConnection();
                while (!fieldList.isEmpty())
                {
                    currentTableInfo = context.getCache().getTableInfo(
                            currentTableName, connection);
                    String field = fieldList.pop();
                    String fieldKey = field.toUpperCase();

                    // try to get the field from the current table
                    FieldInfoBean fieldInfo = currentTableInfo.getFields().get(
                            fieldKey);
                    // also, see if it is a foreign key to another table
                    ForeignKeyInfoBean fkInfo = fieldInfo == null ? null
                            : fieldInfo.getForeignKeyInfo();

                    if (fkInfo == null)
                    {
                        /*
                         * This could be the situation where you want to do a
                         * reverse lookup to a table that has the current table
                         * listed as a foreign key. This is tricky b/c a table
                         * can have multiple foreign keys to another table. I
                         * would say that in the future we add a reverse lookup
                         * map to the query context, that the user can supply.
                         * 
                         * For now, we'll just support setting that column by
                         * the name of the reverse lookup table. We'll use the
                         * foreign key, if there is only 1 referring to this
                         * table. If there are multiple, we'll throw.
                         */

                        // is it a table name?
                        TableInfoBean reverseTableInfo = context.getCache()
                                .getTableInfo(fieldKey, connection);
                        if (reverseTableInfo != null)
                        {
                            // ok, see if there is a fk relationship (only 1 for
                            // now) to this table
                            Iterator<FieldInfoBean> reverseFieldIter = reverseTableInfo
                                    .getFields().values().iterator();
                            while (reverseFieldIter.hasNext())
                            {
                                FieldInfoBean f = reverseFieldIter.next();
                                ForeignKeyInfoBean reverseFKInfo = f
                                        .getForeignKeyInfo();
                                if (reverseFKInfo != null
                                        && StringUtils.equalsIgnoreCase(
                                                reverseFKInfo.getTable(),
                                                currentTableName))
                                {
                                    if (fkInfo != null)
                                    {
                                        // this means we found two FKs to this
                                        // table
                                        throw new Exception(
                                                "Unable to differentiate between multiple FK fields");
                                    }

                                    // reverse lookup!
                                    fkInfo = new ForeignKeyInfoBean();
                                    fkInfo.setColumn(f.getName());
                                    fkInfo.setTable(f.getTable());

                                    // set the fieldInfo for THIS field - which
                                    // is just the FK of the reverse FK info...
                                    // obvious, right?
                                    fieldInfo = currentTableInfo.getFields()
                                            .get(reverseFKInfo.getColumn());

                                    // if this is null, then the logic for
                                    // setting up the TableInfo was incorrect,
                                    // for some magical reason...
                                    if (fieldInfo == null)
                                        throw new Exception(
                                                "Unable to determine the correct FK table/column.");
                                }
                            }
                        }

                        if (fkInfo == null)
                            throw new Exception("Invalid FK Column: " + field);
                    }

                    // if we made it this far, and no field, then nuh-uh
                    if (fieldInfo == null)
                        throw new Exception("Invalid Column: " + field);

                    String fkTable = fkInfo.getTable();
                    String where = String.format("%s.%s=%s.%s",
                            currentTableName, fieldInfo.getName(), fkTable,
                            fkInfo.getColumn());
                    whereList.add(where);
                    tables.add(fkTable);

                    currentTableName = fkTable;
                }

                currentTableInfo = context.getCache().getTableInfo(
                        currentTableName, connection);
                if (!currentTableInfo.getFields().containsKey(
                        lastField.toUpperCase()))
                    throw new Exception("Invalid Column: " + lastField);

                FieldInfoBean fieldInfo = currentTableInfo.getFields().get(
                        lastField.toUpperCase());
                q.setField(fieldInfo);
                Integer sqlType = fieldInfo.getSqlType();

                // add a hint for the dispatcher
                Map hints = new HashMap();
                hints.put(SQLTypeConverterRegistry.HINT_SQL_TYPE, sqlType);

                // turn the String value into an Object for the query
                Object sqlValue = context.getRegistry().convert(valueString,
                        hints);
                q.setValue(sqlValue);
                q.setWhere(whereList);
            }
            q.setTables(tables);
            queryClauses.add(q);
        }

        if (clauses.length == 0)
        {
            // select all...
            QueryClause q = new QueryClause();
            Set<String> tables = new HashSet<String>();
            tables.add(tableName);
            q.setTables(tables);
            queryClauses.add(q);
        }

        return queryClauses;
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
    protected void setPreparedStatementField(PreparedStatement ps,
            FieldInfoBean field, int i, Object object) throws SQLException
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
