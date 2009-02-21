/* =============================================================================
 * This file is part of Rover
 * =============================================================================
 * (C) Copyright 2009, Tom Zellman, tzellman@gmail.com
 *
 * Rover is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package rover;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


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
