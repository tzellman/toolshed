package galoot;

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
}
