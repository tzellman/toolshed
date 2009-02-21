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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;

/**
 * An OGNLConverter evaluates an Object using the OGNL ideology.
 * 
 * For example:
 * 
 * OGNLConverter converter = new OGNLConverter("toString", "length"); Object
 * derived = e.evaluate("test", null); --> will produce the Integer value 4 This
 * would be the same as calling "test".toString().length().
 * 
 * A useful way to streamline is to use the StringUtils from Apache
 * commons-lang. For example:
 * 
 * <code>
 * String ognl1 = "toString.length"; String ognl2 = "toString/length"
 * OGNLConverter e1 = new OGNLConverter(StringUtils.splitByWholeSeparator(ognl1, "."));
 * OGNLConverter e2 = new OGNLConverter(StringUtils.splitByWholeSeparator(ognl2, "/"));
 * </code>
 */
public class OGNLConverter implements IConverter
{

    protected List<String> expressions;

    /**
     * 
     * @param expressions
     *            Array of String expressions to apply to the Object
     */
    public OGNLConverter(String... expressions)
    {
        this.expressions = Arrays.asList(expressions);
    }

    public Object convert(Object from, Map hints)
    {
        Object object = from;
        for (String expression : this.expressions)
        {
            boolean found = false; // used to flag if we found it

            // first, check to see if it has parameters/methods with this name
            List<String> names = new ArrayList<String>();
            names.add(expression);

            // compute the getter name and add it as a possibility
            names.add("get" + expression.substring(0, 1).toUpperCase()
                    + expression.substring(1));

            for (String name : names)
            {
                // try it as a field
                try
                {
                    Field field = object.getClass().getField(name);
                    object = field.get(object);
                    found = true;
                }
                catch (Exception e)
                {
                    // try it as a method
                    try
                    {
                        Method method = object.getClass().getMethod(name, null);
                        if (method.getParameterTypes().length != 0)
                            throw new NullArgumentException(method
                                    .getParameterTypes()[0].getClass()
                                    .getName());
                        object = method.invoke(object, null);
                        found = true;
                        break;
                    }
                    catch (Exception e1)
                    {
                    }
                }
            }

            // if we found it already, continue on
            if (found)
                continue;

            // see if it is a list
            if (object instanceof List)
            {
                List listObj = (List) object;
                // try to convert the name to an index
                try
                {
                    int index = Integer.parseInt(expression);
                    if (index < 0 || index >= listObj.size())
                        throw new IndexOutOfBoundsException(expression);
                    object = listObj.get(index);
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            // try it as a collection, maybe -- more expensive, possibly
            else if (object instanceof Collection)
            {
                Collection collObj = (Collection) object;
                // try to convert the name to an index
                try
                {
                    int index = Integer.parseInt(expression);
                    int offset = 0;

                    for (Iterator iterator = collObj.iterator(); iterator
                            .hasNext()
                            && !found;)
                    {
                        if (index == offset)
                        {
                            object = iterator.next();
                            found = true;
                        }
                        else
                            iterator.next();
                        offset++;
                    }
                    if (!found)
                        throw new IndexOutOfBoundsException(expression);
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            // it could be a member of a Map
            else if (object instanceof Map)
            {
                Map mapObj = (Map) object;
                if (mapObj.containsKey(expression))
                    object = mapObj.get(expression);
                else
                    object = null;
            }
            else
            {
                // maybe it has a DynaBean-like facade?
                try
                {
                    Method method = object.getClass().getMethod("get",
                            String.class);
                    object = method.invoke(object, expression);
                }
                catch (Exception e)
                {
                    // otherwise, it can't find the member object
                    object = null;
                }
            }
        }
        return object;
    }

    /**
     * Static shortcut for evaluating an Object using OGNL notation.
     * 
     * @param object
     * @param hints
     *            optional Map of hints
     * @param expressions
     *            Array of String expressions
     * @return a derived Object, or null if not found
     */
    public static Object evaluate(Object object, Map hints,
            String... expressions)
    {
        return new OGNLConverter(expressions).convert(object, hints);
    }

    public static Object evaluate(Object object, String... expressions)
    {
        return evaluate(object, (Map) null, expressions);
    }

}
