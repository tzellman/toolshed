/* =============================================================================
 * This file is part of Jester
 * =============================================================================
 * (C) Copyright 2009, Tom Zellman, tzellman@gmail.com
 *
 * Jester is free software; you can redistribute it and/or
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
package jester;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.StringUtils;

public class POJOCreator<T> implements IConverter<Map<String, Object>, T>
{
    protected Class<? extends T> model;

    /**
     * 
     * @param expressions
     *            Array of expressions
     */
    public POJOCreator(Class<? extends T> model)
    {
        this.model = model;
    }

    public T convert(Map<String, Object> from, Map hints) throws SerializationException
    {
        try
        {
            T newInstance = (T) model.newInstance();
            Method[] methods = newInstance.getClass().getMethods();
            for (Method method : methods)
            {
                String mName = method.getName();

                if (mName.startsWith("set") && mName.length() > 3
                        && method.getParameterTypes().length == 1)
                {
                    String name = StringUtils.lowerCase(method.getName().charAt(3) + "")
                            + method.getName().substring(4);
                    if (from.containsKey(name))
                    {
                        method.invoke(newInstance, from.get(name));
                    }
                }
            }
            return newInstance;
        }
        catch (Exception e)
        {
            throw new SerializationException(e);
        }
    }

}
