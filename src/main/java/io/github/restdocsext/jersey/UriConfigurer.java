package io.github.restdocsext.jersey;

import org.glassfish.jersey.client.ClientRequest;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.config.AbstractNestedConfigurer;

import javax.annotation.Priority;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.Map;

import static io.github.restdocsext.jersey.DocumentationProperties.ProviderPriorities;


@Priority(ProviderPriorities.CONFIGURER)
public class UriConfigurer extends AbstractNestedConfigurer<JerseyRestDocumentationConfigurer>
        implements ClientResponseFilter {


    private String scheme = null;

    private String host = null;

    private int port = Integer.MIN_VALUE;


    public UriConfigurer(JerseyRestDocumentationConfigurer parent) {
        super(parent);
    }

    /**
     * Configures any documented URIs to use the given {@code scheme}.
     *
     * @param scheme The URI scheme
     * @return {@code this}
     */
    public UriConfigurer withScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    /**
     * Configures any documented URIs to use the given {@code host}.
     *
     * @param host The URI host
     * @return {@code this}
     */
    public UriConfigurer withHost(String host) {
        this.host = host;
        return this;
    }

    /**
     * Configures any documented URIs to use the given {@code port}.
     *
     * @param port The URI port
     * @return {@code this}
     */
    public UriConfigurer withPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Configures any documented URIs to remove the {@code port}.
     * @return {@code this}
     */
    public UriConfigurer removePort() {
        this.port = -1;
        return this;
    }

    @Override
    public void apply(Map<String, Object> configuration, RestDocumentationContext context) {
        ClientRequest request = (ClientRequest) configuration.get(ClientRequest.class.getName());

        UriBuilder uriBuilder = UriBuilder.fromUri(request.getUri());
        if (this.scheme != null) {
            uriBuilder.scheme(this.scheme);
        }
        if (this.host != null) {
            uriBuilder.host(this.host);
        }

        if (this.port == -1 || this.port != Integer.MIN_VALUE) {
            uriBuilder.port(this.port);
        }

        request.setUri(uriBuilder.build());
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        and().filter(requestContext, responseContext);
    }
}
