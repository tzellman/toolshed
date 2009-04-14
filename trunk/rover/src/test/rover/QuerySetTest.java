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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jester.IConverter;
import jester.OGNLConverter;
import jester.json.JSONDeserializer;
import jester.json.JSONSerializer;
import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaBeanMapDecorator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.exception.ExceptionUtils;

import rover.impl.sql.SQLQueryResultSet;
import rover.impl.sql.SQLStatementConverter;

/**
 * @author tzellman
 */
public class QuerySetTest extends TestCase
{
    // protected Connection connection;

    protected File tempFile;

    protected IQueryContext queryContext;

    protected JSONSerializer serializer;

    protected JSONDeserializer deserializer;

    protected static Boolean firstTime = true;

    protected void execute(String sql, Connection connection) throws Exception
    {
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.execute();
        stmt.close();
    }

    @Override
    protected void setUp() throws Exception
    {
        synchronized (firstTime)
        {
            if (firstTime)
            {
                firstTime = false;
                initDB();
            }
        }

        // create a context that we'll use for all tests
        queryContext = new HSQLDBQueryContext();

        serializer = new JSONSerializer();
        deserializer = new JSONDeserializer();

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
    }

    protected void initDB() throws Exception
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

    public void testCount()
    {
        try
        {
            IQueryResultSet q = new SQLQueryResultSet("release", queryContext);
            q = q.filter("name=1.0");
            System.out.println(q.count());
        }
        catch (Exception e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testRelated()
    {
        try
        {
            IQueryResultSet q = new SQLQueryResultSet("requirement",
                    queryContext);
            q = q.filter("release__name=1.0");
            List results = q.selectRelated(2).list();

            // loop over the objects
            for (Object result : results)
            {
                // use the OGNLConverter to dig down and get some values
                String relName = (String) OGNLConverter.evaluate(result,
                        "requirement.release.name".split("[.]"));
                String projectName = (String) OGNLConverter.evaluate(result,
                        "requirement.release.project.name".split("[.]"));
                System.out.println(String.format(
                        "Release: [%s], Project: [%s]", relName, projectName));
            }

            // or, this is easier...
            System.out.println(serializer.convert(results));
        }
        catch (Exception e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testOrderBy()
    {
        try
        {
            IQueryResultSet q = new SQLQueryResultSet("project", queryContext)
                    .orderBy("name");
            Object obj = q.list(1).get(0);
            System.out.println(OGNLConverter.evaluateExpression(obj,
                    "project.name"));

            q = new SQLQueryResultSet("project", queryContext).orderBy("-name");
            obj = q.list(1).get(0);
            System.out.println(OGNLConverter.evaluateExpression(obj,
                    "project.name"));
        }
        catch (Exception e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testValues()
    {
        try
        {
            IQueryResultSet q = new SQLQueryResultSet("project", queryContext);
            List results = q.values("name").list();

            // should just have the names of all projects
            System.out.println(serializer.convert(results));
        }
        catch (Exception e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testOffsetLimit()
    {
        try
        {
            IQueryResultSet q = new SQLQueryResultSet("release", queryContext);
            q = q.filter("name=1.0");
            System.out.println(q.list(0, 10).size());
            System.out.println(q.list(1, 1).size());
            System.out.println(q.list(2).size());
        }
        catch (Exception e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testContains()
    {
        try
        {
            IQueryResultSet q = new SQLQueryResultSet("requirement",
                    queryContext);
            assertEquals(2, q.filter("descr__icontains=END").count());
            assertEquals(2, q.filter("descr__icontains=end").count());
            assertEquals(2, q.filter("descr__contains=end").count());
            assertEquals(1, q.filter("descr__iexact=BACKend").count());
        }
        catch (Exception e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testSerialization()
    {
        try
        {
            IQueryResultSet q = new SQLQueryResultSet("project", queryContext)
                    .orderBy("name");
            List<Object> objects = q.list();
            System.out.println(OGNLConverter.evaluateExpression(objects,
                    "0.project.name"));

            // Let's get it as JSON
            String json = serializer.convert(objects);
            System.out.println(json);

            // Now, let's turn the JSON back into an object
            // this time, I want to use DynaBeans
            Map hints = new HashMap();
            hints.put(JSONDeserializer.HINT_MAPTYPE,
                    JSONDeserializer.HINT_MAPTYPE_DYNABEAN);
            Object jsonObject = deserializer.convert(json, hints);
            System.out.println(OGNLConverter.evaluateExpression(objects,
                    "0.project.name"));
            System.out.println(jsonObject);
        }
        catch (Exception e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testSQLStatementConverter()
    {
        String json = "{\"project\":{\"summary\":\"threading module\",\"updated\":\"2009-04-13 00:00:00.0\",\"name\":\"mt\"}}";
        Object jsonData = deserializer.convert(json);

        SQLStatementConverter converter = new SQLStatementConverter();
        String sqlStatement1 = converter.convert(jsonData);
        System.out.println(sqlStatement1);

        // now, try by passing a hint of the table name
        json = "{\"summary\":\"threading module\",\"updated\":\"2009-04-13 00:00:00.0\",\"name\":\"mt\"}";
        jsonData = deserializer.convert(json);

        Map hints = new HashMap();
        hints.put(SQLStatementConverter.HINT_TABLE_NAME, "project");
        String sqlStatement2 = converter.convert(jsonData, hints);
        System.out.println(sqlStatement2);
        assertEquals(sqlStatement1, sqlStatement2);

        // test the update functionality
        json = "{\"summary\":\"threading module\",\"updated\":\"2009-04-13 00:00:00.0\",\"name\":\"newName\"}";
        jsonData = deserializer.convert(json);

        hints = new HashMap();
        hints.put(SQLStatementConverter.HINT_TABLE_NAME, "project");
        hints.put(SQLStatementConverter.HINT_UPDATE_WHERE, "name='mt'");
        String sqlStatement3 = converter.convert(jsonData, hints);
        System.out.println(sqlStatement3);
    }

}
