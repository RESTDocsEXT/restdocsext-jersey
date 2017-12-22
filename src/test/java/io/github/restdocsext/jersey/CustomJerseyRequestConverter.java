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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.operation.OperationRequestFactory;
import org.springframework.restdocs.operation.OperationRequestPart;
import org.springframework.restdocs.operation.OperationRequestPartFactory;
import org.springframework.restdocs.operation.Parameters;
import org.springframework.restdocs.operation.QueryStringParser;
import org.springframework.restdocs.operation.RequestConverter;
import org.springframework.util.StringUtils;

import jersey.repackaged.com.google.common.collect.Lists;

import static io.github.restdocsext.jersey.DocumentationProperties.REQUEST_BODY_KEY;

/**
 * Spring RestDocs {@code RequestConverter} implementation that converts Jersey
 * {@code ClientRequest} to the required Spring RestDocs {@code OperationRequest}, with the option
 * to set a custom hostname for the request URI.
 *
 * @author Paul Samsotha
 */
class CustomJerseyRequestConverter implements RequestConverter<ClientRequest> {

  private String host;

  CustomJerseyRequestConverter(String host) {
    this.host = host;
  }

  @Override
  public OperationRequest convert(ClientRequest request) {
    return new OperationRequestFactory().create(
        UriBuilder.fromUri(request.getUri()).host(this.host).build(),
        HttpMethod.valueOf(request.getMethod()),
        extractContent(request), extractHeaders(request.getHeaders()),
        extractParameters(request), extractParts(request));
  }

  private static byte[] extractContent(ClientRequest request) {
    final byte[] content = request.resolveProperty(REQUEST_BODY_KEY, new byte[0]);
    return isFormsRequest(request) ? new byte[0] : content;
  }

  /**
   * Extract multiparts from {@code ClientRequest} and convert to a list of
   * {@code OperationRequestPart}.
   *
   * @param request the client request.
   * @return the list of operation request parts.
   */
  private static List<OperationRequestPart> extractParts(ClientRequest request) {
    final List<OperationRequestPart> requestParts = new ArrayList<>();
    if (isMultiPartRequest(request)) {
      final FormDataMultiPart multiPart
          = extractEntity(request, FormDataMultiPart.class, request.getEntityClass());
      for (List<FormDataBodyPart> parts : multiPart.getFields().values()) {
        for (FormDataBodyPart part : parts) {
          requestParts.add(createOperationRequestPart(part));
        }
      }
    }

    return requestParts;
  }

  /**
   * Create an {@code OperationRequestPart} from a Jersey {@code FormDataBodyPart}.
   *
   * @param part the Jersey part.
   * @return the converted operation request part.
   */
  // can't test this on it's own as the part is not a body part entity
  private static OperationRequestPart createOperationRequestPart(FormDataBodyPart part) {
    final HttpHeaders partHeaders = extractHeaders(part.getHeaders());
    final List<String> contentTypeHeader = partHeaders.get(HttpHeaders.CONTENT_TYPE);
    if (part.getMediaType() != null && contentTypeHeader == null) {
      partHeaders.setContentType(org.springframework.http.MediaType.parseMediaType(
          part.getMediaType().toString()));
    }

    final String filename = StringUtils.hasText(part.getContentDisposition().getFileName())
        ? part.getContentDisposition().getFileName() : null;
    return new OperationRequestPartFactory().create(
        part.getName(), filename, part.getEntityAs(byte[].class), partHeaders);
  }

  /**
   * Extract form and query parameters from {@code ClientRequest} and convert
   * to {@code Parameters}.
   *
   * @param request the Jersey client request.
   * @return the Spring REST Docs parameters.
   */
  private static Parameters extractParameters(ClientRequest request) {
    final Parameters parameters = new QueryStringParser().parse(request.getUri());
    if (isFormsRequest(request)) {
      final Form form = extractEntity(request, Form.class, request.getEntityClass());
      final MultivaluedMap<String, String> formMap = form.asMap();
      for (String paramKey : formMap.keySet()) {
        parameters.put(paramKey, formMap.get(paramKey));
      }
    }

    return parameters;
  }

  /**
   * Convert {@code MultivaluedMap} headers to {@code HttpHeaders}.
   *
   * @param mapHeaders the map of headers.
   * @return the converted Spring HTTP headers.
   */
  private static HttpHeaders extractHeaders(MultivaluedMap<String, ?> mapHeaders) {
    final HttpHeaders headers = new HttpHeaders();
    for (String header : mapHeaders.keySet()) {
      for (Object val : mapHeaders.get(header)) {
        headers.add(header, val.toString());
      }
    }
    return headers;
  }

  /**
   * Extract an entity from the {@code ClientRequestContext} and return it in the form of the
   * type specified as the {@code returnType} argument.
   *
   * @param <T>        The type of entity to return.
   * @param request    the {@code ClientRequest} for the request.
   * @param returnType the return type of the entity extraction.
   * @param entityType the entity type.
   * @return the entity in the form if the type specifies as the {@code entityType} argument.
   */
  private static <T, U> T extractEntity(ClientRequest request, Class<T> returnType,
      Class<U> entityType) {
    try {
      final MessageBodyWorkers workers = request.getWorkers();
      final Object entity = request.getEntity();
      final MultivaluedMap<String, Object> headers = request.getHeaders();
      final Type type =
          request.getEntityType() == null ? entityType : request.getEntityType();

      MediaType mediaType = request.getMediaType();

      final ByteArrayOutputStream entityOut = new ByteArrayOutputStream();
      final MessageBodyWriter<U> writer = findWriter(workers, entityType, type,
          new Annotation[0],
          mediaType);
      if (writer == null) {
        throw new IllegalStateException("No MessageBodyWriter found for mediatype "
            + mediaType + " and java type " + entityType);
      }

      writer.writeTo(entityType.cast(entity), entityType, type, new Annotation[0],
          mediaType, headers, entityOut);

      final ByteArrayInputStream entityIn = new ByteArrayInputStream(entityOut.toByteArray());
      final MessageBodyReader<T> reader = workers.getMessageBodyReader(
          returnType, null, new Annotation[0], mediaType);
      if (reader == null) {
        throw new IllegalStateException("No MessageBodyReader found for mediatype "
            + mediaType + " and java type " + returnType);
      }

      if (isMultiPartRequest(request)) {
        // If we don't do this, the boundary is not included
        // So we add the boundary ourselves.
        final String contentType = (String) headers.getFirst(HttpHeaders.CONTENT_TYPE);
        final String[] split = contentType.split(";");
        final Map<String, String> parameters = new HashMap<>();
        for (int i = 1; i < split.length; i++) {
          String[] paramPair = split[i].split("=");
          if (paramPair.length == 2) {
            parameters.put(paramPair[0].trim(), paramPair[1].trim());
          }
        }
        if (!parameters.containsKey("boundary")) {
          throw new IllegalStateException(String.format(
              "Content-Type for multipart request does not have boundary: %s",
              contentType));
        }
        mediaType = new MediaType(mediaType.getType(), mediaType.getSubtype(), parameters);
      }

      return (T) reader.readFrom(returnType, returnType, new Annotation[0], mediaType,
          objectMapToStringMap(headers), entityIn);
    } catch (IOException ex) {
      throw new RuntimeException("Could not extract entity.", ex);
    }
  }

  private static <T> MessageBodyWriter<T> findWriter(MessageBodyWorkers workers, Class<T> cls,
      Type type, Annotation[] annots, MediaType mt) {
    return workers.getMessageBodyWriter(cls, type, annots, mt);
  }

  private static MultivaluedMap<String, String> objectMapToStringMap(
      MultivaluedMap<String, Object> source) {
    final MultivaluedMap<String, String> converted = new MultivaluedHashMap<>();
    for (String key : source.keySet()) {
      converted.put(key, objectListToStringList(source.get(key)));
    }
    return converted;
  }

  private static List<String> objectListToStringList(List<Object> source) {
    final List<String> converted = Lists.newArrayList();
    for (Object obj : source) {
      converted.add(obj.toString());
    }
    return converted;
  }

  private static boolean isPutOrPost(ClientRequest request) {
    return "POST".equalsIgnoreCase(request.getMethod())
        || "PUT".equalsIgnoreCase(request.getMethod());
  }

  private static boolean isMultiPartRequest(ClientRequest request) {
    return isPutOrPost(request)
        && request.getMediaType().isCompatible(MediaType.MULTIPART_FORM_DATA_TYPE);
  }

  private static boolean isFormsRequest(ClientRequest request) {
    return isPutOrPost(request)
        && request.getMediaType().isCompatible(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
  }
}
