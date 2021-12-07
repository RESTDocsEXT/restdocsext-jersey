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

import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.client.ClientResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.operation.OperationResponse;
import org.springframework.restdocs.operation.OperationResponseFactory;
import org.springframework.restdocs.operation.ResponseConverter;

import static io.github.restdocsext.jersey.DocumentationProperties.RESPONSE_BODY_KEY;

/**
 * Spring RestDocs {@code ResponseConverter} implementation that converts a Jersey
 * {@code ClientResponse} to the required Spring RestDocs {@code OperationResponse}.
 *
 * @author Paul Samsotha
 */
class JerseyResponseConverter implements ResponseConverter<ClientResponse> {

    @Override
    public OperationResponse convert(ClientResponse response) {
        return new OperationResponseFactory().create(
                response.getStatus(),
                extractHeaders(response.getHeaders()),
                extractContent(response));
    }

    private static byte[] extractContent(ClientResponse response) {
        return response.getRequestContext().resolveProperty(RESPONSE_BODY_KEY, new byte[0]);
    }

    /**
     * Convert {@code MultivaluedMap} headers to {@code HttpHeaders}.
     *
     * @param mapHeaders the Jersey map of headers.
     * @return the converted Spring HTTP Headers.
     */
    private static HttpHeaders extractHeaders(MultivaluedMap<String, String> mapHeaders) {
        HttpHeaders headers = new HttpHeaders();
        for (String header : mapHeaders.keySet()) {
            for (String val : mapHeaders.get(header)) {
                headers.add(header, val);
            }
        }
        return headers;
    }
}
