/* =============================================================================
 * This file is part of Jester
 * =============================================================================
 * (C) Copyright 2009, Tom Zellman, tzellman@gmail.com
 *
 * Jester is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package jester.json;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

import jester.IJester;
import jester.ITransformer;
import jester.JesterUtils;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

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
    public static class CollectionTransformer implements
            ITransformer<String, Collection>
    {
        protected IJester jester;

        public CollectionTransformer(IJester jester)
        {
            this.jester = jester;
        }

        public String to(Collection c, Map hints) throws Exception
        {
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

        public Collection from(String json, Map hints) throws Exception
        {
            throw new NotImplementedException();
        }
    }

    /**
     * Transforms to/from a String.
     */
    public static class StringTransformer implements
            ITransformer<String, String>
    {
        public String to(String string, Map hints) throws Exception
        {
            return JSONUtils.toJSONString(string.toString());
        }

        public String from(String json, Map hints) throws Exception
        {
            throw new NotImplementedException();
        }
    }

    /**
     * Transforms to/from a Number.
     */
    public static class NumberTransformer implements
            ITransformer<String, Number>
    {
        public String to(Number number, Map hints) throws Exception
        {
            return number.toString();
        }

        public Number from(String json, Map hints) throws Exception
        {
            return NumberUtils.createNumber(json);
        }
    }

    /**
     * Transforms to/from a Boolean.
     */
    public static class BooleanTransformer implements
            ITransformer<String, Boolean>
    {
        public String to(Boolean b, Map hints) throws Exception
        {
            return b.booleanValue() ? "true" : "false";
        }

        public Boolean from(String json, Map hints) throws Exception
        {
            if (StringUtils.equals(json, "true"))
                return true;
            else if (StringUtils.equals(json, "false"))
                return false;
            throw new SerializationException("Invalid boolean type");
        }
    }

    /**
     * Transforms to/from a Map.
     */
    public static class MapTransformer implements ITransformer<String, Map>
    {
        protected IJester jester;

        public MapTransformer(IJester jester)
        {
            this.jester = jester;
        }

        public String to(Map map, Map hints) throws Exception
        {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(bout);

            out.print("{");
            Object[] keys = map.keySet().toArray();
            for (int i = 0, size = keys.length; i < size; ++i)
            {
                Object key = keys[i];
                String keyString = JesterUtils.serializeToString(key, jester,
                        hints);
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

        public Map from(String json, Map hints) throws Exception
        {
            throw new NotImplementedException();
        }
    }

    // private
    private JSONUtils()
    {
    }

}
