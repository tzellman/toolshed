package galoot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    static class Lower implements Filter
    {
        public Object filter(Object object, String args)
        {
            if (object instanceof String)
                return ((String) object).toLowerCase();
            return object;
        }
    }

    static class Upper implements Filter
    {
        public Object filter(Object object, String args)
        {
            if (object instanceof String)
                return ((String) object).toUpperCase();
            return object;
        }
    }

    static class Length implements Filter
    {
        public Object filter(Object object, String args)
        {
            if (object instanceof List)
                return ((List) object).size();
            else if (object instanceof String)
                return ((String) object).length();
            else if (object instanceof Map)
                return ((Map) object).size();
            // return null because length doesn't apply to anything else
            return null;
        }
    }

    /**
     * Returns a random item from a list or string.
     * 
     */
    static class Random implements Filter
    {
        public Object filter(Object object, String args)
        {
            java.util.Random rand = new java.util.Random(System
                    .currentTimeMillis());
            if (object instanceof List)
            {
                List listObj = (List) object;
                if (listObj.isEmpty())
                    return null;
                int index = rand.nextInt(listObj.size());
                return listObj.get(index);
            }
            else if (object instanceof String)
            {
                String stringObj = (String) object;
                if (stringObj.isEmpty())
                    return null;
                int index = rand.nextInt(stringObj.length());
                return String.valueOf(stringObj.charAt(index));
            }
            // return null because length doesn't apply to anything else
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
    static class Make_List implements Filter
    {
        public Object filter(Object object, String args)
        {
            if (object instanceof List)
                return object;

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
            // otherwise, we can't listify it
            return null;
        }
    }

    /**
     * If value is unavailable, use given default.
     */
    static class Default implements Filter
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
    static class YesNo implements Filter
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
    static class Length_Is implements Filter
    {
        public Object filter(Object object, String args)
        {
            //just use the Length filter to get the length
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

}