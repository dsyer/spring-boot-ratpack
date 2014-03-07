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

package ratpack.spring.internal;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import ratpack.handling.Handler;
import ratpack.handling.Handlers;
import ratpack.handling.internal.ClientErrorForwardingHandler;
import ratpack.launch.LaunchConfig;

/**
 * @author Dave Syer
 * 
 */
public class SpringBackedHandlerFactory {

	private LaunchConfig launchConfig;

	private SpringApplicationBuilder builder = new SpringApplicationBuilder();

	private String[] args;

	private Object[] sources;

	public SpringBackedHandlerFactory(LaunchConfig launchConfig) {
	}

	public SpringBackedHandlerFactory(LaunchConfig launchConfig, String[] args,
			Object[] sources) {
		this.launchConfig = launchConfig;
		this.args = args;
		this.sources = sources;
	}

	public Handler create() throws Exception {
		builder.sources(sources);
		ConfigurableApplicationContext context = builder.run(args);
		SpringBackedRegistry registry = new SpringBackedRegistry(context);
		return Handlers.chain(
				Handlers.chain(launchConfig, registry,
						registry.get(ChainConfigurers.class)),
				new ClientErrorForwardingHandler(404));
	}

}
