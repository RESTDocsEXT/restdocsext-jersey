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

import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.generate.RestDocumentationGenerator;
import org.springframework.restdocs.operation.RequestConverter;
import org.springframework.restdocs.operation.ResponseConverter;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.snippet.Snippet;

/**
 * Static factory methods for documenting RESTful APIs using Jersey Test Framework.
 *
 * @author Paul Samsotha
 */
public abstract class JerseyRestDocumentation {

    private static final JerseyRequestConverter REQUEST_CONVERTER = new JerseyRequestConverter();

    private static final JerseyResponseConverter RESPONSE_CONVERTER = new JerseyResponseConverter();

    private JerseyRestDocumentation() {
    }

    /**
     * Documents the API call with the given {@code identifier} using the given {@code snippets}.
     *
     * @param identifier an identifier for the API call that is being documented
     * @param snippets the snippets that will document the API call
     * @return a {@link JerseyRestDocumentationFilter} that will produce the documentation
     */
    public static JerseyRestDocumentationFilter document(String identifier, Snippet... snippets) {
        return new JerseyRestDocumentationFilter(new RestDocumentationGenerator<>(identifier,
                REQUEST_CONVERTER, RESPONSE_CONVERTER, snippets));
    }

    /**
     * Documents the API call with the given {@code identifier} using the given {@code snippets} in
     * addition to any default snippets. The given {@code requestPreprocessor} is applied to the
     * request before it is documented.
     *
     * @param identifier an identifier for the API call that is being documented
     * @param requestPreprocessor the request preprocessor
     * @param snippets the snippets
     * @return a {@link JerseyRestDocumentationFilter} that will produce the documentation
     */
    public static JerseyRestDocumentationFilter document(String identifier,
            OperationRequestPreprocessor requestPreprocessor, Snippet... snippets) {
        return new JerseyRestDocumentationFilter(new RestDocumentationGenerator<>(identifier,
                REQUEST_CONVERTER, RESPONSE_CONVERTER, requestPreprocessor, snippets));
    }

    /**
     * Documents the API call with the given {@code identifier} using the given {@code snippets} in
     * addition to any default snippets. The given {@code responsePreprocessor} is applied to the
     * request before it is documented.
     *
     * @param identifier an identifier for the API call that is being documented
     * @param responsePreprocessor the response preprocessor
     * @param snippets the snippets
     * @return a {@link JerseyRestDocumentationFilter} that will produce the documentation
     */
    public static JerseyRestDocumentationFilter document(String identifier,
            OperationResponsePreprocessor responsePreprocessor, Snippet... snippets) {
        return new JerseyRestDocumentationFilter(new RestDocumentationGenerator<>(identifier,
                REQUEST_CONVERTER, RESPONSE_CONVERTER, responsePreprocessor, snippets));
    }

    /**
     * Documents the API call with the given {@code identifier} using the given {@code snippets} in
     * addition to any default snippets. The given {@code requestPreprocessor} and
     * {@code responsePreprocessor} are applied to the request and response respectively before they
     * are documented.
     *
     * @param identifier an identifier for the API call that is being documented
     * @param requestPreprocessor the request preprocessor
     * @param responsePreprocessor the response preprocessor
     * @param snippets the snippets
     * @return a {@link JerseyRestDocumentationFilter} that will produce the documentation
     */
    public static JerseyRestDocumentationFilter document(String identifier,
            OperationRequestPreprocessor requestPreprocessor,
            OperationResponsePreprocessor responsePreprocessor, Snippet... snippets) {
        return new JerseyRestDocumentationFilter(new RestDocumentationGenerator<>(identifier,
                REQUEST_CONVERTER, RESPONSE_CONVERTER, requestPreprocessor,
                responsePreprocessor, snippets));
    }

    /**
     * Documents the API call with the given {@code identifier} using the given {@code snippets} in
     * addition to any default snippets. The given {@code requestConverter} and
     * {@code responseConverter} are used to convert the {@link ClientRequest} and
     * {@link ClientResponse} to the Spring Restdocs
     * {@link org.springframework.restdocs.operation.OperationRequest} and
     * {@link org.springframework.restdocs.operation.OperationResponse} classes. The given
     * {@code requestPreprocessor} and {@code responsePreprocessor} are applied to the request and
     * response respectively before the are documented.
     *
     * @param identifier an identifier for the API call that is being documented
     * @param requestPreprocessor the request preprocessor
     * @param responsePreprocessor the response preprocessor
     * @param requestConverter the request converter
     * @param responseConverter the response converter
     * @param snippets the snippets
     * @return a {@link JerseyRestDocumentationFilter} that will produce the documentation
     */
    public static JerseyRestDocumentationFilter document(String identifier,
        OperationRequestPreprocessor requestPreprocessor,
        OperationResponsePreprocessor responsePreprocessor,
        RequestConverter<ClientRequest> requestConverter,
        ResponseConverter<ClientResponse> responseConverter,
        Snippet... snippets
    ) {
        if (requestConverter == null) {
            requestConverter = REQUEST_CONVERTER;
        }
        if (responseConverter == null) {
            responseConverter = RESPONSE_CONVERTER;
        }
        return new JerseyRestDocumentationFilter(new RestDocumentationGenerator<>(identifier,
            requestConverter, responseConverter, requestPreprocessor,
            responsePreprocessor, snippets));
    }

    /**
     * Provides access to a {@link JerseyRestDocumentationConfigurer} that can be used to configure
     * Spring REST Docs using the given {@code contextProvider}.
     *
     * @param contextProvider the context provider
     * @return the configurer
     */
    public static JerseyRestDocumentationConfigurer documentationConfiguration(
            RestDocumentationContextProvider contextProvider) {
        return new JerseyRestDocumentationConfigurer(contextProvider);
    }
}
