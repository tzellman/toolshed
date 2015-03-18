## Introduction ##

Galoot is a templating engine written in Java, designed after the [Django templating engine](http://www.djangoproject.com/documentation/templates/) for Python. Currently, this project is being developed for a compilers course and is considered to be in an early stage of development. The project uses [SableCC](http://sablecc.org/) to generate the lexer/parser from the language grammar.


## Templates ##
In the most abstract sense, a template is any form of input (text) which contains special (optional) processing tags. The text (usually represented in the form of a file) is interpreted, resulting in output text.


## Useful Information ##
Below are some useful notes on definitions or circumstantial behaviors.

#### Silently Ignoring Variables ####
If a variable expression ever evaluates to "null", then the variable is not transformed to the output.
Example:
```
This should be empty in the output: "{{ badVar }}"
```

#### Boolean-ness ####
When evaluating variables the following cases result in "false":
  * null object
  * boolean false
  * number == 0
  * empty List/Map/String
Example:
```
Evaluates to false if the list is empty: {% if empty_list %}list was not empty!{% endif %}

Is the list empty? {{ empty_list|yesno:"no it is not,yes it is" }}
```


## Template Inheritance ##
One of the greatest features of Galoot is the fact that you are able to inherit templates. In the following examples we will discuss a common use case for template inheritance.

### HTML Example ###
So you've decided to create the world's greatest online, Web 3.0 pet store. (Yes, 3.0 because you're just that good!) You have to start somewhere, and, yes we might be biased, Galoot is the best place to start. You want to design your application so that you can maximize code reuse. So, you decide to start with a top-level base HTML page which will serve as the root/parent of all of your templates. Here is an example:

```
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
    <link rel="stylesheet" href="site.css" />
    <title>{% block title %}Pets R Us{% endblock %}</title>
    {% block extrahead %}{% endblock %}
</head>

<body>
    <div id="header">{% block header %}{% endblock %}</div>
    <div id="main"
        {% block main %}
            {{ content }}
        {% endblock %}
    </div>
    <div id="footer">{% block footer %}{% endblock %}</div>
</body>
</html>
```

More to come...

## Template Tags ##
See the [GalootTags](GalootTags.md) page for detailed information.

## Template Filters ##
See the [GalootFilters](GalootFilters.md) page for detailed information.


## FAQ ##
  1. **What's up with the name?**
> > The name is sort of random, but not completely. Galoot is uncommonly known as a left-recursive acronym for _"Galoots Are Lovers Of Old Tools"_. That being said, we believe that this library/engine is just that thing that galoots need: something new and exciting. So, if you happen to be a galoot (not the big ogre kind, but the lover of old tools kind), then we think you might want to give Galoot a chance. You might break out of your galoot-ness (for the moment) and use a _new_ tool. Then, you'll become a galoot once again after you start loving Galoot (does that make sense? :)). In addition, the word galoot sounds similar to the phrase _"glue it"_, which is exactly what Galoot lets you do: glue documents, text, and variables together!