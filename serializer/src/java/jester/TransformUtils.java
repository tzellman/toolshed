package jester;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public final class TransformUtils
{

    /**
     * This function evaluates a given object and attempts to retrieve a nested
     * sub-object. Sort of like OGNL.
     * 
     * Example: evalueObject("string", "length"); --> 6
     * 
     * @param object
     * @param expression
     * @return
     */
    public static Object evaluateObject(Object object, String expression)
    {
        String[] members = StringUtils.split(expression, ".");
        for (String memberName : members)
        {
            boolean found = false; // used to flag if we found it

            // first, check to see if it has parameters/methods with this name
            List<String> names = new ArrayList<String>();
            names.add(memberName);

            // compute the getter name and add it as a possibility
            names.add("get" + memberName.substring(0, 1).toUpperCase()
                    + memberName.substring(1));

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
                            throw new InvalidParameterException();
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
                    int index = Integer.parseInt(memberName);
                    if (index < 0 || index >= listObj.size())
                        throw new IndexOutOfBoundsException(memberName);
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
                    int index = Integer.parseInt(memberName);
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
                        throw new IndexOutOfBoundsException(memberName);
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
                if (mapObj.containsKey(memberName))
                    object = mapObj.get(memberName);
                else
                    object = null;
            }
            // otherwise, it can't find the member object
            else
            {
                object = null;
            }
        }
        return object;
    }

    // private
    private TransformUtils()
    {
    }

}
