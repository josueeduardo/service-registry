package com.josue.micro.registry.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import java.text.SimpleDateFormat;

/**
 * Created by Josue on 01/07/2016.
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JacksonJaxrsProvider implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public JacksonJaxrsProvider() {
        mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"));
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //Field Access... ref: http://stackoverflow.com/questions/7105745/how-to-specify-jackson-to-only-use-fields-preferably-globally
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}