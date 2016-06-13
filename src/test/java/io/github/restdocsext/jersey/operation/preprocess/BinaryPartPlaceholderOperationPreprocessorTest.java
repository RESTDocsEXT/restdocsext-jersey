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

package io.github.restdocsext.jersey.operation.preprocess;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationRequestFactory;
import org.springframework.restdocs.operation.OperationRequestPart;
import org.springframework.restdocs.operation.OperationRequestPartFactory;
import org.springframework.restdocs.operation.Parameters;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for {@link BinaryPartPlaceholderOperationPreprocessor}.
 *
 * @author Paul Samsotha
 */
public class BinaryPartPlaceholderOperationPreprocessorTest {

    private final OperationRequestPartFactory partFactory = new OperationRequestPartFactory();
    private final OperationRequestFactory requestFactory = new OperationRequestFactory();

    private BinaryPartPlaceholderOperationPreprocessor preprocessor;

    @Before
    public void setUp() {
        preprocessor = new BinaryPartPlaceholderOperationPreprocessor();
    }

    @Test
    public void replace_binary_multipart_content_with_placeholder() {
        OperationRequestPart part1 = this.partFactory.create("field1",
                "file1.png", "BinaryContent".getBytes(), new HttpHeaders());
        OperationRequestPart part2 = this.partFactory.create("field2",
                null, "TextContent".getBytes(), new HttpHeaders());
        final OperationRequest request = this.requestFactory.create(URI.create("http://localhost"),
                HttpMethod.POST, null, new HttpHeaders(), new Parameters(),
                Arrays.asList(part1, part2));

        this.preprocessor.field("field1", "<<placeholder>>");
        final OperationRequest preprocessed = this.preprocessor.preprocess(request);
        final Collection<OperationRequestPart> parts = preprocessed.getParts();
        assertThat(hasPart(parts, "field1", "<<placeholder>>"), is(true));
        assertThat(hasPart(parts, "field2", "TextContent"), is(true));
    }

    @Test
    public void replace_binary_multipart_content_with__default_placeholder() {
        OperationRequestPart part1 = this.partFactory.create("field1",
                "file1.png", "BinaryContent".getBytes(), new HttpHeaders());
        OperationRequestPart part2 = this.partFactory.create("field2",
                null, "TextContent".getBytes(), new HttpHeaders());
        final OperationRequest request = this.requestFactory.create(URI.create("http://localhost"),
                HttpMethod.POST, null, new HttpHeaders(), new Parameters(),
                Arrays.asList(part1, part2));

        this.preprocessor.field("field1");
        final OperationRequest preprocessed = this.preprocessor.preprocess(request);
        final Collection<OperationRequestPart> parts = preprocessed.getParts();
        assertThat(hasPart(parts, "field1", "<binary-data>"), is(true));
        assertThat(hasPart(parts, "field2", "TextContent"), is(true));
    }

    private boolean hasPart(Collection<OperationRequestPart> parts, String name, String content) {
        for (OperationRequestPart part : parts) {
            if (name.equals(part.getName()) && content.equals(part.getContentAsString())) {
                return true;
            }
        }
        return false;
    }
}
