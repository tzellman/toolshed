/* =============================================================================
 * This file is part of Rover
 * =============================================================================
 * (C) Copyright 2009, Tom Zellman, tzellman@gmail.com
 *
 * Rover is free software; you can redistribute it and/or
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
package rover;

/**
 * The context of a Query, which is provided as input to a Query operation. This
 * could be a reusable object used for several queries.
 * 
 * @author tzellman
 */
public abstract class QueryContext implements IConnectionProvider
{
    protected SQLTypeConverterRegistry registry;

    protected DatabaseInfoCache cache;

    public QueryContext()
    {
        this(null, null);
    }

    public QueryContext(DatabaseInfoCache cache,
            SQLTypeConverterRegistry registry)
    {
        if (cache == null)
            cache = new DatabaseInfoCache();
        if (registry == null)
            registry = new SQLTypeConverterRegistry();

        this.cache = cache;
        this.registry = registry;
    }

    public SQLTypeConverterRegistry getRegistry()
    {
        return registry;
    }

    public DatabaseInfoCache getCache()
    {
        return cache;
    }

}
