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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;

/**
 * Jester that uses Transformers as the means for pluggable serializers,
 * requiring them to be able to transform an object to a String.
 */
public abstract class StringJester implements IJester,
        ITransformer<String, Object>
{

    protected Map<String, ITransformer<String, ? extends Object>> transformers;

    protected Map<String, Class> classes;

    public StringJester()
    {
        transformers = new HashMap<String, ITransformer<String, ? extends Object>>();
        classes = new TreeMap<String, Class>(); // want in order
    }

    /**
     * Registers a Transformer for the given Class
     * 
     * @param clazz
     * @param transformer
     */
    public void registerTransformer(Class clazz,
            ITransformer<String, ? extends Object> transformer)
    {
        String className = clazz.getName();
        transformers.put(className, transformer);
        classes.put(className, clazz);
    }

    public void unregisterTransformer(Class clazz)
    {
        String className = clazz.getName();
        transformers.remove(className);
        classes.remove(className);
    }

    /**
     * Registers a Transformer and attempts to guess the generic class specified
     * as the From type (2nd generic parameter to ITransformer)
     * 
     * @see StringJester#registerTransformer(Class, ITransformer)
     * @param transformer
     */
    public void registerTransformer(
            ITransformer<String, ? extends Object> transformer)
    {
        // attempt to get the "From" Class defined as the F Generic Type of
        // ITransformer (2nd parameter)
        Class fromClass = ReflectionUtils.getGenericTypeClassesList(
                ITransformer.class, transformer).get(1).getTypeClass();
        if (fromClass == null)
        {
            // TODO warn that it's defaulting to Object.class
            fromClass = Object.class;
        }
        registerTransformer(fromClass, transformer);
    }

    /**
     * Retrieves the Transformer that is the best match for the given Class.
     * 
     * @param clazz
     * @return the best match, or null if not supported
     */
    public ITransformer<String, ? extends Object> getBestMatchTransformer(
            Class clazz)
    {
        ITransformer<String, Object> transformer = null;
        String clazzName = clazz.getName();

        // first see if we have an exact match
        if (transformers.containsKey(clazzName))
            transformer = (ITransformer<String, Object>) transformers
                    .get(clazzName);
        else
        {
            // now, see if it is a sub type of any
            for (Class c : classes.values())
            {
                if (c.isAssignableFrom(clazz))
                {
                    transformer = (ITransformer<String, Object>) transformers
                            .get(c.getName());
                    break;
                }
            }
        }
        return transformer;
    }

    /**
     * Fulfills the IJester interface.
     * 
     * @throws NotImplementedException
     */
    public Object in(InputStream stream, Map hints) throws Exception
    {
        return from(IOUtils.toString(stream), hints);
    }

    /**
     * Fulfills the IJester Interface
     */
    public void out(Object object, OutputStream out, Map hints)
            throws Exception
    {
        out.write(to(object, hints).getBytes());
    }

    /**
     * Serializes the input Object to a String
     * 
     * @param object
     * @return
     * @throws Exception
     */
    public String to(Object object, Map hints) throws Exception
    {
        // guard against null objects
        if (object == null)
            return defaultOut(object, hints);

        ITransformer<String, Object> transformer = (ITransformer<String, Object>) getBestMatchTransformer(object
                .getClass());
        if (transformer == null)
        {
            // do the default serialization if we can't decipher it
            return defaultOut(object, hints);
        }
        else
        {
            return transformer.to(object, hints);
        }
    }

    /**
     * Serializes from a String to an Object.
     */
    public Object from(String string, Map hints) throws Exception
    {
        // for now, just loop through the transformers, and try
        for (String className : transformers.keySet())
        {
            ITransformer<String, ? extends Object> transformer = transformers
                    .get(className);
            try
            {
                return transformer.from(string, hints);
            }
            catch (Exception e)
            {
                // wasn't this one... keep trying
            }
        }

        // otherwise, try the default
        return defaultIn(string, hints);
    }

    /**
     * Override this method to provide your own default output serialization.
     * 
     * This, combined with registering Class transformers is how you can
     * customize the serialization.
     * 
     * @param object
     *            the object being serialized
     * @param hints
     *            optional Map of serialization hints
     * @return
     */
    protected abstract String defaultOut(Object object, Map hints)
            throws Exception;

    /**
     * Override this method to provide your own default input serialization.
     * 
     * @param string
     *            the string being deserialized
     * @param hints
     *            optional Map of serialization hints
     * @return
     */
    protected abstract Object defaultIn(String string, Map hints)
            throws Exception;

}
