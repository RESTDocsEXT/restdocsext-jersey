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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

import io.github.restdocsext.jersey.DocumentationProperties.ProviderPriorities;

import static io.github.restdocsext.jersey.DocumentationProperties.RESPONSE_BODY_KEY;

/**
 * A JAX-RS/Jersey client response filter the grabs the incoming response, and stores
 * it for later use by the documentation engine.
 *
 * @author Paul Samsotha
 */
@Priority(ProviderPriorities.RESPONSE_BODY_INTERCEPTOR)
public class ResponseInterceptor implements ClientResponseFilter {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final int maxEntitySize = 1024 * 8;

    @Override
    public void filter(ClientRequestContext request, ClientResponseContext response) throws IOException {
        if (response.hasEntity()) {
            final StringBuilder sb = new StringBuilder();
            response.setEntityStream(getEntity(sb, response.getEntityStream(), DEFAULT_CHARSET));
            request.setProperty(RESPONSE_BODY_KEY, sb.toString().getBytes(DEFAULT_CHARSET));
        }
    }

    private InputStream getEntity(StringBuilder b, InputStream in, final Charset charset) throws IOException {
        if (!in.markSupported()) {
            in = new BufferedInputStream(in);
        }
        in.mark(maxEntitySize + 1);
        final byte[] entity = new byte[maxEntitySize + 1];
        final int entitySize = in.read(entity);
        b.append(new String(entity, 0, Math.min(entitySize, maxEntitySize), charset));
        if (entitySize > maxEntitySize) {
            b.append("...more...");
        }
        in.reset();
        return in;
    }
}
