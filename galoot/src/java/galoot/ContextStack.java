package galoot;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * The context class is a glorified map of variables/objects. It has in internal
 * stack, which corresponds to the context depth.
 * 
 */
public class ContextStack
{
    protected LinkedList<Context> contextStack;

    protected FilterMap filterMap;

    public ContextStack()
    {
        this(new Context());
    }

    public ContextStack(Context initialContext)
    {
        contextStack = new LinkedList<Context>();
        contextStack.addFirst(initialContext != null ? initialContext
                : new Context()); // initialize it
        filterMap = new FilterMap();
    }

    public ContextStack(Map<String, Object> initialContents)
    {
        this();
        add(initialContents);
    }

    public FilterMap getFilterMap()
    {
        return filterMap;
    }

    /**
     * Push a new new context onto the context stack.
     */
    public void push()
    {
        contextStack.addFirst(new Context());
    }

    /**
     * Pops the current map off the context stack.
     * 
     * @return
     */
    public Context pop()
    {
        return contextStack.removeFirst();
    }

    /**
     * Returns the object with the given key. The object can reside in any of
     * the maps in the stack, searching backwards from map on the top of the
     * stack.
     * 
     * @param name
     * @return the object or null if not found
     */
    public Object getVariable(String key)
    {
        for (Iterator<Context> it = contextStack.iterator(); it.hasNext();)
        {
            Context context = (Context) it.next();
            if (context.hasVariable(key))
                return context.getVariable(key);
        }
        return null;
    }

    public Macro getMacro(String macroName)
    {
        for (Iterator<Context> it = contextStack.iterator(); it.hasNext();)
        {
            Context context = (Context) it.next();
            if (context.hasMacro(macroName))
                return context.getMacro(macroName);
        }
        return null;
    }

    /**
     * Puts a variable into the current map
     * 
     * @param key
     * @param val
     */
    public boolean putVariable(String key, Object val)
    {
        if (contextStack.isEmpty())
            return false;
        contextStack.peek().putVariable(key, val);
        return true;
    }

    /**
     * Adds a Macro to the current context
     * 
     * @param macroName
     * @param macro
     * @return
     */
    public boolean putMacro(String macroName, Macro macro)
    {
        if (contextStack.isEmpty())
            return false;
        contextStack.peek().putMacro(macroName, macro);
        return true;
    }

    /**
     * Add the items from the input map to the map on the top of the stack.
     * 
     * @param map
     */
    public void add(Map<String, Object> map)
    {
        if (map != null)
            for (String key : map.keySet())
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
    public Object removeVariable(String key)
    {
        if (contextStack.isEmpty() || !contextStack.peek().hasVariable(key))
            return null;
        return contextStack.peek().removeVariable(key);
    }

}
