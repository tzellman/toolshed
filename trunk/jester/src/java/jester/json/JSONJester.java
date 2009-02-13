package jester.json;

import java.util.Map;

import jester.ITransformer;
import jester.JesterUtils;
import jester.ReflectionUtils;
import jester.StringJester;
import jester.json.JSONUtils.BooleanTransformer;
import jester.json.JSONUtils.CollectionTransformer;
import jester.json.JSONUtils.MapTransformer;
import jester.json.JSONUtils.NumberTransformer;
import jester.json.JSONUtils.StringTransformer;

import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.StringUtils;

/**
 * JSON Serializer/Deserializer. Use an instance of a JSONJester if you want to
 * serialize data to JSON.
 * 
 * TODO: we don't support deserialization yet
 * 
 */
public class JSONJester extends StringJester
{

    public static final String JSON_TYPE = "json";

    /**
     * Create a new JSONSerializer
     */
    public JSONJester()
    {
        // register some defaults
        ITransformer<String, ? extends Object>[] defaultTransformers = new ITransformer[] {
                new StringTransformer(), new NumberTransformer(),
                new BooleanTransformer(), new MapTransformer(this),
                new CollectionTransformer(this) };

        for (ITransformer<String, ? extends Object> transformer : defaultTransformers)
        {
            // get the "From" Class defined as the F Generic Type of
            // ITransformer (2nd parameter)
            Class fromClass = ReflectionUtils.getGenericTypeClassesList(
                    ITransformer.class, transformer).get(1).getTypeClass();
            registerTransformer(fromClass, transformer);
        }
    }

    public String getContentType()
    {
        return JSON_TYPE;
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
        else if (JesterUtils.isArrayType(object))
        {
            return to(JesterUtils.objectToCollection(object), hints);
        }
        return JSONUtils.toJSONString(object.toString());
    }

    @Override
    protected Object defaultIn(String string, Map hints) throws Exception
    {
        if (StringUtils.equals(string, "null"))
            return null;

        throw new SerializationException("Unable to transform JSON to Object.");
    }

}
