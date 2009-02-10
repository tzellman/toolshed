package jester;

/**
 * A Transformer knows how to transform to/from an Object
 */
public interface Transformer<T>
{
    /**
     * Transforms the input Object to some type of ouput data. It is assumed
     * that the returned value is formatted in a finalized state, ready for
     * serialization.
     * 
     * @param object
     * @param jester
     * @return
     * @throws Exception
     */
    T to(Object object, IJester jester) throws Exception;

    /**
     * Returns an Object, transformed from the input data.
     * 
     * @param json
     * @param jester
     * @return
     * @throws Exception
     */
    Object from(T data, IJester jester) throws Exception;
}
