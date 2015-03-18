### Building Galoot With Maven ###
We use Maven 2 to handle the build process. Below are the steps on how to build Galoot.

  * Make sure you have Maven 2 installed. You can download it from http://maven.apache.org.
  * Check out the code from subversion. Go [here](http://code.google.com/p/toolshed/source) for more info.
  * From the `trunk/galoot` directory (the one with `pom.xml` in it) execute the following commands:
```
mvn sablecc:generate
mvn package
```
  * Optionally, if you want the dependencies copied locally:
```
mvn dependency:copy-dependencies
```

The output jar will be located in the `target/` directory.