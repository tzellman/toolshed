## Filters ##
We have a concept known as a _filter_, which plays an essential role in the processing of context variables/objects within templates. A filter is simply a black box that does some type of data processing, or filtering, to an input object, resulting in some type of output object. Each filter has a name, with which can be used inside templates to filter variables. Here is some example usage of a filter named _length_, which just takes in an object and attempts to return its length.
Examples:
```
The length of this string is {{ stringObj|length }}.
The length of this array is {{ arrayObj|length }}.
The length of this list is {{ listObj|length }}.
This will result in an empty string returned: {{ someUnknownObject|length }}
```


## Built-in Filters ##
We don't start you off empty handed. Below are the built-in filters. :)
| **Name** | **Quick Description** | **Supported Object Types** |
|:---------|:----------------------|:---------------------------|
| **default** | accepts a String argument, which is returned if the object is null, otherwise the original object is returned | `Object` |
| **length** | returns the length of the object | `String`, array, `List`, `Map` |
| **length\_is** | takes an int argument and returns true if the object length is equal | same as `length` filter |
| **lower** | returns an lowercase version of the object | 'String' |
| **make\_list** | create a new List from the object and returns it | `String`, `Iterable`, `List`, array |
| **random** | returns a random member of the input object | `String`, array, `List`, `Map` |
| **title** | converts the sentence to title case | `String` |
| **upper** | returns an uppercase version of the object | `String` |
| **wordcount** | returns the number of words in the input | `String` |
| **yesno** | accepts a comma-delimited list of yes/no/maybe(optional) arguments, one of which is returned based on the object's boolean-ness | `Object` |

## Plug-In API ##
Filters can be very useful for template programmers, but we don't want you to be restricted to the ones we have packaged with the library. In order to allow maximum flexibility, we provide a plug-in API for creating and registering your own filters. The API has a Filter interface, which looks as follows:
```
/**
 * Filter an object by applying some type of processing to it. The input
 * object can be anything, so good implementations should only handle known
 * types for the filter.
 * 
 * If the input object does not apply to the operation at hand, you can
 * return null. You could also return the original object.
 * 
 * @param object
 *            the input object
 * @param args
 *            optional arguments, pertinent to the operation
 * @return the resultant object
 */
public Object filter(Object object, String args);

/**
 * Return the name of this filter. Filter names are case-sensitive.
 * 
 * @return the name of the filter
 */
public String getName();
```

If you _implement_ this interface, you can register your own filters which can then be used in your templates.