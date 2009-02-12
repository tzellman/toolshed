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
 * fields, somehow serialized. The passed-in IJester must be able to serialize a
 * Map.
 */
public class POJOTransformer implements ITransformer<String>
{
    // protected List<String> expressions;
    protected Map<String, String> expressions;

    protected IJester jester;

    /**
     * 
     * @param expressions
     *            Array of expressions
     */
    public POJOTransformer(IJester jester, String... expressions)
    {
        this(jester, Arrays.asList(expressions));
    }

    public POJOTransformer(IJester jester, List<String> expressions)
    {
        this.jester = jester;
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

    public Object from(String data, Map hints) throws Exception
    {
        // TODO
        throw new NotImplementedException();
    }

    public String to(Object object, Map hints) throws Exception
    {
        Map data = new HashMap();
        for (String name : expressions.keySet())
        {
            String expression = expressions.get(name);
            data.put(name, JesterUtils.evaluateObject(object, expression));
        }

        return JesterUtils.serializeToString(data, jester, hints);
    }
}
