package galoot;

import java.util.LinkedHashMap;
import java.util.Map;

public class FilterRegistry
{
    private Map<String, Filter> filterMap;

    public FilterRegistry()
    {
        filterMap = new LinkedHashMap<String, Filter>();
    }

    public void registerFilter(Filter filter)
    {
        //we map the lower case name of the class -> filter instance
        String filterName = filter.getClass().getSimpleName().toLowerCase();
        filterMap.put(filterName, filter);
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
}