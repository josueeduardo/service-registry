package com.josue.micro.service.registry;


import com.hazelcast.core.IMap;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created by Josue on 09/06/2016.
 */
@Path("services")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ServiceRegistryResource implements Serializable {

    private static final Logger logger = Logger.getLogger(ServiceRegistryResource.class.getName());

    private static final String HEARTBEAT_ENV_KEY = "service.ttl";
    private static final int DEFAULT_SERVICE_TTL = 20000;
    private int ttl = DEFAULT_SERVICE_TTL;

    @Inject
    private IMap<String, Service> cache;

    @PostConstruct
    public void init() {
        String property = System.getProperty(HEARTBEAT_ENV_KEY);
        if (property != null && !property.matches("\\d+")) {
            logger.info(":: SETTING HEARTBEAT PERIOD TO " + property + "ms ::");
            ttl = Integer.valueOf(property);
        } else {
            logger.info(":: HEARTBEAT NOT PROVIDED, USING DEFAULT " + DEFAULT_SERVICE_TTL + "ms ::");
        }
    }

    @GET
    public Response getServices() {
        return Response.ok(mapped()).build();
    }

    @GET
    @Path("{service}")
    public Response getService(@PathParam("service") String service) {
        Map<String, Collection<Service>> mapped = mapped();
        return Response.ok(mapped.get(service)).build();
    }

    @POST
    public Response addService(Service service) {
        if (service == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(simpleJsonMessage("Service not provided")).build();
        }
        if (service.getName() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(simpleJsonMessage("'name' not provided"))
                    .build();
        }
        if (service.getUrl() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(simpleJsonMessage("'url' not provided"))
                    .build();
        }

        String uuid = UUID.randomUUID().toString();
        service.setUuid(uuid.substring(uuid.lastIndexOf("-") + 1, uuid.length()));
        service.setLastCheck(System.currentTimeMillis());

        cache.put(service.getUuid(), service);

        logger.info(":: ADDING " + service.toString() + " ::");

        return Response.ok(service).build();
    }

    @PUT
    @Path("{id}")
    public Response heartbeat(@PathParam("id") String id) {
        if (!cache.containsKey(id)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        cache.submitToKey(id, new EntryProcessor<String, Service>() {
            @Override
            public Object process(Map.Entry<String, Service> entry) {
                Service valueMap = entry.getValue();
                valueMap.setLastCheck(System.currentTimeMillis());
                entry.setValue(valueMap);
                return null;
            }

            @Override
            public EntryBackupProcessor getBackupProcessor() {
                return null;
            }
        });
        return Response.ok(mapped()).build();
    }

    private Map<String, Collection<Service>> mapped() {
        Map<String, Collection<Service>> computed = new HashMap<>();

        cache.forEach((s, service) -> {
            if (System.currentTimeMillis() - service.getLastCheck() > ttl) {
                logger.info(":: REMOVING " + service.toString() + "... R.I.P. ::");
                cache.remove(service.getUuid());
            } else {
                String type = service.getName();
                if (!computed.containsKey(type)) {
                    computed.put(type, new ArrayList<>());
                }
                computed.get(type).add(service);
            }
        });
        return computed;
    }

    private String simpleJsonMessage(String message) {
        return "{\"message\": \"" + message + "\"}";
    }
}
