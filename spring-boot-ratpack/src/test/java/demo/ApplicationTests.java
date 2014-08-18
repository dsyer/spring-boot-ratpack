package demo;

import static org.junit.Assert.assertEquals;
import static ratpack.jackson.Jackson.json;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.spring.annotation.EnableRatpack;
import demo.ApplicationTests.Application;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest
@DirtiesContext
public class ApplicationTests {

	private TestRestTemplate restTemplate = new TestRestTemplate();

	@Test
	public void contextLoads() {
		assertEquals("{\"message\":\"Hello World\"}",
				restTemplate.getForObject("http://localhost:5050/", String.class));
	}

	@ComponentScan
	@Configuration
	@EnableAutoConfiguration
	@EnableRatpack
	protected static class Application {

		@Autowired
		private MessageService service;

		@Bean
		public Handler handler() {
			return new Handler() {
				@Override
				public void handle(Context context) throws Exception {
					context.render(json(Collections.singletonMap("message",
							service.message())));
				}
			};
		}

		public static void main(String[] args) throws Exception {
			SpringApplication.run(Application.class, args);
		}

	}

	@Service
	protected static class MessageService {

		public String message() {
			return "Hello World";
		}

	}

}