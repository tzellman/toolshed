package jester;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.exception.ExceptionUtils;

public class TransformerTest extends TestCase
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
     * Dumb Transformer that transforms an Object instance to a String, in a
     * really rock dumb fashion.
     */
    class DumbTransformer extends POJOTransformer<String>
    {
        public DumbTransformer(String... expressions)
        {
            super(expressions);
        }

        @Override
        protected String transformFromMap(Map data, IJester jester, Map hints)
                throws Exception
        {
            // this is really rock dumb, but it's an example
            StringBuffer b = new StringBuffer("{");
            for (Object key : data.keySet())
            {
                b.append(key.toString() + ":" + data.get(key) + ",");
            }
            b.append("}");
            return b.toString();
        }
    }

    /**
     * This class just uses a String transformer
     */
    class DumbJester implements IJester
    {
        private DumbTransformer transformer;

        public DumbJester()
        {
            // we want to transform the following fields/methods
            // note that if we try to transform an objec that doesn't have
            // these fields/methods, they will be set to null
            // we could add a flag that prunes null fields... a thought
            transformer = new DumbTransformer("name", "email", "required",
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
            // just write the string value
            String stringVal = transformer.to(object, this, hints);
            out.write(stringVal.getBytes());
        }
    }

    public void testPOJOTransformer()
    {
        DumbJester jester = new DumbJester();

        try
        {
            // a bunch of these should be null, as you'll see
            assertEquals(
                    "{class.simpleName:String,email:null,name:null,toString:coconuts,required:null,}",
                    SerializationUtils.serializeToString("coconuts", jester));

            // now, let's try with an object that has all fields set
            MyCustomModelClass myModel = new MyCustomModelClass();
            myModel.email = "lets@getpumpedwithjava.com";
            myModel.id = 1; // this won't get serialized, since it's "private"
            myModel.name = "Gert P. Frohb";
            myModel.required = true;

            assertEquals(
                    "{class.simpleName:MyCustomModelClass,email:lets@getpumpedwithjava.com,name:Gert P. Frohb,toString:Gert P. Frohb,required:true,}",
                    SerializationUtils.serializeToString(myModel, jester));
        }
        catch (Exception e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }
}
