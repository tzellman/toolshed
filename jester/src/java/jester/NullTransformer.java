package jester;

import java.util.Map;

/**
 * A Transformer knows how to transform to/from an Object
 */
public class NullTransformer<T, F> implements ITransformer<T, F>
{
    /**
     * Returns null by default
     */
    public F from(T object, Map hints) throws Exception
    {
        return null;
    }

    /**
     * Returns null by default
     */
    public T to(F object, Map hints) throws Exception
    {
        return null;
    }
}
