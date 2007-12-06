package galoot.interpret;

import galoot.analysis.DepthFirstAdapter;
import galoot.node.ACharEntity;
import galoot.node.ACommand;
import galoot.node.AStringArgument;
import galoot.node.AVarExpression;
import galoot.node.TId;
import galoot.node.TMember;

import java.util.Iterator;
import java.util.LinkedList;

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

}
