package jester;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.ObjectUtils;

public final class JesterUtils
{

    /**
     * Decides if the passed-in object is a basic array [] type.
     * 
     * @param object
     * @return
     */
    public static boolean isArrayType(Object object)
    {
        return object != null && object.getClass().isArray();
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

    /**
     * Shortcut for serializing to a String
     * 
     * @param object
     * @param jester
     * @return
     * @throws Exception
     */
    public static String serializeToString(Object object, IJester jester,
            Map hints) throws Exception
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        BufferedOutputStream stream = new BufferedOutputStream(bout);
        jester.out(object, stream, hints);
        stream.flush();
        return bout.toString();
    }

    public static String serializeToString(Object object, IJester jester)
            throws Exception
    {
        return serializeToString(object, jester, null);
    }

}
