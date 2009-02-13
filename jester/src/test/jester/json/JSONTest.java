package jester.json;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jester.IJester;
import jester.JesterUtils;
import jester.NullTransformer;
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
            IJester serializer = new JSONJester();

            // ingest a map and spit out JSON
            Map data = new HashMap();
            data.put("drink", "Diet Mountain Dew");

            assertEquals("{\"drink\":\"Diet Mountain Dew\"}", JesterUtils
                    .serializeToString(data, serializer));

            // serialize a String
            assertEquals("\"scream aim fire\"", JesterUtils.serializeToString(
                    "scream aim fire", serializer));

            // serialize a Number
            assertEquals("42", JesterUtils.serializeToString(42, serializer));

            // serialize an array
            Object[] objects = new Object[] { "skateboard", "snowboard",
                    "hack", 300 };
            assertEquals("[\"skateboard\",\"snowboard\",\"hack\",300]",
                    JesterUtils.serializeToString(objects, serializer));

            // serialize a List/Collection
            List<Object> objectList = Arrays.asList(objects);
            assertEquals("[\"skateboard\",\"snowboard\",\"hack\",300]",
                    JesterUtils.serializeToString(objectList, serializer));
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
            JSONJester serializer = new JSONJester();

            // register an anonymous transformer for BigDecimal objects
            serializer.registerTransformer(BigDecimal.class,
                    new NullTransformer()
                    {
                        @Override
                        public String to(Object object, Map hints)
                                throws Exception
                        {
                            BigDecimal d = (BigDecimal) object;
                            return JSONUtils.toJSONString("BigDecimal: "
                                    + String.valueOf(d.doubleValue()));
                        }
                    });

            assertEquals("\"BigDecimal: 42.15\"", JesterUtils
                    .serializeToString(new BigDecimal(42.15), serializer));
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
            // Override a JSONJester inline, providing the defaultOut method
            JSONJester serializer = new JSONJester()
            {
                @Override
                protected String defaultOut(Object object, Map hints)
                        throws Exception
                {
                    return JSONUtils.toJSONString("Default: "
                            + object.toString());
                }
            };

            assertEquals("\"Default: java.lang.Exception: test\"", JesterUtils
                    .serializeToString(new Exception("test"), serializer));
        }
        catch (Exception e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testDeserialize()
    {
        IJester serializer = new JSONJester();

        try
        {
            String json = "12";
            Number n = (Number) serializer
                    .in(IOUtils.toInputStream(json), null);
            assertEquals(12, n.intValue());

            json = "12.42";
            n = (Number) serializer.in(IOUtils.toInputStream(json), null);
            assertEquals(12.42f, n.floatValue());

            assertNull(serializer.in(IOUtils.toInputStream("null"), null));

        }
        catch (Exception e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

}
