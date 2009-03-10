package jester.json.parse;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import jester.json.parse.analysis.DepthFirstAdapter;
import jester.json.parse.node.AArray;
import jester.json.parse.node.AFalseValue;
import jester.json.parse.node.AField;
import jester.json.parse.node.ANullValue;
import jester.json.parse.node.ANumberValue;
import jester.json.parse.node.AObject;
import jester.json.parse.node.AStringValue;
import jester.json.parse.node.ATrueValue;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

public class Interpreter extends DepthFirstAdapter
{
    private Stack<Object> containerStack;

    private Object jsonObject;

    private Stack<String> fieldStack;

    public Interpreter()
    {
        jsonObject = null;
        containerStack = new Stack<Object>();
        fieldStack = new Stack<String>();
    }

    public Object getObject()
    {
        return jsonObject;
    }

    protected void pushList()
    {
        List<Object> list = new LinkedList<Object>();
        if (containerStack.isEmpty())
            jsonObject = list;
        else if (containerStack.peek() instanceof List)
            ((List<Object>) containerStack.peek()).add(list);
        else if (containerStack.peek() instanceof Map)
        {
            if (fieldStack.isEmpty())
                throw new UnsupportedOperationException();
            ((Map<String, Object>) containerStack.peek()).put(fieldStack.pop(),
                    list);
        }
        else
            throw new UnsupportedOperationException();
        containerStack.push(list);
    }

    protected void pushMap()
    {
        Map<String, Object> map = new TreeMap<String, Object>();
        if (containerStack.isEmpty())
            jsonObject = map;
        else if (containerStack.peek() instanceof List)
            ((List<Object>) containerStack.peek()).add(map);
        else if (containerStack.peek() instanceof Map)
        {
            if (fieldStack.isEmpty())
                throw new UnsupportedOperationException();
            ((Map<String, Object>) containerStack.peek()).put(fieldStack.pop(),
                    map);
        }
        else
            throw new UnsupportedOperationException();
        containerStack.push(map);
    }

    protected void addObject(Object value)
    {
        if (containerStack.isEmpty())
            jsonObject = value;
        else if (containerStack.peek() instanceof List)
            ((List<Object>) containerStack.peek()).add(value);
        else if (containerStack.peek() instanceof Map)
        {
            if (fieldStack.isEmpty())
                throw new UnsupportedOperationException();
            ((Map<String, Object>) containerStack.peek()).put(fieldStack.pop(),
                    value);
        }
        else
            throw new UnsupportedOperationException();
    }

    protected void pop()
    {
        containerStack.pop();
    }

    @Override
    public void caseANumberValue(ANumberValue node)
    {
        addObject(NumberUtils.createNumber(node.getNumber().getText()));
    }

    @Override
    public void caseAStringValue(AStringValue node)
    {
        addObject(StringUtils.strip(node.getString().getText(), "\""));
    }

    @Override
    public void caseANullValue(ANullValue node)
    {
        addObject(null);
    }

    @Override
    public void caseATrueValue(ATrueValue node)
    {
        addObject(true);
    }

    @Override
    public void caseAFalseValue(AFalseValue node)
    {
        addObject(false);
    }

    @Override
    public void inAObject(AObject node)
    {
        pushMap();
    }

    @Override
    public void outAObject(AObject node)
    {
        pop();
    }

    @Override
    public void inAArray(AArray node)
    {
        pushList();
    }

    @Override
    public void outAArray(AArray node)
    {
        pop();
    }

    @Override
    public void caseAField(AField node)
    {
        fieldStack.push(node.getKey().getText());
        node.getValue().apply(this);
    }

}
