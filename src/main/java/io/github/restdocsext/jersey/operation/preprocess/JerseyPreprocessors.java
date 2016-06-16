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

/**
 * factory methods for getting Spring REST Docs
 * {@link org.springframework.restdocs.operation.preprocess.OperationPreprocessor}s
 * that are to be used with Jersey client documentation.
 *
 * @author Paul Samsotha
 */
public abstract class JerseyPreprocessors {

    private JerseyPreprocessors() {

    }

    /**
     * Factory method to create a binary part placeholder operation preprocessor.
     *
     * @return the operation preprocessor.
     */
    public static BinaryPartPlaceholderOperationPreprocessor binaryParts() {
        return new BinaryPartPlaceholderOperationPreprocessor();
    }
}
