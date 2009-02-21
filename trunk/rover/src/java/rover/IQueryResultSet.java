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

    IQueryResultSet selectRelated(int depth) throws Exception;

    List<Map<String, ? extends Object>> list() throws Exception;

    List<Map<String, ? extends Object>> list(int limit) throws Exception;

    List<Map<String, ? extends Object>> list(int offset, int limit)
            throws Exception;
}
