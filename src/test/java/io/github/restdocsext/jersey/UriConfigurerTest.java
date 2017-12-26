package io.github.restdocsext.jersey;

import io.github.restdocsext.jersey.test.Mocks;
import org.glassfish.jersey.client.ClientRequest;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class UriConfigurerTest {

    private UriConfigurer uriConfigurer;
    private JerseyRestDocumentationConfigurer docConfigurer;


    @Before
    public void setUp() {
        docConfigurer = new JerseyRestDocumentationConfigurer(null);
        uriConfigurer = new UriConfigurer(docConfigurer);
    }


    @Test
    public void change_uri_scheme() {
        uriConfigurer.withScheme("https");

        Map<String, Object> configuration = new HashMap<>();
        ClientRequest request = Mocks.clientRequestBuilder().uri(URI.create("http://localhost")).build();
        configuration.put(ClientRequest.class.getName(), request);

        uriConfigurer.apply(configuration, null);

        assertThat(request.getUri().getScheme(), is("https"));
    }

    @Test
    public void change_uri_host() {
        uriConfigurer.withHost("test.com");

        Map<String, Object> configuration = new HashMap<>();
        ClientRequest request = Mocks.clientRequestBuilder().uri(URI.create("http://localhost")).build();
        configuration.put(ClientRequest.class.getName(), request);

        uriConfigurer.apply(configuration, null);

        assertThat(request.getUri().getHost(), is("test.com"));
    }

    @Test
    public void change_uri_port() {
        uriConfigurer.withPort(80);

        Map<String, Object> configuration = new HashMap<>();
        ClientRequest request = Mocks.clientRequestBuilder().uri(URI.create("http://localhost:8080")).build();
        configuration.put(ClientRequest.class.getName(), request);

        uriConfigurer.apply(configuration, null);

        assertThat(request.getUri().getPort(), is(80));
    }

    @Test
    public void change_host_remove_port() {
        uriConfigurer.withHost("test.com").removePort();

        Map<String, Object> configuration = new HashMap<>();
        ClientRequest request = Mocks.clientRequestBuilder().uri(URI.create("http://localhost:8080")).build();
        configuration.put(ClientRequest.class.getName(), request);

        uriConfigurer.apply(configuration, null);

        assertThat(request.getUri().toString(), is("http://test.com"));
    }

    @Test
    public void change_scheme_host_port() {
        uriConfigurer.withScheme("https").withHost("test.com").withPort(80);

        Map<String, Object> configuration = new HashMap<>();
        ClientRequest request = Mocks.clientRequestBuilder().uri(URI.create("http://localhost:8080")).build();
        configuration.put(ClientRequest.class.getName(), request);

        uriConfigurer.apply(configuration, null);

        assertThat(request.getUri().toString(), is("https://test.com:80"));
    }
}
