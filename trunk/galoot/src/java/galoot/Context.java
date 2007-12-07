package galoot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * The context class is a glorified map of variables/objects. It has in internal
 * stack, which corresponds to the context depth.
 * 
 * @author tzellman
 * 
 */
public class Context
{
    protected Stack<Map<String, Object>> contextStack;

    protected FilterMap filterMap;

    public Context()
    {
        contextStack = new Stack<Map<String, Object>>();
        contextStack.push(new HashMap<String, Object>()); // initialize it
        filterMap = new FilterMap();
    }

    public Context(Map<String, Object> initialItems)
    {
        this();
        add(initialItems);
    }

    /**
     * Returns the variable with the given name
     * 
     * @param name
     * @return the variable or null if not found
     */
    public Object get(String name)
    {
        for (Iterator<Map<String, Object>> it = contextStack.iterator(); it
                .hasNext();)
        {
            Map<String, Object> context = (Map<String, Object>) it.next();
            if (context.containsKey(name))
                return context.get(name);
        }
        return null;
    }

    /**
     * Push a new new context onto the context stack.
     */
    public void push()
    {
        contextStack.push(new HashMap<String, Object>());
    }

    /**
     * Pops the current context off the context stack.
     * 
     * @return
     */
    public Map<String, Object> pop()
    {
        return contextStack.pop();
    }

    /**
     * Puts a variable into the current context
     * 
     * @param key
     * @param val
     */
    public void put(String key, Object val)
    {
        contextStack.peek().put(key, val);
    }

    /**
     * Add the items from the given map to the current context.
     * 
     * @param map
     */
    public void add(Map<String, Object> map)
    {
        if (map != null)
            for (String key : map.keySet())
                put(key, map.get(key));
    }

    public FilterMap getFilterMap()
    {
        return filterMap;
    }

}
