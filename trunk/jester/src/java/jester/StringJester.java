package jester;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.NotImplementedException;

/**
 * Jester that uses Transformers as the means for pluggable serializers,
 * requiring them to be able to transform an object to a String.
 */
public abstract class StringJester implements IJester
{

    protected Map<String, ITransformer<String>> transformers;

    protected Map<String, Class> classes;

    /**
     * Create a new JSONSerializer
     */
    public StringJester()
    {
        transformers = new HashMap<String, ITransformer<String>>();
        classes = new TreeMap<String, Class>(); // want in order
    }

    /**
     * Registers a JSONProvider for the given Class
     * 
     * @param target
     * @param jsonBeanProcessor
     */
    public void registerTransformer(Class clazz,
            final ITransformer<String> transformer)
    {
        String className = clazz.getName();
        transformers.put(className, transformer);
        classes.put(className, clazz);
    }

    public void unregisterTransformer(Class clazz)
    {
        String className = clazz.getName();
        transformers.remove(className);
        classes.remove(className);
    }

    /**
     * Fulfills the IJester interface.
     * 
     * @throws NotImplementedException
     */
    public Object in(InputStream stream, Map hints) throws Exception
    {
        // TODO
        throw new NotImplementedException();
    }

    /**
     * Serializes the input Object to a String
     * 
     * @param object
     * @return
     * @throws Exception
     */
    public String serialize(Object object, Map hints) throws Exception
    {
        ITransformer<String> transformer = null;

        // guard against null objects
        if (object == null)
            return defaultOut(object, hints);

        Class clazz = object.getClass();
        String clazzName = clazz.getName();

        // first see if we have an exact match
        if (transformers.containsKey(clazzName))
            transformer = transformers.get(clazzName);
        else
        {
            // now, see if it is a sub type of any
            for (Class c : classes.values())
            {
                if (c.isAssignableFrom(clazz))
                {
                    transformer = transformers.get(c.getName());
                    break;
                }
            }
        }

        if (transformer == null)
        {
            // do the default serialization if we can't decipher it
            return defaultOut(object, hints);
        }
        else
        {
            return transformer.to(object, hints);
        }
    }

    /**
     * Fulfills the IJester Interface
     */
    public void out(Object object, OutputStream out, Map hints)
            throws Exception
    {
        out.write(serialize(object, hints).getBytes());
    }

    /**
     * Override this method to provide your own default output serialization.
     * 
     * This, combined with registering Class transformers is how you can
     * customize the serialization.
     * 
     * @param object
     *            the object being serialized
     * @param hints
     *            optional Map of serialization hints
     * @return
     */
    protected abstract String defaultOut(Object object, Map hints)
            throws Exception;

}
