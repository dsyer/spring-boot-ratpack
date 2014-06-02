This project lets you write an application using Ratpack for the HTTP
routing and endpoints, and Spring for wiring.

## Quick Java Example

```java
@ComponentScan
@Configuration
@EnableAutoConfiguration
public class Application {

    @Bean
    public Handler handler() {
        return new Handler() {
            @Override
            public void handle(Context context) throws Exception {
                context.render("Hello World");
            }
        };
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

}
```

If a single `@Bean` of type `Handler` is present it is installed and
serves the default route. For more routes you have to add `@Beans` of
type `Action<Chain>` and add handlers as needed to the chain inside
its execute method.

## Quick Groovy DSL Example

Ratpack has a Groovy DSL for building routes. Spring Boot CLI
applications can take advantage of the nice syntax as follows.

This should run (as it is, with no imports):

```groovy
ratpack {
  handlers {
    get {
      render "Hello World"
    }
  }
}
```

Put it in a file called `app.groovy` and run it like this:

```
$ spring run app.groovy
...
```

Then get the result in a browser at http://localhost:5050.

You can also use `render json(...)` or `render groovyTemplate(...)`
features of Ratpack with no additional effort. Spring will pick up
Groovy templates by default from `classpath:/templates`.

To use Spring effectively you will want to take advantage of the
dependency injection and autoconfiguration features of Spring Boot as
well. Here's a simple example with dependency injection and json:

```groovy
@Service
class MyService {
  String message() { "Hello World" }
}

ratpack {
  handlers {
    get { MyService service ->
      render json([msg:service.message()])
    }
  }
}
```

The `ratpack` DSL keyword only supports `handlers` inside its
top-level closure (in non-Spring apps you might see `bindings` as
well).

## Installing the Spring Boot CLI

To run an application written in the Ratpack DSL you need the ratpack
extensions to the
[Spring Boot CLI](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#getting-started-installing-the-cli). See
below for instructions on where to get the extensions.

Since the Ratpack extensions are not yet part of the main Spring Boot
feature set you will need to clone from
[my fork](https://github.com/dsyer/spring-boot) and checkout the
"feature/ratpack" branch. Build it locally, e.g.

```
$ git clone https://github.com/dsyer/spring-boot
$ cd spring-boot
$ git checkout feature/ratpack
$ mvn install
```

or, more quickly,

```
$ (cd spring-boot-cli; mvn install -DskipTests=true)
```

Once it is built, you can install this version of the CLI using [`gvm`](http://gvmtool.net):

```
$ gvm install springboot dev spring-boot-cli/target/spring-boot-cli-1.1.0.BUILD-SNAPSHOT-bin
$ gvm use springboot dev
```
