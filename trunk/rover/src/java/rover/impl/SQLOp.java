package rover.impl;

import org.apache.commons.lang.StringUtils;

/**
 * Supported SQL Operations
 * 
 * @author tzellman
 */
public enum SQLOp {

    EXACT("="), GT(">"), GTE(">="), LT("<"), LTE("<="), ISNULL("IS NULL", true), NOTNULL(
            "IS NOT NULL", true), NOT("!=");

    private String operator;

    private boolean unary;

    SQLOp(String operator)
    {
        this(operator, false);
    }

    SQLOp(String operator, boolean unary)
    {
        this.operator = operator;
        this.unary = unary;
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
     * @return true if the operator is unary
     */
    public boolean isUnary()
    {
        return unary;
    }
}
