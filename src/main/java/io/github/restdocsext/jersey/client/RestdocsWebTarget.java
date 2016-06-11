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

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.JerseyWebTarget;

import io.github.restdocsext.jersey.JerseyRestDocumentationFilter;
import jersey.repackaged.com.google.common.base.Preconditions;

import static io.github.restdocsext.jersey.DocumentationProperties.DOCS_FILTER_KEY;
import static io.github.restdocsext.jersey.DocumentationProperties.PATH_BUILDER_KEY;
import static io.github.restdocsext.jersey.DocumentationProperties.PROPERTY_KEY_SET;
import static io.github.restdocsext.jersey.DocumentationProperties.QUERY_BUILDER_KEY;

/**
 * Decorator around {@code JerseyWebTarget} that builds the URL template, as its URI related
 * methods are called. This class is to help support the path and query parameters validation
 * feature of Spring REST Docs.
 *
 * This class should act no different than the original {@code JerseyWebTarget}, except the methods
 * that normally return a new {@code JerseyWebTarget}, return {@code RestdocsWebTarget}
 * instead.
 *
 * @author Paul Samsotha
 */
public final class RestdocsWebTarget implements WebTarget {

    private final JerseyWebTarget delegate;

    RestdocsWebTarget(JerseyWebTarget delegate) {
        this.delegate = delegate;
    }

    private StringBuilder getBuilderProperty(String prop) {
        final Object property = this.delegate.getConfiguration().getProperty(prop);
        final StringBuilder builder = property == null
                ? new StringBuilder() : (StringBuilder) property;
        if (property == null) {
            this.delegate.getConfiguration().property(prop, builder);
        }
        return builder;
    }

    @Override
    public URI getUri() {
        return this.delegate.getUri();
    }

    @Override
    public UriBuilder getUriBuilder() {
        return this.delegate.getUriBuilder();
    }

    @Override
    public RestdocsWebTarget path(String path) throws NullPointerException {
        Preconditions.checkNotNull(path, "path is 'null'.");
        getBuilderProperty(PATH_BUILDER_KEY).append(getNormalizedPath(path));
        return new RestdocsWebTarget(this.delegate.path(path));
    }

    private String getNormalizedPath(String path) {
        String stripped = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        stripped = stripped.startsWith("/") ? stripped : "/" + stripped;
        return stripped;
    }

    @Override
    public RestdocsWebTarget resolveTemplate(String name, Object value) {
        return resolveTemplate(name, value, true);
    }

    @Override
    public RestdocsWebTarget resolveTemplate(String name, Object value, boolean encodeSlashInPath) {
        return new RestdocsWebTarget(this.delegate.resolveTemplate(name, value, encodeSlashInPath));
    }

    @Override
    public RestdocsWebTarget resolveTemplateFromEncoded(String name, Object value) {
        return new RestdocsWebTarget(this.delegate.resolveTemplateFromEncoded(name, value));
    }

    @Override
    public RestdocsWebTarget resolveTemplates(Map<String, Object> templateValues) {
        return resolveTemplates(templateValues, true);
    }

    @Override
    public RestdocsWebTarget resolveTemplates(Map<String, Object> templateValues, boolean encodeSlashInPath) {
        if (templateValues.isEmpty()) {
            return this;
        } else {
            return new RestdocsWebTarget(this.delegate.resolveTemplates(templateValues, encodeSlashInPath));
        }
    }

    @Override
    public RestdocsWebTarget resolveTemplatesFromEncoded(Map<String, Object> templateValues) {
        if (templateValues.isEmpty()) {
            return this;
        } else {
            return new RestdocsWebTarget(this.delegate.resolveTemplatesFromEncoded(templateValues));
        }
    }

    @Override
    public RestdocsWebTarget matrixParam(String name, Object... values) throws NullPointerException {
        return new RestdocsWebTarget(this.delegate.matrixParam(name, values));
    }

    @Override
    public RestdocsWebTarget queryParam(String name, Object... values)
            throws NullPointerException {
        final StringBuilder sb = getBuilderProperty(QUERY_BUILDER_KEY);
        for (Object value : values) {
            if (sb.length() == 0) {
                sb.append(name).append("=").append(value.toString());
            } else {
                sb.append("&").append(name).append("=").append(value.toString());
            }
        }
        return new RestdocsWebTarget(this.delegate.queryParam(name, values));
    }

    @Override
    public Invocation.Builder request() {
        return this.delegate.request();
    }

    @Override
    public Invocation.Builder request(String... acceptedResponseTypes) {
        return this.delegate.request(acceptedResponseTypes);
    }

    @Override
    public Invocation.Builder request(MediaType... acceptedResponseTypes) {
        return this.delegate.request(acceptedResponseTypes);
    }

    @Override
    public Configuration getConfiguration() {
        return this.delegate.getConfiguration();
    }

    @Override
    public RestdocsWebTarget property(String name, Object value) {
        if (PROPERTY_KEY_SET.contains(name)) {
            throw new IllegalArgumentException("Setting properties " + PROPERTY_KEY_SET + " not allowed.");
        }
        this.delegate.property(name, value);
        return this;
    }

    @Override
    public RestdocsWebTarget register(Class<?> componentClass) {
        checkForRestdocsFilterClass(componentClass);
        this.delegate.register(componentClass);
        return this;
    }

    @Override
    public RestdocsWebTarget register(Class<?> componentClass, int priority) {
        checkForRestdocsFilterClass(componentClass);
        this.delegate.register(componentClass, priority);
        return this;
    }

    @Override
    public RestdocsWebTarget register(Class<?> componentClass, Class<?>... contracts) {
        checkForRestdocsFilterClass(componentClass);
        this.delegate.register(componentClass, contracts);
        return this;
    }

    @Override
    public RestdocsWebTarget register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
        checkForRestdocsFilterClass(componentClass);
        this.delegate.register(componentClass, contracts);
        return this;
    }

    @Override
    public RestdocsWebTarget register(Object component) {
        final RestdocsWebTarget target = registerDocumentationFilterIfNeeeded(component);
        if (target != null) {
            return target;
        }
        this.delegate.register(component);
        return this;
    }

    @Override
    public RestdocsWebTarget register(Object component, int priority) {
        final RestdocsWebTarget target = registerDocumentationFilterIfNeeeded(component);
        if (target != null) {
            return target;
        }
        this.delegate.register(component, priority);
        return this;
    }

    @Override
    public RestdocsWebTarget register(Object component, Class<?>... contracts) {
        final RestdocsWebTarget target = registerDocumentationFilterIfNeeeded(component);
        if (target != null) {
            return target;
        }
        this.delegate.register(component, contracts);
        return this;
    }

    @Override
    public RestdocsWebTarget register(Object component, Map<Class<?>, Integer> contracts) {
        final RestdocsWebTarget target = registerDocumentationFilterIfNeeeded(component);
        if (target != null) {
            return target;
        }
        this.delegate.register(component, contracts);
        return this;
    }

    private RestdocsWebTarget registerDocumentationFilterIfNeeeded(Object component) {
        if (component instanceof JerseyRestDocumentationFilter) {
            JerseyRestDocumentationFilter provider = (JerseyRestDocumentationFilter)
                    this.delegate.getConfiguration().getProperty(DOCS_FILTER_KEY);
            if (provider != null) {
                provider.addChildFilter((JerseyRestDocumentationFilter) component);
            } else {
                this.delegate.register(component);
                this.delegate.getConfiguration().property(DOCS_FILTER_KEY, component);
            }
            return this;
        } else {
            return null;
        }
    }

    private void checkForRestdocsFilterClass(Class<?> componentClass) {
        if (JerseyRestDocumentationFilter.class.equals(componentClass)) {
            throw new IllegalArgumentException(
                    "JerseyRestDocumentationClass should not be reigsted as a class."
                    + " It should only be registed by calling the static "
                    + "JerseyRestDocumentation.document method.");
        }
    }
}
