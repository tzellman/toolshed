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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jester.utils.ReflectionUtils;
import jester.utils.ReflectionUtils.GenericTypeInfo;

import org.apache.commons.lang.SerializationException;

/**
 * Registry for {@link IConverter}s.
 * 
 * @author tzellman
 */
public abstract class ConverterRegistry<F extends Object, T extends Object>
        implements IConverter<F, T>
{

    // Mapping of IConverters
    protected Map<String, Map<String, IConverter<F, T>>> converters;

    protected Map<String, Class<? extends Object>> classes;

    public ConverterRegistry()
    {
        converters = new TreeMap<String, Map<String, IConverter<F, T>>>();
        classes = new HashMap<String, Class<? extends Object>>();
    }

    /**
     * Registers a converter
     * 
     * @param converter
     */
    public <G extends F, U extends T> void register(IConverter<G, U> converter)
    {
        List<GenericTypeInfo> genericTypeList = ReflectionUtils
                .getGenericTypeClassesList(IConverter.class, converter);
        Class<? extends Object> fromClass = genericTypeList.get(0)
                .getTypeClass();
        Class<? extends Object> toClass = genericTypeList.get(1).getTypeClass();

        String fromName = fromClass.getName();
        String toName = toClass.getName();

        if (!converters.containsKey(fromName))
        {
            converters.put(fromName, new TreeMap<String, IConverter<F, T>>());
        }
        converters.get(fromName).put(toName, (IConverter<F, T>) converter);
        classes.put(fromName, fromClass);
        classes.put(toName, toClass);
    }

    /**
     * Attempts to find the best matched IConverter for the given from object
     * and to class.
     * 
     * @param from
     * @param toClass
     * @return
     */
    protected IConverter<F, T> getBestConverter(Object from,
            Class<? extends Object> toClass)
    {
        if (from == null)
            return null;

        Class<? extends Object> clazz = from.getClass();
        String clazzName = clazz.getName();

        IConverter<F, T> converter = null;

        Map<String, IConverter<F, T>> toMap = null;
        // first, try an exact match
        if (converters.containsKey(clazzName))
        {
            toMap = converters.get(clazzName);
        }
        else
        {
            // find the first one that matches
            for (String key : converters.keySet())
            {
                if (classes.containsKey(key))
                {
                    Class<? extends Object> fromClass = classes.get(key);
                    if (fromClass.isAssignableFrom(clazz))
                    {
                        toMap = converters.get(key);
                        break;
                    }
                }
            }
        }

        // do the same thing, but for the to class
        if (toMap != null)
        {
            clazzName = toClass.getName();
            if (toMap.containsKey(clazzName))
            {
                converter = toMap.get(clazzName);
            }
            else
            {
                // find the first one that matches
                for (String key : toMap.keySet())
                {
                    if (classes.containsKey(key))
                    {
                        Class<? extends Object> thisClazz = classes.get(key);
                        if (thisClazz.isAssignableFrom(toClass))
                        {
                            converter = toMap.get(key);
                            break;
                        }
                    }
                }
            }
        }

        return converter;
    }

    /**
     * Converts the from Object to the to Class. If no {@link IConverter} can be
     * found for the given combination, then
     * {@link ConverterRegistry#defaultConvert(Object, Class)} is called.
     * 
     * @param <T>
     * @param from
     * @param toClass
     * @return the converted Object, or null
     */
    public <S extends T> S convert(Object from, Class<? extends S> toClass,
            Map hints) throws SerializationException
    {
        IConverter<Object, Object> bestConverter = (IConverter<Object, Object>) getBestConverter(
                from, toClass);
        if (bestConverter == null)
            return (S) defaultConvert(from, toClass, hints);
        return (S) bestConverter.convert(from, hints);
    }

    public <S extends T> S convert(Object from, Class<? extends S> toClass)
            throws SerializationException
    {
        return convert(from, toClass, null);
    }

    public T convert(F from, Map hints) throws SerializationException
    {
        Class c = ReflectionUtils.getGenericTypeClassesList(IConverter.class,
                this).get(1).getTypeClass();
        return (T) convert(from, c, hints);
    }

    public T convert(F from) throws SerializationException
    {
        return convert(from, (Map) null);
    }

    /**
     * Override this method to provide your own default output serialization.
     * 
     * This, combined with registering Converters is how you can customize the
     * serialization.
     * 
     * @param from
     * @param toClass
     * @return
     */
    public abstract Object defaultConvert(Object from,
            Class<? extends Object> toClass, Map hints)
            throws SerializationException;

}
