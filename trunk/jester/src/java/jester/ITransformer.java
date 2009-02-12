package jester;

import java.util.Map;

/**
 * A Transformer knows how to transform to/from an Object
 */
public interface ITransformer<T, F>
{
    /**
     * Transforms the input Object to some type of ouput data. It is assumed
     * that the returned value is formatted in a finalized state, ready for
     * serialization.
     * 
     * @param object
     *            the Object to transform
     * @param hints
     *            optional Map of serialization hints
     * @return
     * @throws Exception
     */
    T to(F object, Map hints) throws Exception;

    /**
     * Returns an Object, transformed from the input object.
     * 
     * @param data
     *            the data being serialized to the new Object
     * @param hints
     *            optional Map of serialization hints
     * @return
     * @throws Exception
     */
    F from(T object, Map hints) throws Exception;
}
