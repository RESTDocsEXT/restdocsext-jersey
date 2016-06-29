simple-jersey
=============

This is an example project that uses use the below technologies to provide
a simple documented API.

* Jersey 2.0 for backend
* Jersey Test Framework as the testing framework.
* Spring REST Docs as the core snippet generating framework.
* RESTDocsEXT Jersey for creation of documentation snippets.

To build/run the example, you should have Maven installed. From the root
of this project, just run the following

```
mvn clean package jetty:run
```

This command will test and package the project, and also produce the documentation.
Once you see `[INFO] Started Jetty Server` in the terminal, you can navigate to 

```
http://localhost:8080/docs
```

There you should see the documentation.