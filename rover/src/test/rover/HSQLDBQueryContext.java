package rover;

import java.sql.Connection;
import java.sql.DriverManager;

import rover.impl.DatabaseInfoCache;

public class HSQLDBQueryContext implements IQueryContext
{
    IConnectionProvider connectionProvider;

    DatabaseInfoCache cache;

    SQLTypeConverter converter;

    public HSQLDBQueryContext() throws Exception
    {
        connectionProvider = new IConnectionProvider()
        {
            public Connection newConnection() throws Exception
            {
                return DriverManager.getConnection("jdbc:hsqldb:mem:test",
                        "sa", "");
            }
        };
        cache = new DatabaseInfoCache(connectionProvider);
        converter = new SQLTypeConverter();
    }

    public IConnectionProvider getConnectionProvider()
    {
        return connectionProvider;
    }

    public IDatabaseInfo getDatabaseInfo()
    {
        return cache;
    }

    public SQLTypeConverter getSQLTypeConverter()
    {
        return converter;
    }
}
