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

/**
 * Interface outlining a query resultset.
 * 
 * @author tzellman
 */
public interface IQueryResultSet
{
    /**
     * Filter the result set by passing in an array of clauses. This is
     * implementation dependent.
     * 
     * @param clauses
     * @return
     * @throws Exception
     */
    IQueryResultSet filter(String... clauses) throws Exception;

    /**
     * Returns the row count of the current SQL expression.
     * 
     * @return
     * @throws Exception
     */
    int count() throws Exception;

    /**
     * Makes the result set distinct.
     * 
     * @return
     * @throws Exception
     */
    IQueryResultSet distinct() throws Exception;

    /**
     * Orders the results by the given fields/expressions.
     * 
     * @param fields
     * @return
     * @throws Exception
     */
    IQueryResultSet orderBy(String... fields) throws Exception;

    /**
     * Allows you to specify field names to which the SELECT should be limited.
     * If no fields are provided, this has no effect.
     * 
     * @param fields
     *            fields to with the SELECT should be limited to
     * @return
     * @throws Exception
     */
    IQueryResultSet values(String... fields) throws Exception;

    /**
     * Selects related objects up to the given depth level.
     * 
     * @param depth
     * @return
     * @throws Exception
     */
    IQueryResultSet selectRelated(int depth) throws Exception;

    /**
     * Executes the query and returns a List of Maps. Each Map contains a named
     * objects, according to what was requested.
     * 
     * @param offset
     *            the result set offset
     * @param limit
     *            the limit of the # of results fetched
     * @return
     * @throws Exception
     */
    List<Object> list(int offset, int limit) throws Exception;

    /**
     * @see IQueryResultSet#list(int, int)
     * @return
     * @throws Exception
     */
    List<Object> list() throws Exception;

    /**
     * @see IQueryResultSet#list(int, int)
     * @param limit
     * @return
     * @throws Exception
     */
    List<Object> list(int limit) throws Exception;

}
