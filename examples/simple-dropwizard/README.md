simple-dropwizard
=================

This is an example project that uses use the below technologies to provide
a simple documented API.

* [DropWizard] 9.2 for backend
* [DropWizard JUnit Rule][2] for testing (it uses Jersey Test Framework in the background).
* [Spring REST Docs][3] as the core snippet generating framework.
* [RESTDocsEXT Jersey][4] for creation of documentation snippets.
* Maven as the build system.

To build the example, you should have Maven installed. From the root
of this project, just run the following

```
mvn clean package
```

This command will test and package the project, and also produce the documentation.
Then to run the application run

```
java -jar target/simple-dropwizard.jar server example.yml
```

Once the server has finished initializing, you can navigate to 

```
http://localhost:8080/docs
```

There you should see the documentation.

Notes
-----

* After the documentation is created, it is sent to the target/classes directory,
where a configured `AssetBundle` finds it. See the `SimpleApplication` class.
The moving of the documentation index file is done with the `maven-resources-plugin`

* DropWizard `ResourceTestRule` uses the Jersey Test Framework in-memory
provider be default. I could not get it to work with this, so I added
and used the Grizzly provider. See the pom for the dependency, and the tests
for the configuration (of the `GrizzlyTestContainerFactory`).

* When using the `Client` from the DropWizard Rule, you need to use a complete URL.
It's not like using Jersey Test Framework directly, where you can just call `target`
and use a relative URL. The default port for Grizzly is `9998`.

[1]: http://www.dropwizard.io/0.9.3/docs/
[2]: http://www.dropwizard.io/0.9.3/docs/manual/testing.html#testing-resources
[3]: https://projects.spring.io/spring-restdocs/
[4]: https://github.com/RESTDocsEXT/restdocsext-jersey