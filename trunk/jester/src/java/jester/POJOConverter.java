/* =============================================================================
 * This file is part of Jester
 * =============================================================================
 * (C) Copyright 2009, Tom Zellman, tzellman@gmail.com
 *
 * Jester is free software; you can redistribute it and/or
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
package jester;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.StringUtils;

/**
 * Simple Converter that takes in any POJO, along with some OGNL-like
 * expressions, and produces a String that contains the Object and it's desired
 * fields, serialized. The passed-in IJester must be able to serialize a Map.
 */
public class POJOConverter<F> implements IConverter<F, Map<String, Object>>
{
    public static final String DEFAULT_SPLITTER = ".";

    protected Map<String, String> expressions;

    protected String expressionSplitter = DEFAULT_SPLITTER;

    /**
     * 
     * @param expressions
     *            Array of expressions
     */
    public POJOConverter(String... expressions)
    {
        this(Arrays.asList(expressions));
    }

    public POJOConverter(List<String> expressions)
    {
        this.expressions = new TreeMap<String, String>();
        for (String e : expressions)
            this.expressions.put(e, e);
    }

    public String getExpressionSplitter()
    {
        return expressionSplitter;
    }

    public void setExpressionSplitter(String expressionSplitter)
    {
        this.expressionSplitter = expressionSplitter;
    }

    /**
     * 
     * @param expressions
     *            Map of (name, expression)
     */
    public POJOConverter(Map<String, String> expressions)
    {
        this.expressions = new TreeMap<String, String>();
        expressions.putAll(expressions);
    }

    public Map<String, Object> convert(F from, Map hints)
            throws SerializationException
    {
        Map<String, Object> data = new HashMap<String, Object>();
        for (String name : expressions.keySet())
        {
            String expression = expressions.get(name);
            String[] splitExpression = StringUtils.splitByWholeSeparator(
                    expression, expressionSplitter);
            data.put(name, OGNLConverter.evaluate(from, splitExpression));
        }
        return data;
    }

}
