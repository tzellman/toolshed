package jester;

import java.util.List;

import jester.ReflectionUtils.GenericTypeInfo;
import junit.framework.TestCase;

public class ReflectionTest extends TestCase
{
    interface IChanger<T, F>
    {
        F change(T object);
    }

    abstract class NumberChanger<F> implements IChanger<Number, F>
    {
    }

    class MyChanger extends NumberChanger<String>
    {
        public String change(Number number)
        {
            return number.toString();
        }
    }

    public void testGenericType()
    {
        MyChanger m = new MyChanger();

        // This should have 2 generic types : <T, F>
        List<GenericTypeInfo> genericInfo = ReflectionUtils
                .getGenericTypeClassesList(IChanger.class, m);
        assertEquals(genericInfo.size(), 2);
        assertEquals(genericInfo.get(0).getTypeName(), "T");
        assertEquals(genericInfo.get(0).getTypeClass(), Number.class);
        assertEquals(genericInfo.get(1).getTypeName(), "F");
        assertEquals(genericInfo.get(1).getTypeClass(), String.class);

        // This should only have one, since we are setting the base to
        // NumberChanger : <F>
        genericInfo = ReflectionUtils.getGenericTypeClassesList(
                NumberChanger.class, m);
        assertEquals(genericInfo.size(), 1);
        assertEquals(genericInfo.get(0).getTypeName(), "F");
        assertEquals(genericInfo.get(0).getTypeClass(), String.class);

        // This shouldn't have any, since we set the base to the non-generic
        // class
        genericInfo = ReflectionUtils.getGenericTypeClassesList(
                MyChanger.class, m);
        assertEquals(genericInfo.size(), 0);
    }
}
