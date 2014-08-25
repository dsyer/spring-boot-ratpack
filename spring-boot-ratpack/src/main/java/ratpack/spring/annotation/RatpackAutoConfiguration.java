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

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.HttpMapperProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import ratpack.exec.ExecControl;
import ratpack.groovy.templating.TemplatingConfig;
import ratpack.groovy.templating.internal.DefaultTemplatingConfig;
import ratpack.groovy.templating.internal.GroovyTemplateRenderingEngine;
import ratpack.groovy.templating.internal.TemplateRenderer;
import ratpack.groovy.templating.internal.TemplateRenderingClientErrorHandler;
import ratpack.groovy.templating.internal.TemplateRenderingServerErrorHandler;
import ratpack.jackson.JsonRenderer;
import ratpack.jackson.internal.DefaultJsonRenderer;
import ratpack.jackson.internal.JsonParser;
import ratpack.launch.LaunchConfig;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Dave Syer
 * 
 */
@Configuration
@EnableRatpack
public class RatpackAutoConfiguration {

	@Configuration
	@ConditionalOnClass(ObjectMapper.class)
	@EnableConfigurationProperties(HttpMapperProperties.class)
	protected static class ObjectMappers {

		@Autowired
		private HttpMapperProperties properties = new HttpMapperProperties();

		@Autowired
		private ListableBeanFactory beanFactory;

		@PostConstruct
		public void init() {
			Collection<ObjectMapper> mappers = BeanFactoryUtils
					.beansOfTypeIncludingAncestors(this.beanFactory,
							ObjectMapper.class).values();
			Collection<Module> modules = BeanFactoryUtils
					.beansOfTypeIncludingAncestors(this.beanFactory,
							Module.class).values();
			for (ObjectMapper mapper : mappers) {
				mapper.registerModules(modules);
				if (!mapper.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
					mapper.configure(SerializationFeature.INDENT_OUTPUT,
							properties.isJsonPrettyPrint());
				}
			}
		}

		@Bean
		@ConditionalOnMissingBean
		@Primary
		public ObjectMapper jacksonObjectMapper() {
			return new ObjectMapper();
		}

		@Bean
		@ConditionalOnMissingBean
		public JsonRenderer jsonRenderer() {
			return new DefaultJsonRenderer(jacksonObjectMapper().writer());
		}

		@Bean
		@ConditionalOnMissingBean
		public JsonParser jsonParser() {
			return new JsonParser(jacksonObjectMapper());
		}

	}

	@Configuration
	@ConditionalOnClass(GroovyTemplateRenderingEngine.class)
	protected static class GroovyTemplateConfiguration {

		@Autowired
		private RatpackProperties ratpack;

		@Autowired
		private LaunchConfig launchConfig;

		@Autowired
		private ExecControl execControl;

		@Bean
		@ConditionalOnMissingBean
		public TemplateRenderer groovyTemplateRenderer() {
			return new TemplateRenderer(groovyTemplateRenderingEngine(),
					launchConfig.getBufferAllocator());
		}

		@Bean
		@ConditionalOnMissingBean
		public GroovyTemplateRenderingEngine groovyTemplateRenderingEngine() {
			return new GroovyTemplateRenderingEngine(launchConfig,
					templatingConfig(), execControl);
		}

		@Bean
		@ConditionalOnMissingBean
		public TemplatingConfig templatingConfig() {
			return new DefaultTemplatingConfig(ratpack.getTemplatesPath(),
					ratpack.getCacheSize(), ratpack.isReloadable()
							|| launchConfig.isReloadable(),
					ratpack.isStaticallyCompile());
		}

		@Bean
		protected TemplateRenderingClientErrorHandler clientErrorHandler() {
			return new TemplateRenderingClientErrorHandler(
					launchConfig.getBufferAllocator(),
					groovyTemplateRenderingEngine());
		}

		@Bean
		protected TemplateRenderingServerErrorHandler serverErrorHandler() {
			return new TemplateRenderingServerErrorHandler(
					launchConfig.getBufferAllocator(),
					groovyTemplateRenderingEngine());
		}

	}

}
