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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;

import org.junit.Test;

import io.github.restdocsext.jersey.RequestInterceptor;
import io.github.restdocsext.jersey.ResponseInterceptor;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for {@link RestdocsClient}.
 *
 * @author Paul Samsotha
 */
public class RestdocsClientTest {

    @Test
    public void client_should_be_RestdocsClient_instance() {
        final Client client = ClientBuilder.newClient();
        assertThat(client, instanceOf(RestdocsClient.class));
    }

    @Test
    public void webtarget_should_have_interceptors_registered() {
        WebTarget target = ClientBuilder.newClient().target("http://localhost");
        assertInterceptorsRegistered(target);

        target = ClientBuilder.newClient().target(URI.create("http://localhost"));
        assertInterceptorsRegistered(target);

        target = ClientBuilder.newClient().target(Link.fromUri("http://localhost").build());
        assertInterceptorsRegistered(target);

        target = ClientBuilder.newClient().target(UriBuilder.fromUri("http://localhost").build());
        assertInterceptorsRegistered(target);
    }

    private void assertInterceptorsRegistered(WebTarget target) {
        assertThat(target.getConfiguration().isRegistered(RequestInterceptor.class), is(true));
        assertThat(target.getConfiguration().isRegistered(ResponseInterceptor.class), is(true));
    }
}
