package galoot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

public class MapStack<K, V>
{
    protected Stack<Map<K, V>> mapStack;

    public MapStack()
    {
        mapStack = new Stack<Map<K, V>>();
        mapStack.push(new HashMap<K, V>()); // initialize it
    }

    public MapStack(Map<K, V> initialItems)
    {
        this();
        add(initialItems);
    }

    /**
     * Returns the variable with the given key
     * 
     * @param name
     * @return the variable or null if not found
     */
    public Object get(K key)
    {
        for (Iterator<Map<K, V>> it = mapStack.iterator(); it.hasNext();)
        {
            Map<K, V> context = (Map<K, V>) it.next();
            if (context.containsKey(key))
                return context.get(key);
        }
        return null;
    }

    /**
     * Push a new new context onto the context stack.
     */
    public void push()
    {
        mapStack.push(new HashMap<K, V>());
    }

    /**
     * Pops the current map off the context stack.
     * 
     * @return
     */
    public Map<K, V> pop()
    {
        return mapStack.pop();
    }

    /**
     * Puts a variable into the current map
     * 
     * @param key
     * @param val
     */
    public void put(K key, V val)
    {
        mapStack.peek().put(key, val);
    }

    /**
     * Add the items from the input map to the map on the top of the stack.
     * 
     * @param map
     */
    public void add(Map<K, V> map)
    {
        if (map != null)
            for (K key : map.keySet())
                put(key, map.get(key));
    }

}
