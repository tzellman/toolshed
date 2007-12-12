package galoot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import junit.framework.TestCase;

public class FilterTest extends TestCase
{

    protected FilterMap filters;

    protected void setUp() throws Exception
    {
        super.setUp();
        filters = new FilterMap();

        filters.addFilter(new Filter()
        {
            public Object filter(Object object, String args)
            {
                if (object instanceof Number)
                    return true;
                else if (object instanceof String)
                {
                    String s = (String) object;
                    // try parsing it as a number
                    try
                    {
                        Long.parseLong(s);
                        return true;
                    }
                    catch (NumberFormatException e)
                    {
                    }
                    try
                    {
                        Double.parseDouble(s);
                        return true;
                    }
                    catch (NumberFormatException e)
                    {
                    }
                }
                return false;
            }

            public String getName()
            {
                return "is_num";
            }
        });
    }

    public void testSimple()
    {
        assertTrue(filters.hasFilter("is_num"));
        assertFalse((Boolean) filters.getFilter("is_num").filter("not a num",
                null));
        assertTrue((Boolean) filters.getFilter("is_num").filter("1", null));
        assertTrue((Boolean) filters.getFilter("is_num").filter(1, null));
        assertTrue((Boolean) filters.getFilter("is_num").filter(Math.PI, null));
        assertTrue((Boolean) filters.getFilter("is_num").filter(
                String.valueOf(Math.PI), null));
    }

    public void testWordCount()
    {
        Object result = PluginRegistry.getInstance().getFilter("wordcount")
                .filter("There are 4    words.", null);
        assertEquals(4, ((Integer) result).intValue());
    }

    public void testTitle()
    {
        Object result = PluginRegistry.getInstance().getFilter("title").filter(
                "convert to title case.", null);
        assertEquals("Convert To Title Case.", result);
    }
}
