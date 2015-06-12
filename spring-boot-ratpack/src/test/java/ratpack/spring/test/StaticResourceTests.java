package ratpack.spring.test;

import static org.junit.Assert.assertTrue;

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

import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.server.RatpackServer;
import ratpack.spring.test.StaticResourceTests.Application;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@IntegrationTest("server.port=0")
public class StaticResourceTests {

	private TestRestTemplate restTemplate = new TestRestTemplate();

	@Autowired
	private RatpackServer server;

	@Test
	public void contextLoads() {
		String body = this.restTemplate.getForObject(
				"http://localhost:" + this.server.getBindPort() + "/root/main.css",
				String.class);
		assertTrue("Wrong body" + body, body.contains("background"));
	}

	@Configuration
	@EnableAutoConfiguration
	protected static class Application {

		@Bean
		public Action<Chain> handlers() {
			return new Action<Chain>() {
				@Override
				public void execute(Chain chain) throws Exception {
					chain.prefix("root", new Action<Chain>() {
						@Override
						public void execute(Chain chain) throws Exception {
							chain.files(
									spec -> spec.dir("root").indexFiles("index.html"));
						}
					});
				}
			};
		}

		public static void main(String[] args) throws Exception {
			SpringApplication.run(Application.class, args);
		}

	}

}