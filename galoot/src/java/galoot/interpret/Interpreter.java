package galoot.interpret;

import galoot.ContextStack;
import galoot.Filter;
import galoot.PluginRegistry;
import galoot.Template;
import galoot.TemplateUtils;
import galoot.analysis.DepthFirstAdapter;
import galoot.node.AAndBooleanOp;
import galoot.node.ABooleanExpr;
import galoot.node.ACharEntity;
import galoot.node.ADocument;
import galoot.node.AExtends;
import galoot.node.AFilter;
import galoot.node.AFilterBlock;
import galoot.node.AForBlock;
import galoot.node.AIfBlock;
import galoot.node.AIfequalBlock;
import galoot.node.ALoad;
import galoot.node.AOrBooleanOp;
import galoot.node.AQuotedFilterArg;
import galoot.node.AStringArgument;
import galoot.node.AStringAsPlugin;
import galoot.node.AStringInclude;
import galoot.node.AStringPlugin;
import galoot.node.AUnquotedFilterArg;
import galoot.node.AVarAsPlugin;
import galoot.node.AVarExpression;
import galoot.node.AVarPlugin;
import galoot.node.AVariableArgument;
import galoot.node.AVariableEntity;
import galoot.node.AVariableInclude;
import galoot.node.AWithBlock;
import galoot.node.PArgument;
import galoot.node.PEntity;
import galoot.node.TMember;
import galoot.types.Pair;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Interpreter extends DepthFirstAdapter
{
    private static Log log = LogFactory.getLog(Interpreter.class);

    protected Writer writer;

    protected ContextStack context;

    protected Stack<Object> variableStack;

    protected Deque<Pair<String, String>> filterStack;

    protected Stack<StringBuffer> filterBlockData;

    public Interpreter(ContextStack context, Writer writer)
    {
        this.context = context;
        this.writer = writer;
        variableStack = new Stack<Object>();
        filterStack = new LinkedList<Pair<String, String>>();
        filterBlockData = new Stack<StringBuffer>();
    }

    /**
     * The endpoint for the string processing
     * 
     * @param s
     */
    protected void writeString(String s)
    {
        // if we are in a filterBlock, we need to "write" the data to it
        if (!filterBlockData.isEmpty())
        {
            filterBlockData.peek().append(s);
        }
        else
        {
            // write it directly
            directWrite(s);
        }
    }

    // write the string directly to the writer
    private void directWrite(String s)
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
    public void inADocument(ADocument node)
    {
    }

    @Override
    public void inAExtends(AExtends node)
    {
        // TODO move all of this code somewhere else
        String parent = node.getParentName().getText();
        System.out.println("Parent doc is: " + parent);

        File file = new File(parent);
        if (file.exists())
        {
            // Template template = template
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
        // whenver we come across a variable expression, we evaluate it, then
        // push it onto an expression stack. This way, the operations that use
        // the expressions can just pop off the stack

        Object object = context.get(node.getReferent().getText());
        LinkedList<TMember> members = node.getMembers();

        // turn the members into a String list
        List<String> stringMembers = new ArrayList<String>(members.size());
        for (TMember member : members)
            stringMembers.add(member.getText());

        // evaluate the object/methods
        object = TemplateUtils.evaluateObject(object, stringMembers);

        // apply the filters
        for (int i = 0, size = node.getFilters().size(); i < size; ++i)
        {
            Pair<String, String> nextFilter = filterStack.pollLast();
            Filter filter = context.getFilterMap().getFilter(
                    nextFilter.getFirst());
            if (filter == null)
                object = null;
            else
                object = filter.filter(object, nextFilter.getSecond());
        }

        variableStack.push(object);
    }

    @Override
    public void outAQuotedFilterArg(AQuotedFilterArg node)
    {
        // strip the quotes and push it on the stack
        String arg = node.getArg().getText();
        arg = arg.substring(1, arg.length() - 1);
        variableStack.push(arg);
    }

    @Override
    public void outAUnquotedFilterArg(AUnquotedFilterArg node)
    {
        variableStack.push(node.getArg().getText());
    }

    @Override
    public void outAFilter(AFilter node)
    {
        String name = node.getFilter().getText();
        // pop the filter arg off of the var stack, if it exists
        String arg = node.getArg() != null ? variableStack.pop().toString()
                : "";
        filterStack.push(new Pair<String, String>(name, arg));
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
    public void outAVariableArgument(AVariableArgument node)
    {
        // nothing to do here, since the expression will get evaluated inside of
        // outAVarExpression
    }

    private void loadFilterPlugin(String pluginName, String alias)
    {
        if (alias == null)
            alias = pluginName;
        Filter filter = PluginRegistry.getInstance().getFilter(pluginName);
        if (filter == null)
        {
            // TODO log here
        }
        else
        {
            // add the filter to the context's filter map
            context.getFilterMap().addFilter(filter, alias);
        }
    }

    @Override
    public void outAStringPlugin(AStringPlugin node)
    {
        String name = node.getString().getText();
        // strip the quotes
        name = name.substring(1, name.length() - 1);
        loadFilterPlugin(name, null);
    }

    @Override
    public void outAStringAsPlugin(AStringAsPlugin node)
    {
        String name = node.getString().getText();
        // strip the quotes
        name = name.substring(1, name.length() - 1);
        loadFilterPlugin(name, node.getAlias().getText());
    }

    @Override
    public void outAVarPlugin(AVarPlugin node)
    {
        // pop the var off the stack
        Object var = variableStack.pop();
        loadFilterPlugin(var.toString(), null);
    }

    @Override
    public void outAVarAsPlugin(AVarAsPlugin node)
    {
        // pop the var off the stack
        Object var = variableStack.pop();
        loadFilterPlugin(var.toString(), node.getAlias().getText());
    }

    @Override
    public void outALoad(ALoad node)
    {
        int numPlugins = node.getPlugins().size();
        // LinkedList<PVarExpression> plugins = node.getPlugins();
        // for (PVarExpression var : plugins)
        // {
        // // System.out.println("Asked to load plug-in: " + var.toString());
        // }
    }

    @Override
    public void outAStringInclude(AStringInclude node)
    {
        log.debug("Asked to include file: " + node.getString().getText());
        doInclude(node.getString().getText());
    }

    @Override
    public void outAVariableInclude(AVariableInclude node)
    {
        log.debug("Asked to include a file from var: " + node.getVariable());
        Object pop = variableStack.pop();
        doInclude(pop.toString());
    }

    /**
     * Lookup and render the filename if it is found in the include directories
     * 
     * @param filename
     */
    private void doInclude(String filename)
    {
        context.push();
        File file = null;

        String okFilename = (filename.endsWith("\"")) ? filename.substring(1,
                filename.length() - 1) : filename;

        // 1. test to see if the file exists
        file = new File(okFilename);
        if (!file.exists())
        {
            // look in the registry for the paths where include files can
            // be found.
            Iterable<String> includePaths = PluginRegistry.getInstance()
                    .getIncludePaths();
            boolean found = false;
            File tf = null;

            // loop over the paths to see if the file exists
            for (String path : includePaths)
            {
                tf = new File(path + File.separator + okFilename);
                if (tf.exists())
                {
                    found = true;
                    break;
                }
            }
            file = (found) ? tf : null;
        }

        if (file != null)
        {
            try
            {
                String rendered = new Template(file).render(context);
                writeString(rendered);
            }
            catch (IOException e)
            {
                // don't cause the world to end because of this
                log.error("Error reading file " + file.getPath() + ": "
                        + ExceptionUtils.getFullStackTrace(e));
            }
        }
        else
        {
            // don't cause the world to end because of this
            log.warn("template: " + filename + " could not be found.");
        }

        context.pop();
    }

    @Override
    public void caseAForBlock(AForBlock node)
    {
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

        // this is the loop variable, which will get updated each iteration
        String loopVar = node.getIterVar().getText();

        // pop the loop expression off the stack
        Object loopObj = variableStack.pop();

        // we will only support Iterable types

        // first, see if we can convert the type to an iterable type
        if (TemplateUtils.isArrayType(loopObj))
            loopObj = TemplateUtils.objectToCollection(loopObj);

        if (loopObj instanceof Iterable)
        {
            Iterable iter = (Iterable) loopObj;

            int iterCount = 0;
            for (Iterator it = iter.iterator(); it.hasNext(); ++iterCount)
            {
                Object object = (Object) it.next();
                List<PEntity> copy = new ArrayList<PEntity>(node.getEntities());
                doForLoopIteration(loopVar, object, iterCount, copy, !it
                        .hasNext());
            }
        }

        outAForBlock(node);
    }

    private void doForLoopIteration(String loopVar, Object loopObj,
            int iterCount, Iterable<PEntity> entities, boolean isLast)
    {
        // push a new context
        context.push();

        Map<String, Object> extraLoopVars = new HashMap<String, Object>();

        // add the vars
        context.put(loopVar, loopObj);
        extraLoopVars.put("counter0", iterCount);
        extraLoopVars.put("counter1", iterCount + 1);
        extraLoopVars.put("first", iterCount == 0);
        extraLoopVars.put("last", isLast);
        context.put("forloop", extraLoopVars);

        // apply to the entities
        for (PEntity e : entities)
        {
            e.apply(this);
        }

        // pop the context
        context.pop();

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

    @Override
    public void inAFilterBlock(AFilterBlock node)
    {
        // push a new StringBuffer, for this block
        filterBlockData.push(new StringBuffer());
    }

    @Override
    public void outAFilterBlock(AFilterBlock node)
    {
        StringBuffer filteredData = filterBlockData.pop();
        Object output = filteredData.toString();

        // apply the filters
        for (int i = 0, size = node.getFilters().size(); i < size
                && output != null; ++i)
        {
            Pair<String, String> nextFilter = filterStack.pollLast();
            Filter filter = context.getFilterMap().getFilter(
                    nextFilter.getFirst());

            // TODO decide on what we should do if a bad filter was given
            if (filter == null)
                // if a bad filter, nothing gets written
                output = null;
            else
            {
                output = filter.filter(output, nextFilter.getSecond());
            }
        }

        if (output != null)
            writeString(output.toString());
    }

    @Override
    public void caseAWithBlock(AWithBlock node)
    {
        inAWithBlock(node);
        if (node.getExpression() != null)
        {
            node.getExpression().apply(this);
        }
        if (node.getVar() != null)
        {
            node.getVar().apply(this);
        }

        // this is the loop variable, which will get updated each iteration
        String withVar = node.getVar().getText();

        // pop the loop expression off the stack
        Object withObj = variableStack.pop();

        // push a new context
        context.push();
        // add the vars
        context.put(withVar, withObj);
        {
            List<PEntity> copy = new ArrayList<PEntity>(node.getEntities());
            for (PEntity e : copy)
            {
                e.apply(this);
            }
        }
        // pop the context
        context.pop();
        outAWithBlock(node);
    }

}
