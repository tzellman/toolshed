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
package jester.json;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import jester.ConverterRegistry;
import jester.json.parse.Interpreter;
import jester.json.parse.lexer.Lexer;
import jester.json.parse.lexer.LexerException;
import jester.json.parse.node.Start;
import jester.json.parse.parser.Parser;
import jester.json.parse.parser.ParserException;

import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.exception.ExceptionUtils;

public class JSONDeserializer extends ConverterRegistry<String, Object>
{

    public JSONDeserializer()
    {
    }

    protected Start getAST(Reader reader) throws IOException
    {
        try
        {
            // TODO decide if there is a better buffer size
            Lexer lexer = new Lexer(new PushbackReader(reader, 1024));
            Parser parser = new Parser(lexer);
            return parser.parse();
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

    @Override
    public Object defaultConvert(Object from, Class<? extends Object> toClass,
            Map hints) throws SerializationException
    {
        try
        {
            // we know the input is a string since it really can't be anything
            // else
            String input = (String) from;

            StringReader reader = new StringReader(input);
            Start ast = getAST(reader);
            Interpreter interpreter = new Interpreter();
            ast.apply(interpreter);
            return interpreter.getObject();
        }
        catch (IOException e)
        {
            throw new SerializationException(e);
        }
    }

}
