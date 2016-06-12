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
import java.net.URI;

import javax.annotation.Priority;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import io.github.restdocsext.jersey.DocumentationProperties.ProviderPriorities;
import io.github.restdocsext.jersey.test.TestResource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for {@link ResponseInterceptor}.
 *
 * @author Paul Samsotha
 */
public class ResponseInterceptorTest extends JerseyTest {

    private static final String BASE_URI = "http://localhost:8080/";

    @Override
    public ResourceConfig configure() {
        return new ResourceConfig(TestResource.class);
    }

    @Override
    public URI getBaseUri() {
        return URI.create(BASE_URI);
    }

    @Test
    public void interceptor_should_store_response_body_in_configuration() {
        final ResponseBodyHolder holder = new ResponseBodyHolder();
        final Client client = JerseyClientBuilder.createClient();
        client.target(BASE_URI).path("test/get-default")
                .register(holder)
                .register(ResponseInterceptor.class)
                .request()
                .get().close();

        final byte[] responseContent = holder.getResponseContent();
        assertThat(responseContent, is(notNullValue()));
        assertThat(new String(responseContent), is("Default"));
    }

    @Test
    public void response_body_property_should_be_null_when_no_body() {
        final ResponseBodyHolder holder = new ResponseBodyHolder();
        final Client client = JerseyClientBuilder.createClient();
        client.target(BASE_URI).path("test/post-no-response-body")
                .register(holder)
                .register(ResponseInterceptor.class)
                .request()
                .get().close();

        final byte[] responseContent = holder.getResponseContent();
        assertThat(responseContent, is(nullValue()));
    }

    @Priority(ProviderPriorities.RESPONSE_BODY_INTERCEPTOR - 1)
    private static class ResponseBodyHolder implements ClientResponseFilter {

        private Object responseProperty;

        @Override
        public void filter(ClientRequestContext request, ClientResponseContext response) throws IOException {
            this.responseProperty = request.getProperty(DocumentationProperties.RESPONSE_BODY_KEY);
        }

        byte[] getResponseContent() {
            return responseProperty == null ? null : (byte[]) responseProperty;
        }
    }
}
