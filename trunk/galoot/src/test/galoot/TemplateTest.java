/*
 * =============================================================================
 * This file is part of Galoot
 * =============================================================================
 * (C) Copyright 2009, Tom Zellman, tzellman@gmail.com
 * 
 * Galoot is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package galoot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TemplateTest extends TestCase
{
    private static final Log log = LogFactory.getLog(TemplateTest.class);

    protected ContextStack context;

    int[] intArray = { 7, 13, 15, 26, 42 };

    double[] doubleArray = { 0.0, 3.1415, Math.PI, Math.E };

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        context = new ContextStack();

        // add some variables to the context
        List<String> randomStrings = new ArrayList<String>();
        Random random = new Random();
        for (int i = 0, num = random.nextInt(100); i < num; ++i)
        {
            // pick a random char between ascii 32 & 126
            int minChar = 32, maxChar = 126;
            int stringSize = random.nextInt(30);
            StringBuffer buf = new StringBuffer();
            for (int j = 0; j < stringSize; ++j)
            {
                int charId = minChar + random.nextInt(maxChar - minChar);
                buf.append((char) charId);
            }
            randomStrings.add(buf.toString());
        }

        context.putVariable("name", "Tom");
        context.putVariable("number", 42);
        context.putVariable("randomStrings", randomStrings);
        context.putVariable("intArray", intArray);
        context.putVariable("intList", TemplateUtils
                .objectToCollection(intArray));
        context.putVariable("doubleArray", doubleArray);
        context.putVariable("doubleList", TemplateUtils
                .objectToCollection(doubleArray));
    }

    public void testVarExpression()
    {
        try
        {
            Template t = new Template("{{ name }}");
            String output = t.render(context);
            assertEquals(output, "Tom");

            t = new Template("{{ name.length }}");
            output = t.render(context);
            assertEquals(Integer.parseInt(output), "Tom".length());

            t = new Template("{{ intList.size }}");
            output = t.render(context);
            assertEquals(Integer.parseInt(output), intArray.length);

            // shouldn't render any output since the var doesn't exist
            t = new Template("{{ doesnt_exist }}");
            output = t.render(context);
            assertTrue(StringUtils.isEmpty(output));
        }
        catch (IOException e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testIfStatement()
    {
        try
        {
            Template t = new Template("{% if name %}true{% endif %}");
            String output = t.render(context);
            assertEquals(output, "true");

            t = new Template("{% if not name %}false{% endif %}");
            output = t.render(context);
            assertTrue(StringUtils.isEmpty(output));

            t = new Template("{% if bad_var %}false{% endif %}");
            output = t.render(context);
            assertTrue(StringUtils.isEmpty(output));

            t = new Template("{% if name %}true{% else %}false{% endif %}");
            output = t.render(context);
            assertEquals(output, "true");

            t = new Template("{% if not name %}true{% else %}false{% endif %}");
            output = t.render(context);
            assertEquals(output, "false");

            t = new Template("{% if bad_var %}false{% else %}else{% endif %}");
            output = t.render(context);
            assertEquals(output, "else");

            // nested ifs
            t = new Template(
                    "{% if name %}{% if intArray %}true{% endif %}{% endif %}");
            output = t.render(context);
            assertEquals(output, "true");

        }
        catch (IOException e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testForStatement()
    {
        try
        {
            Template t = new Template(
                    "{% for var in intArray %}{{ var }}, {% endfor %}");
            String output = t.render(context);
            String expected = "";
            for (int i = 0; i < intArray.length; i++)
                expected += String.valueOf(intArray[i]) + ", ";
            assertEquals(output, expected);

            // test counter0
            t = new Template(
                    "{% for var in intArray %}{{ forloop.counter0 }}:{{ var }}, {% endfor %}");
            output = t.render(context);
            expected = "";
            for (int i = 0; i < intArray.length; i++)
                expected += String.valueOf(i) + ":"
                        + String.valueOf(intArray[i]) + ", ";
            assertEquals(output, expected);

            // make sure nested loops work
            t = new Template(
                    "{% for var in intArray %}{% for var in intArray %}c{% endfor %}{% endfor %}");
            output = t.render(context);
            expected = "";
            for (int i = 0; i < intArray.length; i++)
                for (int j = 0; j < intArray.length; j++)
                    expected += "c";
            assertEquals(output, expected);

            // make sure the parent shows up in the child loop
            t = new Template(
                    "{% for var in intArray %}{% for var in intArray %}"
                            + "{% if forloop.parent %}c{% endif %}"
                            + "{% endfor %}{% endfor %}");
            output = t.render(context);
            expected = "";
            for (int i = 0; i < intArray.length; i++)
                for (int j = 0; j < intArray.length; j++)
                    expected += "c";
            assertEquals(output, expected);

            // print a matrix of data
            t = new Template(
                    "{% for i in intArray %} == row {{ forloop.counter0 }} ==\n"
                            + "{% for j in intArray %}"
                            + "[{{ forloop.parent.counter0 }}]"
                            + "[{{ forloop.counter0 }}]: {{ i }}, {% endfor %}"
                            + "\n{% endfor %}");
            output = t.render(context);
            log.info("output matrix:\n" + output);
            expected = "";
            for (int i = 0; i < intArray.length; i++)
            {
                expected += " == row " + String.valueOf(i) + " ==\n";
                for (int j = 0; j < intArray.length; j++)
                {
                    expected += "[" + String.valueOf(i) + "]["
                            + String.valueOf(j) + "]: "
                            + String.valueOf(intArray[i]) + ", ";
                }
                expected += "\n";
            }
            log.info("expected matrix:\n" + expected);
            assertEquals(output, expected);
        }
        catch (IOException e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testInclude()
    {
        // includes
        File f = null;
        try
        {
            // bad template
            Template t = new Template("{% include \"garp\" %}");
            String output = t.render(context);
            assertEquals(output, "");

            // existing template
            f = File.createTempFile("tmp", "fake");
            FileUtils.writeStringToFile(f,
                    "{% with name|upper as bar %}{{ bar }}{% endwith %}",
                    "UTF-8");
            log.debug("creating tmp file: " + f.getAbsolutePath());

            // remove path from registry, in case it already was there
            PluginRegistry.getInstance().removeTemplateIncludePath(
                    f.getParent());

            // test that it doesn't work
            t = new Template("{% include \"" + f.getName() + "\" %}");
            output = t.render(context);
            assertTrue(StringUtils.isEmpty(output));

            // add the path to the pluginregistry
            PluginRegistry.getInstance().addTemplateIncludePath(f.getParent());

            // test case where file is specified by name
            t = new Template("{% include \"" + f.getName() + "\" %}");
            output = t.render(context);
            assertEquals("TOM", output);

            // now test case where file is specified by variable
            context.putVariable("mypath", f.getName());
            t = new Template("{% include mypath %}");
            output = t.render(context);
            assertEquals("TOM", output);

            // remove the parent from the global include paths
            PluginRegistry.getInstance().removeTemplateIncludePath(
                    f.getParent());

            FileUtils.forceDelete(f);
        }
        catch (IOException e)
        {
            if (f != null)
            {
                try
                {
                    FileUtils.forceDelete(f);
                }
                catch (IOException e1)
                {
                    // couldn't delete the file
                }
            }
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testMacroStatement()
    {
        try
        {
            String templateText = "{% macro testMacro(zellmo, var) %}{{ zellmo|upper }}{{ var|upper }}{% endmacro %}"
                    + "{{ testMacro(name, name) }}";
            System.out.println(templateText);
            Template t = new Template(templateText);
            String output = t.render(context);
            assertEquals("TOMTOM", output);
            templateText = "{% macro testMacro(zellmo, var) %}{{ zellmo|upper }}{{ var|upper }}{% endmacro %}"
                    + "{{ testMacro(\"tom\", \"z\") }}";
            t = new Template(templateText);
            output = t.render(context);
            assertEquals("TOMZ", output);
        }
        catch (IOException e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testIncludeMacro()
    {
        // includes
        File f = null;
        try
        {
            f = File.createTempFile("tmp", "includeMacro");
            FileUtils
                    .writeStringToFile(
                            f,
                            "{% macro printVar(var) %}\n{{ var|upper }}\n{% endmacro %}",
                            "UTF-8");
            log.debug("creating tmp file: " + f.getAbsolutePath());

            // add the path to the pluginregistry
            PluginRegistry.getInstance().addTemplateIncludePath(f.getParent());

            // test case where file is specified by name
            final String templateText = "{% include \"" + f.getName()
                    + "\" %}\n" + "{{ printVar(name) }}";
            System.out.println(templateText);
            Template t = new Template(templateText);
            String output = t.render(context).trim();
            assertEquals("TOM", output);

            // remove the parent from the global include paths
            PluginRegistry.getInstance().removeTemplateIncludePath(
                    f.getParent());

            FileUtils.forceDelete(f);
        }
        catch (IOException e)
        {
            if (f != null)
            {
                try
                {
                    FileUtils.forceDelete(f);
                }
                catch (IOException e1)
                {
                    // couldn't delete the file
                }
            }
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testWithStatement()
    {
        // nested ifs
        try
        {
            Template t = new Template(
                    "{% with name|upper as bar %}{{ bar }}{% endwith %}");
            String output = t.render(context);
            assertEquals(output, "TOM");
        }
        catch (IOException e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testDefaultFilters()
    {
        try
        {
            Template t = new Template("{{ name|length }}");
            String output = t.render(context);
            assertEquals(Integer.parseInt(output), "Tom".length());

            t = new Template("{{ name|upper }}");
            output = t.render(context);
            assertEquals(output, "Tom".toUpperCase());

            t = new Template("{{ name|lower }}");
            output = t.render(context);
            assertEquals(output, "Tom".toLowerCase());

            t = new Template("{{ intArray|make_list|length }}");
            output = t.render(context);
            assertEquals(Integer.parseInt(output), intArray.length);

            t = new Template("{{ intArray|make_list }}");
            output = t.render(context);
            assertEquals(output, ((List<Integer>) context
                    .getVariable("intList")).toString());

            t = new Template("{{ intArray|length }}");
            output = t.render(context);
            assertEquals(Integer.parseInt(output), intArray.length);

            t = new Template("{{ name|make_list|length }}");
            output = t.render(context);
            assertEquals(Integer.parseInt(output), "Tom".length());

            context.putVariable("theSize", "Tom".length());
            t = new Template("{{ name|make_list|length_is:theSize }}");
            output = t.render(context);
            assertEquals(output, "true");

        }
        catch (IOException e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testLoad()
    {
        // make an anonymous filter which returns "true" if the object is null,
        // "false" otherwise
        PluginRegistry.getInstance().registerFilter(new Filter()
        {
            public Object filter(Object object, ContextStack context,
                                 String... args)
            {
                return object == null ? "true" : "false";
            }

            public String getName()
            {
                return "is_null";
            }
        });

        PluginRegistry.getInstance().registerFilter(new Filter()
        {
            public Object filter(Object object, ContextStack context,
                                 String... args)
            {
                return NumberUtils.createNumber(ObjectUtils.toString(object))
                        .intValue();
            }

            public String getName()
            {
                return "int";
            }
        });

        PluginRegistry.getInstance().registerFilter(new Filter()
        {
            public Object filter(Object object, ContextStack context,
                                 String... args)
            {
                Integer min = NumberUtils.createInteger(ObjectUtils
                        .toString(object));
                for (String arg : args)
                {
                    Integer num = NumberUtils.createInteger(arg);
                    min = Math.min(min, num);
                }
                return min;
            }

            public String getName()
            {
                return "minInt";
            }
        });

        try
        {
            // this should return nothing, since we don't know about the filter
            Template t = new Template("{{ bad_var|is_null }}");
            String output = t.render(context);
            assertTrue(StringUtils.isEmpty(output));

            // now, we'll load the plug-in we created into the context
            t = new Template("{% load \"is_null\" %}{{ bad_var|is_null }}");
            output = t.render(context);
            assertEquals(output, "true");

            // let's remove the plugin from the context to force another load
            context.getFilterMap().removeFilter("is_null");

            // now, let's test the alias op
            t = new Template(
                    "{% load \"is_null\" as isNull %}{{ bad_var|isNull }}");
            output = t.render(context);
            assertEquals(output, "true");
            context.getFilterMap().removeFilter("is_null");

            // now, let's try loading multiple
            t = new Template("{% load \"is_null\", \"int\" %}{{ 1.2|int }}");
            output = t.render(context);
            assertEquals(output, "1");

            // now, let's try loading and calling with multi args
            t = new Template("{% load \"minInt\" %}{{ 5|minInt:\"42,15,3\" }}");
            output = t.render(context);
            assertEquals(output, "3");

            // cleanup for other tests
            context.getFilterMap().removeFilter("is_null");
            context.getFilterMap().removeFilter("int");
            context.getFilterMap().removeFilter("minInt");
        }
        catch (IOException e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }

    }

    public void testExtends()
    {
        try
        {
            // create some templates, as files
            // existing template

            File parentFile = File.createTempFile("parent", "txt");
            File childFile1 = File.createTempFile("child1", "txt");
            File childFile2 = File.createTempFile("child2", "txt");
            File childFile3 = File.createTempFile("child3", "txt");
            File childFile4 = File.createTempFile("child4", "txt");
            File childFile5 = File.createTempFile("child5", "txt");

            // create the parent document
            FileUtils.writeStringToFile(parentFile,
                    "parent_start {% block block1 %}"
                            + "parent_block1 {% block block2 %}"
                            + "parent_block2" + "{% endblock %}"
                            + "{% endblock %}" + " parent_end");

            // create a direct child of the parent, overriding block1
            FileUtils.writeStringToFile(childFile1, "{% extends \""
                    + parentFile.getName() + "\" %}"
                    + "child_start{% block block1 %}"
                    + "child_block1{% endblock %}child_end");

            // create a direct child of the parent, overriding block2
            FileUtils.writeStringToFile(childFile2, "{% extends \""
                    + parentFile.getName() + "\" %}"
                    + "child_start{% block block2 %}"
                    + "child_block2{% endblock %}child_end");

            // create a sub-child of child2 (3-deep now), but overriding block1
            // in the top parent file
            FileUtils.writeStringToFile(childFile3, "{% extends \""
                    + childFile2.getName() + "\" %}"
                    + "child_start{% block block1 %}"
                    + "child_block1{% endblock %}child_end");

            // create a sub-child of child3 (4-deep now), overriding block1
            // in the parent, and including the super block
            FileUtils.writeStringToFile(childFile4, "{% extends \""
                    + childFile3.getName() + "\" %}"
                    + "child_start{% block block1 %}"
                    + "sub_child_block1 {{ block.super }}"
                    + "{% endblock %}child_end");

            // create a sub-child of child4 (5-deep now), overriding block1
            // in the parent, and including the super block
            FileUtils.writeStringToFile(childFile5, "{% extends \""
                    + childFile4.getName() + "\" %}"
                    + "child_start{% block block1 %}"
                    + "sub_child_block1 {{ block.super }}"
                    + "{% endblock %}child_end");

            // set up the expected strings
            String parentExpected = "parent_start parent_block1 parent_block2 parent_end";
            String child1Expected = "parent_start child_block1 parent_end";
            String child2Expected = "parent_start parent_block1 child_block2 parent_end";
            String child3Expected = "parent_start child_block1 parent_end";
            String child4Expected = "parent_start sub_child_block1 child_block1 parent_end";
            String child5Expected = "parent_start sub_child_block1 sub_child_block1 child_block1 parent_end";

            // add the temp directory to the include path
            PluginRegistry.getInstance().addTemplateIncludePath(
                    parentFile.getParent());

            // now, process the templates
            Template t = new Template(parentFile);
            String output = t.render(context);
            assertEquals(output, parentExpected);

            t = new Template(childFile1);
            output = t.render(context);
            assertEquals(output, child1Expected);

            t = new Template(childFile2);
            output = t.render(context);
            assertEquals(output, child2Expected);

            t = new Template(childFile3);
            output = t.render(context);
            assertEquals(output, child3Expected);

            t = new Template(childFile4);
            output = t.render(context);
            assertEquals(output, child4Expected);

            t = new Template(childFile5);
            output = t.render(context);
            assertEquals(output, child5Expected);

            // cleanup
            FileUtils.forceDelete(parentFile);
            FileUtils.forceDelete(childFile1);
            FileUtils.forceDelete(childFile2);
            FileUtils.forceDelete(childFile3);
            FileUtils.forceDelete(childFile4);
            FileUtils.forceDelete(childFile5);

        }
        catch (IOException e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testTemplateTags()
    {
        try
        {
            Template t = new Template("{% templatetag opencomment %}");
            String output = t.render(context);
            assertEquals(output, "{#");
        }
        catch (IOException e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testNow()
    {
        try
        {
            Date date = new Date();

            Template t = new Template("{% now %}");
            String output = t.render(context);
            System.out.println(output);

            t = new Template("{% now \"Today: %tm/%Td/%tY\" %}");
            output = t.render(context);
            System.out.println(output);
        }
        catch (IOException e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testBlock()
    {
        try
        {
            Template t = new Template("{% block %}here{% endblock %}");
            String output = t.render(context);
            assertEquals(output, "here");

            t = new Template("{% block named %}here{% endblock %}");
            output = t.render(context);
            assertEquals(output, "here");
        }
        catch (IOException e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testSet()
    {
        try
        {
            Template t = new Template(
                    "{% set name|upper as upperName %}{{ upperName }}");
            String output = t.render(context);
            assertEquals(output, "TOM");

            t = new Template("{% set intArray|length as len %}{{ len }}");
            output = t.render(context);
            assertEquals(output, String.valueOf(intArray.length));

            t = new Template("{% set 0 as zero %}{{ zero }}");
            output = t.render(context);
            assertEquals(output, "0");

            t = new Template("{% set \"0\" as zeroString %}{{ zeroString }}");
            output = t.render(context);
            assertEquals(output, "0");

            t = new Template(
                    "{% set \"Some \\\"quotes\\\"\" as quotes %}{{ quotes }}");
            output = t.render(context);
            assertEquals(output, "Some \\\"quotes\\\"");

            t = new Template("{% set doubleList.0 as val1 %}{{ val1 }}");
            output = t.render(context);
            assertEquals(output, "0.0");
        }
        catch (IOException e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testBinaryExpressions()
    {
        try
        {
            Template t = new Template(
                    "{% if number > 1 and number <= 100 %}true{% endif %}");
            String output = t.render(context);
            assertEquals(output, "true");

            t = new Template(
                    "{% if number < 5 %}true{% else %}false{% endif %}");
            output = t.render(context);
            assertEquals(output, "false");

            t = new Template(
                    "{% if number <= 42 %}true{% else %}false{% endif %}");
            output = t.render(context);
            assertEquals(output, "true");

            t = new Template(
                    "{% if name == \"Tom\" %}true{% elseif name == \"Adam\" %}false{% endif %}");
            output = t.render(context);
            assertEquals(output, "true");

            t = new Template("{% if name < number %}true{% endif %}");
            try
            {
                output = t.render(context);
                fail("Should've thrown");
            }
            catch (Exception e)
            {
            }
        }
        catch (IOException e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testElseIf()
    {
        try
        {
            Template t = new Template(
                    "{% if number < 0 %}negative{% elseif number == 42 %}perfect{% else %}other{% endif %}");
            String output = t.render(context);
            assertEquals(output, "perfect");

            t = new Template(
                    "{% if number < 0 %}negative{% elseif number != 42 %}perfect{% endif %}");
            output = t.render(context);
            assertEquals(output, "");
        }
        catch (IOException e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public void testVars()
    {
        try
        {
            Template t = new Template("{{ 0 }}");
            String output = t.render(context);
            assertEquals(output, "0");

            t = new Template("{{ \"string\" }}");
            output = t.render(context);
            assertEquals(output, "string");

            t = new Template("{{ 'string2' }}");
            output = t.render(context);
            assertEquals(output, "string2");

            t = new Template("{{ 'st'|cat:rings}}");
            output = t.render(context);
            assertEquals(output, "strings");
        }
        catch (IOException e)
        {
            fail(ExceptionUtils.getStackTrace(e));
        }
    }
}
