package demo.spring;

import java.io.File;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.handling.Handler;
import ratpack.launch.HandlerFactory;
import ratpack.launch.LaunchConfig;
import ratpack.launch.LaunchConfigBuilder;
import ratpack.registry.Registry;
import ratpack.server.RatpackServerBuilder;
import ratpack.spring.Spring;

@Configuration
class ServiceApplication {
	
	@Bean
	public SomeService service() {
		return new SomeService();
	}
	
	@Bean
	public InjectedHandler handler() {
		return new InjectedHandler(service());
	}

}

class ApplicationBootstrap implements Action<SpringApplicationBuilder> {
	public void execute(SpringApplicationBuilder modules) {
		modules.sources(ServiceApplication.class);
	}
}

class HandlersBootstrap implements Action<Chain> {
	public void execute(Chain chain) {
		Registry registry = chain.getRegistry();
		Handler injectedHandler = registry.get(InjectedHandler.class);
		chain.get("", injectedHandler);
	}
}

class MyHandlerFactory implements HandlerFactory {
	public Handler create(LaunchConfig launchConfig) throws Exception {
		return Spring
				.handler(launchConfig, new ApplicationBootstrap(), new HandlersBootstrap());
	}
}

public class Application {
	public static void main(String[] args) throws Exception {
		LaunchConfig launchConfig = LaunchConfigBuilder.baseDir(new File("."))
				.build(new MyHandlerFactory());
		RatpackServerBuilder.build(launchConfig).start();
	}
}
