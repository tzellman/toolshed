/*
 * =============================================================================
 * This file is part of Galoot
 * =============================================================================
 * (C) Copyright 2009, Tom Zellman, tzellman@gmail.com
 * 
 * Galoot is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
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
        Context context = getVariableContext(key);
        return context != null ? context.getVariable(key) : null;
    }

    public Macro getMacro(String macroName)
    {
        Context context = getMacroContext(macroName);
        return context != null ? context.getMacro(macroName) : null;
    }

    /**
     * Puts a variable into the current map.
     * 
     * Same as putVariable(key, val, false);
     * 
     * @param key
     * @param val
     */
    public boolean putVariable(String key, Object val)
    {
        return putVariable(key, val, false);
    }

    /**
     * Puts a variable into the context. If global is set to true and the
     * variable already exists in a higher stack frame, then the variable is
     * updated in that frame.
     * 
     * @param key
     * @param val
     * @param global
     * @return
     */
    public boolean putVariable(String key, Object val, boolean global)
    {
        if (contextStack.isEmpty())
            return false;
        Context context = global ? getVariableContext(key) : null;
        if (context != null)
            context.putVariable(key, val);
        else
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
        return putMacro(macroName, macro, false);
    }

    public boolean putMacro(String macroName, Macro macro, boolean global)
    {
        if (contextStack.isEmpty())
            return false;
        Context context = global ? getMacroContext(macroName) : null;
        if (context != null)
            context.putMacro(macroName, macro);
        else
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

    protected Context getVariableContext(String key)
    {
        for (Iterator<Context> it = contextStack.iterator(); it.hasNext();)
        {
            Context context = (Context) it.next();
            if (context.hasVariable(key))
                return context;
        }
        return null;
    }

    protected Context getMacroContext(String macro)
    {
        for (Iterator<Context> it = contextStack.iterator(); it.hasNext();)
        {
            Context context = (Context) it.next();
            if (context.hasMacro(macro))
                return context;
        }
        return null;
    }

}
