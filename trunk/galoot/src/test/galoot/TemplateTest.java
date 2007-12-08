package galoot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.commons.lang.exception.ExceptionUtils;

public class TemplateTest extends TestCase
{
    protected ContextStack context;

    int[] intArray = { 7, 13, 15, 26, 42 };

    double[] doubleArray = { 0.0, 3.1415, Math.PI, Math.E };

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        context = new ContextStack();

        // add some variables to the context
        List<String> randomStrings = new ArrayList<String>();
        Random random = new Random();
        for (int i = 0, num = random.nextInt(100); i < num; ++i)
        {
            // pick a random char between ascii 32 & 126
            int minChar = 32, maxChar = 126;
            int stringSize = random.nextInt(30);
            StringBuffer buf = new StringBuffer();
            for (int j = 0; j < stringSize; ++j)
            {
                int charId = minChar + random.nextInt(maxChar - minChar);
                buf.trueappend((char) charId);
            }
            randomStrings.add(buf.toString());
        }

        context.put("name", "Tom");
        context.put("randomStrings", randomStrings);
        context.put("intArray", intArray);
        context.put("intList", TemplateUtils.objectToCollection(intArray));
        context.put("doubleArray", doubleArray);
        context
                .put("doubleList", TemplateUtils
                        .objectToCollection(doubleArray));
    }

    public void testVarExpression()
    {
        try
        {
            Template t = new Template("{{ name }}");
            String output = t.render(context);
            assertEquals(output, "Tom");

            t = new Template("{{ name.length }}");
            output = t.render(context);
            assertEquals(Integer.parseInt(output), "Tom".length());

            t = new Template("{{ intList.size }}");
            output = t.render(context);
            assertEquals(Integer.parseInt(output), intArray.length);

            // shouldn't render any output since the var doesn't exist
            t = new Template("{{ doesnt_exist }}");
            output = t.render(context);
            assertTrue(output.isEmpty());
        }
        catch (IOException e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testIfStatement()
    {
        try
        {
            Template t = new Template("{% if name %}true{% endif %}");
            String output = t.render(context);
            assertEquals(output, "true");

            t = new Template("{% if not name %}false{% endif %}");
            output = t.render(context);
            assertTrue(output.isEmpty());

            t = new Template("{% if bad_var %}false{% endif %}");
            output = t.render(context);
            assertTrue(output.isEmpty());

            t = new Template("{% if name %}true{% else %}false{% endif %}");
            output = t.render(context);
            assertEquals(output, "true");

            t = new Template("{% if not name %}true{% else %}false{% endif %}");
            output = t.render(context);
            assertEquals(output, "false");

            t = new Template("{% if bad_var %}false{% else %}else{% endif %}");
            output = t.render(context);
            assertEquals(output, "else");

            // nested ifs
            t = new Template(
                    "{% if name %}{% if intArray %}true{% endif %}{% endif %}");
            output = t.render(context);
            assertEquals(output, "true");

        }
        catch (IOException e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }
    
    public void testWithStatement()
    {
        // nested ifs
        try
        {
            Template t = new Template(
                    "{% with name|upper as bar %}{{ bar }}{% endwith %}");
            String output = t.render(context);
            assertEquals(output, "TOM");
        }
        catch (IOException e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testDefaultFilters()
    {
        try
        {
            Template t = new Template("{{ name|length }}");
            String output = t.render(context);
            assertEquals(Integer.parseInt(output), "Tom".length());

            t = new Template("{{ name|upper }}");
            output = t.render(context);
            assertEquals(output, "Tom".toUpperCase());

            t = new Template("{{ name|lower }}");
            output = t.render(context);
            assertEquals(output, "Tom".toLowerCase());

            t = new Template("{{ intArray|make_list|length }}");
            output = t.render(context);
            assertEquals(Integer.parseInt(output), intArray.length);

            t = new Template("{{ intArray|make_list }}");
            output = t.render(context);
            assertEquals(output, ((List<Integer>) context.get("intList"))
                    .toString());

            t = new Template("{{ intArray|length }}");
            output = t.render(context);
            assertEquals(Integer.parseInt(output), intArray.length);
            
            t = new Template("{{ name|make_list|length }}");
            output = t.render(context);
            assertEquals(Integer.parseInt(output), "Tom".length());

        }
        catch (IOException e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

}
