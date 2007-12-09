package galoot.interpret;

import galoot.ContextStack;
import galoot.Filter;
import galoot.PluginRegistry;
import galoot.Template;
import galoot.TemplateUtils;
import galoot.analysis.DepthFirstAdapter;
import galoot.lexer.LexerException;
import galoot.node.AAndBooleanOp;
import galoot.node.ABlock;
import galoot.node.ABooleanExpr;
import galoot.node.ACharEntity;
import galoot.node.AExtends;
import galoot.node.AFilter;
import galoot.node.AFilterBlock;
import galoot.node.AForBlock;
import galoot.node.AIfBlock;
import galoot.node.AIfequalBlock;
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
import galoot.node.AVariableEntity;
import galoot.node.AVariableInclude;
import galoot.node.AWithBlock;
import galoot.node.PArgument;
import galoot.node.PEntity;
import galoot.node.TMember;
import galoot.parser.ParserException;
import galoot.types.BlockFragment;
import galoot.types.Document;
import galoot.types.Pair;

import java.io.File;
import java.io.IOException;
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

    private Document document;

    private Document parentDocument = null;

    private ContextStack context;

    private Stack<Object> variableStack;

    private Deque<Pair<String, String>> filterStack;

    private Stack<StringBuffer> filterBlockData;

    private Stack<Map<String, Object>> forLoopStack;

    public Interpreter(ContextStack context)
    {
        this.context = context;
        document = new Document();
        variableStack = new Stack<Object>();
        filterStack = new LinkedList<Pair<String, String>>();
        filterBlockData = new Stack<StringBuffer>();
        forLoopStack = new Stack<Map<String, Object>>();
    }

    @Override
    public void outAVarExpression(AVarExpression node)
    {
        // whenever we come across a variable expression, we evaluate it, then
        // push it onto an expression stack. This way, the operations that use
        // the expressions can just pop off the stack

        String referent = node.getReferent().getText();

        // turn the members into a String list
        LinkedList<TMember> members = node.getMembers();
        List<String> stringMembers = new ArrayList<String>(members.size());
        String fullDotExpression = referent; // construct a full expression
        for (TMember member : members)
        {
            String memberText = member.getText();
            stringMembers.add(memberText);
            fullDotExpression += "." + memberText;
        }

        // first things first, see if the full "dot" expression is in the map
        Object object = context.get(fullDotExpression);
        if (object == null)
        {
            // try to get just the base object then
            object = context.get(referent);

            // evaluate the object/methods
            object = TemplateUtils.evaluateObject(object, stringMembers);
        }

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
        variableStack.push(TemplateUtils.stripEncasedString(node.getArg()
                .getText(), '"'));
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
        {
            // add the text to the document
            document.addContent(object.toString());
        }
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
        // remove the end-quotes and push the string argument onto the stack
        variableStack.push(TemplateUtils.stripEncasedString(node.getString()
                .getText(), '"'));
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
        // strip the quotes
        loadFilterPlugin(TemplateUtils.stripEncasedString(node.getString()
                .getText(), '"'), null);
    }

    @Override
    public void outAStringAsPlugin(AStringAsPlugin node)
    {
        // strip the quotes
        loadFilterPlugin(TemplateUtils.stripEncasedString(node.getString()
                .getText(), '"'), node.getAlias().getText());
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
    public void outAStringInclude(AStringInclude node)
    {
        String filename = TemplateUtils.stripEncasedString(node.getString()
                .getText(), '"');

        processIncludedFile(filename);
    }

    @Override
    public void outAVariableInclude(AVariableInclude node)
    {
        Object pop = variableStack.pop();
        processIncludedFile(pop.toString());
    }

    /**
     * Lookup and render the filename if it is found in the include directories
     * 
     * @param filename
     */
    private void processIncludedFile(String filename)
    {
        context.push();
        try
        {
            Document doc = loadDocument(filename);
            if (doc == null)
                throw new Exception();
            // add the text to the document
            document.addContent(doc.evaluateAsString());
        }
        catch (Throwable e)
        {
            log.warn("File could not be included: " + filename);
            // ignore the problem for now
        }
        finally
        {
            context.pop();
        }
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
        Integer objectSize = TemplateUtils.getObjectLength(loopObj);

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
                processForLoopIteration(loopVar, object, iterCount, copy, !it
                        .hasNext(), objectSize);
            }
        }

        outAForBlock(node);
    }

    /**
     * Process one iteration of a forloop.
     * 
     * @param loopVar
     *            the loop variable name
     * @param loopObj
     *            the loop object, already fully evaluated
     * @param iterCount
     *            the current count of the iteration, zero based
     * @param entities
     *            the entities to process
     * @param isLast
     *            true if this iteration is the last in the loop
     */
    private void processForLoopIteration(String loopVar, Object loopObj,
            int iterCount, Iterable<PEntity> entities, boolean isLast,
            int totalLoops)
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
        int revCounter = totalLoops - iterCount;
        extraLoopVars.put("revcounter", revCounter);
        extraLoopVars.put("revcounter0", revCounter - 1);
        if (!forLoopStack.isEmpty())
            extraLoopVars.put("parent", forLoopStack.peek());
        context.put("forloop", extraLoopVars);

        // push the loop vars onto the stack, so sub-for loops can access it
        forLoopStack.push(extraLoopVars);

        // apply to the entities
        for (PEntity e : entities)
        {
            e.apply(this);
        }

        // pop the context
        context.pop();

        // pop the extra vars off the forloop stack
        forLoopStack.pop();
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
        {
            // add the text to the document
            document.addContent(output.toString());
        }
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

    /**
     * This method probably should be propagated to a higher level somewhere. It
     * searches for the given filename within the template include paths
     * specified in the PluginRegistry.
     * 
     * @param filename
     * @return
     * @throws ParserException
     * @throws LexerException
     * @throws IOException
     */
    private Document loadDocument(String filename) throws ParserException,
            LexerException, IOException
    {
        // look in the registry for the paths where include files can be found.
        Iterable<String> includePaths = PluginRegistry.getInstance()
                .getTemplateIncludePaths();
        File templateFile = null;
        // loop over the paths to see if the file exists
        for (String path : includePaths)
        {
            File file = new File(path, filename);
            if (file.exists())
            {
                templateFile = file;
                break;
            }
        }
        if (templateFile != null)
            return new Template(templateFile).renderDocument(context);
        else
            log.warn("Document could not be located in include paths: "
                    + filename);
        return null;
    }

    @Override
    public void inAExtends(AExtends node)
    {
        // if we are in here, then this document extends another one
        String parentDoc = TemplateUtils.stripEncasedString(node
                .getParentName().getText(), '"');

        try
        {
            // try to load the doc.
            parentDocument = loadDocument(parentDoc);
            if (parentDoc == null)
                throw new Exception("Unable to load parent document: "
                        + parentDoc);
        }
        catch (Throwable e)
        {
            log.warn(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    @Override
    public void outACharEntity(ACharEntity node)
    {
        // add the text to the document
        document.addContent(node.getChar().getText());
    }

    @Override
    public void caseABlock(ABlock node)
    {
        boolean evaluate = false, existsInParent = false;
        int curBlockDepth = document.getBlockDepth();

        String blockName = node.getId().getText();

        /*
         * We only render the block if one of the following is true: (1) if the
         * document has a parent doc AND ((this block is a top-level block of
         * this document AND exists in the parent) OR (this block is NOT a
         * top-level block and does NOT exist in the parent)), or if (2) the
         * document does NOT have a parent doc. AND the block name has not
         * already been used in the current document.
         */

        if (parentDocument != null)
        {
            BlockFragment parentBlock = parentDocument.getDocumentBlock();
            existsInParent = parentBlock.hasBlock(blockName);

            if ((curBlockDepth == 0 && existsInParent)
                    || (curBlockDepth > 0 && !existsInParent))
                evaluate = true;
        }
        else if (document.hasBlock(blockName))
            throw new RuntimeException("Block already exists with name: "
                    + blockName);
        else
            evaluate = true;

        if (evaluate)
        {
            // push on a new context
            context.push();

            // add a new BlockFragment to the document
            BlockFragment newBlock = new BlockFragment(blockName);
            document.addContent(newBlock);

            /*
             * If it existed in the parent, and top-level, we add a special
             * variable to the context, which is essentially a "super" lookup.
             * Note that the data in the super block has already been evaluated.
             */
            if (curBlockDepth == 0 && existsInParent)
            {
                String superBlock = parentDocument.getDocumentBlock().getBlock(
                        blockName).evaluateAsString();
                context.put("block.super", superBlock);
            }

            inABlock(node);
            if (node.getId() != null)
            {
                node.getId().apply(this);
            }

            {
                List<PEntity> copy = new ArrayList<PEntity>(node.getEntities());
                for (PEntity e : copy)
                {
                    e.apply(this);
                }
            }
            outABlock(node);

            // tell the document we are done with the block
            document.popBlock();

            // pop the context
            context.pop();

            // now, replace the "super" fragment, if one existed
            if (curBlockDepth == 0 && existsInParent)
            {
                parentDocument.replaceBlock(newBlock);
            }
        }
    }

    /**
     * Get the fully qualified document which results after applying the
     * interpreter to an AST.
     * 
     * @return
     */
    public Document getDocument()
    {
        /*
         * if we have a parent (via extends keyword) return it, since everything
         * was added to it anyway. Otherwise, return our own document.
         */
        return parentDocument != null ? parentDocument : document;
    }
}
