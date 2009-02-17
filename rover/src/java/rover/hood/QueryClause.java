package rover.hood;

import java.util.List;
import java.util.Set;


/**
 * Internal object used for constructing queries.
 * 
 * @author tzellman
 */
public class QueryClause
{
    protected SQLOp sqlOp;

    protected List<String> where;

    protected Set<String> tables;

    protected FieldInfoBean field;

    protected Object value;

    public QueryClause()
    {
    }

    public SQLOp getSqlOp()
    {
        return sqlOp;
    }

    public void setSqlOp(SQLOp sqlOp)
    {
        this.sqlOp = sqlOp;
    }

    public List<String> getWhere()
    {
        return where;
    }

    public void setWhere(List<String> where)
    {
        this.where = where;
    }

    public Set<String> getTables()
    {
        return tables;
    }

    public void setTables(Set<String> tables)
    {
        this.tables = tables;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }

    public FieldInfoBean getField()
    {
        return field;
    }

    public void setField(FieldInfoBean field)
    {
        this.field = field;
    }

}
