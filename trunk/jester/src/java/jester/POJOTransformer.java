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

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

/**
 * Simple Transformer that takes in any POJO, along with some OGNL-like
 * expressions, and produces a String that contains the Object and it's desired
 * fields, somehow serialized. The passed-in IJester must be able to serialize a
 * Map.
 */
public class POJOTransformer<F> implements ITransformer<Map<String, Object>, F>
{
    public static final String DEFAULT_SPLITTER = ".";

    protected Map<String, String> expressions;

    protected String expressionSplitter = DEFAULT_SPLITTER;

    /**
     * 
     * @param expressions
     *            Array of expressions
     */
    public POJOTransformer(String... expressions)
    {
        this(Arrays.asList(expressions));
    }

    public POJOTransformer(List<String> expressions)
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
    public POJOTransformer(IJester jester, Map<String, String> expressions)
    {
        this.expressions = new TreeMap<String, String>();
        expressions.putAll(expressions);
    }

    /**
     * @throws NotImplementedException
     */
    public F from(Map<String, Object> data, Map hints) throws Exception
    {
        // TODO
        throw new NotImplementedException();
    }

    public Map<String, Object> to(F object, Map hints) throws Exception
    {
        Map<String, Object> data = new HashMap<String, Object>();
        for (String name : expressions.keySet())
        {
            String expression = expressions.get(name);
            String[] splitExpression = StringUtils.splitByWholeSeparator(
                    expression, expressionSplitter);
            data.put(name, OGNLEvaluator.evaluate(object, splitExpression));
        }
        return data;
    }
}
