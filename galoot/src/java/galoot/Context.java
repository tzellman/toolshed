package galoot;

import java.util.Map;

/**
 * The context class is a glorified map of variables/objects. It has in internal
 * stack, which corresponds to the context depth.
 * 
 */
public class Context extends MapStack<String, Object>
{
    protected FilterRegistry filterMap;

    public Context()
    {
        filterMap = new FilterRegistry();
    }

    public Context(Map<String, Object> initialItems)
    {
        super(initialItems);
    }

    public FilterRegistry getFilterMap()
    {
        return filterMap;
    }

}
