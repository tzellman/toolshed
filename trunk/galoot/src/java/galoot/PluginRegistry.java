package galoot;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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

    private static PluginRegistry instance = null;

    FilterMap filterMap;

    private List<String> includePaths;

    protected PluginRegistry()
    {
        filterMap = new FilterMap();
        includePaths = Collections.synchronizedList(new LinkedList<String>());
    }

    public static PluginRegistry getInstance()
    {
        if (instance == null)
        {
            synchronized (DefaultFilters.class)
            {
                if (instance == null)
                {
                    instance = new PluginRegistry();
                }
            }
        }
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

    public void addIncludePath(String includePath)
    {
        includePaths.add(includePath);
    }

    public Iterable<String> getIncludePaths()
    {
        return includePaths;
    }

}
