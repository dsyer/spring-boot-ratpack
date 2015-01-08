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

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.xml.MarkupBuilder;

import java.nio.charset.Charset;
import java.util.Map;

import ratpack.api.Nullable;
import ratpack.groovy.handling.GroovyChain;
import ratpack.groovy.handling.GroovyContext;
import ratpack.groovy.handling.internal.ClosureBackedHandler;
import ratpack.groovy.handling.internal.DefaultGroovyContext;
import ratpack.groovy.handling.internal.GroovyDslChainActionTransformer;
import ratpack.groovy.internal.ClosureInvoker;
import ratpack.groovy.template.Markup;
import ratpack.groovy.template.MarkupTemplate;
import ratpack.groovy.template.TextTemplate;
import ratpack.handling.Context;
import ratpack.handling.Handler;
import ratpack.handling.internal.ChainBuilders;
import ratpack.http.MediaType;
import ratpack.http.internal.DefaultMediaType;
import ratpack.launch.LaunchConfig;
import ratpack.registry.Registry;

import com.google.common.collect.ImmutableMap;

/**
 * @author Dave Syer
 * 
 */
public abstract class Groovy {

	private Groovy() {

	}

	public static void ratpack(
			@DelegatesTo(value = Ratpack.class, strategy = Closure.DELEGATE_FIRST) Closure<?> closure) {
		throw new UnsupportedOperationException(
				"This method should be replaced by the Spring CLI");
	}

	/**
	 * The definition of a Groovy Ratpack application.
	 * 
	 * @see ratpack.groovy.Groovy#ratpack(groovy.lang.Closure)
	 */
	public static interface Ratpack {

		/**
		 * Registers the closure used to build the handler chain of the application.
		 * 
		 * @param configurer The configuration closure, delegating to {@link GroovyChain}
		 */
		void handlers(
				@DelegatesTo(value = GroovyChain.class, strategy = Closure.DELEGATE_FIRST) Closure<?> configurer);

	}

	/**
	 * Builds a handler chain, with no backing registry.
	 * 
	 * @param launchConfig The application launch config
	 * @param closure The chain definition
	 * @return A handler
	 * @throws Exception any exception thrown by the given closure
	 */
	public static Handler chain(
			LaunchConfig launchConfig,
			@DelegatesTo(value = GroovyChain.class, strategy = Closure.DELEGATE_FIRST) Closure<?> closure)
			throws Exception {
		return chain(launchConfig, null, closure);
	}

	/**
	 * Creates a specialized Groovy context.
	 * 
	 * @param context The context to convert to a Groovy context
	 * @return The original context wrapped in a Groovy context
	 */
	public static GroovyContext context(Context context) {
		return context instanceof GroovyContext ? (GroovyContext) context
				: new DefaultGroovyContext(context);
	}

	/**
	 * Builds a chain, backed by the given registry.
	 * 
	 * @param launchConfig The application launch config
	 * @param registry The registry.
	 * @param closure The chain building closure.
	 * @return A handler
	 * @throws Exception any exception thrown by the given closure
	 */
	public static Handler chain(
			@Nullable LaunchConfig launchConfig,
			@Nullable Registry registry,
			@DelegatesTo(value = GroovyChain.class, strategy = Closure.DELEGATE_FIRST) Closure<?> closure)
			throws Exception {
		return ChainBuilders.build(launchConfig != null && launchConfig.isDevelopment(),
				new GroovyDslChainActionTransformer(launchConfig, registry),
				new ClosureInvoker<Object, GroovyChain>(closure).toAction(registry,
						Closure.DELEGATE_FIRST));
	}

	/**
	 * Creates a {@link ratpack.handling.Context#render(Object) renderable} Groovy based
	 * template, using no model and the default content type.
	 * 
	 * @param id The id/name of the template
	 * @return a template
	 */
	public static TextTemplate groovyTemplate(String id) {
		return groovyTemplate(id, null);
	}

	/**
	 * Creates a {@link ratpack.handling.Context#render(Object) renderable} Groovy based
	 * template, using no model.
	 * 
	 * @param id The id/name of the template
	 * @param type The content type of template
	 * @return a template
	 */
	public static TextTemplate groovyTemplate(String id, String type) {
		return groovyTemplate(ImmutableMap.<String, Object> of(), id, type);
	}

	/**
	 * Creates a {@link ratpack.handling.Context#render(Object) renderable} Groovy based
	 * template, using the default content type.
	 * 
	 * @param model The template model
	 * @param id The id/name of the template
	 * @return a template
	 */
	public static TextTemplate groovyTemplate(Map<String, ?> model, String id) {
		return groovyTemplate(model, id, null);
	}

	/**
	 * Creates a {@link ratpack.handling.Context#render(Object) renderable} Groovy based
	 * markup template, using no model and the default content type.
	 *
	 * @param id The id/name of the template
	 * @return a template
	 */
	public static MarkupTemplate groovyMarkupTemplate(String id) {
		return groovyMarkupTemplate(id, null);
	}

	/**
	 * Creates a {@link ratpack.handling.Context#render(Object) renderable} Groovy based
	 * markup template, using no model.
	 *
	 * @param id The id/name of the template
	 * @param type The content type of template
	 * @return a template
	 */
	public static MarkupTemplate groovyMarkupTemplate(String id, String type) {
		return groovyMarkupTemplate(ImmutableMap.<String, Object> of(), id, type);
	}

	/**
	 * Creates a {@link ratpack.handling.Context#render(Object) renderable} Groovy based
	 * markup template, using the default content type.
	 *
	 * @param model The template model
	 * @param id The id/name of the template
	 * @return a template
	 */
	public static MarkupTemplate groovyMarkupTemplate(Map<String, ?> model, String id) {
		return groovyMarkupTemplate(model, id, null);
	}

	/**
	 * Creates a {@link ratpack.handling.Context#render(Object) renderable} Groovy based
	 * template.
	 * 
	 * @param model The template model
	 * @param id The id/name of the template
	 * @param type The content type of template
	 * @return a template
	 */
	public static TextTemplate groovyTemplate(Map<String, ?> model, String id, String type) {
		return new TextTemplate(model, id, type);
	}

	/**
	 * Creates a {@link ratpack.handling.Context#render(Object) renderable} Groovy based
	 * template.
	 *
	 * @param model The template model
	 * @param id The id/name of the template
	 * @param type The content type of template
	 * @return a template
	 */
	public static MarkupTemplate groovyMarkupTemplate(Map<String, ?> model, String id,
			String type) {
		return new MarkupTemplate(id, type, model);
	}

	/**
	 * Creates a handler instance from a closure.
	 * 
	 * @param closure The closure to convert to a handler
	 * @return The created handler
	 */
	public static Handler groovyHandler(
			@DelegatesTo(value = GroovyContext.class, strategy = Closure.DELEGATE_FIRST) Closure<?> closure) {
		return new ClosureBackedHandler(closure);
	}

	/**
	 * Shorthand for {@link #markupBuilder(String, String, groovy.lang.Closure)} with a
	 * content type of {@code "text/html"} and {@code "UTF-8"} encoding.
	 * 
	 * @param closure The html definition
	 * @return A renderable object (i.e. to be used with the
	 * {@link ratpack.handling.Context#render(Object)} method
	 */
	public static Markup htmlBuilder(
			@DelegatesTo(value = MarkupBuilder.class, strategy = Closure.DELEGATE_FIRST) Closure<?> closure) {
		return markupBuilder(MediaType.TEXT_HTML, DefaultMediaType.PLAIN_TEXT_UTF8,
				closure);
	}

	/**
	 * Renderable object for markup built using Groovy's {@link MarkupBuilder}.
	 * 
	 * <pre class="groovy-chain-dsl">
	 * import static ratpack.groovy.Groovy.markupBuilder
	 * 
	 * get("some/path") {
	 *   render markupBuilder("text/html", "UTF-8") {
	 *     // MarkupBuilder DSL in here
	 *   }
	 * }
	 * </pre>
	 * 
	 * @param contentType The content type of the markup
	 * @param encoding The character encoding of the markup
	 * @param closure The definition of the markup
	 * @return A renderable object (i.e. to be used with the
	 * {@link ratpack.handling.Context#render(Object)} method
	 */
	public static Markup markupBuilder(
			CharSequence contentType,
			CharSequence encoding,
			@DelegatesTo(value = MarkupBuilder.class, strategy = Closure.DELEGATE_FIRST) Closure<?> closure) {
		return new Markup(contentType, Charset.forName(encoding.toString()), closure);
	}

	public static Markup markupBuilder(
			CharSequence contentType,
			Charset encoding,
			@DelegatesTo(value = MarkupBuilder.class, strategy = Closure.DELEGATE_FIRST) Closure<?> closure) {
		return new Markup(contentType, encoding, closure);
	}

}