simple-jersey-spring-boot
=================

This is an example project that uses the below technologies to provide
a simple documented API.

* [Spring Boot][1] 1.3.5 and [Jersey][2] 2.23 for backend
* [Spring Test][5] (with Spring Boot) for testing.
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
mvn spring-boot:run
```

This will start a server and run the application. Once the server has finished
initializing, you can navigate to 

```
http://localhost:8080/docs
```

There you should see the documentation.

Notes
-----

* `spring-restdocs-core` is explicitly declared as a dependency, as the Spring Boot
parent 1.3.5, still uses 1.0.1, which will not work for our example. We want to
use the latest version.

* Web Started dependency is only added to server the static content. In this case,
the documentation. See `SimplApplication` for addition of view controllers to
server the documentation from `/docs`, without needed the `index.html`.

* After the documentation is built, the `maven-resources-plugin` will copy it
to the `static/docs` folder. This is where the static content is served from.


[1]: http://projects.spring.io/spring-boot/
[2]: https://jersey.java.net/
[3]: https://projects.spring.io/spring-restdocs/
[4]: https://github.com/RESTDocsEXT/restdocsext-jersey
[5]: http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-testing