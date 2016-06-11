/*
 * Copyright 2016-2016 the original author or authors.
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

package io.github.restdocsext.jersey.client;

import java.net.URI;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyInvocation;
import org.glassfish.jersey.client.JerseyWebTarget;

import io.github.restdocsext.jersey.RequestInterceptor;
import io.github.restdocsext.jersey.ResponseInterceptor;

import static io.github.restdocsext.jersey.DocumentationProperties.PROPERTY_KEY_SET;

/**
 * A JAX-RS {@code Client} that is used for created API documentation, using Spring RestDocs.
 * Generally, the client will not need to be instantiated, though there may be some edge cases.
 * For the most part, it can be created the same way as you would using the standard JAX-RS
 * APIs.
 *
 * <pre>
 * Client client = ClientBuilder.newClient();
 * </pre>
 *
 * There is no need to keep a reference to this particular implementation type.
 *
 * @author Paul Samsotha
 */
public class RestdocsClient implements Client {

    private final JerseyClient delegate;

    /**
     * Creates a RestDocs client using a {@code JerseyClient} delegate.
     *
     * @param delegate the {@code JerseyClient} delegate.
     */
    public RestdocsClient(JerseyClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public void close() {
        this.delegate.close();
    }

    @Override
    public RestdocsWebTarget target(String uri) {
        final JerseyWebTarget target = this.delegate.target(uri);
        checkAndRegisterInterceptors(target);
        return new RestdocsWebTarget(target);
    }

    @Override
    public RestdocsWebTarget target(URI uri) {
        final JerseyWebTarget target = this.delegate.target(uri);
        checkAndRegisterInterceptors(target);
        return new RestdocsWebTarget(target);
    }

    @Override
    public RestdocsWebTarget target(UriBuilder uriBuilder) {
        final JerseyWebTarget target = this.delegate.target(uriBuilder);
        checkAndRegisterInterceptors(target);
        return new RestdocsWebTarget(target);
    }

    @Override
    public RestdocsWebTarget target(Link link) {
        final JerseyWebTarget target = this.delegate.target(link);
        checkAndRegisterInterceptors(target);
        return new RestdocsWebTarget(target);
    }

    private void checkAndRegisterInterceptors(WebTarget target) {
        if (!target.getConfiguration().isRegistered(RequestInterceptor.class)) {
            target.register(RequestInterceptor.class);
        }
        if (!target.getConfiguration().isRegistered(ResponseInterceptor.class)) {
            target.register(ResponseInterceptor.class);
        }
    }

    @Override
    public JerseyInvocation.Builder invocation(Link link) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SSLContext getSslContext() {
        return this.delegate.getSslContext();
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return this.delegate.getHostnameVerifier();
    }

    @Override
    public Configuration getConfiguration() {
        return this.delegate.getConfiguration();
    }

    @Override
    public RestdocsClient property(String name, Object value) {
        if (PROPERTY_KEY_SET.contains(name)) {
            throw new IllegalArgumentException("Setting properties " + PROPERTY_KEY_SET + " not allowed.");
        }
        this.delegate.property(name, value);
        return this;
    }

    @Override
    public RestdocsClient register(Class<?> componentClass) {
        this.delegate.register(componentClass);
        return this;
    }

    @Override
    public RestdocsClient register(Class<?> componentClass, int priority) {
        this.delegate.register(componentClass, priority);
        return this;
    }

    @Override
    public RestdocsClient register(Class<?> componentClass, Class<?>... contracts) {
        this.delegate.register(componentClass, contracts);
        return this;
    }

    @Override
    public RestdocsClient register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
        this.delegate.register(componentClass, contracts);
        return this;
    }

    @Override
    public RestdocsClient register(Object component) {
        this.delegate.register(component);
        return this;
    }

    @Override
    public RestdocsClient register(Object component, int priority) {
        this.delegate.register(component, priority);
        return this;
    }

    @Override
    public RestdocsClient register(Object component, Class<?>... contracts) {
        this.delegate.register(component, contracts);
        return this;
    }

    @Override
    public RestdocsClient register(Object component, Map<Class<?>, Integer> contracts) {
        this.delegate.register(component, contracts);
        return this;
    }

}
