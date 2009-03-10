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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jester.IConverter;
import jester.IJester;
import jester.utils.JesterUtils;
import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

public class JSONTest extends TestCase
{

    /**
     * Tests some of the default transformers
     */
    public void testDefaults()
    {
        try
        {
            IJester jester = new JSONJester();

            // ingest a map and spit out JSON
            Map data = new HashMap();
            data.put("drink", "Diet Mountain Dew");

            assertEquals("{\"drink\":\"Diet Mountain Dew\"}", JesterUtils
                    .serializeToString(data, jester));

            // serialize a String
            assertEquals("\"scream aim fire\"", JesterUtils.serializeToString(
                    "scream aim fire", jester));

            // serialize a Number
            assertEquals("42", JesterUtils.serializeToString(42, jester));

            // serialize an array
            Object[] objects = new Object[] { "skateboard", "snowboard",
                    "hack", 300 };
            assertEquals("[\"skateboard\",\"snowboard\",\"hack\",300]",
                    JesterUtils.serializeToString(objects, jester));

            // serialize a List/Collection
            List<Object> objectList = Arrays.asList(objects);
            assertEquals("[\"skateboard\",\"snowboard\",\"hack\",300]",
                    JesterUtils.serializeToString(objectList, jester));
        }
        catch (Exception e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Tests registering a custom transformer
     */
    public void testRegisterCustom()
    {
        try
        {
            JSONJester jester = new JSONJester();

            // register an anonymous transformer for BigDecimal objects
            jester.getSerializer().register(
                    new IConverter<BigDecimal, String>()
                    {
                        public String convert(BigDecimal from, Map hints)
                        {
                            return JSONSerializer.toJSONString("BigDecimal: "
                                    + from.floatValue());
                        }
                    });

            assertEquals("\"BigDecimal: 42.15\"", JesterUtils
                    .serializeToString(new BigDecimal(42.15), jester));
        }
        catch (Exception e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Tests overriding the defaultOut method.
     */
    public void testOverrideDefaultOut()
    {
        try
        {
            JSONSerializer serializer = new JSONSerializer()
            {
                public String defaultConvert(Object from,
                        Class<? extends Object> toClass, Map hints)
                {
                    return JSONSerializer.toJSONString("Default: "
                            + from.toString());
                }
            };

            // Override a JSONJester inline, providing the defaultOut method
            JSONJester jester = new JSONJester(serializer, null);

            assertEquals("\"Default: java.lang.Exception: test\"", JesterUtils
                    .serializeToString(new Exception("test"), jester));
        }
        catch (Exception e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testSerialize()
    {
        JSONSerializer serializer = new JSONSerializer();
        assertEquals("300", serializer.convert(300));
        assertEquals("\"string\"", serializer.convert("string"));

        System.out.println(serializer.convert("string"));
        System.out.println(serializer.convert(300));
        System.out.println(serializer.convert(3.14159));

        Object[] objects = new Object[] { "skateboard", "snowboard", "hack",
                300 };

        assertEquals("[\"skateboard\",\"snowboard\",\"hack\",300]", serializer
                .convert(objects));
    }

    public void testDeserialize()
    {
        IJester jester = new JSONJester();

        // TODO
        try
        {
            String json = "12";
            Number n = (Number) jester.in(IOUtils.toInputStream(json), null);
            assertEquals(12, n.intValue());

            json = "12.42";
            n = (Number) jester.in(IOUtils.toInputStream(json), null);
            assertEquals(12.42f, n.floatValue());

            assertNull(jester.in(IOUtils.toInputStream("null"), null));
            assertTrue((Boolean) jester.in(IOUtils.toInputStream("true"), null));
            assertFalse((Boolean) jester.in(IOUtils.toInputStream("false"),
                    null));

            assertEquals(jester.in(
                    IOUtils.toInputStream("[[],{},[{},[{},{}]]]"), null)
                    .toString(), "[[], {}, [{}, [{}, {}]]]");

            assertEquals(
                    jester
                            .in(
                                    IOUtils
                                            .toInputStream("[\"string\", 15, false, {\"key\": \"value\"}, []]"),
                                    null).toString(),
                    "[string, 15, false, {\"key\"=value}, []]");

            try
            {
                jester.in(IOUtils.toInputStream("[], []"), null);
                fail("Should've thrown");
            }
            catch (Exception e)
            {
            }
        }
        catch (Exception e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }
}
