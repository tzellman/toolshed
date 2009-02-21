package jester.json;

import java.util.Collection;
import java.util.Map;

import jester.ConverterRegistry;
import jester.IConverter;
import jester.utils.JesterUtils;

import org.apache.commons.lang.SerializationException;

public class JSONSerializer extends ConverterRegistry<Object, String>
{

    // ! Specify this hint along with a function name to get a jsonp result
    public static final String HINT_JSONP = "jsonp";

    public JSONSerializer()
    {
        register(new CollectionConverter(this));
        register(new StringConverter());
        register(new NumberConverter());
        register(new BooleanConverter());
        register(new MapConverter(this));
    }

    /**
     * Returns a JSON-ified string for the input String
     * 
     * @param string
     * @return
     */
    public static String toJSONString(String string)
    {
        StringBuffer b = new StringBuffer("\"");
        string = string.replace("\\", "\\\\");
        string = string.replace("\"", "\\\"");
        string = string.replace("\"", "\\\"");
        string = string.replace("\b", "\\b");
        string = string.replace("\f", "\\f");
        string = string.replace("\n", "\\n");
        string = string.replace("\r", "\\r");
        string = string.replace("\t", "\\t");
        b.append(string);
        b.append("\"");
        return b.toString();
    }

    /**
     * Converts a Collection to a JSON String
     */
    public static class CollectionConverter implements
            IConverter<Collection, String>
    {
        protected JSONSerializer registry;

        public CollectionConverter(JSONSerializer registry)
        {
            this.registry = registry;
        }

        public String convert(Collection from, Map hints)
                throws SerializationException
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append("[");

            Object[] vals = from.toArray();
            for (int i = 0, size = vals.length; i < size; ++i)
            {
                Object val = vals[i];
                String converted = registry.convert(val, String.class, hints);
                buffer.append(converted);
                if (i < size - 1)
                    buffer.append(",");
            }
            buffer.append("]");
            return buffer.toString();
        }
    }

    /**
     * Converts a String to a JSON String
     */
    public static class StringConverter implements IConverter<String, String>
    {
        public String convert(String from, Map hints)
                throws SerializationException
        {
            return toJSONString(from);
        }
    }

    /**
     * Converts a Number to a JSON String
     */
    public static class NumberConverter implements IConverter<Number, String>
    {
        public String convert(Number from, Map hints)
                throws SerializationException
        {
            return from.toString();
        }
    }

    /**
     * Converts a Boolean to a JSON String
     */
    public static class BooleanConverter implements IConverter<Boolean, String>
    {
        public String convert(Boolean from, Map hints)
                throws SerializationException
        {
            return from.booleanValue() ? "true" : "false";
        }
    }

    /**
     * Converts a Map to a JSON String
     */
    public static class MapConverter implements IConverter<Map, String>
    {
        protected JSONSerializer registry;

        public MapConverter(JSONSerializer registry)
        {
            this.registry = registry;
        }

        public String convert(Map from, Map hints)
                throws SerializationException
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append("{");
            Object[] keys = from.keySet().toArray();
            for (int i = 0, size = keys.length; i < size; ++i)
            {
                Object key = keys[i];
                String keyString = registry.convert(key, String.class, hints);
                if (!keyString.startsWith("\"") && !keyString.endsWith("\""))
                    keyString = toJSONString(keyString);

                buffer.append(keyString);
                buffer.append(":");
                buffer.append(registry.convert(from.get(key), String.class,
                        hints));
                if (i < size - 1)
                    buffer.append(",");
            }
            buffer.append("}");
            return buffer.toString();
        }
    }

    @Override
    public Object defaultConvert(Object from, Class<? extends Object> toClass,
            Map hints) throws SerializationException
    {
        if (from == null)
            return "null";
        else if (JesterUtils.isArrayType(from))
        {
            return convert(JesterUtils.objectToCollection(from), String.class,
                    hints);
        }
        // default to String
        return convert(from.toString(), String.class, hints);
    }

    public String convert(Object from, Class<? extends String> toClass,
            Map hints) throws SerializationException
    {
        String jsonp = null;
        if (hints != null && hints.containsKey(HINT_JSONP))
        {
            jsonp = (String) hints.get(HINT_JSONP);
            hints.remove(HINT_JSONP); // remove it so it doesn't get done twice
        }

        String converted = (String) super.convert(from, toClass, hints);
        if (jsonp != null && converted instanceof String)
            return String.format("%s(%s);", jsonp, ((String) converted));
        return converted;
    }

}
