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

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.springframework.http.HttpMethod;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationRequestPart;
import org.springframework.restdocs.operation.Parameters;

import io.github.restdocsext.jersey.test.Mocks;

import static io.github.restdocsext.jersey.DocumentationProperties.REQUEST_BODY_KEY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for {@link JerseyRequestConverter}.
 *
 * @author Paul Samsotha
 */
public class JerseyRequestConverterTest extends AbstractLocatorAwareTest {

    @Test
    public void extract_entity_with_form_encoded() throws Exception {
        final Form form = new Form();
        form.param("a", "alpha");
        form.param("b", "bravo");

        final ClientRequest clientRequest = Mocks.clientRequestBuilder()
                .messageBodyWorkers(getServiceLocator().getService(MessageBodyWorkers.class))
                .method("POST")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .entity(form)
                .entityClass(Form.class)
                .build();

        final Form extractedForm = Whitebox.invokeMethod(JerseyRequestConverter.class,
                "extractEntity", clientRequest, Form.class, Form.class);
        final MultivaluedMap<String, String> map = extractedForm.asMap();
        assertThat(map.getFirst("a"), is("alpha"));
        assertThat(map.getFirst("b"), is("bravo"));
    }

    @Test
    public void extract_entity_with_multipart() throws Exception {
        final MultiPart multiPart = new FormDataMultiPart()
                .field("a", "alpha").field("b", "bravo").field("c", "charlie");
        final ClientRequest request = Mocks.clientRequestBuilder()
                .messageBodyWorkers(getServiceLocator().getService(MessageBodyWorkers.class))
                .uri(URI.create("http://localhost"))
                .method("POST")
                .entity(multiPart)
                .entityClass(MultiPart.class)
                .contentType(MediaType.MULTIPART_FORM_DATA_TYPE)
                .build();

        final FormDataMultiPart extracted = Whitebox.invokeMethod(JerseyRequestConverter.class,
                "extractEntity", request, FormDataMultiPart.class, request.getEntityClass());
        final Map<String, List<FormDataBodyPart>> fields = extracted.getFields();
        assertThat(fields.keySet(), hasItems("a", "b", "c"));

        FormDataBodyPart part = fields.get("a").get(0);
        assertThat(part.getValueAs(String.class), is(equalTo("alpha")));

        part = fields.get("b").get(0);
        assertThat(part.getValueAs(String.class), is(equalTo("bravo")));

        part = fields.get("c").get(0);
        assertThat(part.getValueAs(String.class), is(equalTo("charlie")));
    }

    @Test
    public void extract_parameters_query() throws Exception {
        final ClientRequest request = Mocks.clientRequestBuilder()
                .method("GET")
                .uri(URI.create("http://localhost/api/?a=alpha&b=bravo"))
                .build();
        final Parameters parameters = Whitebox.invokeMethod(JerseyRequestConverter.class,
                "extractParameters", request);
        assertThat(parameters.getFirst("a"), is("alpha"));
        assertThat(parameters.getFirst("b"), is("bravo"));
    }

    @Test
    public void extract_parameters_form() throws Exception {
        final Form form = new Form();
        form.param("a", "alpha");
        form.param("b", "bravo");

        final ClientRequest request = Mocks.clientRequestBuilder()
                .messageBodyWorkers(getServiceLocator().getService(MessageBodyWorkers.class))
                .method("POST")
                .uri(URI.create("http://localhost"))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .entity(form)
                .entityClass(Form.class)
                .build();
        final Parameters parameters = Whitebox.invokeMethod(JerseyRequestConverter.class,
                "extractParameters", request);
        assertThat(parameters.getFirst("a"), is("alpha"));
        assertThat(parameters.getFirst("b"), is("bravo"));
    }

    @Test
    public void extract_parameters_form_and_query() throws Exception {
        final Form form = new Form();
        form.param("a", "alpha");
        form.param("b", "bravo");

        final ClientRequest request = Mocks.clientRequestBuilder()
                .messageBodyWorkers(getServiceLocator().getService(MessageBodyWorkers.class))
                .method("POST")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .entity(form)
                .uri(URI.create("http://localhost/api/?c=charlie"))
                .entityClass(Form.class)
                .build();
        final Parameters parameters = Whitebox.invokeMethod(JerseyRequestConverter.class,
                "extractParameters", request);
        assertThat(parameters.getFirst("a"), is("alpha"));
        assertThat(parameters.getFirst("b"), is("bravo"));
        assertThat(parameters.getFirst("c"), is("charlie"));
    }

    @Test
    public void extract_parts() throws Exception {
        final MultiPart multiPart = new FormDataMultiPart()
                .field("a", "alpha").field("b", "bravo").field("c", "charlie");
        final ClientRequest request = Mocks.clientRequestBuilder()
                .messageBodyWorkers(getServiceLocator().getService(MessageBodyWorkers.class))
                .uri(URI.create("http://localhost"))
                .method("POST")
                .entity(multiPart)
                .entityClass(MultiPart.class)
                .contentType(MediaType.MULTIPART_FORM_DATA_TYPE)
                .build();

        final List<OperationRequestPart> parts = Whitebox.invokeMethod(JerseyRequestConverter.class,
                "extractParts", request);
        assertThat(hasPart(parts, "a", "alpha"), is(true));
        assertThat(hasPart(parts, "b", "bravo"), is(true));
        assertThat(hasPart(parts, "c", "charlie"), is(true));
    }

    private boolean hasPart(List<OperationRequestPart> parts, String name, String content) {
        for (OperationRequestPart part : parts) {
            if (name.equals(part.getName()) && content.equals(part.getContentAsString())) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void convert_json_request() {
        final ClientRequest clientRequest = Mocks.clientRequestBuilder()
                .method("POST")
                .contentType(MediaType.APPLICATION_JSON_TYPE)
                .configProp(REQUEST_BODY_KEY, "{}".getBytes())
                .header("X-Request-Header", "SomeValue")
                .uri(URI.create("http://localhost/api/?a=alpha&b=bravo"))
                .build();
        final OperationRequest request = new JerseyRequestConverter().convert(clientRequest);
        assertThat(request.getMethod(), is(HttpMethod.POST));
        assertThat(request.getParameters().getFirst("a"), is("alpha"));
        assertThat(request.getParameters().getFirst("b"), is("bravo"));
        assertThat(request.getHeaders().getFirst("X-Request-Header"), is("SomeValue"));
        assertThat(request.getContentAsString(), is("{}"));
    }
}
