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

package ratpack.spring.groovy;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ratpack.groovy.Groovy;
import ratpack.spring.autoconfig.RatpackAutoConfiguration;
import ratpack.spring.groovy.internal.RatpackScriptServerCustomizer;

/**
 * @author Dave Syer
 * 
 */
@Configuration
@ConditionalOnClass(Groovy.class)
@AutoConfigureAfter(RatpackAutoConfiguration.class)
public class RatpackGroovyAutoConfiguration {

	@Bean
	public RatpackScriptServerCustomizer ratpackScriptActionFactory() {
		return new RatpackScriptServerCustomizer();
	}

}
