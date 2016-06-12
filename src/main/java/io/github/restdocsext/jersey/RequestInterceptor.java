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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Priority;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import static io.github.restdocsext.jersey.DocumentationProperties.REQUEST_BODY_KEY;

/**
 * A JAX-RS/Jersey interceptor that grabs the request entity and stores it for later use by the documentation engine.
 *
 * @author Paul Samsotha
 */
@Priority(Integer.MAX_VALUE)
@ConstrainedTo(RuntimeType.CLIENT)
public class RequestInterceptor implements WriterInterceptor {

    @Override
    public void aroundWriteTo(WriterInterceptorContext context)
            throws IOException, WebApplicationException {

        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final OutputStream original = context.getOutputStream();

        try {
            context.setOutputStream(buffer);
            context.proceed();

            final byte[] entity = buffer.toByteArray();
            context.setProperty(REQUEST_BODY_KEY, entity);

            original.write(entity);
        } finally {
            context.setOutputStream(original);
        }
    }
}
