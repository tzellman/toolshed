package rover;



/**
 * Simple bean for holding information regarding a database field/column.
 * 
 * @author tzellman
 */
public class FieldInfoBean
{
    protected String table;

    protected String name;

    protected String sqlTypeName;

    protected Integer sqlType;

    protected ForeignKeyInfoBean foreignKeyInfo;

    public FieldInfoBean()
    {
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getSqlTypeName()
    {
        return sqlTypeName;
    }

    public void setSqlTypeName(String sqlTypeName)
    {
        this.sqlTypeName = sqlTypeName;
    }

    public Integer getSqlType()
    {
        return sqlType;
    }

    public void setSqlType(Integer sqlType)
    {
        this.sqlType = sqlType;
    }

    public ForeignKeyInfoBean getForeignKeyInfo()
    {
        return foreignKeyInfo;
    }

    public void setForeignKeyInfo(ForeignKeyInfoBean foreignKeyInfo)
    {
        this.foreignKeyInfo = foreignKeyInfo;
    }

    public boolean isForeignKey()
    {
        return foreignKeyInfo != null;
    }

    public String getTable()
    {
        return table;
    }

    public void setTable(String table)
    {
        this.table = table;
    }

}
