package jester;

import java.util.Map;

/**
 * An ObjectEvaluator evaluates an Object and returns another Object. This
 * satisfies the ITransformer interface, with the caveat that both the to and
 * from methods have the same behavior (i.e. they are not inverses of each
 * other).
 * 
 * Implementing classes need to just provide the evaluate method.
 * 
 * @param <E>
 *            the expression type (e.g. String)
 */
public abstract class ObjectEvaluator<E> implements
        ITransformer<Object, Object>
{
    protected E expression;

    public ObjectEvaluator(E expression)
    {
        this.expression = expression;
    }

    public Object from(Object object, Map hints) throws Exception
    {
        return evaluate(object, hints);
    }

    public Object to(Object object, Map hints) throws Exception
    {
        return evaluate(object, hints);
    }

    /**
     * This function evaluates a given object and attempts to return some sort
     * of derivative Object, based on the given expression.
     * 
     * @param object
     * @param expression
     * @return
     */
    public abstract Object evaluate(Object object, Map hints);

    public Object evaluate(Object object)
    {
        return evaluate(object, null);
    }

}
