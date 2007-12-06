package galoot.interpret;

import galoot.analysis.DepthFirstAdapter;
import galoot.node.ACharEntity;
import galoot.node.ACommentEntity;
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
    public void inACommentEntity(ACommentEntity node)
    {
        // don't print it
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
    
}
