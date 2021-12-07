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

package io.github.restdocsext.jersey.test;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpHeaders;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility class to create mocks Jersey components. The methods return builders from which the
 * user can build mock expectations for the mock objects. An example of creating a
 * {@code ClientRequest} mock would be something like
 *
 * <pre>
 * ClientRequest mockRequest = Mocks.clientRequestBuilder()
 *         .messageBodyWorkers(serviceLocator.getService(MessageBodyWorkers.class)
 *         .method("POST")
 *         .uri(URI.create("http://localhost")
 *         .entity("hello world")
 *         .entityClass(String.class)
 *         .contentType(MediaType.TEXT_PLAIN_TYPE)
 *         .build();
 * </pre>
 *
 * Testing may include reading and writing of entity bodies, so we can set the
 * {@code MessageBodyWorkers} that will perform and actual read or write of the Java
 * entity type to the stream. In most cases you will just pass a
 * {@code ByteArrayOutput(Input)Stream)} to get the entity in bytes.
 *
 * @author Paul Samsotha
 */
public final class Mocks {

    private Mocks() {
    }

    /**
     * Create a client request mock builder. Calls to the builder methods will
     * build expectations on the mock object.
     *
     * @return the client request builder
     */
    public static ClientRequestBuilder clientRequestBuilder() {
        return new ClientRequestBuilder();
    }

    /**
     * Create a client response mock builder. Calls to the builder method will build
     * expectation on the mock object.
     *
     * @return the client response builder.
     */
    public static ClientResponseBuilder clientResponseBuilder() {
        return new ClientResponseBuilder();
    }

    /**
     * Builder for {@code ClientRequest}.
     */
    public static final class ClientRequestBuilder {

        private MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

        private final Map<String, Object> configProps = new HashMap<>();

        private final ClientRequest clientRequest;

        private URI uri;

        private ClientRequestBuilder() {
            this.clientRequest = mock(ClientRequest.class);
            when(this.clientRequest.getHeaders()).thenReturn(this.headers);
            when(this.clientRequest.getHeaderString(anyString())).thenAnswer(
                    new Answer<String>() {
                        @Override
                        public String answer(InvocationOnMock invocation) throws Throwable {
                            String header = invocation.getArgumentAt(0, String.class);
                            return ClientRequestBuilder.this.headers.getFirst(header).toString();
                        }
                    });
            // Configuration
            Configuration configuration = mock(Configuration.class);
            when(configuration.getProperties()).thenReturn(this.configProps);
            when(this.clientRequest.getConfiguration()).thenReturn(configuration);
            when(configuration.getProperties())
                    .thenReturn(Collections.<String, Object>unmodifiableMap(this.configProps));
            when(this.clientRequest.getConfiguration().getProperty(anyString())).thenAnswer(
                    new Answer<Object>() {
                        @Override
                        public Object answer(InvocationOnMock invocation) throws Throwable {
                            String prop = invocation.getArgumentAt(0, String.class);
                            return ClientRequestBuilder.this.configProps.get(prop);
                        }
                    });
            when(this.clientRequest.getProperty(anyString())).thenAnswer(
                    new Answer<Object>() {
                        @Override
                        public Object answer(InvocationOnMock invocation) throws Throwable {
                            String prop = invocation.getArgumentAt(0, String.class);
                            return ClientRequestBuilder.this.configProps.get(prop);
                        }
                    });
            when(this.clientRequest.resolveProperty(anyString(), any())).thenAnswer(
                    new Answer<Object>() {
                        @Override
                        public Object answer(InvocationOnMock invocation) throws Throwable {
                            String prop = invocation.getArgumentAt(0, String.class);
                            final Object value = ClientRequestBuilder.this.configProps.get(prop);
                            return value == null ? invocation.getArguments()[1] : value;
                        }
                    });
            // any doesn't suffice for array-type arguments.
            when(this.clientRequest.resolveProperty(anyString(), any(Array.class))).thenAnswer(
                    new Answer<Object>() {
                        @Override
                        public Object answer(InvocationOnMock invocation) throws Throwable {
                            String prop = invocation.getArgumentAt(0, String.class);
                            final Object value = ClientRequestBuilder.this.configProps.get(prop);
                            return value == null ? invocation.getArguments()[1] : value;
                        }
                    });
            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    ClientRequestBuilder.this.configProps.put(
                            (String) invocation.getArguments()[0],
                            invocation.getArguments()[1]);
                    return null;
                }
            }).when(this.clientRequest).setProperty(anyString(), any());
            // URI
            doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    ClientRequestBuilder.this.uri = (URI) invocation.getArguments()[0];
                    return null;
                }
            }).when(this.clientRequest).setUri(any(URI.class));
            when(this.clientRequest.getUri()).thenReturn(ClientRequestBuilder.this.uri);
            doAnswer(new Answer<URI>() {
                @Override
                public URI answer(InvocationOnMock invocation) throws Throwable {
                    return ClientRequestBuilder.this.uri;
                }
            }).when(this.clientRequest).getUri();
        }

        public ClientRequestBuilder contentType(MediaType mediaType) {
            when(this.clientRequest.getMediaType()).thenReturn(mediaType);
            this.headers.add(HttpHeaders.CONTENT_TYPE, mediaType.toString());
            return this;
        }

        public ClientRequestBuilder entity(Object entity) {
            when(this.clientRequest.getEntity()).thenReturn(entity);
            return this;
        }

        public ClientRequestBuilder headers(MultivaluedMap<String, Object> headers) {
            this.headers = headers;
            return this;
        }

        public ClientRequestBuilder header(String header, String value) {
            this.headers.add(header, value);
            return this;
        }

        public ClientRequestBuilder configProp(String property, Object value) {
            this.configProps.put(property, value);
            return this;
        }

        public ClientRequestBuilder entityClass(Class<?> entityClass) {
            doReturn(entityClass).when(this.clientRequest).getEntityClass();
            return this;
        }

        public ClientRequestBuilder entityType(Type entityType) {
            when(this.clientRequest.getEntityType()).thenReturn(entityType);
            return this;
        }

        public ClientRequestBuilder method(String method) {
            when(this.clientRequest.getMethod()).thenReturn(method);
            return this;
        }

        public ClientRequestBuilder uri(URI uri) {
            ClientRequestBuilder.this.uri = uri;
            return this;
        }

        public ClientRequestBuilder entityStream(OutputStream entityStream) {
            when(this.clientRequest.getEntityStream()).thenReturn(entityStream);
            return this;
        }

        public ClientRequestBuilder messageBodyWorkers(MessageBodyWorkers workers) {
            when(this.clientRequest.getWorkers()).thenReturn(workers);
            return this;
        }

        public ClientRequest build() {
            return this.clientRequest;
        }
    }

    /**
     * Builder for {@code ClientResponse}.
     */
    public static final class ClientResponseBuilder {

        private MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();

        private final ClientResponse clientResponse;

        private ClientResponseBuilder() {
            this.clientResponse = mock(ClientResponse.class);
            when(this.clientResponse.getHeaders()).thenReturn(this.headers);
            when(this.clientResponse.getHeaderString(anyString())).thenAnswer(new Answer<String>() {
                @Override
                public String answer(InvocationOnMock invocation) throws Throwable {
                    String header = invocation.getArgumentAt(0, String.class);
                    return ClientResponseBuilder.this.headers.getFirst(header);
                }
            });
        }

        public ClientResponseBuilder bufferEntity() {
            when(this.clientResponse.bufferEntity()).thenReturn(true);
            return this;
        }

        public ClientResponseBuilder contentType(MediaType mediaType) {
            when(this.clientResponse.getMediaType()).thenReturn(mediaType);
            this.headers.add(HttpHeaders.CONTENT_TYPE, mediaType.toString());
            return this;
        }

        public ClientResponseBuilder entity(Object entity) {
            when(this.clientResponse.getEntity()).thenReturn(entity);
            return this;
        }

        public ClientResponseBuilder entityStream(InputStream entityStream) {
            when(this.clientResponse.getEntityStream()).thenReturn(entityStream);
            when(this.clientResponse.readEntity(InputStream.class)).thenReturn(entityStream);
            return this;
        }

        public ClientResponseBuilder headers(MultivaluedMap<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public ClientResponseBuilder header(String header, String value) {
            this.headers.add(header, value);
            return this;
        }

        public <T> ClientResponseBuilder readEntity(Class<T> clazz, T entity) {
            when(this.clientResponse.readEntity(clazz)).thenReturn(entity);
            return this;
        }

        public ClientResponseBuilder status(int status) {
            when(this.clientResponse.getStatus()).thenReturn(status);
            return this;
        }

        public ClientResponseBuilder messageBodyWorkers(MessageBodyWorkers workers) {
            when(this.clientResponse.getWorkers()).thenReturn(workers);
            return this;
        }

        public ClientResponseBuilder requestContext(ClientRequest requestContext) {
            when(this.clientResponse.getRequestContext()).thenReturn(requestContext);
            return this;
        }

        public ClientResponse build() {
            return this.clientResponse;
        }
    }
}
