/* =============================================================================
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package galoot;

import java.util.LinkedHashMap;
import java.util.Map;

public class Context
{
    protected Map<String, Object> contextMap;

    protected Map<String, Macro> macroMap;

    public Context()
    {
        this(null);
    }

    public Context(Map<String, Object> initialContents)
    {
        contextMap = new LinkedHashMap<String, Object>();
        macroMap = new LinkedHashMap<String, Macro>();
        if (initialContents != null)
            add(initialContents);
    }

    public void putVariable(String key, Object value)
    {
        contextMap.put(key, value);
    }

    /**
     * Only allow package-level access
     * 
     * @param key
     * @param value
     */
    protected void putMacro(String key, Macro value)
    {
        macroMap.put(key, value);
    }

    public Object getVariable(String key)
    {
        return contextMap.get(key);
    }

    public Macro getMacro(String key)
    {
        return macroMap.get(key);
    }

    public boolean hasVariable(String key)
    {
        return contextMap.containsKey(key);
    }

    public boolean hasMacro(String key)
    {
        return macroMap.containsKey(key);
    }

    public Object removeVariable(String key)
    {
        return contextMap.remove(key);
    }

    public void add(Map<String, Object> map)
    {
        if (map != null)
            for (String key : map.keySet())
                putVariable(key, map.get(key));
    }

}
