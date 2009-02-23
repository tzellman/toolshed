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

import galoot.node.PEntity;

import java.util.Collection;
import java.util.LinkedList;

public class Macro
{
    protected String name;

    protected Collection<String> arguments;

    protected Collection<PEntity> entities;

    public Macro(String name, Collection<String> arguments,
            Collection<PEntity> entities)
    {
        this.name = name;
        this.arguments = new LinkedList<String>(arguments);
        this.entities = new LinkedList<PEntity>(entities);
    }

    public Iterable<String> getArguments()
    {
        return arguments;
    }

    public Iterable<PEntity> getEntities()
    {
        return entities;
    }

    public String getName()
    {
        return name;
    }

    public int getNumArguments()
    {
        return arguments.size();
    }

}
