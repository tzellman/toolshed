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
package rover.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import rover.IConnectionProvider;
import rover.IDatabaseInfo;
import rover.ITableInfo;
import rover.QueryConstants;

/**
 * Simple cache for storing database metadata. Implements {@link IDatabaseInfo}.
 * 
 * @author tzellman
 */
public class DatabaseInfoCache implements IDatabaseInfo
{

    /**
     * The cache for storing table name --> TableInfo
     */
    protected Map<String, TableInfoBean> tableCache;

    protected IConnectionProvider connectionProvider;

    protected String databaseType;

    public DatabaseInfoCache(IConnectionProvider connectionProvider)
            throws Exception
    {
        this.connectionProvider = connectionProvider;
        tableCache = Collections
                .synchronizedMap(new HashMap<String, TableInfoBean>());
        Connection connection = connectionProvider.getConnection();
        this.databaseType = connection.getMetaData().getDatabaseProductName();
        // TODO possibly close the connection
    }

    public ITableInfo getTableInfo(String tableName) throws Exception
    {
        tableName = tableName.toUpperCase();

        if (!tableCache.containsKey(tableName))
        {
            Connection connection = connectionProvider.getConnection();
            tableCache.put(tableName, TableInfoBean.getTableInfo(tableName,
                    connection));

            // TODO - close the connection, if I decide to change the API
        }
        return tableCache.get(tableName);
    }

    public String getDatabaseType()
    {
        return databaseType;
    }

    /**
     * Clears the cache
     */
    protected void clear()
    {
        tableCache.clear();
    }

}
