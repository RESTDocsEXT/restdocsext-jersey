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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.Entity;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.junit.Test;

import io.github.restdocsext.jersey.test.TestResource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for {@link RequestInterceptor}.
 *
 * @author Paul Samsotha
 */
public class RequestInterceptorTest extends JerseyTest {

    private static final String BASE_URI = "http://localhost:8080/";

    /**
     * Using Grizzly instead of in-memory because we are creating the client, rather then using the
     * client created by the framework. When using our own client, we would need to make a network request
     * which wouldn't work, as there's no server attached to the network with the in-memory
     * provider.
     */
    public RequestInterceptorTest() {
        super(new GrizzlyTestContainerFactory());
    }

    @Override
    public ResourceConfig configure() {
        return new ResourceConfig(TestResource.class);
    }

    @Override
    public URI getBaseUri() {
        return URI.create(BASE_URI);
    }

    @Test
    public void interceptor_should_store_request_body_in_configuration() {
        final RequestBodyHolder holder = new RequestBodyHolder();
        final Client client = JerseyClientBuilder.createClient();
        client.target(BASE_URI).path("test/post-simple")
                .register(RequestInterceptor.class)
                .register(holder)
                .request()
                .post(Entity.text("TestData"))
                .close();

        byte[] requestContent = holder.getRequestContent();
        assertThat(requestContent, is(notNullValue()));
        assertThat(new String(requestContent), is("TestData"));
    }

    /**
     * {@code WriterInterceptor} is not called when there is no body, so this should always
     * pass, as long as the Jersey implementation doesn't change.
     */
    @Test
    public void response_body_property_should_be_null_when_no_body() {
        final RequestBodyHolder holder = new RequestBodyHolder();
        final Client client = JerseyClientBuilder.createClient();
        client.target(BASE_URI).path("test/get-default")
                .register(holder)
                .register(RequestInterceptor.class)
                .request()
                .get().close();

        final byte[] requestContent = holder.getRequestContent();
        assertThat(requestContent, is(nullValue()));
    }

    private static class RequestBodyHolder implements ClientResponseFilter {

        private Object requestProperty;

        @Override
        public void filter(ClientRequestContext request, ClientResponseContext response) throws IOException {
            this.requestProperty = request.getProperty(DocumentationProperties.REQUEST_BODY_KEY);
        }

        byte[] getRequestContent() {
            return requestProperty == null ? null : (byte[]) requestProperty;
        }
    }
}
