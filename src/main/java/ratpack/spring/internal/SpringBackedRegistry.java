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

import static ratpack.util.ExceptionUtils.toException;
import static ratpack.util.ExceptionUtils.uncheck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;

import ratpack.api.Nullable;
import ratpack.func.Action;
import ratpack.registry.NotInRegistryException;
import ratpack.registry.PredicateCacheability;
import ratpack.registry.Registry;

import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.UncheckedExecutionException;

public class SpringBackedRegistry implements Registry {

	final ApplicationContext context;

	public SpringBackedRegistry(ApplicationContext context) {
		this.context = context;
	}

	@Override
	public <O> O get(Class<O> type) throws NotInRegistryException {
		return get(TypeToken.of(type));
	}

	@Override
	public <O> O get(TypeToken<O> type) throws NotInRegistryException {
		O object = maybeGet(type);
		if (object == null) {
			throw new NotInRegistryException(type);
		} else {
			return object;
		}
	}

	@Override
	public <O> O maybeGet(Class<O> type) {
		return maybeGet(TypeToken.of(type));
	}

	@Override
	public <O> O maybeGet(TypeToken<O> type) {
		List<O> beans = getAll(type);
		if (beans.isEmpty()) {
			return null;
		}
		if (beans.size() == 1) {
			return beans.get(0);
		}
		throw new IllegalStateException("Multiple beans of type " + type + " found");
	}

	@Override
	public <O> List<O> getAll(Class<O> type) {
		return getAll(TypeToken.of(type));
	}

	@Override
	public <O> List<O> getAll(TypeToken<O> type) {
		@SuppressWarnings("unchecked")
		Class<O> rawType = (Class<O>) type.getRawType();
		return new ArrayList<O>(BeanFactoryUtils.beansOfTypeIncludingAncestors(context,
				rawType, true, true).values());
	}

	private <T> List<ObjectFactory<?>> getAll(TypeToken<T> type,
			Predicate<? super T> predicate) {
		try {
			return predicateCache.get(new PredicateCacheability.CacheKey<>(type,
					predicate));
		} catch (ExecutionException | UncheckedExecutionException e) {
			throw uncheck(toException(e.getCause()));
		}
	}

	@Nullable
	@Override
	public <T> T first(TypeToken<T> type, Predicate<? super T> predicate) {
		if (PredicateCacheability.isCacheable(predicate)) {
			List<ObjectFactory<?>> all = getAll(type, predicate);
			if (all.isEmpty()) {
				return null;
			} else {
				@SuppressWarnings("unchecked")
				T cast = (T) all.get(0).getObject();
				return cast;
			}
		} else {
			T object = maybeGet(type);
			if (object != null && predicate.apply(object)) {
				return object;
			} else {
				return null;
			}
		}
	}

	@Override
	public <T> Iterable<? extends T> all(TypeToken<T> type, Predicate<? super T> predicate) {
		if (PredicateCacheability.isCacheable(predicate)) {
			@SuppressWarnings("unchecked")
			List<? extends T> cast = (List<? extends T>) getAll(type, predicate);
			return cast;
		} else {
			return Iterables.filter(getAll(type), predicate);
		}
	}

	@Override
	public <T> boolean each(TypeToken<T> type, Predicate<? super T> predicate,
			Action<? super T> action) throws Exception {
		if (PredicateCacheability.isCacheable(predicate)) {
			Iterable<? extends T> all = all(type, predicate);
			boolean any = false;
			for (T t : all) {
				any = true;
				action.execute(t);
			}
			return any;
		} else {
			boolean foundMatch = false;
			List<ObjectFactory<?>> providers = getObjectFactories(type);
			for (ObjectFactory<?> provider : providers) {
				@SuppressWarnings("unchecked")
				T cast = (T) provider.getObject();
				if (predicate.apply(cast)) {
					action.execute(cast);
					foundMatch = true;
				}
			}
			return foundMatch;
		}
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

	private final LoadingCache<PredicateCacheability.CacheKey<?>, List<ObjectFactory<?>>> predicateCache = CacheBuilder.newBuilder().build(
			new CacheLoader<PredicateCacheability.CacheKey<?>, List<ObjectFactory<?>>>() {

				@Override
				public List<ObjectFactory<?>> load(PredicateCacheability.CacheKey<?> key)
						throws Exception {
					return get(key);
				}

				private <T> List<ObjectFactory<?>> get(
						PredicateCacheability.CacheKey<T> key) throws ExecutionException {
					List<ObjectFactory<?>> providers = getObjectFactories(key.type);
					if (providers.isEmpty()) {
						return Collections.emptyList();
					} else {
						ImmutableList.Builder<ObjectFactory<?>> builder = ImmutableList.builder();
						Predicate<? super T> predicate = key.predicate;
						for (ObjectFactory<?> provider : providers) {
							@SuppressWarnings("unchecked")
							ObjectFactory<T> castProvider = (ObjectFactory<T>) provider;
							if (predicate.apply(castProvider.getObject())) {
								builder.add(castProvider);
							}
						}

						return builder.build();
					}
				}

			});

	private List<ObjectFactory<?>> getObjectFactories(TypeToken<?> type) {
		List<ObjectFactory<?>> result = new ArrayList<ObjectFactory<?>>();
		for (String name : BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context,
				type.getRawType())) {
			final String beanName = name;
			result.add(new ObjectFactory<Object>() {

				@Override
				public Object getObject() throws BeansException {
					return context.getBean(beanName);
				}

			});
		}
		return result;
	}
}
