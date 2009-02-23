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

public class TextFragment implements DocumentFragment
{
    private StringBuffer contents;

    public TextFragment()
    {
        contents = new StringBuffer();
    }

    /**
     * Returns the contents, as a string.
     */
    public Object getContents()
    {
        return contents.toString();
    }

    public void addContent(Object content) throws IllegalArgumentException
    {
        if (content == null)
            return;

        if (content instanceof StringBuffer)
            contents.append((StringBuffer) content);
        else if (content instanceof String)
            contents.append((String) content);
        else if (content instanceof TextFragment)
            contents.append((String) ((TextFragment) content).getContents());
        else
            throw new IllegalArgumentException("Cannot add content of type: "
                    + content.getClass().getName());
    }
}
