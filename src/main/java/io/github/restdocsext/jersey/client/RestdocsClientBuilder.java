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

import java.security.KeyStore;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Configuration;

import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;

/**
 * JAX-RS {@code ClientBuilder} implementation. Generally, this implementation will not
 * need to be referenced/used directly. It will be invoked simply using the normal
 * JAX-RS {@code ClientBuilder}.
 *
 * <pre>
 * Client client = ClientBuilder.newBuilder();
 * </pre>
 *
 * Under the hood, the {@code ClientBuilder} will locate an instance of this class
 * and create use it. This is made possible with the {@code javax.ws.rs.core.ClientBuilder}
 * file located in the {@code META-INF/services}.
 *
 * @author Paul Samsotha
 */
public class RestdocsClientBuilder extends ClientBuilder {

    private final JerseyClientBuilder delegate = new JerseyClientBuilder();

    /**
     * Factory method to to create new {@link RestdocsClient}.
     *
     * @return the new Restdocs client
     */
    public static RestdocsClient newClient() {
        return new RestdocsClientBuilder().build();
    }

    @Override
    public ClientBuilder withConfig(Configuration config) {
        this.delegate.withConfig(config);
        return this;
    }

    @Override
    public RestdocsClientBuilder sslContext(SSLContext sslContext) {
        this.delegate.sslContext(sslContext);
        return this;
    }

    @Override
    public RestdocsClientBuilder keyStore(KeyStore keyStore, char[] password) {
        this.delegate.keyStore(keyStore, password);
        return this;
    }

    @Override
    public RestdocsClientBuilder trustStore(KeyStore trustStore) {
        this.delegate.trustStore(trustStore);
        return this;
    }

    @Override
    public RestdocsClientBuilder hostnameVerifier(HostnameVerifier verifier) {
        this.delegate.hostnameVerifier(verifier);
        return this;
    }

    @Override
    public RestdocsClient build() {
        final JerseyClient jerseyClient = this.delegate.build();
        return new RestdocsClient(jerseyClient);
    }

    @Override
    public Configuration getConfiguration() {
        return this.delegate.getConfiguration();
    }

    @Override
    public RestdocsClientBuilder property(String name, Object value) {
        this.delegate.property(name, value);
        return this;
    }

    @Override
    public RestdocsClientBuilder register(Class<?> componentClass) {
        this.delegate.register(componentClass);
        return this;
    }

    @Override
    public RestdocsClientBuilder register(Class<?> componentClass, int priority) {
        this.delegate.register(componentClass, priority);
        return this;
    }

    @Override
    public RestdocsClientBuilder register(Class<?> componentClass, Class<?>... contracts) {
        this.delegate.register(componentClass, contracts);
        return this;
    }

    @Override
    public RestdocsClientBuilder register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
        this.delegate.register(componentClass, contracts);
        return this;
    }

    @Override
    public RestdocsClientBuilder register(Object component) {
        this.delegate.register(component);
        return this;
    }

    @Override
    public RestdocsClientBuilder register(Object component, int priority) {
        this.delegate.register(component, priority);
        return this;
    }

    @Override
    public RestdocsClientBuilder register(Object component, Class<?>... contracts) {
        this.delegate.register(component, contracts);
        return this;
    }

    @Override
    public RestdocsClientBuilder register(Object component, Map<Class<?>, Integer> contracts) {
        this.delegate.register(component, contracts);
        return this;
    }
}
