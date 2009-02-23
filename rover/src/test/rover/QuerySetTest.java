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

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

import jester.IConverter;
import jester.OGNLConverter;
import jester.json.JSONSerializer;
import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaBeanMapDecorator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.exception.ExceptionUtils;

import rover.impl.DatabaseInfoCache;
import rover.impl.QueryResultSet;

/**
 * @author tzellman
 */
public class QuerySetTest extends TestCase
{
    // protected Connection connection;

    protected File tempFile;

    protected void execute(String sql, Connection connection) throws Exception
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

        Connection connection = DriverManager.getConnection(
                "jdbc:hsqldb:mem:test", "sa", "");

        URL resource = getClass().getResource("test.sql");
        tempFile = File.createTempFile("test", "file.sql");
        FileUtils.copyURLToFile(resource, tempFile);

        String createSQL = FileUtils.readFileToString(tempFile);

        execute(createSQL, connection);

        connection.commit();
    }

    @Override
    protected void tearDown() throws Exception
    {
        // connection.close();
        if (tempFile != null)
            tempFile.delete();
    }

    class HSQLDBQueryContext implements IQueryContext
    {
        IConnectionProvider connectionProvider;

        DatabaseInfoCache cache;

        SQLTypeConverter converter;

        public HSQLDBQueryContext() throws Exception
        {
            connectionProvider = new IConnectionProvider()
            {
                public Connection newConnection() throws Exception
                {
                    return DriverManager.getConnection("jdbc:hsqldb:mem:test",
                            "sa", "");
                }
            };
            cache = new DatabaseInfoCache(connectionProvider);
            converter = new SQLTypeConverter();
        }

        public IConnectionProvider getConnectionProvider()
        {
            return connectionProvider;
        }

        public IDatabaseInfo getDatabaseInfo()
        {
            return cache;
        }

        public SQLTypeConverter getSQLTypeConverter()
        {
            return converter;
        }
    }

    public void testIt()
    {
        final JSONSerializer serializer = new JSONSerializer();
        // add a converter that can handle DynaBeans
        serializer.register(new IConverter<DynaBean, String>()
        {
            public String convert(DynaBean from, Map hints)
                    throws SerializationException
            {
                return serializer.convert(new DynaBeanMapDecorator(from),
                        String.class);
            }
        });

        try
        {
            IQueryContext context = new HSQLDBQueryContext();
            IQueryResultSet q = new QueryResultSet("requirement", context);

            IQueryResultSet filter = q.filter("release__name=1.0");
            System.out.println(filter);

            List<Map<String, ? extends Object>> results = filter.list();
            System.out.println(results.size());

            System.out.println(filter.count());

            results = filter.selectRelated(2).list();

            for (Object result : results)
            {
                Object name = OGNLConverter.evaluate(result,
                        "requirement.release.name".split("[.]"));
                Object project = OGNLConverter.evaluate(result,
                        "requirement.release.project".split("[.]"));
                System.out.println(name);
                System.out.println(project);
            }

            // or, this is easier...
            System.out.println(serializer.convert(results));

            q = new QueryResultSet("release", context);
            filter = q.filter("name=1.0");
            System.out.println(filter.count());

            q = new QueryResultSet("project", context).orderBy("name");
            Object obj = q.list(1).get(0);
            System.out.println(OGNLConverter.evaluateExpression(obj,
                    "project.name"));

            q = new QueryResultSet("project", context).orderBy("-name");
            obj = q.list(1).get(0);
            System.out.println(OGNLConverter.evaluateExpression(obj,
                    "project.name"));

            System.out.println(serializer.convert(q.list(1)));

        }
        catch (Exception e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }
}
