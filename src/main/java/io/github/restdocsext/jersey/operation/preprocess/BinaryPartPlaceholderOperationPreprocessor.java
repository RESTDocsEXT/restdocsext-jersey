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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationRequestFactory;
import org.springframework.restdocs.operation.OperationRequestPart;
import org.springframework.restdocs.operation.OperationRequestPartFactory;
import org.springframework.restdocs.operation.preprocess.OperationPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationPreprocessorAdapter;

/**
 * {@link OperationPreprocessor} to add placeholder content for binary multipart fields.
 *
 * @author Paul Samsotha
 */
public final class BinaryPartPlaceholderOperationPreprocessor
        extends OperationPreprocessorAdapter {

    private static final String DEFAULT_CONTENT = "<<binary data>>";

    private final OperationRequestFactory requestFactory = new OperationRequestFactory();

    private final OperationRequestPartFactory partFactory = new OperationRequestPartFactory();

    private final List<MultiPartField> fields = new ArrayList<>();

    /**
     * Add a multipart field with the specified field name. Calling this method will use the default
     * placeholder content {@code <binary content>}. If you want a different placeholder, call
     * {@code field(String name, String placeholder} instead.
     *
     * @param name the name of the field.
     * @return the multipart field
     */
    public BinaryPartPlaceholderOperationPreprocessor field(String name) {
        Objects.requireNonNull(name, "field name must not be null");
        final MultiPartField field = new MultiPartField(name, null);
        this.fields.add(field);
        return this;
    }

    /**
     * Add a multipart field with the field name and the placeholder content.
     *
     * @param name the field name
     * @param placeholder the placeholder content
     * @return the operation preprocessor instance.
     */
    public BinaryPartPlaceholderOperationPreprocessor field(String name, String placeholder) {
        Objects.requireNonNull(name, "field name must not be null");
        Objects.requireNonNull(placeholder, "placeholder must not be null");
        final MultiPartField field = new MultiPartField(name, placeholder);
        this.fields.add(field);
        return this;
    }

    @Override
    public OperationRequest preprocess(OperationRequest request) {
        final Collection<OperationRequestPart> parts = request.getParts();
        if (parts.isEmpty()) {
            return request;
        }
        final Collection<OperationRequestPart> modifiedParts = modifyParts(parts);
        return this.requestFactory.create(request.getUri(), request.getMethod(),
                request.getContent(), request.getHeaders(), request.getParameters(),
                modifiedParts);
    }

    private Collection<OperationRequestPart> modifyParts(Collection<OperationRequestPart> parts) {
        final List<OperationRequestPart> modifiedParts = new ArrayList<>();
        for (OperationRequestPart part : parts) {
            boolean foundField = false;
            for (MultiPartField field : this.fields) {
                if (field.getName().equals(part.getName())) {
                    final OperationRequestPart modifiedPart = modifyPart(part, field);
                    modifiedParts.add(modifiedPart);
                    foundField = true;
                    break;
                }
            }
            if (!foundField) {
                modifiedParts.add(part);
            }
        }
        return modifiedParts;
    }

    private OperationRequestPart modifyPart(OperationRequestPart part, MultiPartField field) {
        final String content = field.getPlaceholder() == null
                ? DEFAULT_CONTENT : field.getPlaceholder();
        return BinaryPartPlaceholderOperationPreprocessor.this.partFactory
                .create(field.getName(), part.getSubmittedFileName(), content.getBytes(),
                        part.getHeaders());
    }

    /**
     * Class to encapsulate a multipart field name and its placeholder content.
     */
    private static final class MultiPartField {

        private final String name;
        private final String placeholder;

        private MultiPartField(String name, String placeholder) {
            this.name = name;
            this.placeholder = placeholder;
        }

        private String getName() {
            return this.name;
        }

        private String getPlaceholder() {
            return this.placeholder;
        }
    }
}
