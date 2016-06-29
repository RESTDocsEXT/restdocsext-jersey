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

package com.example.resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.example.data.TeamRepository;
import com.example.model.Team;

/**
 *
 * @author Paul Samsotha
 */
@Path("teams")
@Produces("application/json")
@Consumes("application/json")
public class TeamsResource {
    
    @Inject
    private TeamRepository repository;
    
    @GET
    public List<Team> getTeams(@DefaultValue("5") @QueryParam("count") Integer count) {
        List<Team> inStore = this.repository.findAll();
        List<Team> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(inStore.get(i));
        }
        return result;
    }
    
    @GET
    @Path("{teamId}")
    public Team getTeam(@PathParam("teamId") Long teamId) {
        Team team = this.repository.findById(teamId);
        if (team == null) {
            throw new NotFoundException();
        }
        return team;
    }
    
    @POST
    public Response createTeam(@Context UriInfo uriInfo, Team team) {
        try {
            Team created = this.repository.create(team);
            URI createdUri = uriInfo.getAbsolutePathBuilder()
                    .path(created.getId().toString()).build();
            return Response.created(createdUri).build();
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("id not allowed in request entity field");
        }  
    }
    
    @PUT
    @Path("{teamId}")
    public Response updateTeam(@PathParam("teamId") Long teamId, Team team) {
        Team inStore = this.repository.findById(teamId);
        if (inStore == null) {
            throw new NotFoundException();
        }
        this.repository.update(team);
        return Response.noContent().build();
    }
    
    @DELETE
    @Path("{teamId}")
    public Response deleteTeam(@PathParam("teamId") Long teamId) {
        Team inStore = this.repository.findById(teamId);
        if (inStore == null) {
            throw new NotFoundException();
        }
        this.repository.delete(inStore);
        return Response.ok().build();
    }
}
