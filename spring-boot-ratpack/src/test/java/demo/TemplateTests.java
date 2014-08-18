package demo;

import static org.junit.Assert.assertTrue;
import static ratpack.spring.groovy.Groovy.groovyTemplate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.spring.annotation.EnableRatpack;
import demo.TemplateTests.Application;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest
@DirtiesContext
public class TemplateTests {

	private TestRestTemplate restTemplate = new TestRestTemplate();

	@Test
	public void contextLoads() {
		String body = restTemplate.getForObject("http://localhost:5050/", String.class);
		assertTrue("Wrong body" + body, body.contains("<body>Home"));
	}

	@ComponentScan
	@Configuration
	@EnableAutoConfiguration
	@EnableRatpack
	protected static class Application {

		@Bean
		public Handler handler() {
			return new Handler() {
				@Override
				public void handle(Context context) throws Exception {
					context.render(groovyTemplate("index.html"));
				}
			};
		}

		public static void main(String[] args) throws Exception {
			SpringApplication.run(Application.class, args);
		}

	}

}