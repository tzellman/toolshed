package jester.json;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jester.IJester;
import jester.NullTransformer;
import jester.SerializationUtils;
import junit.framework.TestCase;

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

            assertEquals("{\"drink\":\"Diet Mountain Dew\"}",
                    SerializationUtils.serializeToString(data, serializer));

            // serialize a String
            assertEquals("\"scream aim fire\"", SerializationUtils
                    .serializeToString("scream aim fire", serializer));

            // serialize a Number
            assertEquals("42", SerializationUtils.serializeToString(42,
                    serializer));

            // serialize an array
            Object[] objects = new Object[] { "skateboard", "snowboard",
                    "hack", 300 };
            assertEquals("[\"skateboard\",\"snowboard\",\"hack\",300]",
                    SerializationUtils.serializeToString(objects, serializer));

            // serialize a List/Collection
            List<Object> objectList = Arrays.asList(objects);
            assertEquals("[\"skateboard\",\"snowboard\",\"hack\",300]",
                    SerializationUtils
                            .serializeToString(objectList, serializer));
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
                    new NullTransformer<String>()
                    {
                        @Override
                        public String to(Object object, IJester jester,
                                Map hints) throws Exception
                        {
                            BigDecimal d = (BigDecimal) object;
                            return JSONUtils.toJSONString("BigDecimal: "
                                    + String.valueOf(d.doubleValue()));
                        }
                    });

            assertEquals("\"BigDecimal: 42.15\"", SerializationUtils
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

            assertEquals("\"Default: java.lang.Exception: test\"",
                    SerializationUtils.serializeToString(new Exception("test"),
                            serializer));
        }
        catch (Exception e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

}
