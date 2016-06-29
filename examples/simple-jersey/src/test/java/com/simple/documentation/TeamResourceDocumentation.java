/*
 * Copyright 2016 the original author or authors.
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

package com.simple.documentation;

import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.operation.preprocess.OperationPreprocessor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;

import com.example.AppConfig;
import com.example.model.Team;

import static io.github.restdocsext.jersey.JerseyRestDocumentation.document;
import static io.github.restdocsext.jersey.JerseyRestDocumentation.documentationConfiguration;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.removeHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;

/**
 *
 * @author Paul Samsotha
 */
public class TeamResourceDocumentation extends JerseyTest {
    
    @Rule
    public JUnitRestDocumentation restDocumentation
            = new JUnitRestDocumentation("target/generated-snippets");
    
    private final FieldDescriptor[] entityFieldDescriptors = {
        fieldWithPath("id").description("The system identifier of the team"),
        fieldWithPath("name").description("The name of the team."),
        fieldWithPath("numOfPlayers").description("The number of players on the team."),
        fieldWithPath("city").description("The city the team is located in.")
    };
    
    private final ParameterDescriptor[] pathParameterDescriptors = {
        parameterWithName("teamId").description("The identifier of the team")
    };
    
    private final OperationPreprocessor removeUserAgent = removeHeaders("User-Agent");
    
    @Override
    public ResourceConfig configure() {
        return new AppConfig();
    }
    
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();   
    }
    
    @Test
    public void getAllTeams() {
        final ParameterDescriptor[] queryParameterDescriptors = {
            parameterWithName("count").description("The number of teams to return")
        };
        
        final Response response = target("teams")
                .queryParam("count", "2")
                .register(documentationConfiguration(this.restDocumentation))
                .register(document("get-all-teams",
                        preprocessRequest(this.removeUserAgent),
                        requestParameters(queryParameterDescriptors)))
                .request()
                .get();
        assertThat(response.getStatus(), is(200));
        final List<Team> teams = response.readEntity(new GenericType<List<Team>>(){});
        assertThat(teams.size(), is(2));
    }
    
    @Test
    public void getTeamById() {
        final Response response = target("teams/{teamId}")
                .resolveTemplate("teamId", "1")
                .register(documentationConfiguration(this.restDocumentation))
                .register(document("get-team-by-id",
                        preprocessRequest(this.removeUserAgent),
                        pathParameters(this.pathParameterDescriptors),
                        responseFields(this.entityFieldDescriptors)))
                .request()
                .get();
        assertThat(response.getStatus(), is(200));
        Team team = response.readEntity(Team.class);
        assertThat(team.getId(), is((long)1));
    }
    
    @Test
    public void createTeam() {
        final Team team = new Team(null, "Cobras", 14, "Bakcyardville");
        final Response response = target("teams")
                .register(documentationConfiguration(this.restDocumentation))
                .register(document("create-a-team",
                        preprocessRequest(this.removeUserAgent),
                        requestFields(this.entityFieldDescriptors)))
                .request()
                .post(Entity.json(team));
        assertThat(response.getStatus(), is(Response.Status.CREATED.getStatusCode()));
        assertThat(response.getHeaderString("Location"), containsString("teams/"));
    }
    
    @Test
    public void updateTeam() {    
        Response response = target("teams")
                .register(documentationConfiguration(this.restDocumentation))
                .register(document("update-team-initial-get",
                        preprocessRequest(this.removeUserAgent),
                        responseFields(this.entityFieldDescriptors)))
                .path("1")
                .request()
                .get();
        assertThat(response.getStatus(), is(200));
        Team team = response.readEntity(Team.class);
        response.close();
        team.setNumOfPlayers(100);
        
        response = target("teams")
                .register(documentationConfiguration(this.restDocumentation))
                .register(document("update-team",
                        preprocessRequest(this.removeUserAgent),
                        pathParameters(this.pathParameterDescriptors),
                        requestFields(this.entityFieldDescriptors)))
                .path("{teamId}")
                .resolveTemplate("teamId", "1")
                .request()
                .put(Entity.json(team));
        assertThat(response.getStatus(), is(Response.Status.NO_CONTENT.getStatusCode()));
        response.close();
        
        response = target("teams/1").request().get();
        team = response.readEntity(Team.class);
        assertThat(team.getNumOfPlayers(), is(100));
    }
    
    @Test
    public void deleteTeam() {
        Response response = target("teams/{teamId}")
                .register(documentationConfiguration(this.restDocumentation))
                .register(document("delete-team",
                        preprocessRequest(this.removeUserAgent),
                        pathParameters(this.pathParameterDescriptors)))
                .resolveTemplate("teamId", "5")
                .request()
                .delete();
        assertThat(response.getStatus(), is(200));
        response.close();
        
        response = target("teams/5").request().get();
        assertThat(response.getStatus(), is(404));
    }
}
