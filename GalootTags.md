## Built-in Tags ##

### Quick Details ###
| **Name** | **Quick Description** | **Inline Example** |
|:---------|:----------------------|:-------------------|
| **[block](GalootTags#block.md)** | used to fragment the document into sections which can be overridden by sub-documents | ` {% block content %}{{ content }}{% endblock %} ` |
| **[comment](GalootTags#comment.md)** | comment your code | ` {# a comment #} or {% comment %} another comment {% endcomment %} ` |
| **[extends](GalootTags#extends.md)** | signifies that the template extends another template | ` {% extends "base.html" %} ` |
| **[filter](GalootTags#filter.md)** | apply filters to a block of data | ` {% filter lower %}lowercase me{% endfilter %} ` |
| **[firstof](GalootTags#firstof.md)** | returns the first non-false value from a list of arguments. | ` Welcome {% firstof user.name "New User" %}! ` |
| **[for](GalootTags#for.md)** | loop over an object | ` {% for var in list %}{{ forloop.counter0 }}: {{ var }}{% endfor %} ` |
| **[if](GalootTags#if.md)** | conditionally evaluate a block of data | ` {% if var1 or var2 %}true!{% else %}false...{% endif %} ` |
| **[ifequal](GalootTags#ifequal.md)** | conditionally evaluate a block if two variables/strings are equal | ` {% ifequal user.name "admin" %}Welcome Admin{% endifequal %} ` |
| **[include](GalootTags#include.md)** | include and render a file at the specified location in the document | ` {% ifequal user.name "admin" %}{% include "pages/adminPage.html" %}{% endifequal %} ` |
| **[load](GalootTags#load.md)** | load a filter plug-in | ` {% load "is_null" %} {{ var|is_null|yesno:"yes,no" }} ` |
| **[macro](GalootTags#macro.md)** | define a re-usable macro block | ` {% macro sayHello(name) %} hello {{ name }} {% endmacro %} ` |
| **[now](GalootTags#now.md)** | format the current date/time | ` Copyright {% now "%tY" %} ` |
| **[set](GalootTags#set.md)** | set a variable within the current context | ` {% set users|length as numUsers %}There are {{ numUsers }} users. ` |
| **[templatetag](GalootTags#templatetag.md)** | display a reserved template tag | ` {% templatetag opencomment %} ` |
| **[with](GalootTags#with.md)** | sets a variable within a new block | ` {% with users|length as count %}user count: {{ count }}{% endwith %} ` |


### More Details ###
#### block ####
The block tag deals with template inheritance. It is used to fragment the document into sections which can be overridden by sub-documents. The block tag takes an optional name argument. The name is important if you plan on possibly overriding the block (or overriding another of the same name). It is useful to create blocks without names if all you want to do is create a new context scope.
Examples:
```
{% block content %}
    block text
{% endblock %}


Unnamed block, used purely to set some variables in a protected scope.
{% block %}
    {% set name|upper as upperName %}
    {% set users|length as numUsers %}
    {% set database.users as users %}

    ... now do something with the vars ...
{% endblock %}
```

#### comment ####
Place a comment in the file, which will be ignored during processing. There are two ways to comment, one being far less verbose.
Example:
```
{% comment %} this is the verbose way to create a comment... {% endcomment %}

{# the more concise comment #}
```

#### extends ####
The extends tag signifies that the template extends a parent template. The extends tag, if being used, must occur as the very first tag in the document. Read the section on Template Inheritance to learn more.

Examples:
Extend a template, by file name:
```
{% extends "base.html" %}
{% block content %} sub-content {% endblock %}
```

Extend a template by evaluating a variable:
```
{% extends parentTemplate|lower %}
{% block content %} sub-content {% endblock %}
```

#### filter ####
Apply filters to a block of data.

Examples:
```
{% filter lower %}
    Make this block ALL lower text, Including variables, etc.: {{ var }}
{% endfilter %}

{# we can chain filters together too #}

{% filter lower|count %}
    First, lower case this block, then evaluate the length of the data...
{% endfilter %}
```

#### firstof ####
Returns the first non-false value from a list of arguments. (See the boolean-ness note above.) The arguments can be variable expressions (including filters), or strings. This is useful if you want to provide a default value.

Examples:
```
{% firstof var1 var2 var3 "nothing" %}
    
Welcome {% firstof user.name "New User" %}!
```

#### for ####
Loop over a variable (List, Iterable, array, String).

Example:
```
{% for user in users %}
    User {{ forloop.counter0 }}: {{ user.name }}
{% endfor %}
```

#### if ####
Conditionally evaluate a block of data.

Examples:
```
{% if var1 or var2 %}
    at least one of var1 or var2 is not "false"
{% endif %}

{% if var1 %}
    true
{% else %}
    false
{% endif %}

{% if not var1 %}
    false
{% endif %}

{% if not var1 and not var2 %}
    both var1 and var2 are "false"
{% endif %}

```

#### ifequal ####
Conditionally evaluate a block if two variables/strings are equal. The arguments can be variable expressions or strings.

Examples:
```
{% ifequal "tom" "adam" %}
    we are equals!
{% else %}
    we are not equals...
{% endifequal %}

{% ifequal user.name "admin" %}
    hello admin user!
{% endifequal %}

{% ifequal user.name|lower pageUser.name|lower %}
    you are viewing your own page
{% endifequal %}
```

#### include ####
Include a file at the specified location in the document. The included document will be rendered using the current context (all variables/filters/tags will be available to it).

Example:
```
{% for user in users %}

    {# the userPage will be able to use the "user" loop variable #}
    {% include "userPage.html %}

{% endfor %}
```

#### load ####
The load tag loads a list of filter plug-ins into the current context. Currently, once loaded, the filters are available globally to the document being evaluated. This may change in the future to be available only in the current context (and sub-contexts) to which it is declared. All plug-ins must be registered with the PluginRegistry, otherwise they will not be loaded. Optionally, filter plug-ins can be aliased using the "as" keyword.

Examples:
```
{% load "is_null" %}

{# now the is_null filter is available to use, as long as it existed in the global registry #}

{% if var|is_null %}
    the variable is null
{% endif %}


{% load "is_null" as IsNull %}
{% if var|IsNull %}
    the variable is null
{% endif %}


{# we can also load filter plug-ins that are referenced from a variable expression #}

{% load pluginName|lower as my_filter %}

{% filter my_filter %}
    apply the dynamically loaded filter plug-in to this block
{% endfilter %}

```

### macro ###
Create a re-usable macro, adding it to the context it was defined in, making it callable by all sub-contexts.

An example:
```
{% macro makeTable(items) %}
<table><tbody>
{% for item in items %}
<tr><td>{{ forloop.counter }}</td><td>{{ item.name }}</td><td>{{ item.toString }}</td></tr>
{% endfor %}
{% endmacro %}
```

To call it:
```
{{ makeTable(myItems) }}
```

#### now ####
Format the current date according to an optional format string. This uses the Java Formatter syntax to format the passed in string. Complete documentation on the supported date-format syntax is located here: http://java.sun.com/j2se/1.5.0/docs/api/java/util/Formatter.html#dt.

**Note:** You do not need to add the 1$, 2$, etc. positional arguments to your format string. Check out the examples below for better clarification.

Examples:
```
Copyright {% now "%tY" %}

The verbose date/time is: {% now %}

Today is {% now "%tm/%td/%tY" %}
```

#### set ####
The `set` tag allows you to set a variable within the current context. This will overwrite  a pre-existing variable of the same name (within the same context).

Examples:
```
{% set users|length as numUsers %}

{% block %}
The next var is only available within this block and does not modify the variable of the same name in the parent context.
    {% set users.admins|length as numUsers %}
    There are {{ numUsers }} admins.
{% endblock %}

There are {{ numUsers }} users.
```

#### templatetag ####
This tag is here to match the supported tag in Django. Since escaping is not built-in (yet), this tag lets you evaluate the special tag characters. Just like with Django, the supported keyword arguments to this tag, and corresponding output are:
| **keyword** | **output** |
|:------------|:-----------|
| openblock |     {% |
| closeblock |    %} |
| openvariable |  {{ |
| closevariable | }} |
| openbrace | 	  { |
| closebrace | 	  } |
| opencomment |   {# |
| closecomment |  #} |

Example:
```
This is how you specify a Galoot comment:
{% templatetag opencomment %} my comment! {% templatetag closecomment %}
```

#### with ####
Set a variable in the context of the containing block. This is useful for "expensive" operations, to cache a variable.

Example:
```
{% with database.users.count as count %}

    there are {{ count }} users.

    the variable count is only available in this block.

{% endfor %}
```