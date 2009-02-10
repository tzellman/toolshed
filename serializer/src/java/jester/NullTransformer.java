package jester;

/**
 * A Transformer knows how to transform to/from an Object
 */
public class NullTransformer<T> implements Transformer<T>
{
    /**
     * Returns null by default
     */
    public Object from(T data, IJester jester) throws Exception
    {
        return null;
    }

    /**
     * Returns null by default
     */
    public T to(Object object, IJester jester) throws Exception
    {
        return null;
    }
}
