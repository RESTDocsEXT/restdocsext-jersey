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

import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;

import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.generate.RestDocumentationGenerator;

import io.github.restdocsext.jersey.DocumentationProperties.ProviderPriorities;
import io.github.restdocsext.jersey.test.TestResource;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;

/**
 * Tests to make sure components (i.e. filter and interceptors) are called in the correct order base
 * on the priorities.
 *
 * @author Paul Samsotha
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(RestDocumentationGenerator.class)
public class ProviderPrioritiesTest extends JerseyTest {

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("build");

    private JerseyRestDocumentationConfigurer configurer;

    private JerseyRestDocumentationFilter documentationFilter;

    private ResponseInterceptor responseInterceptor;

    @Override
    public ResourceConfig configure() {
        return new ResourceConfig(TestResource.class);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        final RestDocumentationGenerator<ClientRequest, ClientResponse> generator
                = getMockedGenerator();

        this.documentationFilter = spy(new JerseyRestDocumentationFilter(generator));
        this.configurer = spy(new JerseyRestDocumentationConfigurer(this.restDocumentation));
        this.responseInterceptor = spy(new ResponseInterceptor());

        super.setUp();
    }

    /*
     * The calls should be in the order
     * 1. Configurer
     * 2. Response interceptor
     * 3. Documentation filter.
     *
     */
    @Test
    public void components_should_be_called_in_correct_order() throws Exception {

        // Use regular Jersey client so we can register all spied components ourselves.
        //
        // We have the call the overloaded `register` method that accepts the priority
        // because when we spy, the instance is no longer the actual instance. So when
        // Jersey tries to get the class to get the priority annotation, it is not
        // there on the spy class, so the priorities would be based on the order
        // that we register the component. We are purposely registering in the wrong
        // order below. But we have to add the priority that is also already on
        // top of the actual class.
        Client client = JerseyClientBuilder.createClient()
                .register(this.documentationFilter, ProviderPriorities.DOCUMENTATION)
                .register(this.responseInterceptor, ProviderPriorities.RESPONSE_BODY_INTERCEPTOR)
                .register(this.configurer, ProviderPriorities.CONFIGURER);

        client.target("http://localhost:9998/test/get-default").request()
                .get().close();

        InOrder inOrder = inOrder(this.configurer, this.responseInterceptor, this.documentationFilter);
        inOrder.verify(this.configurer)
                .filter(any(ClientRequestContext.class), any(ClientResponseContext.class));
        inOrder.verify(this.responseInterceptor)
                .filter(any(ClientRequestContext.class), any(ClientResponseContext.class));
        inOrder.verify(this.documentationFilter)
                .filter(any(ClientRequestContext.class), any(ClientResponseContext.class));
    }

    private RestDocumentationGenerator<ClientRequest, ClientResponse> getMockedGenerator() {
        final RestDocumentationGenerator<ClientRequest, ClientResponse> generator
                = PowerMockito.mock(RestDocumentationGenerator.class);
        Mockito.doNothing().when(generator).handle(any(ClientRequest.class),
                any(ClientResponse.class), any(Map.class));
        return generator;
    }
}
