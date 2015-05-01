/*
 * Copyright 2012-2014 the original author or authors.
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

package org.springframework.boot.ratpack.cli.compiler;

import java.lang.reflect.Modifier;
import java.util.ArrayList;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.springframework.boot.cli.compiler.AstUtils;
import org.springframework.boot.cli.compiler.DependencyAutoConfigurationTransformation;
import org.springframework.boot.cli.compiler.SpringBootAstTransformation;
import org.springframework.core.annotation.Order;

/**
 * {@link ASTTransformation} to resolve beans declarations inside application source
 * files. Users only need to define a <code>ratpack{}</code> DSL element, and this
 * transformation will remove it and make it accessible to the Spring application via an
 * interface.
 * 
 * @author Dave Syer
 */
@Order(DependencyAutoConfigurationTransformation.ORDER - 50)
public class RatpackAstTransformation implements SpringBootAstTransformation {

	public static final String SOURCE_INTERFACE = "ratpack.spring.groovy.internal.RatpackScriptServerCustomizer.GroovyRatpackSource";

	@Override
	public void visit(ASTNode[] nodes, SourceUnit source) {
		for (ASTNode node : nodes) {
			if (node instanceof ModuleNode) {
				ModuleNode module = (ModuleNode) node;
				for (ClassNode classNode : new ArrayList<ClassNode>(module.getClasses())) {
					if (classNode.isScript()) {
						classNode.visitContents(new ClassVisitor(source, classNode));
					}
				}
			}
		}
	}

	private class ClassVisitor extends ClassCodeVisitorSupport {

		private static final String SOURCE_INTERFACE = RatpackAstTransformation.SOURCE_INTERFACE;
		private static final String RATPACK = "ratpack";
		private final SourceUnit source;
		private final ClassNode classNode;
		private boolean xformed = false;

		public ClassVisitor(SourceUnit source, ClassNode classNode) {
			this.source = source;
			this.classNode = classNode;
		}

		@Override
		protected SourceUnit getSourceUnit() {
			return this.source;
		}

		@Override
		public void visitBlockStatement(BlockStatement block) {
			if (block.isEmpty() || this.xformed) {
				return;
			}
			ClosureExpression closure = ratpack(block);
			if (closure != null) {
				this.classNode.addAnnotation(new AnnotationNode(ClassHelper
						.make("Component")));
				// Add a marker interface to the current script
				this.classNode.addInterface(ClassHelper.make(SOURCE_INTERFACE));
				// Implement the interface by adding a public read-only property with the
				// same name as the method in the interface (getRatpack). Make it return the
				// closure.
				this.classNode.addProperty(new PropertyNode(RATPACK, Modifier.PUBLIC
						| Modifier.FINAL, ClassHelper.CLOSURE_TYPE
						.getPlainNodeReference(), this.classNode, closure, null, null));
				// Only do this once per class
				this.xformed = true;
			}
		}

		/**
		 * Extract a top-level <code>ratpack{}</code> closure from inside this block if
		 * there is one. Removes it from the block at the same time.
		 * @param block a block statement (class definition)
		 * @return a beans Closure if one can be found, null otherwise
		 */
		private ClosureExpression ratpack(BlockStatement block) {
			return AstUtils.getClosure(block, RATPACK, true);
		}
	}
}
