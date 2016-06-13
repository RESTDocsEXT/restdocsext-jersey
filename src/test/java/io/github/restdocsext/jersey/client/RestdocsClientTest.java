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

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.github.restdocsext.jersey.DocumentationProperties;
import io.github.restdocsext.jersey.RequestInterceptor;
import io.github.restdocsext.jersey.ResponseInterceptor;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * Test for {@link RestdocsClient}.
 *
 * @author Paul Samsotha
 */
public class RestdocsClientTest {

    private Client client;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        this.client = ClientBuilder.newClient();
    }

    @After
    public void tearDown() {
        this.client.close();
        this.client = null;
    }

    @Test
    public void client_should_be_RestdocsClient_instance() {
        assertThat(this.client, instanceOf(RestdocsClient.class));
    }

    @Test
    public void should_return_RestdocsWebTarget_instances() {
        WebTarget target = this.client.target("http://localhost");
        assertRestdocsTargetInstance(target);

        target = this.client.target(URI.create("http://localhost"));
        assertRestdocsTargetInstance(target);

        target = this.client.target(Link.fromUri("http://localhost").build());
        assertRestdocsTargetInstance(target);

        target = this.client.target(UriBuilder.fromUri("http://localhost").build());
        assertRestdocsTargetInstance(target);
    }

    private void assertRestdocsTargetInstance(WebTarget target) {
        assertThat(target, instanceOf(RestdocsWebTarget.class));
    }

    @Test
    public void webtarget_should_have_interceptors_registered() {
        WebTarget target = this.client.target("http://localhost");
        assertInterceptorsRegistered(target);

        target = this.client.target(URI.create("http://localhost"));
        assertInterceptorsRegistered(target);

        target = this.client.target(Link.fromUri("http://localhost").build());
        assertInterceptorsRegistered(target);

        target = this.client.target(UriBuilder.fromUri("http://localhost").build());
        assertInterceptorsRegistered(target);
    }

    private void assertInterceptorsRegistered(WebTarget target) {
        assertThat(target.getConfiguration().isRegistered(RequestInterceptor.class), is(true));
        assertThat(target.getConfiguration().isRegistered(ResponseInterceptor.class), is(true));
    }

    @Test
    public void set_property() {
        this.client.property("someProp", "someValue");
        final Object value = this.client.getConfiguration().getProperty("someProp");
        assertThat(value, is(notNullValue()));
        assertThat((String) value, is("someValue"));
    }

    @Test
    public void set_internal_property_throws_exception() {
        for (String internalProperty : DocumentationProperties.PROPERTY_KEY_SET) {
            try {
                this.client.property(internalProperty, "testing");
                fail("Expected IllegalArgumentException.");
            } catch (IllegalArgumentException ex) {
                assertThat(ex.getMessage(), containsString(internalProperty));
            }
        }
    }

    @Test
    public void components_get_registered() {
        Client c = RestdocsClientBuilder.newClient();
        c.register(DummyResponseFilter.class);
        assertRegisteredComponent(c, DummyResponseFilter.class);

        c = RestdocsClientBuilder.newClient();
        c.register(DummyResponseFilter.class, 2000);
        assertRegisteredComponent(c, DummyResponseFilter.class);

        c = RestdocsClientBuilder.newClient();
        c.register(DummyResponseFilter.class, ClientResponseFilter.class);
        assertRegisteredComponent(c, DummyResponseFilter.class);

        c = RestdocsClientBuilder.newClient();
        c.register(new DummyResponseFilter());
        assertRegisteredComponent(c, DummyResponseFilter.class);

        c = RestdocsClientBuilder.newClient();
        c.register(new DummyResponseFilter(), 2000);
        assertRegisteredComponent(c, DummyResponseFilter.class);

        c = RestdocsClientBuilder.newClient();
        c.register(new DummyResponseFilter(), ClientResponseFilter.class);
        assertRegisteredComponent(c, DummyResponseFilter.class);

        final Map<Class<?>, Integer> contracts = new HashMap<>();
        contracts.put(ClientResponseFilter.class, 1);

        c = RestdocsClientBuilder.newClient();
        c.register(DummyResponseFilter.class, contracts);
        assertRegisteredComponent(c, DummyResponseFilter.class);

        c = RestdocsClientBuilder.newClient();
        c.register(new DummyResponseFilter(), contracts);
        assertRegisteredComponent(c, DummyResponseFilter.class);
    }

    @Test
    public void creation_of_invocation_through_client_not_supported() {
        thrown.expect(UnsupportedOperationException.class);
        this.client.invocation(Link.fromUri("http://localhost:8080/").build());
    }

    private void assertRegisteredComponent(Client client, Class<?> componentCls) {
        assertThat(client.getConfiguration().isRegistered(componentCls), is(true));
    }

    private static class DummyResponseFilter implements ClientResponseFilter {

        @Override
        public void filter(ClientRequestContext requestContext,
                ClientResponseContext responseContext) throws IOException {
        }
    }
}
