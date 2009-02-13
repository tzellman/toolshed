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
import java.util.TreeMap;
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
     */
    public static class GenericTypeClassesRetriever
    {
        protected Class baseClass;

        /**
         * 
         * @param baseClass
         *            the Class to use as the base
         */
        public GenericTypeClassesRetriever(Class baseClass)
        {
            this.baseClass = baseClass;
        }

        /**
         * 
         * @param actualTypeMap
         * @param type
         * @return the raw Type of the input Type
         */
        protected Class mapArguments(Map<String, Type> actualTypeMap,
                ParameterizedType type)
        {
            Class rawType = (Class) type.getRawType();

            TypeVariable[] typeVars = rawType.getTypeParameters();
            Type[] actualTypes = type.getActualTypeArguments();

            if (typeVars.length == actualTypes.length)
            {
                for (int i = 0, size = typeVars.length; i < size; ++i)
                {
                    // don't tromp an existing one from higher up
                    if (!actualTypeMap.containsKey(typeVars[i].getName()))
                    {
                        actualTypeMap
                                .put(typeVars[i].getName(), actualTypes[i]);
                    }
                }
            }
            return rawType;
        }

        protected void mapGenericInterfaces(Map<String, Type> actualTypeMap,
                Class clazz)
        {
            Type[] genericInterfaces = clazz.getGenericInterfaces();

            for (Type type : genericInterfaces)
            {
                if (type instanceof ParameterizedType)
                {
                    mapArguments(actualTypeMap, (ParameterizedType) type);
                }
            }
        }

        /**
         * Returns a List of {@link GenericTypeInfo}
         * 
         * @param instance
         * @return
         */
        public List<GenericTypeInfo> getGenericTypeClassesList(Object instance)
        {
            List<GenericTypeInfo> genericTypeInfo = new LinkedList<GenericTypeInfo>();
            Map<String, Type> actualTypeMap = new HashMap<String, Type>();
            List<Type> vars = new Vector<Type>();

            Type current = instance.getClass();

            if (!baseClass.isAssignableFrom((Class) current))
                return genericTypeInfo;

            // drive up the hierarchy stack...
            do
            {
                if (current instanceof Class)
                {
                    // get the parent
                    Type parent = ((Class) current).getGenericSuperclass();

                    if (ObjectUtils.equals(current, baseClass)
                            || ObjectUtils.equals(parent, Object.class))
                    {
                        mapGenericInterfaces(actualTypeMap, (Class) current);
                        current = baseClass;
                    }
                    else
                        current = parent;
                }

                if (current instanceof ParameterizedType
                        && !ObjectUtils.equals(current, baseClass))
                {
                    Class rawType = mapArguments(actualTypeMap,
                            (ParameterizedType) current);

                    // update, if we aren't there yet
                    if (!ObjectUtils.equals(rawType, baseClass))
                    {
                        if (!baseClass.isInterface())
                            current = rawType.getGenericSuperclass();
                        else
                        {
                            current = rawType;
                            if (current instanceof Class)
                            {
                                mapGenericInterfaces(actualTypeMap,
                                        (Class) current);
                            }
                        }
                    }
                }

            } while (!ObjectUtils.equals(getTypeClass(current), baseClass));

            // get the type variables of the current Type and fill our Map
            if (current instanceof Class)
                vars.addAll(Arrays
                        .asList(((Class) current).getTypeParameters()));
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

        /**
         * Returns a Map of {@link GenericTypeInfo}
         * 
         * @param instance
         * @return
         */
        public Map<String, GenericTypeInfo> getGenericTypeClassesMap(
                Object instance)
        {
            Map<String, GenericTypeInfo> map = new TreeMap<String, GenericTypeInfo>();
            List<GenericTypeInfo> list = getGenericTypeClassesList(instance);
            for (GenericTypeInfo genericTypeInfo : list)
            {
                map.put(genericTypeInfo.getTypeName(), genericTypeInfo);
            }
            return map;
        }
    }

    /**
     * @see GenericTypeClassesRetriever
     */
    public static List<GenericTypeInfo> getGenericTypeClassesList(Class base,
            Object instance)
    {
        return new GenericTypeClassesRetriever(base)
                .getGenericTypeClassesList(instance);
    }

    /**
     * @see GenericTypeClassesRetriever
     */
    public static Map<String, GenericTypeInfo> getGenericTypeClassesMap(
            Class base, Object instance)
    {
        return new GenericTypeClassesRetriever(base)
                .getGenericTypeClassesMap(instance);
    }

    private ReflectionUtils()
    {
    }
}
