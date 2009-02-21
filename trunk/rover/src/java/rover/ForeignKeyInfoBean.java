package rover;

/**
 * Simple bean for holding information regarding a foreign key.
 * 
 * @author tzellman
 */
public class ForeignKeyInfoBean
{
    protected String table;

    protected String column;

    public String getTable()
    {
        return table;
    }

    public void setTable(String table)
    {
        this.table = table;
    }

    public String getColumn()
    {
        return column;
    }

    public void setColumn(String column)
    {
        this.column = column;
    }
}
