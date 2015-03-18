## Introduction ##

To show a quick and simple example of how the [Galoot](Galoot.md) template engine could be used, we have written a Mad Lib generator. The example application fills in a [Galoot](Galoot.md) template with randomly chosen words from a dictionary configuration file.

The configuration file consists of lists of various words split up into various classifications from the specific (names, liquids, places) to the general (parts of speech: nouns, verbs, adjectives, etc.)

## Show Me The Mad Lib! ##
So you don't want to read through all of the documentation for this example and just want to make some MadLibs huh. Well, if you follow these simple steps, you'll be in business:
  * Download the galoot library jar [here](http://toolshed.googlecode.com/files/galoot-0.1-dev.jar) _(all dependencies are included in the jar, as well as the MadLib sample application)_.
  * Create a MadLib (sorry, you'll have to read further to do that), or, you can just use the default MadLib provided by passing the --default parameter.
  * Usage: java -jar galoot-0.1-dev.jar [--default] [--window] <madlib file>
    * --default will use the default built-in MadLib
    * --window will show the MadLib in a dialog window

## Madlib.galoot ##

```
{% load "underline" %}
{% include "header.galoot" %}
{% include "body.galoot" %}
{% include "footer.galoot" %}
```

As Galoot reads this file, the load directive tells the framework to make the "underline" filter available. What this means will become clearer as we progress.

The include directive tells Galoot to look into a special set of paths (configurable by the user of the framework) for the each of the listed .galoot template files. These files will be loaded and parsed by the framework. Any galoot directives encountered therein will  in turn be parsed and evaluated in the same fashion.

Note that templates can be any text file.

## body.galoot ##

body.galoot is where most of the "action" occurs in the evaluation of Madlib.galoot. Here it is in all its glory:
```
<h1>A day at the amusement park with *{{ person|random|underline }}*</h1>

<p>
An amusement park is always fun to visit on a hot summer
{{ noun|random|underline }}. When you get there, you can rent a
{{ noun|random|underline }} and go for a swim. And there are lots of
{{ adjective|random|underline }} things to eat. You can start off with a hot dog on
a/an {{ noun|random|underline }} with mustard, relish, and {{ plnoun|random|underline }}
on it. Then you can have a buttered ear of {{ noun|random|underline }} with a
nice {{ adjective|random|underline }} slice of watermelon and a big bottle of
cold {{ liquid|random|underline }}. When you are full, it's time to go on the
roller coaster, which should settle your {{ noun|random|underline }}.
Other amusement park rides are the Dodge-Em which has little
{{ plnoun|random|underline }}, that you drive and run into other {{ plnoun|random|underline }},
and the Merry-Go-Round where you can sit on a big {{ noun|random|underline }}
and try to grab the gold {{ noun|random|underline }} as you ride past.
</p>
```

Doesn't get much nicer than that does it? (Didn't think so.) In between the funny brackets `{{` `}}` is where the [Galoot](Galoot.md) magic happens. For example:
```
{{ plnoun|random|underline }}
```

This is an example of a _variable expression_. A variable expression consists of variable
(`plnoun` in this case) followed by one or more _filters_. Note that in a variable expression, we can also refer to the member fields and methods of a variable via the familiar dot notation. If `plnoun` has a `length` member, we can paste it directly into the document this way:
```
How many plural nouns are there? Answer: '{{ plnoun.length }}'
```

#### What's a filter? ####
Filters are an idea borrowed from the Django framework, who were no doubt inspired by the ability to create pipelines of commands in many Unix shells. Here we are piping the 'output' of a variable to the input of one or more special function objects that will

In our example, we make use of two filters: 'random' and 'underline'. 'random' is a built-in filter provided with the [Galoot](Galoot.md) framework. It takes a list-like object, and picks a random element from it. 'underline' is an example of a user-defined filter. It will wrap the the string representation of the object piped to it in special formatting tags that will cause it to render with an underline when viewed in a web browser.

Remember when we issued a `{{ load "underline"}}` back in Madlib.galoot? That directive told galoot to make available a non-default filter for use in the templates rendered from that point on.

## header.galoot and footer.galoot ##

The header and footer galoot templates are (very) simple text files that create the header and footer (who'd have guessed?) of an html document. They contain no special galoot directives, and are thus pasted verbatim into Madlib.galoot as it is evaluated.

## How do I actually use this?! ##

All of the above is well and good, but how do we actually populate data into the template? for this we need to delve into the Java programming layer of [Galoot](Galoot.md).

We start off in in `main`, where all of the action starts in our example. Our first step is to configure the `PluginRegistry`:
```
public class MadlibExample
{
    ...
    public static void main(String[] args) throws ConfigurationException,
            FileNotFoundException, IOException
    {
        ...
        PluginRegistry.getInstance().registerFilter(new Underline());    
```

The `PluginRegistry` is a singleton class that is used to configure the framework. Setup of template paths and registering of filter objects are two of this class's responsibilities.

### Underline? ###

We take a detour from `main` to look at a simple example of how to create a user defined filter:
```
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

        public String getName()
        {
            return "underline";
        }
    }     
```

To create a custom filter, the easiest route is to subclass `AbstractFilter`, overriding the `getName()` and `filter()` methods. The `filter` method takes two arguments: the first is the object that we are interested in transforming. The second is a String of user defined arguments. This can be a string delimited by any separator string/character defined by chosen by the filter creator. An example makes this more clear:
```
{% load "myfilter" %}
{{ myvar|myfilter:"foo;bar;baz" }}
```

Here we load 'myfilter', and pipe myvar into it, with the arguments foo, bar, and baz, which are separated by the ';' chosen by myfilter's creator.


### Back to Basics ###
Now back to our originally scheduled main method:

```
        InputAdapter madlibInputAdapter = new MadlibInputAdapter(MADLIB_CONFIG);
```
The next step is to actually read in some data to populate our madlib template. We do this use an implementation of the `InputAdapter` interface. Implementors of this interface are used to wrap a new data source in an object that can generate input usable by [Galoot](Galoot.md). This interface defines a single method `getContextStackInput` that returns a Map of String/Object pairs. In this case, we use a custom `MadlibInputAdapter` to read in the dictionary of words that we will use to populate our madlib.

```
        ContextStack contextStack = new ContextStack(madlibInputAdapter
                .getContextStackInput());
```

In this step, we create a new `ContextStack`. The `ContextStack` is the object that forms the bridge between a map of input data and the template engine.

```
        //we wrote the input file/url to a StringWriter instance already
        Template template = new Template(stringWriter.toString());
        String filledInMadlibs = template.render(contextStack);
        System.out.println(filledInMadlibs);
    }
```

Finally, we create a new `Template` object, and use it to render the data into the `File` passed into it as an argument. In the call to `render()`, the file used in the constructor will be fed into a template parser (generated with SableCC), and populated with the data contained in the context. We dump the data to file, and pat ourselves on the back for a job well done.

NB: Templates can be created using instances of `Reader` as well.