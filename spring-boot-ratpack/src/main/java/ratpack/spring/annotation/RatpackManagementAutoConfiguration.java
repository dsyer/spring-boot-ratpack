/*
 * Copyright 2013-2014 the original author or authors.
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

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.jackson.Jackson;

/**
 * @author Dave Syer
 *
 */
@Configuration
@ConditionalOnClass(EndpointAutoConfiguration.class)
@AutoConfigureAfter({ RatpackAutoConfiguration.class,
		EndpointAutoConfiguration.class })
@EnableConfigurationProperties
public class RatpackManagementAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public ManagementServerProperties managementServerProperties() {
		return new ManagementServerProperties();
	}

	@Bean
	protected EndpointInitializer ratpackEndpointInitializer() {
		return new EndpointInitializer();
	}

	private static class EndpointInitializer implements Action<Chain> {

		@Autowired
		private List<Endpoint<?>> endpoints = Collections.emptyList();

		@Autowired
		private ManagementServerProperties management;

		@PostConstruct
		public void init() {

		}

		@Override
		public void execute(Chain chain) throws Exception {
			String prefix = management.getContextPath();
			if (StringUtils.hasText(prefix)) {
				prefix = prefix.endsWith("/") ? prefix : prefix + "/";
			} else {
				prefix = "";
			}
			for (Endpoint<?> endpoint : endpoints) {
				final Endpoint<?> point = endpoint;
				if (point.isEnabled()) {
					chain.get(prefix + endpoint.getId(), new Handler() {
						@Override
						public void handle(Context context) throws Exception {
							// TODO: content types?
							context.render(Jackson.json(point.invoke()));
						}
					});
				}
			}
		}

	}

}
