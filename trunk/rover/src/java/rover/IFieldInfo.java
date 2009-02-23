package rover;

public interface IFieldInfo
{
    String getTable();

    String getName();

    String getSQLType();

    IForeignKeyInfo getForeignKeyInfo();
}
