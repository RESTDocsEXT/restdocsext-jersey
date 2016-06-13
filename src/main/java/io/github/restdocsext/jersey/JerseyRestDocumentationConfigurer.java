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
import java.util.Map;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.config.RestDocumentationConfigurer;

import io.github.restdocsext.jersey.DocumentationProperties.ProviderPriorities;

import static io.github.restdocsext.jersey.DocumentationProperties.CONTEXT_CONFIGURATION_KEY;

/**
 * A configurer for Spring RestDocs. It is implemented as a JAX-RS/Jersey client response filter.
 * This filter should be called before the documentation filter.
 *
 * @author Paul Samsotha
 */
@Priority(ProviderPriorities.CONFIGURER)
public class JerseyRestDocumentationConfigurer
        extends
        RestDocumentationConfigurer<JerseySnippetConfigurer, JerseyRestDocumentationConfigurer>
        implements ClientResponseFilter {

    private final JerseySnippetConfigurer snippetConfigurer = new JerseySnippetConfigurer(this);

    private final RestDocumentationContextProvider contextProvider;

    JerseyRestDocumentationConfigurer(RestDocumentationContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }

    @Override
    public JerseySnippetConfigurer snippets() {
        return this.snippetConfigurer;
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext)
            throws IOException {
        RestDocumentationContext context = this.contextProvider.beforeOperation();
        setProperty(requestContext, RestDocumentationContext.class.getName(), context);

        Map<String, Object> configuration = new HashMap<>();
        setProperty(requestContext, CONTEXT_CONFIGURATION_KEY, configuration);

        apply(configuration, context);
    }

    private void setProperty(ClientRequestContext requestContext, String prop, Object value) {
        requestContext.setProperty(prop, value);
    }
}
