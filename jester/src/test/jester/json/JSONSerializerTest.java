package jester.json;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jester.IBean;

import junit.framework.TestCase;

public class JSONSerializerTest extends TestCase
{
    public class MyFakeBeanClass implements IBean
    {
        private int foo;
        private String bar;

        public MyFakeBeanClass()
        {
            foo = 0;
            bar = "bell";
        }

        public void setFoo(int foo)
        {
            this.foo = foo;
        }

        public int getFoo()
        {
            return foo;
        }

        public void setBar(String bar)
        {
            this.bar = bar;
        }

        public String getBar()
        {
            return bar;
        }
    }
    
    /**
     * Test converting bean classes into JSON with type information intact.
     */
    public void testConvertIBean()
    {
        List<MyFakeBeanClass> beans = new LinkedList<MyFakeBeanClass>();
        beans.add(new MyFakeBeanClass());
        beans.add(new MyFakeBeanClass());

        Map<String, List<MyFakeBeanClass>> theMap = new HashMap<String, List<MyFakeBeanClass>>();
        theMap.put("beans", beans);

        JSONSerializer serializer = new JSONSerializer();
        String converted = serializer.convert(theMap);

        assertEquals("{\"beans\":" + "["
                + "{\"class\":\"class jester.json.JSONSerializerTest$MyFakeBeanClass\",\"foo\":0,\"bar\":\"bell\"},"
                + "{\"class\":\"class jester.json.JSONSerializerTest$MyFakeBeanClass\",\"foo\":0,\"bar\":\"bell\"}"
                + "]" + "}", converted);
    }

}
