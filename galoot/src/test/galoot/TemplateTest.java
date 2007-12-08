package galoot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.commons.lang.exception.ExceptionUtils;

public class TemplateTest extends TestCase
{
    protected Context context;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        context = new Context();

        // add some variables to the context

        int[] intArray = { 7, 13, 15, 26, 42 };
        double[] doubleArray = { 0.0, 3.1415, Math.PI, Math.E };

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
                buf.append((char) charId);
            }
            randomStrings.add(buf.toString());
        }

        context.put("name", "Tom");
        context.put("randomStrings", randomStrings);
        context.put("intArray", intArray);
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

            t = new Template("{% for var in intArray %}{{ var }},{% endfor %}");
            output = t.render(context);
            System.out.println(output);
            
            //this time, don't print an ending comma
            t = new Template("{% for var in intArray %}{{ var }}{% if not forloop.last %},{% endif %}{% endfor %}");
            output = t.render(context);
            System.out.println(output);

            t = new Template("{{ randomStrings }}");
            output = t.render(context);
            System.out.println(output);

            t = new Template(
                    "{% for var in doubleArray %}{{ var }},{% endfor %}");
            output = t.render(context);
            System.out.println(output);

            t = new Template(
                    "{% for var in doubleList %}{{ var }},{% endfor %}");
            output = t.render(context);
            System.out.println(output);

        }
        catch (IOException e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }

    }

}
