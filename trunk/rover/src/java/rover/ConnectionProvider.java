package rover;

import java.sql.Connection;

/**
 * All implementing Classes must provide a way to get a Connection.
 * 
 * @author tzellman
 */
public interface ConnectionProvider
{
    Connection getConnection() throws Exception;
}
