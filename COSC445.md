## Introduction ##

Imitation is the sincerest form of flattery. In that spirit, we decided to create an implementation of of the templating language used by the [Django Project](http://www.djangoproject.com/) in Java using SableCC. While we cannot claim any points for originality in coming up with a project idea, creating an alternate implementation of a language presented us with the opportunity to explore the strengths and weaknesses of each implementation.

One point of comparison between the two approaches is the method of parsing employed. Our implementation makes use of a SableCC grammar to generate a language recognizer. In contrast, Django makes use of the regular expression support available in Python to perform parsing. One drawback to the SableCC approach is the lack of support for exception throwing in SableCC. Bailing out of a parse requires resorting to throwing RuntimeExceptions, which need not be declared in a method's signature to be thrown. Additionally, it lacks the error production mechanisms available in Yacc/Bison that allow more robust error handling to be embedded into the grammar itself. The advantage that using SableCC has over the python implementation is that there is a clearer separation between the language recognition constructs used in SableCC's lexer and parser, than in regular expressions, which must perform a double-duty of parsing and lexing.

## Sample Application ##

View the [MadLibs](MadLibs.md) wiki page for information on how we used our [Galoot](Galoot.md) library to create a simple Mad Lib generation application.

## Design ##

#### Template API ####
The first step of the design process involved defining what we wanted the final API to look like. We came up with the following simple API (borrowing some ideas from Django):
```
/*
 * Keep it simple. A template is simply a string of data, which
 * can be read from anywhere: file, url, memory, etc.
 */

// initialize the template from a file
Template template1 = new Template(new File("template.txt"));

// initialize the template from a String
Template template2 = new Template(
        "my name is {{ name|lower }}, and my lottery numbers are: "
      + "{% for num in lottery %}num{% if not forloop.last %},{% endif %}{% endfor %}");

/*
 * We need some way of passing values into the template. This is
 * done via a ContextStack, which is basically a glorified Map
 * of key/value pairs representing objects and their names. The
 * ContextStack keeps a stack of maps so that the top-level
 * operations (put, get, etc.) are akin to scope-ing variables
 * in a normal programming language.
 */

// fill up the context with some pertinent data
ContextStack context = new ContextStack();
context.put("name", "Morpheus");
context.put("answer", 42);
context.put("lottery", new int[] { 7, 13, 15, 26, 42 });

// render the templates to a String... easy as pie
String output1 = template1.render(context);
String output2 = template2.render(context);

// you can do anything with the output
System.out.print(output1);
System.out.print(output2);
```

#### Filters ####
Refer to the [GalootFilters](GalootFilters.md) page for information relating to filters.

#### Internal Design ####
The API that users have to deal with is much simpler than the code we wrote to support some of the internal workings of the interpreter. For example, as you'll read about in the Challenges section, we came up with a Document model which abstracts each template as a document consisting of text and block fragments. This design helped us to create a common interface amongst the blocking code, which helped with the template inheritance task.

## Challenges ##

#### Template Inheritance ####
One of the most difficult and daunting tasks was implementing template inheritance. Template inheritance allows you to define blocks within a template which can later be overridden or extended. Dealing with template inheritance meant that we needed to be able to recursively load documents while keeping track of document blocks.

We realized that at an abstract level, each template is comprised of two types of "fragments":
  * **text** - any sequence of text that has been processed
  * **blocks** - a _named_ block containing a sequence of fragments.
While processing a document we only categorize the processed data as either a text fragment or a block (of fragments). The processing flow initializes by constructing a document object, which in turn keeps track of a fragment stack. The depth of the block fragment stack is synonymous with the depth of the block nesting.

We took a look at how Django deals with inheritance and came up with the following processing flow. When processing a document:
  * if the document contains an extends tag, load the parent document. This is a recursive behavior.
  * when we come across a block tag, we do the following:
    * check to see if the block name already exists in the current document:
      * if it does, we (currently) log an error message stating that you cannot have two blocks with the same name in the same document.
    * next, we check to see if we have a parent document (i.e. if the _extends_ tag was used):
      * if we do have a parent, check to see if the block name already exists in the parent hierarchy:
        * if the block name DOES already exist:
          * and we are in the top-level of the document (i.e if we aren't in a nested block), then we replace the parent's block with this block.
          * and we are NOT in the top-level of the document (i.e. if we ARE in a nested block), then we log an error message since this type of behavior is not supported.
        * if the block name does NOT already exist:
          * and we are in a sub-block, then we just push the block onto the current block in the fragment stack.
          * and we are in a top-level block, then we ignore the block, since we don't really know where to put it within the document hierarchy.
      * if we do NOT have a parent document, we push the block onto the current block of the document fragment stack.
  * all other tags were processed in a manner that was far less challenging than processing blocks with an inheritance scheme.

#### Lexer States ####
Another hurdle to be overcome was learning to use the lexer states feature available in SableCC. Because the template language essentially embeds one language within a stream of raw data, the lexer needed to be manipulated to pass the raw data through, unmodified, while recognizing the language embedded within the stream of data. To effect this, lexer states were used in a fashion very much analogous to the way used to recognize C-style comments in Lex.


## Further Material ##
Take a look at the main [Galoot](Galoot.md) wiki page, which describes some of the more in-depth documentation.