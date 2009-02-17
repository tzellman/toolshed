package rover;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import rover.hood.TableInfoBean;

/**
 * Simple cache for storing database metadata.
 * 
 * @author tzellman
 */
public class DatabaseInfoCache
{

    /**
     * The cache for storing table name --> TableInfo
     */
    protected Map<String, TableInfoBean> tableCache;

    public DatabaseInfoCache()
    {
        tableCache = Collections
                .synchronizedMap(new HashMap<String, TableInfoBean>());
    }

    /**
     * Gets the TableInfo for the given table
     * 
     * @param tableName
     * @param connection
     * @return
     * @throws Exception
     */
    public TableInfoBean getTableInfo(String tableName, Connection connection)
            throws Exception
    {
        tableName = tableName.toUpperCase();

        if (!tableCache.containsKey(tableName))
        {
            tableCache.put(tableName, TableInfoBean.getTableInfo(tableName,
                    connection));
        }
        return tableCache.get(tableName);
    }

    public boolean hasTable(String tableName)
    {
        return tableCache.containsKey(tableName.toUpperCase());
    }

    /**
     * Clears the cache
     */
    protected void clear()
    {
        tableCache.clear();
    }

}
