package rover;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

import jester.OGNLConverter;
import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import rover.impl.QueryResultSet;

/**
 * @author tzellman
 */
public class QuerySetTest extends TestCase
{
    protected Connection connection;

    protected File tempFile;

    protected void execute(String sql) throws Exception
    {
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.execute();
        stmt.close();
    }

    @Override
    protected void setUp() throws Exception
    {
        try
        {
            Class.forName("org.hsqldb.jdbcDriver");
        }
        catch (Exception e)
        {
            fail("ERROR: failed to load HSQLDB JDBC driver.");
        }

        connection = DriverManager.getConnection("jdbc:hsqldb:mem:test", "sa",
                "");

        URL resource = getClass().getResource("test.sql");
        tempFile = File.createTempFile("test", "file.sql");
        FileUtils.copyURLToFile(resource, tempFile);

        String createSQL = FileUtils.readFileToString(tempFile);

        execute(createSQL);

        connection.commit();
    }

    @Override
    protected void tearDown() throws Exception
    {
        connection.close();
        if (tempFile != null)
            tempFile.delete();
    }

    class HSQLDBQueryContext extends QueryContext
    {
        public Connection getConnection() throws Exception
        {
            return connection;
        }
    }

    public void testIt()
    {
        QueryContext context = new HSQLDBQueryContext();

        try
        {
            IQueryResultSet q = new QueryResultSet("requirement", context);

            IQueryResultSet filter = q.filter("release__name=1.0");
            System.out.println(filter);

            List<Map<String, ? extends Object>> results = filter.list();
            System.out.println(results.size());

            System.out.println(filter.count());

            results = filter.selectRelated(1).list();

            for (Object result : results)
            {
                Object name = OGNLConverter.evaluate(result,
                        "requirement.release.name".split("[.]"));
                Object project = OGNLConverter.evaluate(result,
                        "requirement.release.project".split("[.]"));
                System.out.println(name);
                System.out.println(project);
            }

        }
        catch (Exception e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

}
