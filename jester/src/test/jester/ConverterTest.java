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
package jester;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import jester.utils.JesterUtils;
import junit.framework.TestCase;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.exception.ExceptionUtils;

public class ConverterTest extends TestCase
{

    /**
     * Test class to show how we can transform it
     */
    class MyCustomModelClass
    {
        public String name;

        protected int id;

        public boolean required;

        public String email;

        @Override
        public String toString()
        {
            return name;
        }
    }

    /**
     * This class just uses a POJOTransformer to transform data to a Map, and
     * then to a String. This is by no means a correct implementation, so don't
     * go about using it.
     */
    class DumbJSONJester implements IJester
    {
        // we are going to always use this transformer
        private POJOConverter converter;

        public DumbJSONJester()
        {
            // we want to transform the following fields/methods
            // note that if we try to transform an object that doesn't have
            // these fields/methods, they will be set to null
            // we could add a flag that prunes null fields... a thought
            converter = new POJOConverter("name", "email", "required",
                    "toString", "class.simpleName");
        }

        public String getContentType()
        {
            return "dumbText";
        }

        public Object in(InputStream stream, Map hints) throws Exception
        {
            throw new NotImplementedException();
        }

        public void out(Object object, OutputStream out, Map hints)
                throws Exception
        {
            if (object instanceof Map)
            {
                out.write(mapToString((Map) object).getBytes());
            }
            else
            {
                Map map = converter.convert(object, hints);
                out(map, out, hints);
            }
        }

        private String mapToString(Map data)
        {
            // this is really rock dumb, but it's an example
            StringBuffer b = new StringBuffer("{");
            Object[] keys = data.keySet().toArray();
            for (int i = 0, size = keys.length; i < size; ++i)
            {
                Object key = keys[i];
                b
                        .append("\"" + key.toString() + "\":\"" + data.get(key)
                                + "\"");
                if (i < size - 1)
                    b.append(",");
            }
            b.append("}");
            return b.toString();
        }
    }

    public void testPOJOConverter()
    {
        DumbJSONJester jester = new DumbJSONJester();

        try
        {
            System.out.println(JesterUtils
                    .serializeToString("coconuts", jester));

            // a bunch of these should be null, as you'll see
            assertEquals(
                    "{\"class.simpleName\":\"String\",\"email\":\"null\",\"name\":\"null\",\"toString\":\"coconuts\",\"required\":\"null\"}",
                    JesterUtils.serializeToString("coconuts", jester));

            // now, let's try with an object that has all fields set
            MyCustomModelClass myModel = new MyCustomModelClass();
            myModel.email = "lets@getpumpedwithjava.com";
            myModel.id = 1; // this won't get serialized, since it's "private"
            myModel.name = "Gert P. Frohb";
            myModel.required = true;

            assertEquals(
                    "{\"class.simpleName\":\"MyCustomModelClass\",\"email\":\"lets@getpumpedwithjava.com\",\"name\":\"Gert P. Frohb\",\"toString\":\"Gert P. Frohb\",\"required\":\"true\"}",
                    JesterUtils.serializeToString(myModel, jester));

        }
        catch (Exception e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }
}
