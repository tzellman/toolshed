package rover.hood;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import rover.QueryConstants;

/**
 * This object holds the data that will be used for input to a SQL query. It
 * also will construct the query statement based on its contents.
 * 
 * @author tzellman
 */
public class QueryInput
{
    protected Set<String> tables;

    protected Set<String> select;

    protected List<String> where;

    protected List<Object> values;

    protected List<FieldInfoBean> valueFields;

    protected boolean distinct;

    // ! Oracle, MySQL, etc.
    protected String databaseTypeName;

    public QueryInput()
    {
        distinct = false;
    }

    public QueryInput(QueryInput dolly)
    {
        Set<String> select = dolly.getSelect();
        Set<String> tables = dolly.getTables();
        List<FieldInfoBean> valueFields = dolly.getValueFields();
        List<Object> values = dolly.getValues();
        List<String> where = dolly.getWhere();

        setDatabaseTypeName(dolly.getDatabaseTypeName());
        setDistinct(dolly.isDistinct());
        setSelect(select == null ? null : new HashSet<String>(select));
        setTables(tables == null ? null : new HashSet<String>(tables));
        setValueFields(valueFields == null ? null : new Vector<FieldInfoBean>(
                valueFields));
        setValues(values == null ? null : new Vector<Object>(values));
        setWhere(where == null ? null : new Vector<String>(where));
    }

    public Set<String> getTables()
    {
        return tables;
    }

    public void setTables(Set<String> tables)
    {
        this.tables = tables;
    }

    public void addTables(String... tables)
    {
        if (this.tables == null)
            this.tables = new HashSet<String>();
        this.tables.addAll(Arrays.asList(tables));
    }

    public Set<String> getSelect()
    {
        return select;
    }

    public void setSelect(Set<String> select)
    {
        this.select = select;
    }

    public void addSelect(String... select)
    {
        if (this.select == null)
            this.select = new HashSet<String>();
        this.select.addAll(Arrays.asList(select));
    }

    public List<String> getWhere()
    {
        return where;
    }

    public void setWhere(List<String> where)
    {
        this.where = where;
    }

    public void addWhere(String... where)
    {
        if (this.where == null)
            this.where = new LinkedList<String>();
        this.where.addAll(Arrays.asList(where));
    }

    public List<Object> getValues()
    {
        return values;
    }

    public void setValues(List<Object> values)
    {
        this.values = values;
    }

    public void addValues(Object... values)
    {
        if (this.values == null)
            this.values = new LinkedList<Object>();
        this.values.addAll(Arrays.asList(values));
    }

    public List<FieldInfoBean> getValueFields()
    {
        return valueFields;
    }

    public void setValueFields(List<FieldInfoBean> valueFields)
    {
        this.valueFields = valueFields;
    }

    public void addValueFields(FieldInfoBean... valueFields)
    {
        if (this.valueFields == null)
            this.valueFields = new LinkedList<FieldInfoBean>();
        this.valueFields.addAll(Arrays.asList(valueFields));
    }

    public boolean isDistinct()
    {
        return distinct;
    }

    public void setDistinct(boolean distinct)
    {
        this.distinct = distinct;
    }

    public String getDatabaseTypeName()
    {
        return databaseTypeName;
    }

    public void setDatabaseTypeName(String databaseTypeName)
    {
        this.databaseTypeName = databaseTypeName;
    }

    public void checkSanity() throws Exception
    {
        // we must select something and specify at least one table...
        if (tables == null || select == null || tables.isEmpty()
                || select.isEmpty())
            throw new Exception(
                    "Must specify at least 1 table and 1 select field");

        // we must have the same # of values as value fields
        if ((valueFields != null && values != null && valueFields.size() != values
                .size())
                || (valueFields == null && values != null)
                || (valueFields != null && values == null))
            throw new Exception("The # of fields and values are not the same.");

        int numExpectedFields = 0;
        if (where != null)
        {
            for (String whereStatement : where)
            {
                // I assume this is ok...
                if (whereStatement.trim().endsWith("?"))
                    numExpectedFields++;
            }
        }

        // basically, if we gave more or less bound values for the where clauses
        if ((numExpectedFields > 0 && (values == null || values.size() != numExpectedFields))
                || (numExpectedFields == 0 && values != null && values.size() > 0))
            throw new Exception("Bound values incorrect. Expected "
                    + numExpectedFields + " but found "
                    + (values == null ? 0 : values.size()));
    }

    public String getQueryString() throws Exception
    {
        return getQueryString(0, 0);
    }

    /**
     * Creates and returns the query string for the current query input.
     * 
     * @param offset
     * @param limit
     * @return the query String or null if there is incomplete data
     */
    public String getQueryString(int offset, int limit) throws Exception
    {
        checkSanity();

        StringBuffer queryBuf = new StringBuffer();
        queryBuf.append("select ");
        if (distinct)
            queryBuf.append("distinct ");
        queryBuf.append(StringUtils.join(select.toArray(), ","));
        queryBuf.append(" from " + StringUtils.join(tables, ","));

        if (where != null && !where.isEmpty())
        {
            queryBuf.append(" where " + StringUtils.join(where, " and "));
        }

        if (limit > 0 || offset > 0)
        {
            if (StringUtils.equalsIgnoreCase(databaseTypeName,
                    QueryConstants.DATABASE_MYSQL)
                    || StringUtils.equalsIgnoreCase(databaseTypeName,
                            QueryConstants.DATABASE_POSTGRES))
            {
                if (limit > 0)
                    queryBuf.append(" limit " + limit);
                if (offset > 0)
                    queryBuf.append(" offset " + limit);
            }
            else if (StringUtils.equalsIgnoreCase(databaseTypeName,
                    QueryConstants.DATABASE_ORACLE))
            {
                String limitStr = " rownum ";
                if (limit > 0 && offset > 0)
                    limitStr += " between " + (offset + 1) + " and "
                            + (limit + 1);
                else if (offset > 0)
                    limitStr += "> " + (offset + 1);
                else
                    limitStr += "<= " + limit;
                if (where != null && !where.isEmpty())
                    queryBuf.append(" and " + limitStr);
                else
                    queryBuf.append(" where " + limitStr);
            }
        }

        return queryBuf.toString();
    }

}
