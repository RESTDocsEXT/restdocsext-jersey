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

import java.util.HashMap;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.jersey.internal.ContextResolverFactory;
import org.glassfish.jersey.internal.ExceptionMapperFactory;
import org.glassfish.jersey.internal.JaxrsProviders;
import org.glassfish.jersey.media.multipart.internal.MultiPartReaderClientSide;
import org.glassfish.jersey.media.multipart.internal.MultiPartWriter;
import org.glassfish.jersey.message.internal.MessageBodyFactory;
import org.glassfish.jersey.message.internal.MessagingBinders;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

/**
 * Abstract base class used for tests that need access to Jersey's {@code ServiceLocator}
 * that is populated with most of the core providers.
 *
 * @author Paul Samsotha
 */
public abstract class AbstractLocatorAwareTest {

    private static ServiceLocator serviceLocator;

    /**
     * Set up the {@code ServiceLocator} with Jersey messaging providers. Included are the
     * core providers as well as those for multipart and JSON.
     */
    @BeforeClass
    public static void setUpLocator() {
        // creat locator with core providers
        serviceLocator = ServiceLocatorUtilities.bind(
                new MessagingBinders.MessageBodyProviders(new HashMap<String, Object>(), RuntimeType.CLIENT),
                new MessageBodyFactory.Binder(),
                new ContextResolverFactory.Binder(),
                new ExceptionMapperFactory.Binder(),
                new JaxrsProviders.Binder());

        // add multipart providers
        Providers providers = serviceLocator.getService(Providers.class);
        ServiceLocatorUtilities.addOneConstant(serviceLocator,
                new MultiPartWriter(providers), "multiPartWriter", MessageBodyWriter.class);
        MultiPartReaderClientSide clientSideReader = new MultiPartReaderClientSide(providers);
        serviceLocator.inject(clientSideReader);
        ServiceLocatorUtilities.addOneConstant(serviceLocator,
                clientSideReader, "multiPartReaderClientSide", MessageBodyReader.class);

        // add JSON provider.
        ServiceLocatorUtilities.addOneConstant(serviceLocator, new JacksonJsonProvider(),
                "jsonProvider", MessageBodyReader.class, MessageBodyWriter.class);
    }

    /**
     * Shut down the {@code ServiceLocator}.
     */
    @AfterClass
    public static void tearDownLocator() {
        serviceLocator.shutdown();
    }

    public static ServiceLocator getServiceLocator() {
        return serviceLocator;
    }
}
