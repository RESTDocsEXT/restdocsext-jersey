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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Common properties and used by this module. Most of the properties are used to store information
 * in the client configuration, to be be retrieved at a later time in the request processing.
 * Properties ending with "_KEY" are such properties.
 *
 * The properties are only to be used internally and MUST NOT be used to set configurations
 * explicitly by the client. Doing so may possibly cause the processing the fail.
 *
 * @author Paul Samsotha
 */
public final class DocumentationProperties {

    /**
     * Prevent instantiation.
     */
    private DocumentationProperties() {
    }

    /**
     * Property for storing the request body.
     */
    public static final String REQUEST_BODY_KEY = "io.github.restdocsext.jersey.requestBody";

    /**
     * Property for storing the response body.
     */
    public static final String RESPONSE_BODY_KEY = "io.github.restdocsext.jersey.responseBody";

    /**
     * Property for storing the {@code StringBuilder} used for building the path part of the URL
     * template used by Spring REST Docs.
     */
    public static final String PATH_BUILDER_KEY = "io.github.restdocsext.jersey.pathBuilder";

    /**
     * Property for storing the {@code StringBuilder} used for building the query string part of the
     * URL template used by Spring REST Docs.
     */
    public static final String QUERY_BUILDER_KEY = "io.github.restdocsext.jersey.queryBuilder";

    /**
     * Property for storing an instance of this filter into the configuration. Retrieval of the
     * filter will be needed to add child filters.
     */
    public static final String DOCS_FILTER_KEY = "io.github.restdocsext.jersey.docsFilter";

    /**
     * Property to store Spring REST Docs configuration properties map.
     */
    public static final String CONTEXT_CONFIGURATION_KEY = "io.github.restdocsext.jersey.configuration";

    /**
     * Property to disable automatically registered interceptors use for documentation.
     * This property should only be used for clients don't want the interceptors added
     * for non-documenting usage of the client.
     */
    public static final String DISABLE_INTERCEPTORS = "io.github.restdocsext.jersey.disableInterceptors";

    /**
     * Set of all configuration property keys.
     */
    public static final Set<String> PROPERTY_KEY_SET = new HashSet<>(
            Arrays.asList(REQUEST_BODY_KEY, RESPONSE_BODY_KEY, PATH_BUILDER_KEY, QUERY_BUILDER_KEY,
                    DOCS_FILTER_KEY, CONTEXT_CONFIGURATION_KEY));

    /**
     * Priorities for Jersey providers.
     */
    public static final class ProviderPriorities {

        /**
         * Prevent instantiation.
         */
        private ProviderPriorities() {
        }

        /**
         * Configurer priority. Must be first of response providers.
         */
        public static final int CONFIGURER = 6000;

        /**
         * Response body interceptor priority. Must come before documentation.
         */
        public static final int RESPONSE_BODY_INTERCEPTOR = 5900;

        /**
         * Documentation priority. Must come last after all providers.
         */
        public static final int DOCUMENTATION = 5800;
    }
}
