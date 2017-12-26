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
import java.util.Map;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientRequest;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.config.AbstractNestedConfigurer;

import static io.github.restdocsext.jersey.DocumentationProperties.ProviderPriorities;


/**
 * A REST Docs configurer to configure the URI output.
 *
 * @author Paul Samsotha
 */
@Priority(ProviderPriorities.CONFIGURER)
public class UriConfigurer extends AbstractNestedConfigurer<JerseyRestDocumentationConfigurer>
        implements ClientResponseFilter {


    private String scheme = null;

    private String host = null;

    private int port = Integer.MIN_VALUE;


    UriConfigurer(JerseyRestDocumentationConfigurer parent) {
        super(parent);
    }

    /**
     * Configures any documented URIs to use the given {@code scheme}.
     *
     * @param scheme The URI scheme
     * @return {@code this}
     */
    public UriConfigurer withScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    /**
     * Configures any documented URIs to use the given {@code host}.
     *
     * @param host The URI host
     * @return {@code this}
     */
    public UriConfigurer withHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * Configures any documented URIs to use the given {@code port}.
     *
     * @param port The URI port
     * @return {@code this}
     */
    public UriConfigurer withPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Configures any documented URIs to remove the {@code port}.
     * @return {@code this}
     */
    public UriConfigurer removePort() {
        this.port = -1;
        return this;
    }

    @Override
    public void apply(Map<String, Object> configuration, RestDocumentationContext context) {
        ClientRequest request = (ClientRequest) configuration.get(ClientRequest.class.getName());

        UriBuilder uriBuilder = UriBuilder.fromUri(request.getUri());
        if (this.scheme != null) {
            uriBuilder.scheme(this.scheme);
        }
        if (this.host != null) {
            uriBuilder.host(this.host);
        }

        if (this.port == -1 || this.port != Integer.MIN_VALUE) {
            uriBuilder.port(this.port);
        }

        request.setUri(uriBuilder.build());
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        and().filter(requestContext, responseContext);
    }
}
