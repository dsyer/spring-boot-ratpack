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

package ratpack.spring.annotation;

import java.io.File;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import ratpack.launch.LaunchConfig;
import ratpack.launch.LaunchConfigBuilder;
import ratpack.server.RatpackServer;
import ratpack.server.RatpackServerBuilder;
import ratpack.spring.internal.ChainConfigurers;
import ratpack.spring.internal.SpringBackedHandlerFactory;

/**
 * @author Dave Syer
 * 
 */
@Configuration
@Import(ChainConfigurers.class)
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

		@Bean
		@ConditionalOnMissingBean
		public LaunchConfig ratpackLaunchConfig(ApplicationContext context) {
			return LaunchConfigBuilder.baseDir(new File(".")).build(
					new SpringBackedHandlerFactory(context));
		}

	}

	@Configuration
	protected static class ServerConfiguration {

		@Autowired
		private LaunchConfig launchConfig;

		@Bean
		@ConditionalOnMissingBean
		public RatpackServer ratpackServer() {
			RatpackServer server = RatpackServerBuilder.build(launchConfig);
			return server;
		}

	}

}
