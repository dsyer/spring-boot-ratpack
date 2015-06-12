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

package ratpack.spring.autoconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ratpack.groovy.template.MarkupTemplateModule;
import ratpack.groovy.template.TextTemplateModule;
import ratpack.groovy.template.internal.TextTemplateRenderingEngine;
import ratpack.spring.config.EnableRatpack;
import ratpack.spring.config.RatpackProperties;

/**
 * @author Dave Syer
 *
 */
@Configuration
@EnableRatpack
public class RatpackAutoConfiguration {

	@Configuration
	@ConditionalOnClass(TextTemplateRenderingEngine.class)
	@ConditionalOnResource(resources = "${ratpack.templatesPath:templates}")
	protected static class GroovyTemplateConfiguration {

		@Autowired
		private RatpackProperties ratpack;

		@Bean
		@ConditionalOnMissingBean
		public MarkupTemplateModule markupTemplateGuiceModule() {
			MarkupTemplateModule module = new MarkupTemplateModule();
			module.configure(config -> {
				config.setTemplatesDirectory(this.ratpack.getTemplatesPath());
			});
			return module;
		}

		@Bean
		@ConditionalOnMissingBean
		public TextTemplateModule textTemplateGuiceModule() {
			TextTemplateModule module = new TextTemplateModule();
			module.configure(config -> {
				config.setTemplatesPath(this.ratpack.getTemplatesPath());
				config.setStaticallyCompile(this.ratpack.isStaticallyCompile());
			});
			return module;
		}

	}

}
