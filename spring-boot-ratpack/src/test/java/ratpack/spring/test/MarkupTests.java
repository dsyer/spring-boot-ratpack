package ratpack.spring.test;

import static org.junit.Assert.assertTrue;
import static ratpack.groovy.Groovy.groovyMarkupTemplate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.server.RatpackServer;
import ratpack.spring.test.MarkupTests.Application;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest("server.port=0")
public class MarkupTests {

	private TestRestTemplate restTemplate = new TestRestTemplate();

	@Autowired
	private RatpackServer server;

	@Test
	public void contextLoads() {
		String body = restTemplate.getForObject("http://localhost:" + server.getBindPort(), String.class);
		assertTrue("Wrong body" + body, body.contains("<body>Home"));
	}

	@Configuration
	@EnableAutoConfiguration
	protected static class Application {
		
		@Bean
		public Handler handler() {
			return new Handler() {
				@Override
				public void handle(Context context) throws Exception {
					context.render(groovyMarkupTemplate("markup.html"));
				}
			};
		}

		public static void main(String[] args) throws Exception {
			SpringApplication.run(Application.class, args);
		}

	}

}