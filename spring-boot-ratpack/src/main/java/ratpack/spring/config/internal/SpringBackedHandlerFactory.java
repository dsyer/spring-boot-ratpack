/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ratpack.spring.config.internal;

import org.springframework.context.ApplicationContext;

import ratpack.func.Action;
import ratpack.guice.BindingsSpec;
import ratpack.guice.Guice;
import ratpack.handling.Handler;
import ratpack.handling.Handlers;
import ratpack.handling.internal.ClientErrorForwardingHandler;
import ratpack.launch.HandlerFactory;
import ratpack.launch.LaunchConfig;
import ratpack.registry.Registry;
import ratpack.spring.Spring;
import ratpack.spring.groovy.internal.RatpackScriptActionFactory;

import com.google.inject.Module;

/**
 * @author Dave Syer
 * 
 */
public class SpringBackedHandlerFactory implements HandlerFactory {

	private ApplicationContext context;

	public SpringBackedHandlerFactory(ApplicationContext context) {
		this.context = context;
	}

	@Override
	public Handler create(LaunchConfig launchConfig) throws Exception {
		Registry registry = Spring.spring(context);
		Action<BindingsSpec> action = registry.get(RatpackScriptActionFactory.class).getBindings();
		Handler handler = Guice.builder(launchConfig).bindings(bindings -> {
			for (Module module : registry.getAll(Module.class)) {
				bindings.add(module);
			}
			action.execute(bindings);
		}).build(registry.get(ChainConfigurers.class));
		return Handlers.chain(Handlers.register(registry, handler),
				new ClientErrorForwardingHandler(404));
	}

}
