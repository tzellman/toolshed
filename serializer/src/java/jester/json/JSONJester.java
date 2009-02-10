package jester.json;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import jester.IJester;
import jester.SerializationUtils;
import jester.Transformer;
import jester.json.JSONUtils.BooleanTransformer;
import jester.json.JSONUtils.CollectionTransformer;
import jester.json.JSONUtils.MapTransformer;
import jester.json.JSONUtils.NumberTransformer;
import jester.json.JSONUtils.StringTransformer;

import org.apache.commons.lang.NotImplementedException;

/**
 * JSON Serializer/Deserializer
 * 
 */
public class JSONJester implements IJester
{

    public static final String JSON_TYPE = "json";

    Map<String, Transformer<String>> transformers;

    Map<String, Class> classes;

    /**
     * Create a new JSONSerializer
     */
    public JSONJester()
    {
        transformers = new HashMap<String, Transformer<String>>();
        classes = new TreeMap<String, Class>(); // want in order

        // register some defaults
        registerTransformer(String.class, new StringTransformer());
        registerTransformer(Number.class, new NumberTransformer());
        registerTransformer(Boolean.class, new BooleanTransformer());
        registerTransformer(Map.class, new MapTransformer());
        registerTransformer(Collection.class, new CollectionTransformer());
    }

    public String getContentType()
    {
        return JSON_TYPE;
    }

    /**
     * Registers a JSONProvider for the given Class
     * 
     * @param target
     * @param jsonBeanProcessor
     */
    public void registerTransformer(Class clazz,
            final Transformer<String> transformer)
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
     * Serializes the input Object to JSON
     * 
     * @param object
     * @return
     * @throws Exception
     */
    public String serialize(Object object, Map hints) throws Exception
    {
        Transformer<String> serializer = null;

        // guard against null objects
        if (object == null)
            return defaultOut(object, hints);

        Class clazz = object.getClass();
        String clazzName = clazz.getName();

        // first see if we have an exact match
        if (transformers.containsKey(clazzName))
            serializer = transformers.get(clazzName);
        else
        {
            // now, see if it is a sub type of any
            for (Class c : classes.values())
            {
                if (c.isAssignableFrom(clazz))
                {
                    serializer = transformers.get(c.getName());
                    break;
                }
            }
        }

        if (serializer == null)
        {
            // do the default serialization if we can't decipher it
            return defaultOut(object, hints);
        }
        else
        {
            return serializer.to(object, this, hints);
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
    protected String defaultOut(Object object, Map hints) throws Exception
    {
        if (object == null)
            return "null";
        else if (SerializationUtils.isArrayType(object))
        {
            return serialize(SerializationUtils.objectToCollection(object),
                    hints);
        }
        return JSONUtils.toJSONString(object.toString());
    }

}
