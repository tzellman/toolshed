/* Create an AST, then invoke our interpreter. */

package galoot;

import galoot.interpret.Interpreter;
import galoot.lexer.Lexer;
import galoot.node.Start;
import galoot.parser.Parser;

import java.io.FileReader;
import java.io.PushbackReader;

public class TestMain
{
    public static void main(String[] args)
    {
        if (args.length > 0)
        {
            try
            {
                Lexer lexer = new Lexer(new PushbackReader(new FileReader(
                        args[0]), 1024));
                Parser parser = new Parser(lexer);
                Start ast = parser.parse();

                Interpreter interp = new Interpreter();

                ast.apply(interp);
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