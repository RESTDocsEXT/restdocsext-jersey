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

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.generate.RestDocumentationGenerator;
import org.springframework.restdocs.snippet.WriterResolver;
import org.springframework.restdocs.templates.TemplateEngine;

import io.github.restdocsext.jersey.test.Mocks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

/**
 * Tests for {@link JerseyRestDocumentationConfigurer}.
 *
 * @author Paul Samsotha
 */
public class JerseyRestDocumentationConfigurerTest {

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("build");

    private final ClientRequestContext requestContext = Mocks.clientRequestBuilder()
            .uri(URI.create("http://localhost"))
            .build();

    private final ClientResponseContext responseContext = Mocks.clientResponseBuilder()
            .build();

    private final JerseyRestDocumentationConfigurer configurer
            = new JerseyRestDocumentationConfigurer(this.restDocumentation);

    @Test
    public void configuration_is_added_to_the_context() throws Exception {
        this.configurer.filter(this.requestContext, this.responseContext);

        @SuppressWarnings("unchecked")
        Map<String, Object> configuration = getContextProperty(this.requestContext,
                DocumentationProperties.CONTEXT_CONFIGURATION_KEY, Map.class);
        assertThat(configuration, is(notNullValue()));
        assertThat(configuration, hasEntry(equalTo(TemplateEngine.class.getName()),
                instanceOf(TemplateEngine.class)));
        assertThat(configuration, hasEntry(equalTo(WriterResolver.class.getName()),
                instanceOf(WriterResolver.class)));
        assertThat(configuration,
                hasEntry(
                        equalTo(RestDocumentationGenerator.ATTRIBUTE_NAME_DEFAULT_SNIPPETS),
                        instanceOf(List.class)));
    }

    private static <T> T getContextProperty(ClientRequestContext requestContext, String property,
            Class<T> cls) {
        return cls.cast(requestContext.getProperty(property));
    }
}
