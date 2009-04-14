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

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.LazyDynaMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

public class Interpreter extends DepthFirstAdapter
{
    public enum MapType {
        MAP, DYNABEAN
    }

    private Stack<Object> containerStack;

    private Object jsonObject;

    private Stack<String> fieldStack;

    private MapType mapType;

    public Interpreter()
    {
        this(MapType.MAP);
    }

    public Interpreter(MapType mapType)
    {
        this.mapType = mapType;
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
            ((Map) containerStack.peek()).put(fieldStack.pop(), list);
        }
        else if (containerStack.peek() instanceof DynaBean)
        {
            if (fieldStack.isEmpty())
                throw new UnsupportedOperationException();
            ((DynaBean) containerStack.peek()).set(fieldStack.pop(), list);
        }
        else
            throw new UnsupportedOperationException();
        containerStack.push(list);
    }

    protected void pushMap()
    {
        Object map = null;
        switch (mapType)
        {
        case DYNABEAN:
            map = new LazyDynaMap();
            break;
        case MAP:
        default:
            map = new TreeMap<String, Object>();
        }

        if (containerStack.isEmpty())
            jsonObject = map;
        else if (containerStack.peek() instanceof List)
            ((List<Object>) containerStack.peek()).add(map);
        else if (containerStack.peek() instanceof Map)
        {
            if (fieldStack.isEmpty())
                throw new UnsupportedOperationException();
            ((Map) containerStack.peek()).put(fieldStack.pop(), map);
        }
        else if (containerStack.peek() instanceof DynaBean)
        {
            if (fieldStack.isEmpty())
                throw new UnsupportedOperationException();
            ((DynaBean) containerStack.peek()).set(fieldStack.pop(), map);
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
            ((Map) containerStack.peek()).put(fieldStack.pop(), value);
        }
        else if (containerStack.peek() instanceof DynaBean)
        {
            if (fieldStack.isEmpty())
                throw new UnsupportedOperationException();
            ((DynaBean) containerStack.peek()).set(fieldStack.pop(), value);
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
        String key = node.getKey().getText();
        if (key.startsWith("\"") && key.startsWith("\""))
            key = StringUtils.strip(key, "\"");
        fieldStack.push(key);
        node.getValue().apply(this);
    }

}
