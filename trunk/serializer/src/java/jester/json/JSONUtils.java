package jester.json;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

import jester.IJester;
import jester.SerializationUtils;
import jester.Transformer;

import org.apache.commons.lang.NotImplementedException;

/**
 * JSON Utilities
 */
public final class JSONUtils
{

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
     * Transforms to/from a Collection.
     */
    public static class CollectionTransformer implements Transformer<String>
    {
        public String to(Object object, IJester jester) throws Exception
        {
            Collection c = (Collection) object;
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(bout);

            out.print("[");
            Object[] vals = c.toArray();
            for (int i = 0, size = vals.length; i < size; ++i)
            {
                Object val = vals[i];
                jester.out(val, out);
                if (i < size - 1)
                    out.print(",");
            }
            out.print("]");
            return bout.toString();
        }

        public Object from(String json, IJester jester) throws Exception
        {
            throw new NotImplementedException();
        }
    }

    /**
     * Transforms to/from a String.
     */
    public static class StringTransformer implements Transformer<String>
    {
        public String to(Object object, IJester jester) throws Exception
        {
            return JSONUtils.toJSONString(object.toString());
        }

        public Object from(String json, IJester jester) throws Exception
        {
            throw new NotImplementedException();
        }
    }

    /**
     * Transforms to/from a Number.
     */
    public static class NumberTransformer implements Transformer<String>
    {
        public String to(Object object, IJester jester) throws Exception
        {
            return ((Number) object).toString();
        }

        public Object from(String json, IJester jester) throws Exception
        {
            throw new NotImplementedException();
        }
    }

    /**
     * Transforms to/from a Boolean.
     */
    public static class BooleanTransformer implements Transformer<String>
    {
        public String to(Object object, IJester jester) throws Exception
        {
            return ((Boolean) object).booleanValue() ? "true" : "false";
        }

        public Object from(String json, IJester jester) throws Exception
        {
            throw new NotImplementedException();
        }
    }

    /**
     * Transforms to/from a Map.
     */
    public static class MapTransformer implements Transformer<String>
    {
        public String to(Object object, IJester jester) throws Exception
        {
            Map map = (Map) object;
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(bout);

            out.print("{");
            Object[] keys = map.keySet().toArray();
            for (int i = 0, size = keys.length; i < size; ++i)
            {
                Object key = keys[i];
                String keyString = SerializationUtils.serializeToString(key,
                        jester);
                if (!keyString.startsWith("\"") && !keyString.endsWith("\""))
                    keyString = toJSONString(keyString);

                out.print(keyString);
                out.print(":");
                jester.out(map.get(key), out);
                if (i < size - 1)
                    out.print(",");
            }
            out.print("}");
            return bout.toString();
        }

        public Object from(String json, IJester jester) throws Exception
        {
            throw new NotImplementedException();
        }
    }

    // private
    private JSONUtils()
    {
    }

}
