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
package galoot.types;

/**
 * A Document Fragment is constituent of a document. Document fragments have
 * "content", which is user defined. The content can be anything, and can also
 * be restricted.
 */
public interface DocumentFragment
{
    /**
     * Returns the contents of the fragment.
     * 
     * @return
     */
    public Object getContents();

    /**
     * Add the given contents to the fragment. This throws an
     * {@link IllegalArgumentException} if the contents do not apply to this
     * fragment.
     * 
     * @param content
     */
    public void addContent(Object content) throws IllegalArgumentException;
}
