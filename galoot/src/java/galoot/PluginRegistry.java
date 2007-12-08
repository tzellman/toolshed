package galoot;

import java.util.LinkedList;
import java.util.List;

/**
 * Global singleton registry for tracking plug-ins.
 */
public final class PluginRegistry
{
    private static PluginRegistry instance = null;

    private FilterMap filterMap;

    private List<String> includePaths;

    protected PluginRegistry()
    {
        filterMap = new FilterMap();
        includePaths = new LinkedList<String>();
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
    
    public void registerFilter(Filter filter)
    {
        
    }

    public Filter getFilter(String name)
    {
        return filterMap.hasFilter(name) ? filterMap.getFilter(name) : null;
    }

    public Iterable<String> getFilterNames()
    {
        return filterMap.getFilterNames();
    }

    public boolean hasFilter(String name)
    {
        return filterMap.hasFilter(name);
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
