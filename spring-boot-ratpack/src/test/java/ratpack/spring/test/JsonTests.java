package ratpack.spring.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ratpack.jackson.Jackson.fromJson;
import static ratpack.jackson.Jackson.json;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.handling.Handler;
import ratpack.server.RatpackServer;
import ratpack.spring.test.JsonTests.Application;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest("server.port=0")
public class JsonTests {

	private TestRestTemplate restTemplate = new TestRestTemplate();

	@Autowired
	private RatpackServer server;

	@Test
	public void get() {
		String body = this.restTemplate
				.getForObject("http://localhost:" + this.server.getBindPort(), String.class);
		assertTrue("Wrong body" + body, body.contains("{"));
		assertFalse("Wrong body" + body, body.toLowerCase().contains("<html"));
	}

	@Test
	public void post() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Map<String, String>> entity = new HttpEntity<Map<String, String>>(
				Collections.singletonMap("foo", "bar"), headers);
		ResponseEntity<String> result = this.restTemplate.postForEntity(
				"http://localhost:" + this.server.getBindPort(), entity, String.class);
		assertEquals(HttpStatus.OK, result.getStatusCode());
		String body = this.restTemplate
				.getForObject("http://localhost:" + this.server.getBindPort(), String.class);
		assertTrue("Wrong body" + body, body.contains("foo"));
	}

	@Configuration
	@EnableAutoConfiguration
	protected static class Application {

		private Map<String, Object> map = new LinkedHashMap<String, Object>();

		@Bean
		public Action<Chain> chain() {
			return chain -> chain.all(handler());
		}

		@SuppressWarnings("unchecked")
		@Bean
		public Handler handler() {
			// @formatter:off
			return context -> context.byMethod(spec -> spec
				.get(
					() -> context.render(json(this.map))
				).post(
					() -> {
						context.parse(fromJson(Map.class)).then(m -> {
							this.map.putAll(m);
							context.render(json(this.map));
						});
					}
				)
			);
			// @formatter:on
		}

		public static void main(String[] args) throws Exception {
			SpringApplication.run(Application.class, args);
		}

	}

}