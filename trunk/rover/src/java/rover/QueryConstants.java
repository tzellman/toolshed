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

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Query Utilities
 * 
 * @author tzellman
 */
public final class QueryConstants
{
    public static final String QUERY_SEP = "__";

    public static final String PKCOLUMN_NAME_FIELD = "PKCOLUMN_NAME";

    public static final String PKTABLE_NAME_FIELD = "PKTABLE_NAME";

    public static final String FKCOLUMN_NAME_FIELD = "FKCOLUMN_NAME";

    public static final String COLUMN_TYPE_FIELD = "TYPE_NAME";

    public static final String COLUMN_NAME_FIELD = "COLUMN_NAME";

    public static final String HINT_DATABASE_TYPE = "DatabaseType";

    public static final String DATABASE_ORACLE = "ORACLE";

    public static final String DATABASE_MYSQL = "MYSQL";

    public static final String DATABASE_POSTGRES = "POSTGRES";

    public static final String DATABASE_HSQLDB = "HSQL Database Engine";

    public static final String[] COMMON_DATE_PATTERNS = new String[] {
            "MM/dd/yy", "MM/dd/yyyy", "yyyyMMdd", "yyyy.MM.dd.HH.mm.ss",
            "yyyy-MM-dd'T'HH:mm:ss", // ISO datetime
            "yyyy-MM-dd'T'HH:mm:ssZZ", // ISO datetime TZ
            "yyyy-MM-dd", // ISO date
            "yyyy-MM-ddZZ", // ISO date TZ
            "EEE, dd MMM yyyy HH:mm:ss Z", // SMTP datetime
            "yyyy-MM-dd HH:mm:ss.S" //common Oracle DB format
    };

    /**
     * Returns a mapping of the SQL Type names to their integer IDs
     * 
     * @return
     */
    protected static Map<String, Integer> getSQLTypeNames()
    {
        Map<String, Integer> typeNames = new HashMap<String, Integer>();
        Field[] fields = Types.class.getFields();
        for (int i = 0, size = fields.length; i < size; i++)
        {
            try
            {
                typeNames.put(fields[i].getName(), (Integer) fields[i]
                        .get(null));
            }
            catch (IllegalAccessException e)
            {
            }
        }

        // plus, add some custom ones
        typeNames.put("VARCHAR2", Types.VARCHAR);
        typeNames.put("TIMESTAMP(6)", Types.TIMESTAMP);

        return typeNames;
    }

    /**
     * Static mapping of the SQL Type names to their integer IDs
     */
    public static final Map<String, Integer> SQL_TYPE_NAMES = Collections
            .unmodifiableMap(getSQLTypeNames());

}
