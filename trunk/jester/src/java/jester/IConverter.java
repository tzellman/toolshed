package jester;

import java.util.Map;

/**
 * Converts one type to another.
 * 
 * @author tzellman
 * 
 * @param <F>
 *            the from/input object type
 * @param <T>
 *            the to/output object type
 */
public interface IConverter<F, T>
{
    /**
     * Convert the from F object to a T object.
     * 
     * @param from
     *            the input object to convert from
     * @param hints
     *            (optional) Map of hints to be used during the conversion
     *            process
     * @return a T object
     */
    T convert(F from, Map hints);
}
