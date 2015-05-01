package demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.server.RatpackServer;
import demo.ApplicationTests.Application;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest({ "debug=true", "server.port=0" })
public class ApplicationTests {

	private TestRestTemplate restTemplate = new TestRestTemplate();

	@Autowired
	private RatpackServer server;

	@Test
	public void homePage() {
		assertEquals(
				"{" + System.getProperty("line.separator")
						+ "  \"message\" : \"Hello World\""
						+ System.getProperty("line.separator") + "}",
				restTemplate.getForObject("http://localhost:" + server.getBindPort(),
						String.class));
	}

	@Test
	public void notFound() {
		ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:"
				+ server.getBindPort() + "/none", String.class);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNull("Default 404 handler has null body", response.getBody());
	}

	@Test
	public void managementEndpoints() {
		ResponseEntity<String> result = restTemplate.getForEntity("http://localhost:"
				+ server.getBindPort() + "/dump", String.class);
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}

	@Configuration
	@EnableAutoConfiguration
	@Import(MessageService.class)
	protected static class Application {

		@Autowired
		private MessageService service;

		@Bean
		public Action<Chain> handler() {
			return chain -> chain.get(context -> {
				// We're not using the registry here directly but it's good to confirm
				// that it contains our service:
					assertNotNull(context.get(MessageService.class));
					context.render(json(Collections.singletonMap("message",
							service.message())));
				});
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