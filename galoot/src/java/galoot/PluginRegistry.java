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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Global singleton registry for tracking plug-ins.
 * 
 * The RegisterFilter keeps global instances of plug-ins, for easy reuse.
 */
public final class PluginRegistry
{
    private static final Log log = LogFactory.getLog(PluginRegistry.class);

    private static PluginRegistry instance = new PluginRegistry();

    private FilterMap filterMap;

    private Map<String, String> templateIncludePaths;

    private PluginRegistry()
    {
        filterMap = new FilterMap();
        templateIncludePaths = Collections
                .synchronizedMap(new LinkedHashMap<String, String>());
    }

    public static PluginRegistry getInstance()
    {
        return instance;
    }

    /**
     * Register a filter
     * 
     * @param filter
     */
    public void registerFilter(Filter filter)
    {
        if (hasFilter(filter.getName()))
            log.warn("Filter already exists: " + filter.getName()
                    + ", replacing...");
        filterMap.addFilter(filter);
    }

    public Filter getFilter(String name)
    {
        return hasFilter(name) ? filterMap.getFilter(name) : null;
    }

    public Iterable<String> getFilterNames()
    {
        return filterMap.getFilterNames();
    }

    public boolean hasFilter(String name)
    {
        return filterMap.hasFilter(name);
    }

    public boolean unregisterFilter(String name)
    {
        return filterMap.removeFilter(name);
    }

    public void addTemplateIncludePath(String includePath)
    {
        templateIncludePaths.put(includePath, includePath);
    }

    public Iterable<String> getTemplateIncludePaths()
    {
        return templateIncludePaths.keySet();
    }

    public void removeTemplateIncludePath(String path)
    {
        templateIncludePaths.remove(path);
    }

}
