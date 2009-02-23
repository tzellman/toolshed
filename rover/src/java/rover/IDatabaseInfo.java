package rover;

public interface IDatabaseInfo
{
    ITableInfo getTableInfo(String tableName) throws Exception;

    String getDatabaseType();
}
