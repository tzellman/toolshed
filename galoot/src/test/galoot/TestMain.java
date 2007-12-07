/* Create an AST, then invoke our interpreter. */

package galoot;

import galoot.interpret.Interpreter;
import galoot.lexer.Lexer;
import galoot.node.Start;
import galoot.parser.Parser;

import java.io.File;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.PushbackReader;

public class TestMain
{
    public static void main(String[] args)
    {
        if (args.length > 0)
        {
            try
            {
                Template template = new Template(new File(args[0]));
                
                //fill up a context with some sample data
                Context context = new Context();
                context.put("var", "a string");
                context.put("var2", 42);
                
                String output = template.render(context);
                System.out.print(output);
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
        else
        {
            System.err.println("usage: java Main inputFile");
            System.exit(1);
        }
    }
}