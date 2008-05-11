package galoot.types;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class MapStack<K, V>
{
    protected LinkedList<Map<K, V>> mapStack;

    public MapStack()
    {
        mapStack = new LinkedList<Map<K, V>>();
        mapStack.addFirst(new HashMap<K, V>()); // initialize it
    }

    public MapStack(Map<K, V> initialItems)
    {
        this();
        add(initialItems);
    }

    /**
     * Returns the object with the given key. The object can reside in any of
     * the maps in the stack, searching backwards from map on the top of the
     * stack.
     * 
     * @param name
     * @return the object or null if not found
     */
    public Object getVariable(K key)
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
        mapStack.addFirst(new HashMap<K, V>());
    }

    /**
     * Pops the current map off the context stack.
     * 
     * @return
     */
    public Map<K, V> pop()
    {
        return mapStack.removeFirst();
    }

    /**
     * Puts a variable into the current map
     * 
     * @param key
     * @param val
     */
    public boolean putVariable(K key, V val)
    {
        if (mapStack.isEmpty())
            return false;
        mapStack.peek().put(key, val);
        return true;
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
                putVariable(key, map.get(key));
    }

    /**
     * Removes the object with the specified key, only if it exists in the map
     * on the top of the stack. There is an enforcement that you can not remove
     * a value from a map lower on the stack.
     * 
     * @param key
     * @return
     */
    public Object removeVariable(K key)
    {
        if (mapStack.isEmpty() || !mapStack.peek().containsKey(key))
            return null;
        return mapStack.peek().remove(key);
    }

}