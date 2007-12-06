package galoot.interpret;

import galoot.analysis.DepthFirstAdapter;
import galoot.node.ACharEntity;
import galoot.node.ACommentEntity;
import galoot.node.AVariableEntity;

public class Interpreter extends DepthFirstAdapter
{

    public Interpreter()
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void inACharEntity(ACharEntity node)
    {
        System.out.print(node.getTextChar().getText());
    }

    @Override
    public void inACommentEntity(ACommentEntity node)
    {
        // don't print it
    }

    @Override
    public void inAVariableEntity(AVariableEntity node)
    {
        System.out.println("Var: " + node.getVarExpression().getText());
    }

}
