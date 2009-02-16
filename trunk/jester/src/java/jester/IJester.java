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
import java.util.Map;

/**
 * The IJester interface provides a way to serialize an Object to an
 * OutputStream, and to serialize an InputStream back to an Object.
 * 
 * @author tzellman
 * 
 * @param <T>
 */
public interface IJester<T extends Object>
{
    /**
     * Serializes an object to an OutputStream
     * 
     * @param object
     *            the object to be serialized
     * @param out
     *            the OutputStream to serialize to
     * @param hints
     *            an optional Map of serialization hints
     * @return
     * @throws Exception
     */
    void out(T object, OutputStream out, Map hints) throws Exception;

    /**
     * Serializes an InputStream to an object
     * 
     * @param stream
     *            the InputStream to read from
     * @param hints
     *            optional Map of serialization hints
     * @return
     * @throws Exception
     */
    T in(InputStream stream, Map hints) throws Exception;

    /**
     * @return the content type supported by this serializer
     */
    String getContentType();

}
