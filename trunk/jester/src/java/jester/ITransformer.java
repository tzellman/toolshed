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

/**
 * A Transformer knows how to transform to/from an Object
 */
public interface ITransformer<T, F>
{
    /**
     * Transforms the input Object to some type of ouput data. It is assumed
     * that the returned value is formatted in a finalized state, ready for
     * serialization.
     * 
     * @param object
     *            the Object to transform
     * @param hints
     *            optional Map of serialization hints
     * @return
     * @throws Exception
     */
    T to(F object, Map hints) throws Exception;

    /**
     * Returns an Object, transformed from the input object.
     * 
     * @param data
     *            the data being serialized to the new Object
     * @param hints
     *            optional Map of serialization hints
     * @return
     * @throws Exception
     */
    F from(T object, Map hints) throws Exception;
}
