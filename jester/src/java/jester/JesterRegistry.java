/* =============================================================================
 * This file is part of Jester
 * =============================================================================
 * (C) Copyright 2009, Tom Zellman, tzellman@gmail.com
 *
 * Jester is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
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

    public void out(Object object, String contentType, OutputStream out,
            Map hints) throws Exception
    {
        if (!registry.containsKey(contentType))
            contentType = defaultContentType;
        if (StringUtils.isEmpty(contentType))
            throw new Exception("Unable to serialize object");
        registry.get(contentType).out(object, out, hints);
    }

    public Object in(InputStream in, String contentType, Map hints)
            throws Exception
    {
        if (!registry.containsKey(contentType))
            contentType = defaultContentType;
        if (StringUtils.isEmpty(contentType))
            throw new Exception("Unable to serialize object");
        return registry.get(contentType).in(in, hints);
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
