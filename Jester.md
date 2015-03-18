## Introduction ##

Jester is a pluggable serialization library written in Java. You can use it to serialize Objects to/from an output/input streams. In addition, Jester provides tools that assist in the serialization process.

The best way to really highlight how Jester can work for you is by going through a couple of examples.


---


### Example : JSON ###

JSON is a very simple yet powerful notation that can be used to serialize server-side data to client-side JavaScript. Let's use Jester to serialize our data to JSON. We will have some custom objects that need to be serialized transparently.

First, we have our sample class that we need to serialize.
```
/**
 * Test class to show how we can transform it
 */
class MyCustomModelClass
{
    public String name;

    protected int id;

    public boolean required;

    public String email;

    @Override
    public String toString()
    {
        return name;
    }
}
```


In your application (maybe in a servlet), let's use some of Jester.

Let's create an implementation of the IJester interface that will serialize our JSON data. This is pretty trivial, as you can see below. For now, we only care about serializing to an OutputStream, so we'll ignore the in() method. Note that the expressions passed into the POJOTransformer can evaluate to fields, methods, or even sub-objects/values.
```
/**
 * This class just uses a POJOTransformer to transform data to a Map, and
 * then to a String. This is by no means a correct implementation, so don't
 * go about using it.
 */
class DumbJSONJester implements IJester
{
    // we are going to always use this transformer
    private POJOTransformer transformer;

    public DumbJSONJester()
    {
        // we want to transform the following fields/methods
        // note that if we try to transform an object that doesn't have
        // these fields/methods, they will be set to null
        // we could add a flag that prunes null fields... a thought
        transformer = new POJOTransformer<Object>("name", "email",
                "required", "toString", "class.simpleName");
    }

    public String getContentType()
    {
        return "dumbText";
    }

    public Object in(InputStream stream, Map hints) throws Exception
    {
        throw new NotImplementedException();
    }

    public void out(Object object, OutputStream out, Map hints)
            throws Exception
    {
        if (object instanceof Map)
        {
            out.write(mapToString((Map) object).getBytes());
        }
        else
        {
            Map map = transformer.to(object, hints);
            out(map, out, hints);
        }
    }

    private String mapToString(Map data)
    {
        // this is really rock dumb, but it's an example
        StringBuffer b = new StringBuffer("{");
        Object[] keys = data.keySet().toArray();
        for (int i = 0, size = keys.length; i < size; ++i)
        {
            Object key = keys[i];
            b
                    .append("\"" + key.toString() + "\":\"" + data.get(key)
                            + "\"");
            if (i < size - 1)
                b.append(",");
        }
        b.append("}");
        return b.toString();
    }
}
```

Now, let's serialize an object!

```
DumbJSONJester jester = new DumbJSONJester();

// now, let's try with an object that has all fields set
MyCustomModelClass myModel = new MyCustomModelClass();
myModel.email = "lets@getpumpedwithjava.com";
myModel.id = 1; // this won't get serialized, since it's "private"
myModel.name = "Gert P. Frohb";
myModel.required = true;

//serialize to a String
String jsonData = JesterUtils.serializeToString(myModel, jester);

// or...

jester.out(myModel, System.out, null); //serialize to stdout
```

If you got this far, congratulations, because the best is yet to come! Now, I wouldn't recommend that you go ahead and use the above example in your code. Instead, you should use the JSONJester and JSONUtils classes in the repository. Below is an example.

```
JSONJester jester = JSONJester();

// ingest a map and spit out JSON
Map data = new HashMap();
data.put("drink", "Diet Mountain Dew");

String jsonData = JesterUtils.serializeToString(data, jester);

//or, you can serialize to, say an HttpServletResponse OutputStream

OutputStream stream = servletResponse.getOutputStream();
jester.serialize(data, stream, null);
stream.flush();
```

It's easy to provide your own serializers if you want.
```
jester.getSerializer().register(new POJOConverter<MyCustomModelClass>("name", "email", "required"));
```

Now, all MyCustomModelClass objects will use the serializer we just registered.

Another example of registering a serializer, which really isn't very useful, but serves as an example:
```
// register an anonymous transformer for BigDecimal objects
jester.getSerializer().register(
        new IConverter<BigDecimal, String>()
        {
            public String convert(BigDecimal from, Map hints)
            {
                return JSONSerializer.toJSONString("BigDecimal: "
                        + from.floatValue());
            }
        });
```

If you don't want to serialize to a Stream, you can always just use a serializer independent of a Jester. For example:
```
JSONSerializer serializer = new JSONSerializer();

System.out.println(serializer.convert("string"));
System.out.println(serializer.convert(300));
System.out.println(serializer.convert(3.14159));

Object[] objects = new Object[] { "skateboard", "snowboard", "hack",
        300 };
System.out.println(serializer.convert(objects));
```