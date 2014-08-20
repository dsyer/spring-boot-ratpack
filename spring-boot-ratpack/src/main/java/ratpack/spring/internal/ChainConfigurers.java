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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import ratpack.func.Action;
import ratpack.handling.Chain;
import ratpack.handling.Handler;
import ratpack.spring.groovy.internal.RatpackScriptActionFactory;

/**
 * @author Dave Syer
 * 
 */
@Configuration
@ConditionalOnMissingBean(ChainConfigurers.class)
@ConditionalOnClass(Chain.class)
public class ChainConfigurers implements Action<Chain> {

	@Autowired(required=false)
	private List<Action<Chain>> delegates = Collections.emptyList();

	@Autowired(required=false)
	private List<Handler> handlers = Collections.emptyList();
	
	@Bean
	protected RatpackScriptActionFactory ratpackScriptBacking() {
		return new RatpackScriptActionFactory();
	}

	public void execute(Chain chain) throws Exception {
		List<Action<Chain>> delegates = new ArrayList<Action<Chain>>(this.delegates);
		if (delegates.isEmpty()) {
			delegates = ratpackScriptBacking().getHandlerActions();
		}
		if (handlers.size() == 1) {
			delegates.add(singleHandlerAction());
		}
		else if (delegates.isEmpty()) {
			delegates = Arrays.asList(singleHandlerAction());
		}
		AnnotationAwareOrderComparator.sort(delegates);
		for (Action<Chain> delegate : delegates) {
			if (!(delegate instanceof ChainConfigurers)) {
				delegate.execute(chain);
			}
		}
	}

	private Action<Chain> singleHandlerAction() {
		return new Action<Chain>() {
			public void execute(Chain chain) {
				if (handlers.size() == 1) {
					chain.get(handlers.get(0));
				} else {
					throw new IllegalStateException(
							"No Action<Chain> defined to expecting a single Handler (found "
									+ handlers.size() + ")");
				}
			}
		};
	}

}
