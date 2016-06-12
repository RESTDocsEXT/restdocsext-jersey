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

import java.nio.charset.StandardCharsets;

import org.glassfish.jersey.client.ClientResponse;
import org.junit.Test;
import org.springframework.restdocs.operation.OperationResponse;

import io.github.restdocsext.jersey.test.Mocks;

import static io.github.restdocsext.jersey.DocumentationProperties.RESPONSE_BODY_KEY;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for {@link JerseyResponseConverter}.
 *
 * @author Paul Samsotha
 */
public class JerseyResponseConverterTest {

    @Test
    public void convers_response() {
        final ClientResponse clientResponse = Mocks.clientResponseBuilder()
                .status(200)
                .header("X-Response-Header1", "SomeValue1")
                .header("X-Response-Header2", "SomeValue2")
                .requestContext(Mocks.clientRequestBuilder()
                        .configProp(RESPONSE_BODY_KEY, "Testing".getBytes(StandardCharsets.UTF_8))
                        .build())
                .build();
        final OperationResponse response = new JerseyResponseConverter().convert(clientResponse);
        assertThat(response.getContentAsString(), is("Testing"));
        assertThat(response.getStatus().value(), is(200));
        assertThat(response.getHeaders().getFirst("X-Response-Header1"), is("SomeValue1"));
        assertThat(response.getHeaders().getFirst("X-Response-Header2"), is("SomeValue2"));
    }
}
