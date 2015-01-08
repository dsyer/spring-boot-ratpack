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
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import ratpack.exec.ExecControl;
import ratpack.groovy.template.internal.TextTemplateRenderer;
import ratpack.groovy.template.internal.TextTemplateRenderingEngine;
import ratpack.jackson.internal.JsonParser;
import ratpack.jackson.internal.JsonRenderer;
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
	@EnableConfigurationProperties(JacksonProperties.class)
	protected static class ObjectMappers {

		@Autowired
		private JacksonProperties properties = new JacksonProperties();

		@Autowired
		private ListableBeanFactory beanFactory;

		@PostConstruct
		public void init() {
			Collection<ObjectMapper> mappers = BeanFactoryUtils
					.beansOfTypeIncludingAncestors(this.beanFactory, ObjectMapper.class)
					.values();
			Collection<Module> modules = BeanFactoryUtils.beansOfTypeIncludingAncestors(
					this.beanFactory, Module.class).values();
			for (ObjectMapper mapper : mappers) {
				mapper.registerModules(modules);
				if (!mapper.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
					Boolean prettyPrint = properties.getSerialization().get(
							SerializationFeature.INDENT_OUTPUT);
					if (prettyPrint != null) {
						mapper.configure(SerializationFeature.INDENT_OUTPUT, prettyPrint);
					}
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
			return new JsonRenderer(jacksonObjectMapper().writer());
		}

		@Bean
		@ConditionalOnMissingBean
		public JsonParser jsonParser() {
			return new JsonParser(jacksonObjectMapper());
		}

	}

	@Configuration
	@ConditionalOnClass(TextTemplateRenderingEngine.class)
	protected static class GroovyTemplateConfiguration {

		@Autowired
		private RatpackProperties ratpack;

		@Autowired
		private LaunchConfig launchConfig;

		@Autowired
		private ExecControl execControl;

		@Bean
		@ConditionalOnMissingBean
		public TextTemplateRenderer groovyTemplateRenderer() {
			return new TextTemplateRenderer(groovyTemplateRenderingEngine());
		}

		@Bean
		@ConditionalOnMissingBean
		public TextTemplateRenderingEngine groovyTemplateRenderingEngine() {
			return new TextTemplateRenderingEngine(execControl,
					launchConfig.getBufferAllocator(), launchConfig.getBaseDir().binding(
							ratpack.getTemplatesPath()), ratpack.isDevelopment()
							|| launchConfig.isDevelopment(),
					ratpack.isStaticallyCompile());
		}

	}

}
