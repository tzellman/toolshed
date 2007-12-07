package galoot.interpret;

import galoot.Context;
import galoot.analysis.DepthFirstAdapter;
import galoot.node.AAndBooleanOp;
import galoot.node.ABooleanExpr;
import galoot.node.ACharEntity;
import galoot.node.AForBlock;
import galoot.node.AIfBlock;
import galoot.node.AIfequalBlock;
import galoot.node.ALoad;
import galoot.node.AOrBooleanOp;
import galoot.node.AStringArgument;
import galoot.node.AStringInclude;
import galoot.node.AVarExpression;
import galoot.node.AVariableEntity;
import galoot.node.AVariableInclude;
import galoot.node.PArgument;
import galoot.node.PEntity;
import galoot.node.PVarExpression;
import galoot.node.TMember;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Interpreter extends DepthFirstAdapter
{
    protected Writer writer;

    protected Context context;

    protected Stack<Object> variableStack;

    public Interpreter(Context context, Writer writer)
    {
        this.context = context;
        this.writer = writer;
        variableStack = new Stack<Object>();
    }

    protected void writeString(String s)
    {
        try
        {
            writer.write(s);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void outACharEntity(ACharEntity node)
    {
        writeString(node.getChar().getText());
    }

    @Override
    public void outAVarExpression(AVarExpression node)
    {
        Object object = context.get(node.getReferent().getText());
        LinkedList<TMember> members = node.getMembers();

        // turn the members into a String list
        List<String> stringMembers = new ArrayList<String>(members.size());
        for (TMember member : members)
            stringMembers.add(member.getText());

        // evaluate the object/methods
        object = Interpreter.evaluateObject(object, stringMembers);

        // TODO apply filters

        variableStack.push(object);
    }

    @Override
    public void outAVariableEntity(AVariableEntity node)
    {
        Object object = variableStack.pop();
        if (object != null)
            writeString(object.toString());
    }

    @Override
    public void outABooleanExpr(ABooleanExpr node)
    {
        // pop the variable (evaluated expression) off the stack
        Object object = variableStack.pop();
        boolean negate = node.getNot() != null;

        boolean boolObj = evaluateAsBoolean(object);
        boolObj = negate ? !boolObj : boolObj;
        variableStack.push(boolObj);
    }

    @Override
    public void outAAndBooleanOp(AAndBooleanOp node)
    {
        // pop the top two items off the variable stack
        boolean result = evaluateAsBoolean(variableStack.pop())
                && evaluateAsBoolean(variableStack.pop());
        variableStack.push(result);
    }

    @Override
    public void outAOrBooleanOp(AOrBooleanOp node)
    {
        // pop the top two items off the variable stack
        boolean result = evaluateAsBoolean(variableStack.pop())
                || evaluateAsBoolean(variableStack.pop());
        variableStack.push(result);
    }

    @Override
    public void outAStringArgument(AStringArgument node)
    {
        String text = node.getString().getText();
        // remove the end-quotes
        text = text.substring(1, text.length() - 1);

        // just push the string argument onto the stack
        variableStack.push(text);
    }

    @Override
    public void outALoad(ALoad node)
    {
        LinkedList<PVarExpression> plugins = node.getPlugins();
        for (PVarExpression var : plugins)
        {
            // System.out.println("Asked to load plug-in: " + var.toString());
        }
    }

    @Override
    public void outAStringInclude(AStringInclude node)
    {
        // System.out.println("Asked to include file: "
        // + node.getString().getText());
    }

    @Override
    public void outAVariableInclude(AVariableInclude node)
    {
        // System.out.println("Asked to include a file from var: "
        // + node.getVariable());
    }

    @Override
    public void caseAForBlock(AForBlock node)
    {
        // TODO
        // figure out how many times we will have to iterate
        // for each iteration, push, then pop a new context onto the stack
        // the context will contain some extra variables, including the
        // loop variable, and some other "special" counter variables

        inAForBlock(node);
        if (node.getIterVar() != null)
        {
            node.getIterVar().apply(this);
        }
        if (node.getVariable() != null)
        {
            node.getVariable().apply(this);
        }

        {
            List<PEntity> copy = new ArrayList<PEntity>(node.getEntities());
            for (PEntity e : copy)
            {
                e.apply(this);
            }
        }
        outAForBlock(node);
    }

    @Override
    public void caseAIfBlock(AIfBlock node)
    {
        // if true, execute the entities
        // if false, and an else block exists, execute the else entities

        inAIfBlock(node);
        if (node.getExpr1() != null)
        {
            node.getExpr1().apply(this);
        }
        if (node.getExpr2() != null)
        {
            node.getExpr2().apply(this);
        }

        // at this point, the boolean expression result is on the var. stack
        boolean evaluateIf = evaluateAsBoolean(variableStack.pop());

        if (evaluateIf)
        {
            List<PEntity> copy = new ArrayList<PEntity>(node.getIf());
            for (PEntity e : copy)
            {
                e.apply(this);
            }
        }
        else
        {
            List<PEntity> copy = new ArrayList<PEntity>(node.getElse());
            for (PEntity e : copy)
            {
                e.apply(this);
            }
        }
        outAIfBlock(node);
    }

    @Override
    public void caseAIfequalBlock(AIfequalBlock node)
    {
        inAIfequalBlock(node);
        {
            List<PArgument> copy = new ArrayList<PArgument>(node.getArguments());
            for (PArgument e : copy)
            {
                e.apply(this);
            }
        }

        // get the objects from the stack and check to see if they are equal
        boolean equals = true;
        Object object = variableStack.pop();
        int numArgs = node.getArguments().size();
        for (int i = 1; i < numArgs && equals && object != null; i++)
        {
            Object nextObj = variableStack.pop();
            equals &= (nextObj != null && object.equals(nextObj));
        }

        if (equals)
        {
            List<PEntity> copy = new ArrayList<PEntity>(node.getIfequal());
            for (PEntity e : copy)
            {
                e.apply(this);
            }
        }
        else
        {
            List<PEntity> copy = new ArrayList<PEntity>(node.getElse());
            for (PEntity e : copy)
            {
                e.apply(this);
            }
        }
        outAIfequalBlock(node);
    }

    /**
     * This function evaluates a given object and attempts to retrieve a nested
     * sub-object
     * 
     * @param object
     * @param members
     * @return
     */
    protected static Object evaluateObject(Object object,
            Iterable<String> members)
    {
        for (Iterator<String> it = members.iterator(); object != null
                && it.hasNext();)
        {
            String memberName = (String) it.next();

            boolean found = false; // used to flag if we found it

            // first, check to see if it has parameters/methods with this name
            try
            {
                Field field = object.getClass().getField(memberName);
                object = field.get(object);
                found = true;
            }
            catch (Exception e)
            {
                // ok, let's see if it has a method with that name
                List<String> names = new ArrayList<String>();
                names.add(memberName);

                // compute the getter name and add it as a possibility
                names.add("get" + memberName.substring(0, 1).toUpperCase()
                        + memberName.substring(1));

                for (String name : names)
                {
                    try
                    {
                        Method method = object.getClass().getMethod(memberName,
                                null);
                        if (method.getParameterTypes().length != 0)
                            throw new InvalidParameterException();
                        object = method.invoke(object, null);
                        found = true;
                        break;
                    }
                    catch (Exception e1)
                    {
                    }
                }

            }

            // if we found it already, continue on
            if (found)
                continue;

            // see if it is a list
            if (object instanceof List)
            {
                List listObj = (List) object;
                // try to convert the name to an index
                try
                {
                    int index = Integer.parseInt(memberName);
                    if (index < 0 || index >= listObj.size())
                        throw new IndexOutOfBoundsException(memberName);
                    object = listObj.get(index);
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            // try it as a collection, maybe -- more expensive, possibly
            else if (object instanceof Collection)
            {
                Collection collObj = (Collection) object;
                // try to convert the name to an index
                try
                {
                    int index = Integer.parseInt(memberName);
                    int offset = 0;

                    for (Iterator iterator = collObj.iterator(); iterator
                            .hasNext()
                            && !found;)
                    {
                        if (index == offset)
                        {
                            object = iterator.next();
                            found = true;
                        }
                        else
                            iterator.next();
                        offset++;
                    }
                    if (!found)
                        throw new IndexOutOfBoundsException(memberName);
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            // it could be a member of a Map
            else if (object instanceof Map)
            {
                Map mapObj = (Map) object;
                if (mapObj.containsKey(memberName))
                    object = mapObj.get(memberName);
                else
                    object = null;
            }
        }
        return object;
    }

    /**
     * Evaluates the given object, returning its "boolean-ness"
     * 
     * @param object
     * @return
     */
    protected static boolean evaluateAsBoolean(Object object)
    {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (Boolean) object;
        if (object instanceof Number)
            return !Double.valueOf(((Number) object).doubleValue()).equals(
                    new Double(0.0));
        if (object instanceof String)
            return !((String) object).isEmpty();
        if (object instanceof List)
            return ((List) object).size() != 0;
        if (object instanceof Map)
            return ((Map) object).size() != 0;
        return true;
    }
}
