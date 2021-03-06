/*
 * =============================================================================
 * This file is part of Galoot
 * =============================================================================
 * (C) Copyright 2009, Tom Zellman, tzellman@gmail.com
 * 
 * Galoot is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package galoot.interpret;

import galoot.ContextStack;
import galoot.Filter;
import galoot.Macro;
import galoot.PluginRegistry;
import galoot.Template;
import galoot.TemplateUtils;
import galoot.analysis.DepthFirstAdapter;
import galoot.lexer.LexerException;
import galoot.node.AAndBooleanOp;
import galoot.node.ABinaryBooleanExpr;
import galoot.node.ABlock;
import galoot.node.ACharEntity;
import galoot.node.AElseifBlock;
import galoot.node.AEqBinaryExpr;
import galoot.node.AExtends;
import galoot.node.AFilter;
import galoot.node.AFilterBlock;
import galoot.node.AFirstOfEntity;
import galoot.node.AForBlock;
import galoot.node.AGtBinaryExpr;
import galoot.node.AGteBinaryExpr;
import galoot.node.AIfBlock;
import galoot.node.AIfequalBlock;
import galoot.node.ALtBinaryExpr;
import galoot.node.ALteBinaryExpr;
import galoot.node.AMacroBlock;
import galoot.node.AMacroVariableBlock;
import galoot.node.ANeBinaryExpr;
import galoot.node.ANowEntity;
import galoot.node.ANumberVarExpression;
import galoot.node.AOrBooleanOp;
import galoot.node.AQuotedFilterArg;
import galoot.node.ASetEntity;
import galoot.node.AStringAsPlugin;
import galoot.node.AStringInclude;
import galoot.node.AStringPlugin;
import galoot.node.AStringVarExpression;
import galoot.node.ATemplatetagEntity;
import galoot.node.AUnaryBooleanExpr;
import galoot.node.AUnquotedFilterArg;
import galoot.node.AVarAsPlugin;
import galoot.node.AVarPlugin;
import galoot.node.AVariableInclude;
import galoot.node.AVariableVarExpression;
import galoot.node.AVariableVariableBlock;
import galoot.node.AWithBlock;
import galoot.node.PElseifBlock;
import galoot.node.PEntity;
import galoot.node.PVarExpression;
import galoot.node.Start;
import galoot.node.TId;
import galoot.node.TMember;
import galoot.parser.ParserException;
import galoot.types.BlockFragment;
import galoot.types.Document;
import galoot.types.Pair;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Interpreter extends DepthFirstAdapter
{
    private static Log log = LogFactory.getLog(Interpreter.class);

    // ! The document that contains all of the fragments
    private Document document = null;

    // ! If the document is an extension of another, the parent document
    private Document parentDocument = null;

    // ! the context stack
    private ContextStack context;

    // ! stack used to keep track of variable expressions as they evaluated
    private Stack<Object> variableStack;

    // ! stack used to keep track of filter expressions, as they are evaluated
    private LinkedList<Pair<String, String>> filterStack;

    /**
     * stack of actual {% filter %} blocks, which needs to do post-processing on
     * the block of data
     */
    private Stack<StringBuffer> filterBlockData;

    // ! keeps track of for loops vars, so we can refer to parent loop
    // counter(s)
    private Stack<Map<String, Object>> forLoopStack;

    /**
     * Creates a new Interpreter, using the given context stack as the initial
     * values available to any processed templates
     * 
     * @param context
     */
    public Interpreter(ContextStack context)
    {
        if (context == null)
            context = new ContextStack();
        this.context = context;
    }

    /**
     * Initialize the instance vars used in processing
     */
    private void init()
    {
        document = new Document();
        variableStack = new Stack<Object>();
        filterStack = new LinkedList<Pair<String, String>>();
        filterBlockData = new Stack<StringBuffer>();
        forLoopStack = new Stack<Map<String, Object>>();
    }

    @Override
    public void inStart(Start node)
    {
        // when in the start production, we initialize everything
        init();
        super.inStart(node);
    }

    /**
     * Call finishString when the evaluated string is completely finished being
     * processed.
     * 
     * @param s
     */
    private void finishString(String s)
    {
        // if we are in a filterBlock, we need to "write" the data to it
        if (!filterBlockData.isEmpty())
        {
            filterBlockData.peek().append(s);
        }
        else
        {
            // add the text to the document
            document.addContent(s.toString());
        }
    }

    protected Object evaluateObject(Object object, List<TMember> members,
                                    int nFilters)
    {
        if (object != null && members != null && members.size() > 0)
        {
            List<String> stringMembers = new ArrayList<String>(
                    members != null ? members.size() : 0);

            for (TMember member : members)
                stringMembers.add(member.getText().trim());

            object = TemplateUtils.evaluateObject(object, stringMembers,
                    context);
        }

        // pop off the last N filters from the stack
        LinkedList<Pair<String, String>> filters = new LinkedList<Pair<String, String>>();
        for (int i = 0; i < nFilters; ++i)
            filters.addFirst(filterStack.removeLast());

        // apply the filters
        for (Pair<String, String> nextFilter : filters)
        {
            Filter filter = context.getFilterMap().getFilter(
                    nextFilter.getFirst());
            if (filter == null)
            {
                log.warn("Missing filter: [" + nextFilter.getFirst()
                        + "] - setting object to 'null'");
                object = null;
            }
            else
            {
                log.info("running filter: " + nextFilter.getFirst());
                String args = nextFilter.getSecond();
                object = filter.filter(object, context,
                        args != null ? StringUtils.split(args, ',')
                                : new String[0]);
            }
        }
        return object;
    }

    @Override
    public void outAStringVarExpression(AStringVarExpression node)
    {
        String referent = TemplateUtils.stripEncasedString(node.getReferent()
                .getText(), '"', '\'');
        variableStack.push(evaluateObject(referent, node.getMembers(), node
                .getFilters().size()));
    }

    @Override
    public void outANumberVarExpression(ANumberVarExpression node)
    {
        variableStack.push(evaluateObject(NumberUtils.createNumber(node
                .getReferent().getText()), node.getMembers(), node.getFilters()
                .size()));
    }

    @Override
    public void outAVariableVarExpression(AVariableVarExpression node)
    {
        // whenever we come across a variable expression, we evaluate it, then
        // push it onto an expression stack. This way, the operations that use
        // the expressions can just pop off the stack

        String referent = node.getReferent().getText();

        // turn the members into a String list
        LinkedList<TMember> members = node.getMembers();
        String fullDotExpression = referent; // construct a full expression
        for (TMember member : members)
            fullDotExpression += "." + member.getText().trim();

        // first things first, see if the full "dot" expression is in the map
        Object object = context.getVariable(fullDotExpression);
        if (object != null)
            object = evaluateObject(object, null, node.getFilters().size());
        else
            object = evaluateObject(context.getVariable(referent), members,
                    node.getFilters().size());
        variableStack.push(object);
    }

    @Override
    public void outAMacroVariableBlock(AMacroVariableBlock node)
    {
        String macroName = node.getMacro().getText();

        Macro macro = context.getMacro(macroName);
        if (macro != null)
        {
            if (macro.getNumArguments() != variableStack.size())
                throw new RuntimeException("Macro '" + macroName
                        + "' expected " + macro.getNumArguments()
                        + " arguments");

            // push a new context
            context.push();

            // add the arguments as vars in the context
            List<String> args = IteratorUtils.toList(macro.getArguments()
                    .iterator());
            // reverse the list, since we are popping in reverse order
            Collections.reverse(args);
            for (String arg : args)
                context.putVariable(arg, variableStack.pop());

            {
                for (PEntity e : macro.getEntities())
                {
                    e.apply(this);
                }
            }
            // pop the context
            context.pop();
        }
        else
        {
            log.warn("Unknown macro: " + macroName);
        }
    }

    @Override
    public void outAQuotedFilterArg(AQuotedFilterArg node)
    {
        // strip the quotes and push it on the stack
        variableStack.push(TemplateUtils.stripEncasedString(node.getArg()
                .getText(), '"', '\''));
    }

    @Override
    public void outAUnquotedFilterArg(AUnquotedFilterArg node)
    {
        final String text = node.getArg().getText();
        // look it up in the context
        final Object var = context.getVariable(text);

        if (var == null)
            variableStack.push(text);
        else
            variableStack.push(var);
    }

    @Override
    public void outAFilter(AFilter node)
    {
        String name = node.getFilter().getText();
        // pop the filter arg off of the var stack, if it exists
        String arg = node.getArg() != null ? variableStack.pop().toString()
                : "";
        filterStack.addLast(new Pair<String, String>(name, arg));
    }

    @Override
    public void outAVariableVariableBlock(AVariableVariableBlock node)
    {
        Object object = variableStack.pop();
        if (object != null)
        {
            finishString(object.toString());
        }
    }

    @Override
    public void outAUnaryBooleanExpr(AUnaryBooleanExpr node)
    {
        // pop the variable (evaluated expression) off the stack
        Object object = variableStack.pop();
        boolean negate = node.getNot() != null;

        boolean boolObj = TemplateUtils.evaluateAsBoolean(object);
        boolObj = negate ? !boolObj : boolObj;
        variableStack.push(boolObj);
    }

    @Override
    public void outABinaryBooleanExpr(ABinaryBooleanExpr node)
    {
        Object object = variableStack.pop();
        boolean boolObj = TemplateUtils.evaluateAsBoolean(object);
        variableStack.push(boolObj);
    }

    @Override
    public void outAGtBinaryExpr(AGtBinaryExpr node)
    {
        Object rhs = variableStack.pop();
        Object lhs = variableStack.pop();
        boolean boolObj = TemplateUtils.compareObjects(lhs, rhs) == 1;
        variableStack.push(boolObj);
    }

    @Override
    public void outAGteBinaryExpr(AGteBinaryExpr node)
    {
        Object rhs = variableStack.pop();
        Object lhs = variableStack.pop();
        int val = TemplateUtils.compareObjects(lhs, rhs);
        boolean boolObj = (val == 1 || val == 0);
        variableStack.push(boolObj);
    }

    @Override
    public void outALtBinaryExpr(ALtBinaryExpr node)
    {
        Object rhs = variableStack.pop();
        Object lhs = variableStack.pop();
        boolean boolObj = TemplateUtils.compareObjects(lhs, rhs) == -1;
        variableStack.push(boolObj);
    }

    @Override
    public void outALteBinaryExpr(ALteBinaryExpr node)
    {
        Object rhs = variableStack.pop();
        Object lhs = variableStack.pop();
        int val = TemplateUtils.compareObjects(lhs, rhs);
        boolean boolObj = (val == -1 || val == 0);
        variableStack.push(boolObj);
    }

    @Override
    public void outAEqBinaryExpr(AEqBinaryExpr node)
    {
        Object rhs = variableStack.pop();
        Object lhs = variableStack.pop();
        boolean boolObj = TemplateUtils.compareObjects(lhs, rhs) == 0;
        variableStack.push(boolObj);
    }

    @Override
    public void outANeBinaryExpr(ANeBinaryExpr node)
    {
        Object rhs = variableStack.pop();
        Object lhs = variableStack.pop();
        boolean boolObj = TemplateUtils.compareObjects(lhs, rhs) != 0;
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

    private void loadFilterPlugin(String pluginName, String alias)
    {
        if (alias == null)
            alias = pluginName;
        Filter filter = PluginRegistry.getInstance().getFilter(pluginName);
        if (filter == null)
        {
            log.warn("Unable to load filter plugin: [" + pluginName + "]");
        }
        else
        {
            log.info("Loaded filter plugin: [" + pluginName + "] as [" + alias
                    + "]");
            // add the filter to the context's filter map
            context.getFilterMap().addFilter(filter, alias);
        }
    }

    @Override
    public void outAStringPlugin(AStringPlugin node)
    {
        // strip the quotes
        loadFilterPlugin(TemplateUtils.stripEncasedString(node.getString()
                .getText(), '"', '\''), null);
    }

    @Override
    public void outAStringAsPlugin(AStringAsPlugin node)
    {
        // strip the quotes
        loadFilterPlugin(TemplateUtils.stripEncasedString(node.getString()
                .getText(), '"', '\''), node.getAlias().getText());
    }

    @Override
    public void outAVarPlugin(AVarPlugin node)
    {
        // pop the var off the stack
        Object var = variableStack.pop();
        if (var != null)
            loadFilterPlugin(var.toString(), null);
        else
            log
                    .warn("plug-in expression evaluated to null: "
                            + node.toString());
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
                .getText(), '"', '\'');

        processIncludedFile(filename);
    }

    @Override
    public void outAVariableInclude(AVariableInclude node)
    {
        Object pop = variableStack.pop();
        if (pop != null)
            processIncludedFile(pop.toString());
        else
            log.warn("include expression evaluated to null: "
                    + node.getVariable().toString());
    }

    /**
     * Lookup and render the filename if it is found in the include directories
     * 
     * @param filename
     */
    private void processIncludedFile(String filename)
    {
        // context.push();
        try
        {
            Document doc = loadDocument(filename);
            if (doc == null)
                throw new Exception();
            finishString(doc.evaluateAsString());
        }
        catch (Throwable e)
        {
            log.warn("File could not be included: " + filename);
            // ignore the problem for now
        }
        // finally
        // {
        // context.pop();
        // }
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
                                         int iterCount,
                                         Iterable<PEntity> entities,
                                         boolean isLast, int totalLoops)
    {
        // push a new context
        context.push();

        Map<String, Object> extraLoopVars = new HashMap<String, Object>();

        // add the vars
        context.putVariable(loopVar, loopObj);
        extraLoopVars.put("counter0", iterCount);
        extraLoopVars.put("counter1", iterCount + 1);
        extraLoopVars.put("first", iterCount == 0);
        extraLoopVars.put("last", isLast);
        int revCounter = totalLoops - iterCount;
        extraLoopVars.put("revcounter", revCounter);
        extraLoopVars.put("revcounter0", revCounter - 1);
        if (!forLoopStack.isEmpty())
            extraLoopVars.put("parent", forLoopStack.peek());
        context.putVariable("forloop", extraLoopVars);

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
    public void caseAElseifBlock(AElseifBlock node)
    {
        inAElseifBlock(node);

        if (node.getExpr1() != null)
        {
            node.getExpr1().apply(this);
        }
        if (node.getExpr2() != null)
        {
            node.getExpr2().apply(this);
        }

        // at this point, the boolean expression result is on the var. stack
        boolean evaluateElseIf = TemplateUtils.evaluateAsBoolean(variableStack
                .pop());

        // we will throw an exception if it doesn't work
        if (evaluateElseIf)
        {
            List<PEntity> copy = new ArrayList<PEntity>(node.getElseif());
            for (PEntity e : copy)
            {
                e.apply(this);
            }
        }

        // finally, this might be a hack, but push a var on the stack saying
        // whether we evaluated
        variableStack.push(evaluateElseIf);
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
        // otherwise, evaluate the elseifs and/or else-clause
        else
        {
            boolean evaluated = false;
            LinkedList<PElseifBlock> elseif = node.getElseif();
            if (elseif != null)
            {
                for (PElseifBlock elseifBlock : elseif)
                {
                    elseifBlock.apply(this);

                    // pop a boolean from the stack saying whether we excercised
                    // the
                    // elseif
                    evaluated = TemplateUtils.evaluateAsBoolean(variableStack
                            .pop());
                    if (evaluated)
                        break;
                }
            }

            if (!evaluated && node.getElse() != null)
            {
                List<PEntity> copy = new ArrayList<PEntity>(node.getElse());
                for (PEntity e : copy)
                {
                    e.apply(this);
                }
            }
        }
        outAIfBlock(node);
    }

    @Override
    public void caseAIfequalBlock(AIfequalBlock node)
    {
        inAIfequalBlock(node);
        {
            List<PVarExpression> copy = new ArrayList<PVarExpression>(node
                    .getArguments());
            for (PVarExpression e : copy)
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

        // pop off the last N filters from the stack
        LinkedList<Pair<String, String>> filters = new LinkedList<Pair<String, String>>();
        for (int i = 0, size = node.getFilters().size(); i < size; ++i)
            filters.addFirst(filterStack.removeLast());

        // apply the filters
        for (Pair<String, String> nextFilter : filters)
        {
            Filter filter = context.getFilterMap().getFilter(
                    nextFilter.getFirst());

            // TODO decide on what we should do if a bad filter was given
            if (filter == null)
                // if a bad filter, nothing gets written
                output = null;
            else
            {
                String args = nextFilter.getSecond();
                output = filter.filter(output, context,
                        args != null ? StringUtils.split(args, ',')
                                : new String[0]);
            }
        }
        finishString(ObjectUtils.toString(output, ""));
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
        context.putVariable(withVar, withObj);
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

        if (templateFile == null)
        {
            // try local or absolute filenames
            File file = new File(filename);
            if (file.exists())
                templateFile = file;
        }

        if (templateFile != null)
            return new Template(templateFile).renderDocument(context);
        else
        {
            // try to load it as a resource (url)
            InputStream stream = Interpreter.class
                    .getResourceAsStream(filename);

            if (stream == null)
            {
                // try to load it as a straight URL
                stream = new URL(filename).openStream();
            }

            if (stream != null)
            {
                Reader reader = new InputStreamReader(stream);
                final Document doc = new Template(reader)
                        .renderDocument(context);
                stream.close();
                return doc;
            }

            log.warn("File could not be located in include paths: " + filename);
        }
        return null;
    }

    @Override
    public void inAExtends(AExtends node)
    {
        // if we are in here, then this document extends another one
        String parentDoc = TemplateUtils.stripEncasedString(node
                .getParentName().getText(), '"', '\'');

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
        finishString(node.getChar().getText());
    }

    @Override
    public void caseABlock(ABlock node)
    {
        boolean evaluate = false, existsInParent = false;
        int curBlockDepth = document.getBlockDepth();

        String blockName = node.getId() != null ? node.getId().getText() : null;

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
            existsInParent = blockName != null
                    && parentBlock.hasBlock(blockName);

            if ((curBlockDepth == 0 && existsInParent)
                    || (curBlockDepth > 0 && !existsInParent))
                evaluate = true;
        }
        else if (blockName != null && document.hasBlock(blockName))
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
                context.putVariable("block.super", superBlock);
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

    @Override
    public void outAFirstOfEntity(AFirstOfEntity node)
    {
        int numArgs = node.getArgs().size();

        /*
         * since the args are pushed on the expression stack in order, we have
         * to use reverse logic here
         */
        Object obj = null;
        for (int i = 0; i < numArgs; ++i)
        {
            Object var = variableStack.pop();
            if (TemplateUtils.evaluateAsBoolean(var))
                obj = var;
        }
        if (obj != null)
            finishString(obj.toString());
    }

    @Override
    public void outATemplatetagEntity(ATemplatetagEntity node)
    {
        String tag = node.getTag().getText().toLowerCase();
        if (tag.equals("openblock"))
            finishString("{%");
        else if (tag.equals("closeblock"))
            finishString("%}");
        else if (tag.equals("openvariable"))
            finishString("{{");
        else if (tag.equals("closevariable"))
            finishString("}}");
        else if (tag.equals("openbrace"))
            finishString("{");
        else if (tag.equals("closebrace"))
            finishString("}");
        else if (tag.equals("opencomment"))
            finishString("{#");
        else if (tag.equals("closecomment"))
            finishString("#}");
    }

    @Override
    public void outANowEntity(ANowEntity node)
    {
        Calendar cal = Calendar.getInstance();
        String format = node.getFormat() != null ? TemplateUtils
                .stripEncasedString(node.getFormat().getText(), '"', '\'')
                : null;

        try
        {
            String formatted = cal.getTime().toString();
            if (format != null)
            {
                // add a little hack here that lets you not have to specify the
                // 1$ positional chars in the format
                String[] parts = format.split("%[tT]");
                if (parts.length == 0)
                    formatted = new Formatter().format(format, cal).toString();
                else
                {
                    Calendar calArr[] = new Calendar[parts.length];
                    for (int i = 0, size = parts.length; i < size; ++i)
                        calArr[i] = cal;
                    formatted = new Formatter().format(format, calArr)
                            .toString();
                }
            }

            if (formatted != null)
                finishString(formatted);
        }
        catch (Throwable e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void outASetEntity(ASetEntity node)
    {
        // pop off the stack
        Object value = variableStack.pop();
        String varName = node.getVar().getText();

        // add the variable to the current or higher context
        context.putVariable(varName, value, true);
    }

    @Override
    public void caseAMacroBlock(AMacroBlock node)
    {
        inAMacroBlock(node);
        LinkedList<TId> vars = node.getVars();
        List<String> args = new ArrayList<String>(vars.size());
        for (TId e : vars)
        {
            e.apply(this);
            args.add(e.getText());
        }
        String name = node.getId().getText();
        Macro macro = new Macro(name, args, node.getEntities());
        context.putMacro(name, macro);

        outAMacroBlock(node);
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
