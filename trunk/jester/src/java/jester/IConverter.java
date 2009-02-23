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

import java.util.Map;

import org.apache.commons.lang.SerializationException;

/**
 * Converts one type to another.
 * 
 * @author tzellman
 * 
 * @param <F>
 *            the from/input object type
 * @param <T>
 *            the to/output object type
 */
public interface IConverter<F, T>
{
    /**
     * Convert the from F object to a T object.
     * 
     * @param from
     *            the input object to convert from
     * @param hints
     *            (optional) Map of hints to be used during the conversion
     *            process
     * @return a T object
     * @throws SerializationException
     */
    T convert(F from, Map hints) throws SerializationException;
}
