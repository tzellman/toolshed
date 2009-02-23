/* =============================================================================
 * This file is part of Galoot
 * =============================================================================
 * (C) Copyright 2009, Tom Zellman, tzellman@gmail.com
 *
 * Galoot is free software; you can redistribute it and/or
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
package galoot;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FilterMap
{
    private static final Log log = LogFactory.getLog(FilterMap.class);

    private Map<String, Filter> filterMap;

    public FilterMap()
    {
        filterMap = Collections
                .synchronizedMap(new LinkedHashMap<String, Filter>());
    }

    public void addFilter(Filter filter)
    {
        addFilter(filter, filter.getName());
    }
    
    public void addFilter(Filter filter, String alias)
    {
        if (alias == null)
            alias = filter.getName();
        if (hasFilter(filter.getName()))
            log.warn("Filter already exists: " + alias
                    + ", replacing...");
        filterMap.put(alias, filter);
    }

    public Filter getFilter(String name)
    {
        return filterMap.containsKey(name) ? filterMap.get(name)
                : (DefaultFilters.getInstance().hasFilter(name) ? DefaultFilters
                        .getInstance().getFilter(name)
                        : null);
    }

    /**
     * Removes the specified filter. If the filter is not found, or is only
     * available as a global filter, then the method returns false.
     * 
     * @param name
     * @return true iff the filter was removed
     */
    public boolean removeFilter(String name)
    {
        if (filterMap.containsKey(name))
        {
            filterMap.remove(name);
            return true;
        }
        else if (hasFilter(name))
            log
                    .warn("You cannot unregister a global filter, but you CAN override it.");
        return false;
    }

    public boolean hasFilter(String name)
    {
        return filterMap.containsKey(name)
                || DefaultFilters.getInstance().hasFilter(name);
    }

    public Iterable<String> getFilterNames(boolean includeGlobals)
    {
        if (!includeGlobals)
            return filterMap.keySet();
        else
        {
            List<String> filters = new LinkedList<String>(filterMap.keySet());
            Iterable<String> defNames = DefaultFilters.getInstance()
                    .getFilterNames();
            for (String filter : defNames)
                filters.add(filter);
            return filters;
        }
    }

    public Iterable<String> getFilterNames()
    {
        return getFilterNames(true);
    }
}
