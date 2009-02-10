package jester;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Dispatches objects to get serialized. Serializers are registered based on
 * mimeTypes.
 */
public class JesterRegistry
{

    // ! Maps content types to serializers
    protected Map<String, IJester> registry;

    protected String defaultContentType;

    public JesterRegistry()
    {
        this(null);
    }

    public JesterRegistry(String defaultContentType)
    {
        this.defaultContentType = defaultContentType;
        registry = new HashMap<String, IJester>();
    }

    public void register(IJester jester)
    {
        registry.put(jester.getContentType(), jester);
    }

    public void unregister(String contentType)
    {
        registry.remove(contentType);
    }

    public void register(Collection<IJester> jesters)
    {
        for (IJester jester : jesters)
            register(jester);
    }

    public void out(Object object, String contentType, OutputStream out)
            throws Exception
    {
        if (!registry.containsKey(contentType))
            contentType = defaultContentType;
        if (StringUtils.isEmpty(contentType))
            throw new Exception("Unable to serialize object");
        registry.get(contentType).out(object, out);
    }

    public Object in(InputStream in, String contentType) throws Exception
    {
        if (!registry.containsKey(contentType))
            contentType = defaultContentType;
        if (StringUtils.isEmpty(contentType))
            throw new Exception("Unable to serialize object");
        return registry.get(contentType).in(in);
    }

    public String getDefaultContentType()
    {
        return defaultContentType;
    }

    public void setDefaultContentType(String defaultContentType)
    {
        this.defaultContentType = defaultContentType;
    }
    
    
}
