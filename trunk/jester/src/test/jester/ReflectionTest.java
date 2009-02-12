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

        List<GenericTypeInfo> genericInfo = ReflectionUtils
                .getGenericTypeClasses(IChanger.class, m);
        assertEquals(genericInfo.size(), 1);
        assertEquals(genericInfo.get(0).getTypeName(), "F");
        assertEquals(genericInfo.get(0).getTypeClass(), String.class);

        genericInfo = ReflectionUtils.getGenericTypeClasses(
                NumberChanger.class, m);
        assertEquals(genericInfo.size(), 1);
        assertEquals(genericInfo.get(0).getTypeName(), "F");
        assertEquals(genericInfo.get(0).getTypeClass(), String.class);

        genericInfo = ReflectionUtils.getGenericTypeClasses(MyChanger.class, m);
        assertEquals(genericInfo.size(), 0);
    }
}
