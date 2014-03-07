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

import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.handling.Handler;
import ratpack.handling.Handlers;
import ratpack.handling.internal.ClientErrorForwardingHandler;
import ratpack.launch.LaunchConfig;

/**
 * @author Dave Syer
 * 
 */
public class DefaultSpringBackedHandlerFactory {

	private LaunchConfig launchConfig;

	public DefaultSpringBackedHandlerFactory(LaunchConfig launchConfig) {
		this.launchConfig = launchConfig;
	}

	public Handler create(Action<? super SpringApplicationBuilder> applicationAction,
			Action<? super Chain> chainConfigurer) throws Exception {
		SpringApplicationBuilder builder = new SpringApplicationBuilder();
		applicationAction.execute(builder); // TODO : apply launch config
		return Handlers.chain(Handlers.chain(launchConfig, new SpringBackedRegistry(
				builder.run()), chainConfigurer), new ClientErrorForwardingHandler(404));
	}

}
