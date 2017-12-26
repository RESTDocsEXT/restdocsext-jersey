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

package io.github.restdocsext.jersey.test;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;

/**
 * Simple resource class for testing.
 *
 * @author Paul Samsotha
 */
@Path("test")
public class TestResource {

    @GET
    @Path("get-default")
    public String getDefault() {
        return "Default";
    }

    @POST
    @Path("post-simple")
    @Consumes("text/plain")
    @Produces("text/plain")
    public String postSimple(String content) {
        return content;
    }

    @POST
    @Path("post-form")
    @Produces("text/plain")
    @Consumes("application/x-www-form-urlencoded")
    public String postForm(Form form) {
        return form.asMap().toString();
    }

    @POST
    @Path("post-json")
    @Produces("application/json")
    @Consumes("application/json")
    public Response postModel(TestModel model) {
        return Response.status(201).entity(model).build();
    }

    @POST
    @Path("post-no-response-body")
    public void postNoResponse() {
    }

    @POST
    @Path("post-random-json")
    @Produces("application/json")
    @Consumes("application/json")
    public String postRandomJson(String json) {
        return json;
    }

    @GET
    @Path("path-params/{param1}/sub/{param2}")
    public String getPathParam(@PathParam("param1") String param1,
            @PathParam("param2") String param2) {
        return param1 + ":" + param2;
    }

    @POST
    @Path("post-multipart")
    @Consumes("multipart/form-data")
    public void postMultipart(FormDataMultiPart multiPart) {

    }
}
