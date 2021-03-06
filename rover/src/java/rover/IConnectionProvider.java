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

import java.sql.Connection;

/**
 * All implementing Classes must provide a way to get a new Connection.
 * 
 * @author tzellman
 */
public interface IConnectionProvider
{
    /**
     * Returns a new {@link Connection}. The caller is in charge of calling
     * {@link Connection#close()} when finished.
     * 
     * @return
     * @throws Exception
     */
    Connection newConnection() throws Exception;
}
