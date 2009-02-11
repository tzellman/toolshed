package jester;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.NotImplementedException;

/**
 * Simple Transformer that takes in any POJO, along with some OGNL-like
 * expressions, and produces a String that contains the Object and it's desired
 * fields, somehow serialized.
 */
public abstract class POJOTransformer implements Transformer<String>
{
    // protected List<String> expressions;
    protected Map<String, String> expressions;

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

    /**
     * 
     * @param expressions
     *            Map of (name, expression)
     */
    public POJOTransformer(Map<String, String> expressions)
    {
        this.expressions = new TreeMap<String, String>();
        expressions.putAll(expressions);
    }

    public Object from(String data, IJester jester, Map hints) throws Exception
    {
        // TODO
        throw new NotImplementedException();
    }

    public String to(Object object, IJester jester, Map hints) throws Exception
    {
        Map data = new HashMap();
        for (String name : expressions.keySet())
        {
            String expression = expressions.get(name);
            data.put(name, TransformUtils.evaluateObject(object, expression));
        }
        return transformFromMap(data, jester, hints);
    }

    /**
     * Transforms Map to the desired type
     * 
     * @param data
     * @return
     */
    protected abstract String transformFromMap(Map data, IJester jester,
            Map hints) throws Exception;

}
