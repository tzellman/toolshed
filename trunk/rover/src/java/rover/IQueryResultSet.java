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

import java.util.List;
import java.util.Map;

/**
 * @author tzellman
 */
public interface IQueryResultSet
{
    IQueryResultSet filter(String... clauses) throws Exception;

    int count() throws Exception;

    IQueryResultSet distinct() throws Exception;

    IQueryResultSet orderBy(String... fields) throws Exception;

    IQueryResultSet selectRelated(int depth) throws Exception;

    List<Map<String, ? extends Object>> list() throws Exception;

    List<Map<String, ? extends Object>> list(int limit) throws Exception;

    List<Map<String, ? extends Object>> list(int offset, int limit)
            throws Exception;
}
