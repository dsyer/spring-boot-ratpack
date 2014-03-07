package demo.guice;

import java.io.File;

import ratpack.func.Action;
import ratpack.guice.Guice;
import ratpack.guice.ModuleRegistry;
import ratpack.handling.Chain;
import ratpack.handling.Handler;
import ratpack.launch.HandlerFactory;
import ratpack.launch.LaunchConfig;
import ratpack.launch.LaunchConfigBuilder;
import ratpack.registry.Registry;
import ratpack.server.RatpackServerBuilder;

import com.google.inject.AbstractModule;

class ServiceModule extends AbstractModule {
	protected void configure() {
		bind(SomeService.class);
	}
}

class ModuleBootstrap implements Action<ModuleRegistry> {
	public void execute(ModuleRegistry modules) {
		modules.register(new ServiceModule());
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
		return Guice
				.handler(launchConfig, new ModuleBootstrap(), new HandlersBootstrap());
	}
}

public class Application {
	public static void main(String[] args) throws Exception {
		LaunchConfig launchConfig = LaunchConfigBuilder.baseDir(new File(".")).build(
				new MyHandlerFactory());
		RatpackServerBuilder.build(launchConfig).start();
	}
}
