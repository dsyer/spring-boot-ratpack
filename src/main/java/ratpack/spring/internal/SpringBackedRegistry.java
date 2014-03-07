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
import java.util.List;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;

import ratpack.registry.NotInRegistryException;
import ratpack.registry.Registry;

public class SpringBackedRegistry implements Registry {

	final ApplicationContext context;

	public SpringBackedRegistry(ApplicationContext context) {
		this.context = context;
	}

	@Override
	public <O> O get(Class<O> type) throws NotInRegistryException {
		O object = maybeGet(type);
		if (object == null) {
			throw new NotInRegistryException(type);
		} else {
			return object;
		}
	}

	@Override
	public <O> O maybeGet(Class<O> type) {
		List<O> beans = getAll(type);
		if (beans.isEmpty()) {
			return null;
		}
		if (beans.size()==1) {
			return beans.get(0);
		}
		throw new IllegalStateException("Multiple beans of type " + type + " found");
	}

	@Override
	public <O> List<O> getAll(Class<O> type) {
		return new ArrayList<O>(BeanFactoryUtils.beansOfTypeIncludingAncestors(context,
				type, true, true).values());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		SpringBackedRegistry that = (SpringBackedRegistry) o;

		return context.equals(that.context);
	}

	@Override
	public int hashCode() {
		return context.hashCode();
	}

}
