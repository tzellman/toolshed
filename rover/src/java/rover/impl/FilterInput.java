package rover.impl;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import rover.QueryConstants;

public final class FilterInput
{
    protected Deque<String> fields;

    protected SQLOp op;

    protected String value;

    public static List<FilterInput> transformFilters(String... clauses)
            throws Exception
    {
        List<FilterInput> filterInputs = new LinkedList<FilterInput>();
        for (String clause : clauses)
        {
            String[] parts = StringUtils.split(clause, "=", 2);
            String fieldString = null, valueString = null;
            fieldString = parts[0];
            if (parts.length == 2)
                valueString = parts[1];

            String[] fields = StringUtils.splitByWholeSeparator(fieldString,
                    QueryConstants.QUERY_SEP);
            Deque<String> fieldList = new LinkedList<String>();

            // filter out empty fields
            for (String f : fields)
            {
                if (StringUtils.isEmpty(f))
                    throw new Exception("Invalid field expression");
                fieldList.add(f.toUpperCase());
            }

            // check to see if they provided a SQL op
            SQLOp op = SQLOp.EXACT;
            int numFields = fieldList.size();
            if (numFields > 1)
            {
                String lastField = fieldList.peekLast();
                op = SQLOp.getOp(lastField);
                if (op != null)
                    fieldList.pollLast();
                else
                    op = SQLOp.EXACT;
            }

            if (valueString == null && !op.isUnary())
                throw new Exception(
                        "Invalid field expression: must have value for binary operation");
            if (valueString != null && op.isUnary())
                throw new Exception(
                        "Invalid field expression: unary operation does not take a value");

            FilterInput filterInput = new FilterInput();
            filterInput.fields = fieldList;
            filterInput.op = op;
            filterInput.value = valueString;
            filterInputs.add(filterInput);
        }
        return filterInputs;
    }

    public static void parseFilters(QueryInput queryInput, String... clauses)
            throws Exception
    {
        List<FilterInput> filterClauses = transformFilters(clauses);
        for (FilterInput filterInput : filterClauses)
        {
            Deque<String> fieldList = filterInput.fields;
            if (!fieldList.isEmpty())
            {
                String lastField = fieldList.pollLast();
                while (!fieldList.isEmpty())
                {
                    String field = fieldList.pop();
                    queryInput.addFK(field);
                }
                queryInput.addWhere(lastField, filterInput.op,
                        filterInput.value);
            }
        }
    }
}
