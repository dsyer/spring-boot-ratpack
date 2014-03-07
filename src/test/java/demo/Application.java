package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.spring.annotation.EnableRatpack;

@Configuration
@EnableAutoConfiguration
@EnableRatpack
public class Application implements Action<Chain> {

	public void execute(Chain chain) {
		chain.get(handler());
	}

	@Bean
	public Handler handler() {
		return new Handler() {
			@Override
			public void handle(Context context) throws Exception {
				context.render("Hello World");
			}
		};
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}

}
