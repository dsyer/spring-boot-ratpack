package demo;

import static ratpack.jackson.Jackson.json;

import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.spring.annotation.EnableRatpack;


@Configuration
@EnableAutoConfiguration
@EnableRatpack
public class Application {
	
	@Bean
	public Handler handler() {
		return new Handler() {
			@Override
			public void handle(Context context) throws Exception {
				context.render(json(Collections.singletonMap("message", "Hello World")));
			}
		};
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}

}
