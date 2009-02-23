/* =============================================================================
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package galoot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

public final class DefaultFilters
{

    private static DefaultFilters instance = null;

    private Map<String, Filter> filterMap;

    protected DefaultFilters()
    {
        filterMap = new HashMap<String, Filter>();

        Class<?>[] declaredClasses = DefaultFilters.class.getDeclaredClasses();
        for (int i = 0; i < declaredClasses.length; i++)
        {
            Class<?> class1 = declaredClasses[i];
            if (Filter.class.isAssignableFrom(class1) && !class1.isLocalClass())
            {
                Class<Filter> filterClass = (Class<Filter>) class1;
                try
                {
                    Filter filter = filterClass.newInstance();
                    filterMap.put(filter.getClass().getSimpleName()
                            .toLowerCase(), filter);
                }
                catch (Exception e)
                {
                    System.out.println(ExceptionUtils.getStackTrace(e));
                }
            }
        }
    }

    public static DefaultFilters getInstance()
    {
        if (instance == null)
        {
            synchronized (DefaultFilters.class)
            {
                if (instance == null)
                {
                    instance = new DefaultFilters();
                }
            }
        }
        return instance;
    }

    public Filter getFilter(String name)
    {
        return filterMap.containsKey(name) ? filterMap.get(name) : null;
    }

    public Iterable<String> getFilterNames()
    {
        return filterMap.keySet();
    }

    public boolean hasFilter(String name)
    {
        return filterMap.containsKey(name);
    }

    static class Lower extends AbstractFilter
    {
        public Object filter(Object object, String args)
        {
            if (object instanceof String)
                return ((String) object).toLowerCase();
            return object;
        }
    }

    static class Upper extends AbstractFilter
    {
        public Object filter(Object object, String args)
        {
            if (object instanceof String)
                return ((String) object).toUpperCase();
            return object;
        }
    }

    static class Length extends AbstractFilter
    {
        public Object filter(Object object, String args)
        {
            return TemplateUtils.getObjectLength(object);
        }
    }

    /**
     * Returns a random item from a list or string.
     * 
     */
    static class Random extends AbstractFilter
    {
        public Object filter(Object object, String args)
        {
            java.util.Random rand = new java.util.Random();
            if (object instanceof List)
            {
                List listObj = (List) object;
                if (listObj.isEmpty())
                    return null;
                int index = rand.nextInt(listObj.size());
                return listObj.get(index);
            }
            else if (TemplateUtils.isArrayType(object))
            {
                int size = TemplateUtils.getObjectLength(object);
                if (size > 0)
                {
                    int index = rand.nextInt(size);
                    if (object instanceof Object[])
                        return ((Object[]) object)[index];
                    if (object instanceof int[])
                        return ((int[]) object)[index];
                    if (object instanceof long[])
                        return ((long[]) object)[index];
                    if (object instanceof float[])
                        return ((float[]) object)[index];
                    if (object instanceof double[])
                        return ((double[]) object)[index];
                    if (object instanceof boolean[])
                        return ((boolean[]) object)[index];
                    if (object instanceof byte[])
                        return ((byte[]) object)[index];
                    if (object instanceof short[])
                        return ((short[]) object)[index];
                    if (object instanceof char[])
                        return ((char[]) object)[index];
                }
            }
            else if (object instanceof String)
            {
                String stringObj = (String) object;
                if (StringUtils.isEmpty(stringObj))
                    return null;
                int index = rand.nextInt(stringObj.length());
                return String.valueOf(stringObj.charAt(index));
            }
            // return null because random doesn't apply to anything else
            return null;
        }
    }

    /**
     * make_list
     * 
     * Returns the value turned into a list. For an integer, it’s a list of
     * digits. For a string, it’s a list of characters.
     * 
     */
    static class Make_List extends AbstractFilter
    {
        public Object filter(Object object, String args)
        {
            if (object instanceof List)
                return object;

            if (TemplateUtils.isArrayType(object))
                return TemplateUtils.objectToCollection(object);

            // convert some numbers to Strings
            if (object instanceof Number)
                object = String.valueOf(((Number) object));

            if (object instanceof String)
            {
                String stringObj = (String) object;
                Character[] arr = new Character[stringObj.length()];
                for (int i = 0, size = stringObj.length(); i < size; ++i)
                    arr[i] = stringObj.charAt(i);

                List<Character> charList = Arrays.asList(arr);
                return charList;
            }

            if (object instanceof Iterable)
            {
                LinkedList list = new LinkedList();
                Iterable iterable = (Iterable) object;
                for (Object obj : iterable)
                    list.add(obj);
                return list;
            }

            // otherwise, we can't listify it
            return null;
        }
    }

    /**
     * If value is unavailable, use given default.
     */
    static class Default extends AbstractFilter
    {
        public Object filter(Object object, String args)
        {
            return object != null ? object : args;
        }
    }

    /**
     * Given a string mapping values for true, false and (optionally) null,
     * returns one of those strings according to the value.
     * 
     */
    static class YesNo extends AbstractFilter
    {
        public Object filter(Object object, String args)
        {
            if (args == null)
                return null;
            String[] split = args.split(",");
            boolean boolVal = TemplateUtils.evaluateAsBoolean(object);

            if (split.length == 3 && object == null)
                return split[2];
            else if (split.length == 2 || split.length == 3)
                return boolVal ? split[0] : split[1];
            else
                return null;
        }
    }

    /**
     * Returns a boolean of whether the value’s length is the argument.
     * 
     */
    static class Length_Is extends AbstractFilter
    {
        public Object filter(Object object, String args)
        {
            // just use the Length filter to get the length
            Object lenObj = new Length().filter(object, null);
            if (lenObj != null && args != null && lenObj instanceof Number)
            {
                try
                {
                    int lengthIs = Integer.parseInt(args);
                    Number len = ((Number) lenObj);
                    return len.intValue() == lengthIs;
                }
                catch (NumberFormatException e)
                {
                    return false;
                }
            }
            return false;
        }
    }

    static class WordCount extends AbstractFilter
    {
        public Object filter(Object object, String args)
        {
            if (object instanceof String)
                return ((String) object).split("\\s+").length;
            return null;
        }
    }

    static class Title extends AbstractFilter
    {
        public Object filter(Object object, String args)
        {
            if (object instanceof String)
            {
                String stringObj = (String) object;
                String titled = "";

                Pattern pattern = Pattern.compile(("((^\\w)|(\\s+\\w))"));
                Matcher matcher = pattern.matcher(stringObj);
                int lastEnd = -1;
                while (matcher.find())
                {
                    if (lastEnd >= 0)
                        titled += stringObj.substring(lastEnd, matcher.start());
                    titled += matcher.group().toUpperCase();
                    lastEnd = matcher.end();
                }
                if (lastEnd < stringObj.length() && lastEnd >= 0)
                    titled += stringObj.substring(lastEnd);
                return titled;
            }
            return null;
        }
    }

}
