package jester.json;

import java.util.Collection;
import java.util.Map;

import jester.JesterUtils;
import jester.StringJester;
import jester.json.JSONUtils.BooleanTransformer;
import jester.json.JSONUtils.CollectionTransformer;
import jester.json.JSONUtils.MapTransformer;
import jester.json.JSONUtils.NumberTransformer;
import jester.json.JSONUtils.StringTransformer;

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
        registerTransformer(String.class, new StringTransformer());
        registerTransformer(Number.class, new NumberTransformer());
        registerTransformer(Boolean.class, new BooleanTransformer());
        registerTransformer(Map.class, new MapTransformer(this));
        registerTransformer(Collection.class, new CollectionTransformer(this));
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
            return serialize(JesterUtils.objectToCollection(object),
                    hints);
        }
        return JSONUtils.toJSONString(object.toString());
    }

}
