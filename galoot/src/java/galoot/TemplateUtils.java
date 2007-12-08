package galoot;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class TemplateUtils
{
    private TemplateUtils()
    {
    }

    /**
     * This function evaluates a given object and attempts to retrieve a nested
     * sub-object. Sort of like OGNL.
     * 
     * Example: Object object = new String("some text"); list.add("toString");
     * //not necessary, but showing how we can chain them list.add("length");
     * Object obj = evaluateObject(object, list);
     * System.out.println(obj.toString()); // will print the length
     * 
     * 
     * @param object
     * @param members
     * @return
     */
    public static Object evaluateObject(Object object, Iterable<String> members)
    {
        for (Iterator<String> it = members.iterator(); object != null
                && it.hasNext();)
        {
            String memberName = (String) it.next();

            boolean found = false; // used to flag if we found it

            // first, check to see if it has parameters/methods with this name
            try
            {
                Field field = object.getClass().getField(memberName);
                object = field.get(object);
                found = true;
            }
            catch (Exception e)
            {
                // ok, let's see if it has a method with that name
                List<String> names = new ArrayList<String>();
                names.add(memberName);

                // compute the getter name and add it as a possibility
                names.add("get" + memberName.substring(0, 1).toUpperCase()
                        + memberName.substring(1));

                for (String name : names)
                {
                    try
                    {
                        Method method = object.getClass().getMethod(memberName,
                                null);
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
        }
        return object;
    }

    /**
     * Evaluates the given object, returning its "boolean-ness"
     * 
     * @param object
     * @return
     */
    public static boolean evaluateAsBoolean(Object object)
    {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (Boolean) object;
        if (object instanceof Number)
            return !Double.valueOf(((Number) object).doubleValue()).equals(
                    new Double(0.0));
        if (object instanceof String)
            return !((String) object).isEmpty();
        if (object instanceof List)
            return ((List) object).size() != 0;
        if (object instanceof Map)
            return ((Map) object).size() != 0;
        return true;
    }

    /**
     * Decides if the passed-in object is a basic array [] type.
     * 
     * @param object
     * @return
     */
    public static boolean isArrayType(Object object)
    {
        return object.getClass().isArray();
        // return object != null
        // && ((object instanceof Object[]) || (object instanceof int[])
        // || (object instanceof long[])
        // || (object instanceof float[])
        // || (object instanceof double[])
        // || (object instanceof boolean[])
        // || (object instanceof byte[])
        // || (object instanceof short[]) || (object instanceof char[]));
    }

    /**
     * Returns the length of the object, if it is a known object with a
     * size/length-like attribute. For example, lists, arrays, strings.
     * 
     * @param object
     * @return the length of the object, or null
     */
    public static Integer getObjectLength(Object object)
    {
        if (object instanceof List)
            return ((List) object).size();
        else if (object instanceof String)
            return ((String) object).length();
        else if (object instanceof Map)
            return ((Map) object).size();
        else if (isArrayType(object))
        {
            if (object instanceof Object[])
                return ((Object[]) object).length;
            if (object instanceof int[])
                return ((int[]) object).length;
            if (object instanceof long[])
                return ((long[]) object).length;
            if (object instanceof float[])
                return ((float[]) object).length;
            if (object instanceof double[])
                return ((double[]) object).length;
            if (object instanceof boolean[])
                return ((boolean[]) object).length;
            if (object instanceof byte[])
                return ((byte[]) object).length;
            if (object instanceof short[])
                return ((short[]) object).length;
            if (object instanceof char[])
                return ((char[]) object).length;
        }
        return null;
    }

    /**
     * Returns a collection which contains the items of the given object, if it
     * is an array[] type.
     * 
     * @param a
     * @return Collection, or null if it is not an array[] object
     */
    public static Collection<?> objectToCollection(Object a)
    {
        if (!isArrayType(a))
            return null;

        if (a instanceof Object[])
            return arrayToCollection((Object[]) a);
        if (a instanceof int[])
        {
            int[] arr = (int[]) a;
            Collection<Integer> c = new ArrayList<Integer>(arr.length);
            for (int i = 0, size = arr.length; i < size; ++i)
                c.add(arr[i]);
            return c;
        }
        if (a instanceof long[])
        {
            long[] arr = (long[]) a;
            Collection<Long> c = new ArrayList<Long>(arr.length);
            for (int i = 0, size = arr.length; i < size; ++i)
                c.add(arr[i]);
            return c;
        }
        if (a instanceof float[])
        {
            float[] arr = (float[]) a;
            Collection<Float> c = new ArrayList<Float>(arr.length);
            for (int i = 0, size = arr.length; i < size; ++i)
                c.add(arr[i]);
            return c;
        }
        if (a instanceof double[])
        {
            double[] arr = (double[]) a;
            Collection<Double> c = new ArrayList<Double>(arr.length);
            for (int i = 0, size = arr.length; i < size; ++i)
                c.add(arr[i]);
            return c;
        }
        if (a instanceof boolean[])
        {
            boolean[] arr = (boolean[]) a;
            Collection<Boolean> c = new ArrayList<Boolean>(arr.length);
            for (int i = 0, size = arr.length; i < size; ++i)
                c.add(arr[i]);
            return c;
        }
        if (a instanceof byte[])
        {
            byte[] arr = (byte[]) a;
            Collection<Byte> c = new ArrayList<Byte>(arr.length);
            for (int i = 0, size = arr.length; i < size; ++i)
                c.add(arr[i]);
            return c;
        }
        if (a instanceof short[])
        {
            short[] arr = (short[]) a;
            Collection<Short> c = new ArrayList<Short>(arr.length);
            for (int i = 0, size = arr.length; i < size; ++i)
                c.add(arr[i]);
            return c;
        }
        if (a instanceof char[])
        {
            char[] arr = (char[]) a;
            Collection<Character> c = new ArrayList<Character>(arr.length);
            for (int i = 0, size = arr.length; i < size; ++i)
                c.add(arr[i]);
            return c;
        }

        return null;
    }

    public static <T> void arrayToCollection(T[] a, Collection<T> c)
    {
        for (T o : a)
            c.add(o);
    }

    public static <T> Collection<T> arrayToCollection(T[] a)
    {
        Collection<T> c = new ArrayList<T>(a.length);
        arrayToCollection(a, c);
        return c;
    }

}
