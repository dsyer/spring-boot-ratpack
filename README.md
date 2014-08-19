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
[Spring Boot CLI](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#getting-started-installing-the-cli).

Since the Ratpack extensions are not part of the main Spring Boot
feature set you will need to clone from
[this repo](https://github.com/dsyer/spring-boot-ratpack) and build it
locally, e.g.

```
$ git clone https://github.com/dsyer/spring-boot-ratpack
$ cd spring-boot-ratpack
$ mvn install
```

Once it is built, you can install the ratpack plugin. You either need
to build Spring Boot or download a snapshot build of the CLI first,
e.g.

```
$ mkdir -p /tmp/spring && cd $_
$ wget -O spring.tgz https://repo.spring.io/libs-snapshot-local/org/springframework/boot/spring-boot-cli/1.2.0.BUILD-SNAPSHOT/spring-boot-cli-1.2.0.BUILD-SNAPSHOT-bin.tar.gz
$ tar -zxf spring.tgz
```

The `spring` CLI is now installed at `/tmp/spring/spring-boot-cli-1.2.0.BUILD-SNAPSHOT`, so if you are a gvm user you can do this:

```
$ gvm install springboot ratpack `pwd`/spring-boot-cli-1.2.0.BUILD-SNAPSHOT
$ gvm use springboot ratpack
```

and then you can install the `spring-boot-ratpack-cli` jar:

```
$ spring install org.springframework.boot:spring-boot-ratpack-cli:1.0.0.BUILD-SNAPSHOT
```

