package ratpack.spring.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ratpack.jackson.Jackson.fromJson;
import static ratpack.jackson.Jackson.json;

import java.util.Collections;
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
import ratpack.spring.test.JsonMapperTests.Application;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest("server.port=0")
public class JsonMapperTests {

	private TestRestTemplate restTemplate = new TestRestTemplate();

	@Autowired
	private RatpackServer server;

	@Test
	public void get() {
		String body = restTemplate.getForObject(
				"http://localhost:" + server.getBindPort(), String.class);
		assertTrue("Wrong body" + body, body.contains("x"));
		assertFalse("Wrong body" + body, body.toLowerCase().contains("<html"));
	}

	@Test
	public void post() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Map<String, String>> entity = new HttpEntity<Map<String, String>>(
				Collections.singletonMap("x", "1.0"), headers);
		ResponseEntity<String> result = restTemplate.postForEntity("http://localhost:"
				+ server.getBindPort(), entity, String.class);
		assertEquals(HttpStatus.OK, result.getStatusCode());
		String body = restTemplate.getForObject(
				"http://localhost:" + server.getBindPort(), String.class);
		assertTrue("Wrong body" + body, body.contains("x"));
	}

	@Configuration
	@EnableAutoConfiguration
	protected static class Application {

		private Point point = new Point(0, 0);

		@SuppressWarnings("serial")
		@Bean
		public Module jacksonModule() {
			return new SimpleModule() {
				{
					setMixInAnnotation(Point.class, PointMixin.class);
				}
			};
		}

		@Bean
		public Action<Chain> chain() {
			return chain -> chain.all(handler());
		}

		@Bean
		public Handler handler() {
			return context -> {
				context.byMethod(spec -> {
					// @formatter:off	
					spec
						.get(() -> context.render(json(point)))
						.post(() -> {
							point = context.parse(fromJson(Point.class));
							context.render(json(point));
						});
					// @formatter:on
				});
			};
		}

		public static void main(String[] args) throws Exception {
			SpringApplication.run(Application.class, args);
		}

		static abstract class PointMixin {
			PointMixin(@JsonProperty("x") double x, @JsonProperty("y") double y) {
			}
		}

		static class Point {
			private final double x;
			private final double y;

			public Point(double x, double y) {
				this.x = x;
				this.y = y;
			}

			public double getX() {
				return x;
			}

			public double getY() {
				return y;
			}
		}

	}

}