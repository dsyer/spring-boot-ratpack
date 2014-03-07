package demo.spring;

import java.io.File;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.launch.LaunchConfig;
import ratpack.launch.LaunchConfigBuilder;
import ratpack.server.RatpackServerBuilder;
import ratpack.spring.Spring;
import ratpack.spring.annotation.EnableRatpack;

@Configuration
@EnableAutoConfiguration
@EnableRatpack
class Application implements Action<Chain> {

	public void execute(Chain chain) {
		chain.get("", handler());
	}

	@Bean
	public SomeService service() {
		return new SomeService();
	}

	@Bean
	public InjectedHandler handler() {
		return new InjectedHandler(service());
	}

	public static void main(String[] args) throws Exception {
		LaunchConfig launchConfig = LaunchConfigBuilder.baseDir(new File(".")).build(
				Spring.handlers(args, Application.class));
		RatpackServerBuilder.build(launchConfig).start();
	}

}
