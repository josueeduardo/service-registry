package com.josue.micro.service.registry.rest;


import com.josue.micro.service.registry.ServiceConfig;
import com.josue.micro.service.registry.ServiceControl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Josue on 09/06/2016.
 */
@Path("services")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ServiceResource implements Serializable {


    @Inject
    private ServiceControl control;

    @GET
    public Response getServices(@QueryParam("name") String serviceName) {
        Map<String, Collection<ServiceConfig>> services = control.getServices();
        if (serviceName == null || serviceName.isEmpty()) {
            return Response.ok(services).build();
        }
        Optional<Map.Entry<String, Collection<ServiceConfig>>> found =
                services.entrySet()
                        .stream()
                        .filter(stringCollectionEntry -> stringCollectionEntry.getKey().equals(serviceName))
                        .findFirst();

        if (found.isPresent()) {
            return Response.ok(found.get()).build();
        }
        return Response.ok().build();
    }

    @POST
    public Response register(ServiceConfig serviceConfig) throws Exception {
        return Response.status(Response.Status.CREATED).entity(control.register(serviceConfig)).build();
    }

    @PUT
    @Path("{id}")
    public Response heartbeat(@PathParam("id") String id) throws Exception {
        return Response.ok(control.heartbeat(id)).build();
    }

    @DELETE
    @Path("{id}")
    public Response deregister(@PathParam("id") String id) throws Exception {
        control.deregister(id);
        return Response.noContent().build();
    }

}
