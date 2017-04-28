/*
 * Copyright 2002-2017 the original author or authors.
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

package org.springframework.web.reactive.result.method.annotation;

import java.util.Optional;

import reactor.core.publisher.Mono;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.SyncHandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;

/**
 * An extension of {@link AbstractNamedValueArgumentResolver} for named value
 * resolvers that are synchronous and yet non-blocking. Sub-classes implement
 * the synchronous {@link #resolveNamedValue} to which the asynchronous
 * {@link #resolveName} delegates to by default.
 *
 * @author Rossen Stoyanchev
 * @since 5.0
 */
public abstract class AbstractNamedValueSyncArgumentResolver extends AbstractNamedValueArgumentResolver
		implements SyncHandlerMethodArgumentResolver {

	/**
	 * @param factory a bean factory to use for resolving  ${...}
	 * placeholder and #{...} SpEL expressions in default values;
	 * or {@code null} if default values are not expected to have expressions
	 * @param registry for checking reactive type wrappers
	 */
	protected AbstractNamedValueSyncArgumentResolver(ConfigurableBeanFactory factory, ReactiveAdapterRegistry registry) {
		super(factory, registry);
	}


	@Override
	public Mono<Object> resolveArgument(
			MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {

		// Flip the default implementation from SyncHandlerMethodArgumentResolver:
		// instead of delegating to (sync) resolveArgumentValue,
		// call (async) super.resolveArgument shared with non-blocking resolvers;
		// actual resolution below still sync...

		return super.resolveArgument(parameter, bindingContext, exchange);
	}

	@Override
	public Optional<Object> resolveArgumentValue(
			MethodParameter parameter, BindingContext context, ServerWebExchange exchange) {

		// This won't block since resolveName below doesn't
		Object value = resolveArgument(parameter, context, exchange).block();

		return Optional.ofNullable(value);
	}

	@Override
	protected final Mono<Object> resolveName(String name, MethodParameter param, ServerWebExchange exchange) {
		return Mono.justOrEmpty(resolveNamedValue(name, param, exchange));
	}

	/**
	 * Actually resolve the value synchronously.
	 */
	protected abstract Optional<Object> resolveNamedValue(String name, MethodParameter param, ServerWebExchange exchange);

}