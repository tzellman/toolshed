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

public class Pair<I1, I2>
{
    private I1 first;

    private I2 second;

    public Pair(I1 first, I2 second)
    {
        this.first = first;
        this.second = second;
    }

    public I1 getFirst()
    {
        return first;
    }

    public I2 getSecond()
    {
        return second;
    }

}
