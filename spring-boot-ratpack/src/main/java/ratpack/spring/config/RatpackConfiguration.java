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

package ratpack.spring.config;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import ratpack.launch.LaunchConfig;
import ratpack.launch.LaunchConfigBuilder;
import ratpack.server.RatpackServer;
import ratpack.server.RatpackServerBuilder;
import ratpack.spring.config.internal.ChainConfigurers;
import ratpack.spring.config.internal.SpringBackedHandlerFactory;

/**
 * @author Dave Syer
 * 
 */
@Configuration
@Import(ChainConfigurers.class)
@EnableConfigurationProperties(RatpackProperties.class)
public class RatpackConfiguration implements CommandLineRunner {

	@Autowired
	private RatpackServer server;

	@Override
	public void run(String... args) throws Exception {
		server.start();
	}

	@PreDestroy
	public void stop() throws Exception {
		server.stop();
	}

	@Configuration
	protected static class LaunchConfiguration {

		@Autowired
		private RatpackProperties ratpack;

		@Bean
		@ConditionalOnMissingBean
		public LaunchConfig ratpackLaunchConfig(ApplicationContext context) {
			// @formatter:off
			LaunchConfigBuilder builder = LaunchConfigBuilder
					.baseDir(ratpack.getBasepath())
					.address(ratpack.getAddress())
					.threads(ratpack.getMaxThreads());
			// @formatter:on
			if (ratpack.getPort() != null) {
				builder.port(ratpack.getPort());
			}
			return builder.build(new SpringBackedHandlerFactory(context));
		}

	}

	@Configuration
	@ConditionalOnMissingBean(RatpackServer.class)
	protected static class ServerConfiguration {

		@Autowired
		private LaunchConfig launchConfig;

		@Bean
		public RatpackServer ratpackServer() {
			RatpackServer server = RatpackServerBuilder.build(launchConfig);
			return server;
		}
		
	}

}
