/*
 * =============================================================================
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package galoot;

public interface Filter
{
    /**
     * Filter an object by applying some type of processing to it. The input
     * object can be anything, so good implementations should only handle known
     * types for the filter.
     * 
     * If the input object does not apply to the operation at hand, you can
     * return null. You could also return the original object.
     * 
     * @param object
     *            the input object
     * @param args
     *            optional arguments, pertinent to the operation
     * @return the resultant object
     */
    public Object filter(Object object, ContextStack contextStack,
                         String... args);

    /**
     * Return the name of this filter. Filter names are case-sensitive.
     * 
     * @return the name of the filter
     */
    public String getName();
}
