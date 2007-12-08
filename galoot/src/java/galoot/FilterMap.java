package galoot;

import java.util.LinkedHashMap;
import java.util.Map;

public class FilterMap
{
    private Map<String, Filter> filterMap;

    public FilterMap()
    {
        filterMap = new LinkedHashMap<String, Filter>();
    }

    public void registerFilter(Filter filter)
    {
        filterMap.put(filter.getName(), filter);
    }

    public Filter getFilter(String name)
    {
        return filterMap.containsKey(name) ? filterMap.get(name)
                : (DefaultFilters.getInstance().hasFilter(name) ? DefaultFilters
                        .getInstance().getFilter(name)
                        : null);
    }

    public void removeFilter(String name)
    {
        filterMap.remove(name);
    }

    public boolean hasFilter(String name)
    {
        return filterMap.containsKey(name)
                || DefaultFilters.getInstance().hasFilter(name);
    }

    public Iterable<String> getFilterNames()
    {
        return filterMap.keySet();
    }
}
