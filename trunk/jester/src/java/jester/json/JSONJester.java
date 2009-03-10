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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import jester.IJester;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SerializationException;

/**
 * JSON Serializer/Deserializer. Use an instance of a JSONJester if you want to
 * serialize Objects to/from JSON to/from a Stream.
 * 
 */
public class JSONJester implements IJester
{

    public static final String JSON_TYPE = "json";

    protected JSONSerializer serializer;

    protected JSONDeserializer deserializer;

    /**
     * NOTE: This will change to also take in a JSONDeserializer.
     * 
     * @param serializer
     */
    public JSONJester(JSONSerializer serializer, JSONDeserializer deserializer)
    {
        if (serializer == null)
            serializer = new JSONSerializer();
        if (deserializer == null)
            deserializer = new JSONDeserializer();
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    /**
     * Create a new JSONJester
     */
    public JSONJester()
    {
        this(null, null);
    }

    public String getContentType()
    {
        return JSON_TYPE;
    }

    public JSONSerializer getSerializer()
    {
        return serializer;
    }

    public void out(Object object, OutputStream out, Map hints)
            throws Exception
    {
        String converted = serializer.convert(object, String.class, hints);
        if (converted == null)
            throw new SerializationException("Unable to serialize object");
        out.write(converted.getBytes());
    }

    public Object in(InputStream stream, Map hints) throws Exception
    {
        String string = IOUtils.toString(stream);
        return deserializer.convert(string, hints);
    }
}
