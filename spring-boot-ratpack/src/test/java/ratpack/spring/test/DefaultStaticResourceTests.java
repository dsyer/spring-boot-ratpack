package ratpack.spring.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ratpack.server.RatpackServer;
import ratpack.spring.test.DefaultStaticResourceTests.Application;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest("server.port=0")
public class DefaultStaticResourceTests {

	private TestRestTemplate restTemplate = new TestRestTemplate();

	@Autowired
	private RatpackServer server;

	@Test
	public void contextLoads() {
		ResponseEntity<String> result = restTemplate.getForEntity(
				"http://localhost:" + server.getBindPort() + "/main.css",
				String.class);
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertTrue("Wrong body" + result.getBody(),
				result.getBody().contains("background: red;"));
	}

	@Configuration
	@EnableAutoConfiguration
	protected static class Application {
		public static void main(String[] args) throws Exception {
			SpringApplication.run(Application.class, args);
		}
	}

}