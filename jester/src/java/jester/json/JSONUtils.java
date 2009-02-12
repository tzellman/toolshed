package jester.json;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

import jester.IJester;
import jester.ITransformer;
import jester.JesterUtils;

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
    public static class CollectionTransformer implements ITransformer<String>
    {
        protected IJester jester;

        public CollectionTransformer(IJester jester)
        {
            this.jester = jester;
        }

        public String to(Object object, Map hints) throws Exception
        {
            Collection c = (Collection) object;
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(bout);

            out.print("[");
            Object[] vals = c.toArray();
            for (int i = 0, size = vals.length; i < size; ++i)
            {
                Object val = vals[i];
                jester.out(val, out, hints);
                if (i < size - 1)
                    out.print(",");
            }
            out.print("]");
            return bout.toString();
        }

        public Object from(String json, Map hints) throws Exception
        {
            throw new NotImplementedException();
        }
    }

    /**
     * Transforms to/from a String.
     */
    public static class StringTransformer implements ITransformer<String>
    {
        public String to(Object object, Map hints) throws Exception
        {
            return JSONUtils.toJSONString(object.toString());
        }

        public Object from(String json, Map hints) throws Exception
        {
            throw new NotImplementedException();
        }
    }

    /**
     * Transforms to/from a Number.
     */
    public static class NumberTransformer implements ITransformer<String>
    {
        public String to(Object object, Map hints) throws Exception
        {
            return ((Number) object).toString();
        }

        public Object from(String json, Map hints) throws Exception
        {
            throw new NotImplementedException();
        }
    }

    /**
     * Transforms to/from a Boolean.
     */
    public static class BooleanTransformer implements ITransformer<String>
    {
        public String to(Object object, Map hints) throws Exception
        {
            return ((Boolean) object).booleanValue() ? "true" : "false";
        }

        public Object from(String json, Map hints) throws Exception
        {
            throw new NotImplementedException();
        }
    }

    /**
     * Transforms to/from a Map.
     */
    public static class MapTransformer implements ITransformer<String>
    {
        protected IJester jester;

        public MapTransformer(IJester jester)
        {
            this.jester = jester;
        }

        public String to(Object object, Map hints) throws Exception
        {
            Map map = (Map) object;
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(bout);

            out.print("{");
            Object[] keys = map.keySet().toArray();
            for (int i = 0, size = keys.length; i < size; ++i)
            {
                Object key = keys[i];
                String keyString = JesterUtils.serializeToString(key,
                        jester, hints);
                if (!keyString.startsWith("\"") && !keyString.endsWith("\""))
                    keyString = toJSONString(keyString);

                out.print(keyString);
                out.print(":");
                jester.out(map.get(key), out, hints);
                if (i < size - 1)
                    out.print(",");
            }
            out.print("}");
            return bout.toString();
        }

        public Object from(String json, Map hints) throws Exception
        {
            throw new NotImplementedException();
        }
    }

    // private
    private JSONUtils()
    {
    }

}
