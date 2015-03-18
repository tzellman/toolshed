_A collection of code/tools which help make life easier._

### _Rover_ ###
[Rover](Rover.md) is a "query set" API which builds and executes SQL statements. The current implementation automatically reflects database metadata on the fly, meaning you don't need to generate beans/ORM models apriori. The commons-beanutils DynaBean is the common currency returned, including nested "related" objects/beans.

```
// create a IQueryContext for HSQLDB
IQueryContext context = new HSQLDBQueryContext();
// query the requirement table
IQueryResultSet q = new QueryResultSet("requirement", context);

// give me all requirements that have a release (fk) name == 1.0
IQueryResultSet filter = q.filter("release__name=1.0");
// select all related objects to a depth of 2
List results = filter.selectRelated(2).list();

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
```

### _Galoot_ ###
[Galoot](Galoot.md) is a templating engine for Java which uses similar syntax to the Django templating system. It exhibits much of the same behavior as Django, including filter plug-ins, template inheritance, and a rich suite of built-in tags/filters.

```
{% extends "parent.html" %}

{# load myPlugin into the context and alias it as rm_tags #}
{% load "myPlugin" as rm_tags %}

{% block main %}
    {% for user in users %}
        <div class="{{ uClass }}">User {{ forloop.counter0 }}:
             {{ user.name|title }}
        </div>
        {% include "userPage.html" %}

        {# remove all tags and convert to lowercase #}
        {% filter rm_tags|lower %}
            {{ existingContent }}
        {% endfilter %}

        {% ifequal user.name|lower "admin" %}
            {% include "adminPage.html %}
        {% endif %}

    {% endfor %}

    Copyright {% now "%tY" %}
{% endblock %}
```