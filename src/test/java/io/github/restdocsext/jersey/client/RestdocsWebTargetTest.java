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
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.WebTarget;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import io.github.restdocsext.jersey.DocumentationProperties;
import io.github.restdocsext.jersey.JerseyRestDocumentationFilter;

import static io.github.restdocsext.jersey.DocumentationProperties.DOCS_FILTER_KEY;
import static io.github.restdocsext.jersey.DocumentationProperties.PATH_BUILDER_KEY;
import static io.github.restdocsext.jersey.DocumentationProperties.QUERY_BUILDER_KEY;
import static io.github.restdocsext.jersey.JerseyRestDocumentation.document;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link RestdocsWebTarget}.
 *
 * @author Paul Samsotha
 */
public class RestdocsWebTargetTest {

    private static final String BASE_URI = "http://localhost/";

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
    public void instance_of_RestdocsWebTarget_obtained_from_jaxrs_api() {
        final WebTarget target = this.client.target(BASE_URI);
        assertThat(target, instanceOf(RestdocsWebTarget.class));
    }

    /**
     * Methods that should return new instance (according to spec).
     * - matrixParam
     * - path
     * - queryParam
     * - resolveTemplate(String name, Object value)
     * - resolveTemplate(String name, Object value, boolean encodeSlashInPath)
     * - resolveTemplateFromEncoded(String name, Object value)
     * - resolveTemplates(Map&lt;String,Object&gt; templateValues) - if map is empty, return same instance
     * - resolveTemplates(Map&lt;String,Object&gt; templateValues, boolean encodeSlashInPath) - same as previous
     * - resolveTemplatesFromEncoded(Map&lt;String,Object&gt; templateValues) - same as previous
     *
     * Method that should NOT return new instance (according to spec).
     * - all register methods.
     * - property
     *
     * See http://docs.oracle.com/javaee/7/api/javax/ws/rs/client/WebTarget.html
     */
    @Test
    public void correct_methods_should_return_new_target_instance() {
        WebTarget target1 = this.client.target(BASE_URI);
        WebTarget target2 = target1.matrixParam("a", "value");
        WebTarget target3;

        assertNotSame(target1, target2);

        target2 = target1.path("path");
        assertNotSame(target1, target2);

        target2 = target1.queryParam("a", "value2");
        assertNotSame(target1, target2);

        target2 = target1.path("{template}");
        target3 = target2.resolveTemplate("template", "value");
        assertNotSame(target2, target3);

        target2 = target1.path("{template}");
        target3 = target2.resolveTemplate("template", "value", true);
        assertNotSame(target2, target3);

        target2 = target1.path("{template}");
        target3 = target2.resolveTemplateFromEncoded("template", "value");
        assertNotSame(target2, target3);

        final Map<String, Object> params = new HashMap<>();
        params.put("template", "value");

        target2 = target1.path("{template}");
        target3 = target2.resolveTemplates(params);
        assertNotSame(target2, target3);

        target2 = target1.path("{template}");
        target3 = target2.resolveTemplates(new HashMap<String, Object>());
        assertSame(target2, target3);

        target2 = target1.path("{template}");
        target3 = target2.resolveTemplates(params, true);
        assertNotSame(target2, target3);

        target2 = target1.path("{template}");
        target3 = target2.resolveTemplates(new HashMap<String, Object>(), true);
        assertSame(target2, target3);

        target2 = target1.path("{template}");
        target3 = target2.resolveTemplatesFromEncoded(params);
        assertNotSame(target2, target3);

        target2 = target1.path("{template}");
        target3 = target2.resolveTemplatesFromEncoded(new HashMap<String, Object>());
        assertSame(target2, target3);

        target2 = target1.property("hello", "test");
        assertSame(target1, target2);

        target3 = target1.path("path");
        target2 = target3.register(DummyResponseFilter.class);
        assertSame(target3, target2);

        target3 = target1.path("path");
        target2 = target3.register(DummyResponseFilter.class, 2000);
        assertSame(target3, target2);

        target3 = target1.path("path");
        target2 = target3.register(new DummyResponseFilter());
        assertSame(target3, target2);

        target3 = target1.path("path");
        target2 = target3.register(new DummyResponseFilter(), 2000);
        assertSame(target3, target2);

        target3 = target1.path("path");
        target2 = target3.register(DummyResponseFilter.class, ClientResponseFilter.class);
        assertSame(target3, target2);

        target3 = target1.path("path");
        target2 = target3.register(new DummyResponseFilter(), ClientResponseFilter.class);
        assertSame(target3, target2);

        final Map<Class<?>, Integer> contracts = new HashMap<>();
        contracts.put(ClientResponseFilter.class, 1);

        target3 = target1.path("path");
        target2 = target3.register(DummyResponseFilter.class, contracts);
        assertSame(target3, target2);

        target3 = target1.path("path");
        target2 = target3.register(new DummyResponseFilter(), contracts);
        assertSame(target3, target2);
    }

    private void assertNotSame(WebTarget target1, WebTarget target2) {
        assertThat(target1, not(sameInstance(target2)));
    }

    private void assertSame(WebTarget target1, WebTarget target2) {
        assertThat(target1, sameInstance(target2));
    }

    @Test
    public void path_template_stored_in_configuration() {
        WebTarget target = this.client.target(BASE_URI)
                .path("testing").path("{param1}").path("sub").path("{param2}");
        assertPathTemplateStoredCorrectly(target);

        target = this.client.target(BASE_URI)
                .path("/testing").path("/{param1}").path("/sub").path("/{param2}");
        assertPathTemplateStoredCorrectly(target);

        target = this.client.target(BASE_URI)
                .path("/testing/").path("/{param1}/").path("/sub/").path("/{param2}/");
        assertPathTemplateStoredCorrectly(target);

        target = this.client.target(BASE_URI)
                .path("testing/").path("{param1}/").path("/sub/").path("/{param2}");
        assertPathTemplateStoredCorrectly(target);
    }

    private void assertPathTemplateStoredCorrectly(WebTarget target) {
        final Object pathProperty = target.getConfiguration().getProperty(PATH_BUILDER_KEY);
        assertThat(pathProperty, is(notNullValue()));
        final String pathTemplate = ((StringBuilder) pathProperty).toString();
        assertThat(pathTemplate, is("/testing/{param1}/sub/{param2}"));
    }

    @Test
    public void query_parameters_should_be_stored_in_configuration() {
        WebTarget target = this.client.target(BASE_URI)
                .queryParam("a", "avalue").queryParam("b", "bvalue").queryParam("c", "cvalue");
        final Object queryProperty = target.getConfiguration().getProperty(QUERY_BUILDER_KEY);
        assertThat(queryProperty, is(notNullValue()));
        final String queryString = ((StringBuilder) queryProperty).toString();
        assertThat(queryString, is("a=avalue&b=bvalue&c=cvalue"));
    }

    @Test
    public void exception_when_trying_to_set_one_of_the_internal_properties() {
        final WebTarget target = this.client.target(BASE_URI);

        for (String internalProperty : DocumentationProperties.PROPERTY_KEY_SET) {
            try {
                target.property(internalProperty, "value");
                fail("Expected IllegalArgumentException.");
            } catch (IllegalArgumentException ex) {
                assertThat(ex.getMessage(), containsString(internalProperty));
            }
        }
    }

    @Test
    public void documentation_filter_should_get_registered() {
        final WebTarget target = this.client.target(BASE_URI);
        target.register(document("testing"));
        assertThat(target.getConfiguration().isRegistered(JerseyRestDocumentationFilter.class), is(true));
    }

    @Test
    public void documentation_filter_should_get_stored_in_configuration() {
        final WebTarget target = this.client.target(BASE_URI);
        target.register(document("testing"));
        final Object property = target.getConfiguration().getProperty(DOCS_FILTER_KEY);
        assertThat(property, is(notNullValue()));
        assertThat(property, instanceOf(JerseyRestDocumentationFilter.class));
    }

    @Test
    public void subsequent_documentation_filters_should_get_added_to_first() {
        final WebTarget target = this.client.target(BASE_URI);
        final JerseyRestDocumentationFilter filter1 = document("testing1");
        final JerseyRestDocumentationFilter filter2 = document("testing2");
        final JerseyRestDocumentationFilter filter3 = document("testing3");
        final JerseyRestDocumentationFilter spyFilter = Mockito.spy(filter1);

        target.register(spyFilter).register(filter2).register(filter3);

        verify(spyFilter, times(2)).addChildFilter(any(JerseyRestDocumentationFilter.class));
        verify(spyFilter).addChildFilter(filter2);
        verify(spyFilter).addChildFilter(filter3);
    }

    @Test
    public void registering_the_documentation_filter_as_a_class_should_fail() {
        this.thrown.expect(IllegalArgumentException.class);
        this.thrown.expectMessage(startsWith(
                "JerseyRestDocumentationFilter should not be reigsted as a class."));
        final WebTarget target = this.client.target(BASE_URI);
        target.register(JerseyRestDocumentationFilter.class);
    }

    @Test
    public void registering_non_documentation_filter_components() {
        WebTarget target = this.client.target(BASE_URI);
        target.register(DummyResponseFilter.class);
        assertTargetRegisteredComponent(target, DummyResponseFilter.class);

        target = this.client.target(BASE_URI);
        target.register(DummyResponseFilter.class, 2000);
        assertTargetRegisteredComponent(target, DummyResponseFilter.class);

        target = this.client.target(BASE_URI);
        target.register(DummyResponseFilter.class, ClientResponseFilter.class);
        assertTargetRegisteredComponent(target, DummyResponseFilter.class);

        target = this.client.target(BASE_URI);
        target.register(new DummyResponseFilter());
        assertTargetRegisteredComponent(target, DummyResponseFilter.class);

        target = this.client.target(BASE_URI);
        target.register(new DummyResponseFilter(), 2000);
        assertTargetRegisteredComponent(target, DummyResponseFilter.class);

        target = this.client.target(BASE_URI);
        target.register(new DummyResponseFilter(), ClientResponseFilter.class);
        assertTargetRegisteredComponent(target, DummyResponseFilter.class);

        final Map<Class<?>, Integer> contracts = new HashMap<>();
        contracts.put(ClientResponseFilter.class, 1);

        target = this.client.target(BASE_URI);
        target.register(DummyResponseFilter.class, contracts);
        assertTargetRegisteredComponent(target, DummyResponseFilter.class);

        target = this.client.target(BASE_URI);
        target.register(new DummyResponseFilter(), contracts);
        assertTargetRegisteredComponent(target, DummyResponseFilter.class);
    }

    private void assertTargetRegisteredComponent(WebTarget target, Class<?> componentCls) {
        assertThat(target.getConfiguration().isRegistered(componentCls), is(true));
    }

    private static class DummyResponseFilter implements ClientResponseFilter {

        @Override
        public void filter(ClientRequestContext requestContext,
                ClientResponseContext responseContext) throws IOException {
        }
    }
}
