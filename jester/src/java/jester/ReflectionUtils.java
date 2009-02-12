package jester;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.ObjectUtils;

/**
 * Reusable utilities for dealing with Reflection. For now, this only contains
 * utilities for getting Generic type info.
 * 
 */
public final class ReflectionUtils
{

    /**
     * Returns the Class of the given Type. If it is a variable, null is
     * returned.
     * 
     * @param type
     * @return the Class of the given Type
     */
    public static Class getTypeClass(Type type)
    {
        if (type instanceof Class)
        {
            // raw type
            return (Class) type;
        }
        else if (type instanceof ParameterizedType)
        {
            // parameterized type
            return getTypeClass(((ParameterizedType) type).getRawType());
        }
        else if (type instanceof GenericArrayType)
        {
            // array type
            Type t = ((GenericArrayType) type).getGenericComponentType();
            Class componentClass = getTypeClass(t);
            if (componentClass != null)
                return Array.newInstance(componentClass, 0).getClass();
            else
                return null;
        }
        else
        {
            // variable type - what about WildcardType??
            return null;
        }
    }

    /**
     * Holder for a Generic Type name/class
     */
    public static class GenericTypeInfo
    {
        protected String typeName;

        protected Class typeClass;

        public String getTypeName()
        {
            return typeName;
        }

        public void setTypeName(String typeName)
        {
            this.typeName = typeName;
        }

        public Class getTypeClass()
        {
            return typeClass;
        }

        public void setTypeClass(Class typeClass)
        {
            this.typeClass = typeClass;
        }
    }

    /**
     * Returns a mapping of the Classes used as the types for a Class that
     * supports generics. If no type was specified, it will show up as having a
     * null Class.
     * 
     * This is useful if you want to figure out what Class was used for a
     * generic type, for example if looping over a collection of generic items,
     * you can figure out what types were actually used for each.
     * 
     * @param base
     *            the base Class the instance inherits from
     * @param instance
     *            the Object you want to find information about
     * @return a Map<String, Class> of genericTypeName --> actualUsedClass
     */
    public static List<GenericTypeInfo> getGenericTypeClasses(Class base,
            Object instance)
    {
        List<GenericTypeInfo> genericTypeInfo = new LinkedList<GenericTypeInfo>();
        Map<String, Type> actualTypeMap = new HashMap<String, Type>();
        List<Type> vars = new Vector<Type>();

        Type current = instance.getClass();
        // drive up the hierarchy stack...
        do
        {
            if (current instanceof Class)
            {
                if (ObjectUtils.equals(current, base))
                    break;
                
                // get the parent
                current = ((Class) current).getGenericSuperclass();
            }

            if (current instanceof ParameterizedType)
            {
                ParameterizedType paramType = (ParameterizedType) current;
                Class paramRawType = (Class) paramType.getRawType();

                TypeVariable[] typeVars = paramRawType.getTypeParameters();
                Type[] actualTypes = paramType.getActualTypeArguments();

                if (typeVars.length == actualTypes.length)
                {
                    for (int i = 0, size = typeVars.length; i < size; ++i)
                    {
                        // don't tromp an existing one from higher up
                        if (!actualTypeMap.containsKey(typeVars[i].getName()))
                        {
                            actualTypeMap.put(typeVars[i].getName(),
                                    actualTypes[i]);
                        }
                    }
                }

                // update, if we aren't there yet
                if (!ObjectUtils.equals(paramRawType, base))
                {
                    if (!base.isInterface())
                        current = paramRawType.getGenericSuperclass();
                    else
                        current = base;
                }
            }

        } while (!ObjectUtils.equals(getTypeClass(current), base));

        // get the type variables of the current Type and fill our Map
        if (current instanceof Class)
            vars.addAll(Arrays.asList(((Class) current).getTypeParameters()));
        else if (current instanceof ParameterizedType)
        {
            ParameterizedType paramType = (ParameterizedType) current;
            Class paramRawType = (Class) paramType.getRawType();
            vars.addAll(Arrays.asList(paramRawType.getTypeParameters()));
        }

        for (Type var : vars)
        {
            TypeVariable typeVar = null;
            while (var != null && var instanceof TypeVariable)
            {
                typeVar = (TypeVariable) var;
                String typeVarName = typeVar.getName();
                if (actualTypeMap.containsKey(typeVarName))
                    var = actualTypeMap.get(typeVarName);
                else
                    var = null;
            }
            if (typeVar != null && var != null)
            {
                GenericTypeInfo info = new GenericTypeInfo();
                info.setTypeName(typeVar.getName());
                info.setTypeClass(getTypeClass(var));
                genericTypeInfo.add(info);
            }
        }

        return genericTypeInfo;
    }

    private ReflectionUtils()
    {
    }
}
