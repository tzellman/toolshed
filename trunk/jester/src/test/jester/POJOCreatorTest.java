package jester;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jester.json.JSONDeserializer;
import junit.framework.TestCase;

public class POJOCreatorTest extends TestCase
{
    public static class MyBean
    {
        private String goo;
        private String barf;

        public void setGoo(String val)
        {
            this.goo = val;
        }

        public String getGoo()
        {
            return goo;
        }

        public void set()
        {
            // Don't actually do anything
        }
    }

    @SuppressWarnings("unchecked")
    public void testConvert()
    {
        String json = "[{\"goo\":\"boo\"}, {\"goo\":\"foo\"}]";

        JSONDeserializer deserializer = new JSONDeserializer();

        Map hints = new HashMap();
        hints.put(JSONDeserializer.HINT_MAPTYPE, JSONDeserializer.HINT_MAPTYPE_DYNABEAN);
        Object object = deserializer.convert(json);
        List list = (List) object;

        assertEquals(2, list.size());
        
        POJOCreator<MyBean> creator = new POJOCreator<MyBean>(MyBean.class);

        Map<String, Object> map1 = (Map<String, Object>) list.get(0);
        MyBean mb1 = creator.convert(map1, hints);
        assertEquals("boo", mb1.getGoo());

        Map<String, Object> map2 = (Map<String, Object>) list.get(1);
        MyBean mb2 = creator.convert(map2, hints);
        assertEquals("foo", mb2.getGoo());
    }

}
