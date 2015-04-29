package demo;

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
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.render.RendererSupport;
import ratpack.server.RatpackServer;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CustomRendererTests.Application.class)
@IntegrationTest("server.port=0")
public class CustomRendererTests {

	private TestRestTemplate restTemplate = new TestRestTemplate();

	@Autowired
	private RatpackServer server;

	@Test
	public void contextLoads() {
		String body = restTemplate.getForObject("http://localhost:" + server.getBindPort(), String.class);
		assertEquals("Hello World", body);
	}

	@Configuration
	@EnableAutoConfiguration
	@Import(MessageRenderer.class)
	protected static class Application {

		@Bean
		public Handler handler() {
			return context -> context.render(new Message("Hello World"));
		}

		public static void main(String[] args) throws Exception {
			SpringApplication.run(Application.class, args);
		}
	}

	@Component
	protected static class MessageRenderer extends RendererSupport<Message> {
		@Override
		public void render(Context context, Message message) throws Exception {
			context.getResponse().send(message.getValue());
		}
	}

	protected static class Message {
		private final String value;

		public Message(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

}
