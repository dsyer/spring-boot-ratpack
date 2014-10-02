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

import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.handling.Handlers;
import ratpack.handling.internal.ClientErrorForwardingHandler;
import ratpack.launch.HandlerFactory;
import ratpack.launch.LaunchConfig;
import ratpack.registry.Registry;
import ratpack.spring.Spring;

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
		return Handlers.chain(
				new RegistryHandler(registry, Handlers.chain(launchConfig, registry,
						registry.get(ChainConfigurers.class))),
				new ClientErrorForwardingHandler(404));
	}
	
	private static class RegistryHandler implements Handler {

		private Registry registry;
		private Handler chain;

		public RegistryHandler(Registry registry, Handler chain) {
			this.registry = registry;
			this.chain = chain;
		}

		@Override
		public void handle(Context context) throws Exception {
			context.insert(registry, chain);
		}
		
	}

}
