package jester;

import java.util.Map;

/**
 * A Transformer knows how to transform to/from an Object
 */
public class NullTransformer<T> implements ITransformer<T>
{
    /**
     * Returns null by default
     */
    public Object from(T data, Map hints) throws Exception
    {
        return null;
    }

    /**
     * Returns null by default
     */
    public T to(Object object, Map hints) throws Exception
    {
        return null;
    }
}
