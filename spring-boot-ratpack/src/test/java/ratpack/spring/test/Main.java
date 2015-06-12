/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package ratpack.spring.test;

import static ratpack.jackson.Jackson.json;

import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.spring.config.EnableRatpack;

/**
 * Quick test app to verify a sample in Ractpack documentation.
 *
 * @author Dave Syer
 *
 */
@SpringBootApplication
@EnableRatpack
public class Main {

	@Bean
	public Action<Chain> home() {
		return chain -> chain.get(ctx -> ctx.render(json(Collections.singletonMap("message", "Hello"))));
	}

	@Bean
	public Service service() {
		return () -> "World";
	}

	public static void main(String... args) throws Exception {
		SpringApplication.run(Main.class, args);
	}

}

interface Service {
	String message();
}
