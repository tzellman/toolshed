package galoot;

import galoot.types.MapStack;

import java.util.Map;

/**
 * The context class is a glorified map of variables/objects. It has in internal
 * stack, which corresponds to the context depth.
 * 
 */
public class ContextStack extends MapStack<String, Object>
{
    protected FilterMap filterMap;

    public ContextStack()
    {
        filterMap = new FilterMap();
    }

    public ContextStack(Map<String, Object> initialItems)
    {
        super(initialItems);
        filterMap = new FilterMap();
    }

    public FilterMap getFilterMap()
    {
        return filterMap;
    }

}
