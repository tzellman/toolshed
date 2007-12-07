package galoot.interpret;

import galoot.Context;
import galoot.Filter;
import galoot.Pair;
import galoot.TemplateUtils;
import galoot.analysis.DepthFirstAdapter;
import galoot.node.AAndBooleanOp;
import galoot.node.ABooleanExpr;
import galoot.node.ACharEntity;
import galoot.node.AFilter;
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
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Interpreter extends DepthFirstAdapter
{
    protected Writer writer;

    protected Context context;

    protected Stack<Object> variableStack;

    protected Deque<Pair<String, String>> filterStack;

    public Interpreter(Context context, Writer writer)
    {
        this.context = context;
        this.writer = writer;
        variableStack = new Stack<Object>();
        filterStack = new LinkedList<Pair<String, String>>();
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
        object = TemplateUtils.evaluateObject(object, stringMembers);

        // TODO apply filters
        System.out.println("HERE: " + node.toString());

        Iterator<Pair<String, String>> iterator = filterStack
                .descendingIterator();
        while (iterator.hasNext() && object != null)
        {
            Pair<String, String> stringPair = (Pair<String, String>) iterator
                    .next();
            Filter filter = context.getFilterMap().getFilter(
                    stringPair.getFirst());
            if (filter == null)
                object = null;
            else
                object = filter.filter(object, stringPair.getSecond());
        }

        variableStack.push(object);
    }

    @Override
    public void outAFilter(AFilter node)
    {
//        filterStack.push(new Pair<String, String>(node.getFilter().getText(),
//                variableStack.pop().toString()));
//
//        System.out.println("Filter:" + node.getFilter().getText());
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

        boolean boolObj = TemplateUtils.evaluateAsBoolean(object);
        boolObj = negate ? !boolObj : boolObj;
        variableStack.push(boolObj);
    }

    @Override
    public void outAAndBooleanOp(AAndBooleanOp node)
    {
        // pop the top two items off the variable stack
        boolean result = TemplateUtils.evaluateAsBoolean(variableStack.pop())
                && TemplateUtils.evaluateAsBoolean(variableStack.pop());
        variableStack.push(result);
    }

    @Override
    public void outAOrBooleanOp(AOrBooleanOp node)
    {
        // pop the top two items off the variable stack
        boolean result = TemplateUtils.evaluateAsBoolean(variableStack.pop())
                || TemplateUtils.evaluateAsBoolean(variableStack.pop());
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
        boolean evaluateIf = TemplateUtils.evaluateAsBoolean(variableStack
                .pop());

        // evalute the if-clause
        if (evaluateIf)
        {
            List<PEntity> copy = new ArrayList<PEntity>(node.getIf());
            for (PEntity e : copy)
            {
                e.apply(this);
            }
        }
        // otherwise, evaluate the else-clause
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

        // this assumes that each of the arguments has already been evaluated
        // and added to the stack
        // get the objects from the stack and check to see if they are equal
        boolean equals = true;
        Object object = variableStack.pop();
        int numArgs = node.getArguments().size();
        for (int i = 1; i < numArgs && equals && object != null; i++)
        {
            Object nextObj = variableStack.pop();
            equals &= (nextObj != null && object.equals(nextObj));
        }

        // if they are equal, we evaluate the if-clause
        if (equals)
        {
            List<PEntity> copy = new ArrayList<PEntity>(node.getIfequal());
            for (PEntity e : copy)
            {
                e.apply(this);
            }
        }
        // otherwise, evaluate the else clause
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

}
