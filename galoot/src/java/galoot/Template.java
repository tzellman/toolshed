/* =============================================================================
 * This file is part of Galoot
 * =============================================================================
 * (C) Copyright 2009, Tom Zellman, tzellman@gmail.com
 *
 * Galoot is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package galoot;

import galoot.interpret.Interpreter;
import galoot.lexer.Lexer;
import galoot.lexer.LexerException;
import galoot.node.Start;
import galoot.parser.Parser;
import galoot.parser.ParserException;
import galoot.types.Document;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import org.apache.commons.lang.exception.ExceptionUtils;

public class Template
{

    private Start templateAST;

    public Template(String text) throws IOException
    {
        this(new StringReader(text));
    }

    public Template(File file) throws IOException
    {

        final FileReader reader = new FileReader(file);
        initAST(reader);
        reader.close();
    }

    public Template(Reader reader) throws IOException
    {
        initAST(reader);
    }

    protected void initAST(Reader reader) throws IOException
    {
        try
        {
            // TODO decide if there is a better buffer size
            Lexer lexer = new Lexer(new PushbackReader(reader, 1024));
            Parser parser = new Parser(lexer);
            templateAST = parser.parse();
        }
        catch (ParserException e)
        {
            // in Java 1.6 we *could* just pass the exception...
            throw new IOException(ExceptionUtils.getStackTrace(e));
        }
        catch (LexerException e)
        {
            // in Java 1.6 we *could* just pass the exception...
            throw new IOException(ExceptionUtils.getStackTrace(e));
        }
    }

    public void render(ContextStack contextStack, Writer writer)
            throws IOException
    {
        writer.write(render(contextStack));
    }

    public String render(ContextStack contextStack) throws IOException
    {
        return renderDocument(contextStack).evaluateAsString();
    }

    public void render(Context context, Writer writer) throws IOException
    {
        render(new ContextStack(context), writer);
    }

    public String render(Context context) throws IOException
    {
        return renderDocument(context).evaluateAsString();
    }

    public Document renderDocument(Context context)
    {
        return renderDocument(new ContextStack(context));
    }

    public Document renderDocument(ContextStack contextStack)
    {
        Interpreter interp = new Interpreter(contextStack);
        templateAST.apply(interp);
        return interp.getDocument();
    }

}
