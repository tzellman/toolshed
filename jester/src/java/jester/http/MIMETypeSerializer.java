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
package jester.http;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import jester.JesterRegistry;

import org.apache.commons.lang.StringUtils;

/**
 * Serializes Objects based on requested MIME types.
 */
public class MIMETypeSerializer extends JesterRegistry
{
    // ! Maps mimeTypes to simple types (e.g. (text/html --> html) )
    protected Map<String, String> mimeTypeMap = new TreeMap<String, String>();

    public MIMETypeSerializer()
    {
        this("text");
    }

    public MIMETypeSerializer(String defaultContentType)
    {
        super(defaultContentType);
        mimeTypeMap.put("text/plain", "text");
    }

    /**
     * Add a mapping from an actual mimeType to a contentType. An example might
     * be: addMimeType("text/html", "html");
     * 
     * @param mimeType
     * @param contentType
     */
    public void addMIMEType(String mimeType, String contentType)
    {
        mimeTypeMap.put(mimeType, contentType);
    }

    /**
     * Dispatches an Object to the HttpServletRequest
     * 
     * @see http://tools.ietf.org/html/rfc2616#section-14.1
     * 
     * @param object
     * @param accepthHeader
     * @param out
     * @return the mimeType used
     * @throws Exception
     */
    public String serialize(Object object, HttpServletRequest request,
            OutputStream out, Map hints) throws Exception
    {
        String mimeType = guessMIMEType(request);
        if (StringUtils.isEmpty(mimeType) || !mimeTypeMap.containsKey(mimeType))
        {
            // use the first mimeType that matches the default contentType
            for (String key : mimeTypeMap.values())
            {
                if (mimeTypeMap.get(key).equals(defaultContentType))
                {
                    mimeType = key;
                    break;
                }
            }
        }

        if (StringUtils.isEmpty(mimeType) || !mimeTypeMap.containsKey(mimeType))
            throw new Exception("Unable to serialize object");

        String contentType = mimeTypeMap.get(mimeType);
        super.out(object, contentType, out, hints);
        return mimeType;
    }

    /**
     * Guesses the best value of the mimeType of the request based on the
     * httpAccept.
     * 
     * @param request
     * @return the best mimeType to use, or null if there isn't a match
     */
    protected String guessMIMEType(HttpServletRequest request)
    {
        String httpAccept = request.getHeader("Accept");
        if (StringUtils.isEmpty(httpAccept))
            return null;

        List<String> registeredTypes = new LinkedList<String>();
        for (String mimeType : mimeTypeMap.keySet())
        {
            String contentType = mimeTypeMap.get(mimeType);
            if (registry.containsKey(contentType))
                registeredTypes.add(mimeType);
        }
        return MIMEParse.bestMatch(registeredTypes, httpAccept);
    }

}
