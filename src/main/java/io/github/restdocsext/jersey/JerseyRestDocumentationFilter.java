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

package io.github.restdocsext.jersey;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.generate.RestDocumentationGenerator;
import org.springframework.restdocs.snippet.Snippet;

import io.github.restdocsext.jersey.DocumentationProperties.ProviderPriorities;
import jersey.repackaged.com.google.common.base.Preconditions;

import static io.github.restdocsext.jersey.DocumentationProperties.CONTEXT_CONFIGURATION_KEY;
import static io.github.restdocsext.jersey.DocumentationProperties.PATH_BUILDER_KEY;
import static io.github.restdocsext.jersey.DocumentationProperties.QUERY_BUILDER_KEY;

/**
 * A JAX-RS/Jersey client response filter, used to create Spring RestDocs API
 * documentation snippets. This filter should be called after all other filters.
 *
 * @author Paul Samsotha
 */
@Priority(ProviderPriorities.DOCUMENTATION)
public class JerseyRestDocumentationFilter implements ClientResponseFilter {

    private final List<JerseyRestDocumentationFilter> childFilters = new LinkedList<>();

    private final RestDocumentationGenerator<ClientRequest, ClientResponse> delegate;

    JerseyRestDocumentationFilter(
            RestDocumentationGenerator<ClientRequest, ClientResponse> delegate) {
        Preconditions.checkNotNull(delegate, "delegate must not be null");
        this.delegate = delegate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext)
            throws IOException {

        final Map<String, Object> configuration = new HashMap<>(
                getContextProperty(requestContext, CONTEXT_CONFIGURATION_KEY, Map.class));
        configuration.put(RestDocumentationContext.class.getName(),
                getContextProperty(requestContext, RestDocumentationContext.class.getName(),
                        RestDocumentationContext.class));

        final String uriTemplate = createUriTemplate(
                getConfigProperty(requestContext, PATH_BUILDER_KEY, StringBuilder.class),
                getConfigProperty(requestContext, QUERY_BUILDER_KEY, StringBuilder.class));
        configuration.put(RestDocumentationGenerator.ATTRIBUTE_NAME_URL_TEMPLATE, uriTemplate);

        this.delegate.handle((ClientRequest) requestContext, (ClientResponse) responseContext,
                configuration);

        for (JerseyRestDocumentationFilter filter : childFilters) {
            filter.filter(requestContext, responseContext);
        }
    }

    /**
     * Adds the given {@code snippets} such that they are documented when this result handler
     * is called.
     *
     * @param snippets the snippets to add
     * @return this {@code JerseyRestDocumentationFilter}
     */
    public JerseyRestDocumentationFilter snippet(Snippet... snippets) {
        this.delegate.addSnippets(snippets);
        return this;
    }

    /**
     * Jersey only allows one of the same type of filter. So we will delegate calls to all filters
     * added to this list.
     *
     * @param filter a filter to add the to filter list.
     */
    public void addChildFilter(JerseyRestDocumentationFilter filter) {
        this.childFilters.add(filter);
    }

    private static <T> T getContextProperty(ClientRequestContext requestContext, String property,
            Class<T> cls) {
        return cls.cast(requestContext.getProperty(property));
    }

    private static <T> T getConfigProperty(ClientRequestContext requestContext, String property,
            Class<T> cls) {
        return cls.cast(requestContext.getConfiguration().getProperty(property));
    }

    private static String createUriTemplate(StringBuilder pathBuilder, StringBuilder queryBuilder) {

        final String query = queryBuilder == null ? null : queryBuilder.toString();
        final String path = pathBuilder == null ? null : pathBuilder.toString();

        String result = "/";
        if (path != null) {
            result = result + path;
        }
        if (query != null) {
            result = result + "?" + query;
        }

        return result;
    }
}
