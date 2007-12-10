/**
 * 
 */
package galoot.examples.madlibs;

import galoot.AbstractFilter;
import galoot.ContextStack;
import galoot.InputAdapter;
import galoot.PluginRegistry;
import galoot.Template;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JEditorPane;
import javax.swing.JFrame;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An example illustrating the use of galoot for generating an HTML Document
 * that displays a madlib
 * 
 */
public class MadlibExample
{
    private static Log log = LogFactory.getLog(MadlibExample.class);

    // **** The configuration file that defines our vocabulary
    private static final String MADLIB_CONFIG = "madlib.config";

    /**
     * Sample illustrating the features of the galoot framework
     * 
     * @param args
     * @throws ConfigurationException
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws ConfigurationException,
            FileNotFoundException, IOException
    {
        if (!(args.length == 1 || args.length == 2))
        {
            System.out.println("Usage: " + MadlibExample.class + " "
                    + "<madlib file> <show in browser>");
            System.exit(0);
        }

        File file = new File(args[0]);

        // register a filter with the plugin registry
        PluginRegistry.getInstance().registerFilter(new Underline());
        // register the directory where our templates live
        PluginRegistry.getInstance().addTemplateIncludePath(file.getParent());

        // create a new input adapter that generates data for feeding to a
        // template

        URL config = MadlibExample.class.getResource(MADLIB_CONFIG);
        InputAdapter madlibInputAdapter = new MadlibInputAdapter(config);

        // create a new context stack for from the MadlibInputAdapter
        ContextStack contextStack = new ContextStack(madlibInputAdapter
                .getContextStackInput());

        Template template = new Template(new File(args[0]));
        String filledInMadlibs = template.render(contextStack);

        // dump the output of the replace
        System.out.println(filledInMadlibs);
        File madlibFile = new File("madlib.html");

        FileUtils.writeStringToFile(madlibFile, filledInMadlibs, "UTF-8");

        // display in a mini web browser
        if (args.length == 2)
        {
            try
            {
                URL[] urls = FileUtils.toURLs(new File[] { madlibFile });
                JFrame frame = new JFrame();

                Dimension screenSize = Toolkit.getDefaultToolkit()
                        .getScreenSize();
                frame.setLocation(screenSize.width / 4, screenSize.height / 4);
                frame.setSize(screenSize.width / 2, screenSize.height / 2);
                JEditorPane editorPane = new JEditorPane(urls[0]);

                // editorPane.setText(filledInMadlibs);
                editorPane.setSize(375, 375);
                frame.add(editorPane);

                // frame.pack();
                frame.setVisible(true);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
            catch (HeadlessException e)
            {
                log.warn("Display was unavailable");
            }
        }

    }

    /**
     * 
     * An InputAdapter for adapting an input source to a context
     * 
     */
    private static final class MadlibInputAdapter implements InputAdapter
    {
        XMLConfiguration xmlconfig;

        /**
         * Create a new MadlibInput adapter instance
         * 
         * @throws IOException
         * @throws FileNotFoundException
         * @throws ConfigurationException
         * 
         */
        public MadlibInputAdapter(String path) throws FileNotFoundException,
                IOException, ConfigurationException
        {
            xmlconfig = new XMLConfiguration();
            xmlconfig.load(new File(path));
        }

        public MadlibInputAdapter(URL url) throws FileNotFoundException,
                IOException, ConfigurationException
        {
            xmlconfig = new XMLConfiguration();
            xmlconfig.load(url);
        }

        /*
         * (non-Javadoc)
         * 
         * @see galoot.InputAdapter#getContextStackInput()
         */
        public Map<String, Object> getContextStackInput()
        {
            Map<String, Object> ctxtMap = new HashMap<String, Object>();
            ctxtMap.put("person", xmlconfig.getList("person.item"));
            ctxtMap.put("place", xmlconfig.getList("place.item"));
            ctxtMap.put("noun", xmlconfig.getList("noun.item"));
            ctxtMap.put("plnoun", xmlconfig.getList("plnoun.item"));
            ctxtMap.put("adverb", xmlconfig.getList("adverb.item"));
            ctxtMap.put("adjective", xmlconfig.getList("adjective.item"));
            ctxtMap.put("verb", xmlconfig.getList("verb.item"));
            ctxtMap.put("liquid", xmlconfig.getList("liquid.item"));
            return ctxtMap;
        }
    }

    /**
     * 
     * Filter to underline a string in an html document
     * 
     */
    private static final class Underline extends AbstractFilter
    {
        public Object filter(Object object, String args)
        {
            if (object != null)
            {
                return "<span style=\"text-decoration: underline\">"
                        + object.toString() + "</span>";
            }
            else
            {
                log.debug("null object passed for argument");
                return "";
            }
        }

        @Override
        public String getName()
        {
            return "underline";
        }
    }

}
