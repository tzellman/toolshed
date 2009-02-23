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

/**
 * AbstractFilter provides the getName() method of the Filter interface. It just
 * uses the lower-cased simpleName of the class as the filter name.
 * 
 * For example:
 * My_Cool_Filter --> my_cool_filter
 * MyFilter --> myfilter
 */
public abstract class AbstractFilter implements Filter
{
    private String name;

    public AbstractFilter()
    {
        name = this.getClass().getSimpleName().toLowerCase();
    }

    public String getName()
    {
        return name;
    }

}
