package galoot.interpret;

import galoot.analysis.DepthFirstAdapter;
import galoot.node.ABlock;
import galoot.node.ACharEntity;
import galoot.node.ACommand;
import galoot.node.AForBlock;
import galoot.node.AIfBlock;
import galoot.node.ALoad;
import galoot.node.AStringArgument;
import galoot.node.AStringInclude;
import galoot.node.AVarExpression;
import galoot.node.AVariableInclude;
import galoot.node.PEntity;
import galoot.node.TId;
import galoot.node.TMember;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Interpreter extends DepthFirstAdapter
{

    public Interpreter()
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void inACharEntity(ACharEntity node)
    {
        System.out.print(node.getChar().getText());
    }

    @Override
    public void inAVarExpression(AVarExpression node)
    {
        TId referent = node.getReferent();
        System.out.print("Var expression:" + referent.getText());
        LinkedList<TMember> members = node.getMembers();
        for (Iterator iter = members.iterator(); iter.hasNext();)
        {
            TMember member = (TMember) iter.next();
            System.out.print("." + member.getText());
        }
        System.out.println();
    }

    @Override
    public void outACommand(ACommand node)
    {
        System.out.println("command: " + node.getCommand());
    }

    @Override
    public void inAStringArgument(AStringArgument node)
    {
        System.out.println("argument: " + node.getString().getText());
    }

    @Override
    public void outABlock(ABlock node)
    {
        System.out.println("leaving block: " + node.getId().getText());
    }

    @Override
    public void outALoad(ALoad node)
    {
        LinkedList<TId> plugins = node.getPlugins();
        for (TId id : plugins)
        {
            System.out.println("Asked to load plug-in: " + id.getText());
        }
    }

    @Override
    public void outAStringInclude(AStringInclude node)
    {
        System.out.println("Asked to include file: "
                + node.getString().getText());
    }

    @Override
    public void outAVariableInclude(AVariableInclude node)
    {
        System.out.println("Asked to include a file from var: "
                + node.getVariable());
    }

    @Override
    public void inAForBlock(AForBlock node)
    {
        // TODO Auto-generated method stub
        super.inAForBlock(node);
    }

    @Override
    public void caseAForBlock(AForBlock node)
    {
        inAForBlock(node);
        if (node.getIterVar() != null)
        {
            node.getIterVar().apply(this);
        }
        if (node.getVariable() != null)
        {
            node.getVariable().apply(this);
        }

        // TODO
        // figure out how many times we will have to iterate
        // for each iteration, push, then pop a new context onto the stack
        // the context will contain some extra variables, including the
        // loop variable, and some other "special" counter variables

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
        inAIfBlock(node);
        if (node.getExpr1() != null)
        {
            node.getExpr1().apply(this);
        }
        if (node.getExpr2() != null)
        {
            node.getExpr2().apply(this);
        }
        
        //TODO
        //compute the boolean result
        //if true, execute the entities
        //if false, and an else block exists, execute the else entities 
        
        {
            List<PEntity> copy = new ArrayList<PEntity>(node.getEntities());
            for (PEntity e : copy)
            {
                e.apply(this);
            }
        }
        {
            List<PEntity> copy = new ArrayList<PEntity>(node.getElse());
            for (PEntity e : copy)
            {
                e.apply(this);
            }
        }
        outAIfBlock(node);
    }

}
