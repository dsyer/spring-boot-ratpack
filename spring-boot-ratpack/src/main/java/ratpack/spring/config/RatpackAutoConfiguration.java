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

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ratpack.groovy.template.MarkupTemplateModule;
import ratpack.groovy.template.TextTemplateModule;
import ratpack.groovy.template.internal.TextTemplateRenderingEngine;
import ratpack.jackson.JacksonModule;

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

		@Autowired(required = false)
		private List<Module> modules = Collections.emptyList();

		@Bean
		@ConditionalOnMissingBean
		public JacksonModule jacksonGuiceModule() {
			Boolean prettyPrint = properties.getSerialization().get(
					SerializationFeature.INDENT_OUTPUT);
			JacksonModule module = new JacksonModule();
			module.configure(config -> {
				if (prettyPrint != null) {
					config.prettyPrint(prettyPrint);
				}
				config.modules(modules);
			});
			return module;
		}

	}

	@Configuration
	@ConditionalOnClass(TextTemplateRenderingEngine.class)
	protected static class GroovyTemplateConfiguration {

		@Autowired
		private RatpackProperties ratpack;

		@Bean
		@ConditionalOnMissingBean
		public MarkupTemplateModule markupTemplateGuiceModule() {
			MarkupTemplateModule module = new MarkupTemplateModule();
			module.configure(config -> {
				config.setTemplatesDirectory(ratpack.getTemplatesPath());
			});
			return module;
		}

		@Bean
		@ConditionalOnMissingBean
		public TextTemplateModule textTemplateGuiceModule() {
			TextTemplateModule module = new TextTemplateModule();
			module.configure(config -> {
				config.setTemplatesPath(ratpack.getTemplatesPath());
				config.setStaticallyCompile(ratpack.isStaticallyCompile());
			});
			return module;
		}

	}

}
