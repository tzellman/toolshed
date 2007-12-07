package galoot;

import galoot.interpret.Interpreter;
import galoot.lexer.Lexer;
import galoot.lexer.LexerException;
import galoot.node.Start;
import galoot.parser.Parser;
import galoot.parser.ParserException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

public class Template
{

    private Start templateAST;

    public Template(String text) throws IOException
    {
        this(new StringReader(text));
    }

    public Template(File file) throws IOException
    {
        this(new FileReader(file));
    }

    public Template(Reader reader) throws IOException
    {
        try
        {
            Lexer lexer = new Lexer(new PushbackReader(reader, 1024));
            Parser parser = new Parser(lexer);
            templateAST = parser.parse();
        }
        catch (ParserException e)
        {
            throw new IOException(e);
        }
        catch (LexerException e)
        {
            throw new IOException(e);
        }
    }

    public void render(Context context, Writer writer)
    {
        Interpreter interp = new Interpreter(context, writer);
        templateAST.apply(interp);
    }

    public String render(Context context)
    {
        StringWriter stringWriter = new StringWriter();
        render(context, stringWriter);
        return stringWriter.getBuffer().toString();
    }

}
