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
public abstract class ConverterRegistry
{

    // Mapping of IConverters
    protected Map<String, Map<String, IConverter<? extends Object, ? extends Object>>> converters;

    protected Map<String, Class<? extends Object>> classes;

    public ConverterRegistry()
    {
        converters = new TreeMap<String, Map<String, IConverter<? extends Object, ? extends Object>>>();
        classes = new HashMap<String, Class<? extends Object>>();
    }

    /**
     * Registers a converter
     * 
     * @param converter
     */
    public void register(
            IConverter<? extends Object, ? extends Object> converter)
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
            converters
                    .put(
                            fromName,
                            new TreeMap<String, IConverter<? extends Object, ? extends Object>>());
        }
        converters.get(fromName).put(toName, converter);
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
    protected IConverter<? extends Object, ? extends Object> getBestConverter(
            Object from, Class<? extends Object> toClass)
    {
        if (from == null)
            return null;

        Class<? extends Object> clazz = from.getClass();
        String clazzName = clazz.getName();

        IConverter<? extends Object, ? extends Object> converter = null;

        Map<String, IConverter<? extends Object, ? extends Object>> toMap = null;
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
    public <T extends Object> T convert(Object from,
            Class<? extends T> toClass, Map hints)
            throws SerializationException
    {
        IConverter<Object, Object> bestConverter = (IConverter<Object, Object>) getBestConverter(
                from, toClass);
        if (bestConverter == null)
            return (T) defaultConvert(from, toClass, hints);
        return (T) bestConverter.convert(from, hints);
    }

    public <T extends Object> T convert(Object from, Class<? extends T> toClass)
            throws SerializationException
    {
        return convert(from, toClass, null);
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
