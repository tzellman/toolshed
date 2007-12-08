/* Create an AST, then invoke our interpreter. */

package galoot;

import java.io.File;

import org.apache.commons.lang.exception.ExceptionUtils;

public class Main
{
    public static void main(String[] args)
    {
        if (args.length > 0)
        {
            try
            {
                Template template = new Template(new File(args[0]));

                // fill up a context with some sample data
                Context context = new Context();
                context.put("var", "a string");
                context.put("var2", 42);

                String output = template.render(context);
                System.out.print(output);
            }
            catch (Throwable e)
            {
                System.out.println(ExceptionUtils.getStackTrace(e));
            }
        }
        else
        {
            System.err.println("usage: java Main inputFile");
            System.exit(1);
        }
    }
}