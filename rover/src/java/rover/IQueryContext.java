package rover;

public interface IQueryContext
{
    IConnectionProvider getConnectionProvider();

    SQLTypeConverter getSQLTypeConverter();

    IDatabaseInfo getDatabaseInfo();
}
