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
package rover.impl;

import org.apache.commons.lang.StringUtils;

/**
 * Supported SQL Operations
 * 
 * @author tzellman
 */
public enum SQLOp {

    EXACT("="), IEXACT("ILIKE"), GT(">"), GTE(">="), LT("<"), LTE("<="), ISNULL(
            "IS NULL", true), NOTNULL("IS NOT NULL", true), NOT("!="), CONTAINS(
            "LIKE", "%"), ICONTAINS("ILIKE", "%");

    private String operator;

    private boolean unary;

    private String valueWrap = null;

    SQLOp(String operator)
    {
        this(operator, false);
    }

    SQLOp(String operator, boolean unary)
    {
        this.operator = operator;
        this.unary = unary;
    }

    SQLOp(String operator, String valueWrap)
    {
        this.operator = operator;
        this.valueWrap = valueWrap;
    }

    @Override
    public String toString()
    {
        return super.toString().toLowerCase();
    }

    /**
     * Returns a SQLOp based on a String name
     * 
     * @param opString
     *            name of an op (e.g. gte)
     * @return
     */
    public static SQLOp getOp(String opString)
    {
        SQLOp[] ops = SQLOp.values();
        for (SQLOp op : ops)
        {
            if (StringUtils.equalsIgnoreCase(op.toString(), opString))
                return op;
        }
        return null;
    }

    /**
     * @return the operator used in the SQL statement
     */
    public String getOperator()
    {
        return operator;
    }

    /**
     * If the value should be wrapped (by say, %), this is what to wrap with.
     * 
     * @return
     */
    public String getValueWrap()
    {
        return valueWrap;
    }

    /**
     * @return true if the operator is unary
     */
    public boolean isUnary()
    {
        return unary;
    }
}
