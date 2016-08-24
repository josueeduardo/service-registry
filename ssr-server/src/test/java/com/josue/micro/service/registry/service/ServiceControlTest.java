package com.josue.micro.service.registry.service;

import com.josue.micro.service.registry.ServiceException;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Created by Josue on 09/07/2016.
 */
public class ServiceControlTest {

    private ServiceControl control = new ServiceControl();

    @Before
    public void init() {
        ServiceControl.store.clear();
    }

    @Test(expected = ServiceException.class)
    public void registerNullInstance() throws Exception {
        control.register("id", null);
    }

    @Test(expected = ServiceException.class)
    public void registerNullAddress() throws Exception {
        Instance instance = new Instance();
        instance.setName("service-name");
        instance.setId("123");

        control.register("id", instance);
    }

    @Test(expected = ServiceException.class)
    public void registerEmptyAddress() throws Exception {
        Instance instance = new Instance();
        instance.setName("service-name");
        instance.setId("123");
        instance.setAddress("");

        control.register("id", instance);
    }

    @Test(expected = ServiceException.class)
    public void registerNullId() throws Exception {
        Instance instance = new Instance();
        instance.setName("service-name");
        instance.setId(null);

        control.register("id", instance);
    }

    @Test(expected = ServiceException.class)
    public void registerEmptyId() throws Exception {
        Instance instance = new Instance();
        instance.setName("service-name");
        instance.setAddress("http://localhost:8080");
        instance.setId("");

        control.register("id", instance);
    }

    @Test
    public void register() throws Exception {
        String serviceName = "serviceA";
        Instance instance = registerService("id", serviceName);

        Set<Service> services = control.getServices();
        assertEquals(1, services.size());
        assertEquals(Instance.State.UP, instance.getState());
    }

    @Test
    public void getAllServices() throws Exception {
        String serviceName = "serviceA";
        Instance instance = registerService("id-123", serviceName);

        Service found = control.getService(serviceName);
        assertEquals(1, found.getInstances().size());
        assertEquals(instance, found.getInstances().iterator().next());
    }

    @Test
    public void getServiceByName() throws Exception {
        String serviceName = "serviceA";
        Instance created = registerService("id-123", serviceName);

        Service service = control.getService(serviceName);
        assertEquals(serviceName, service.getName());
        assertEquals(1, service.getInstances().size());
        assertEquals(created, service.getInstances().iterator().next());
    }

    @Test
    public void deregister() throws Exception {
        String id = "id-123";
        String serviceName = "serviceA";
        registerService(id, serviceName);

        Instance deregistered = control.updateInstanceState(id, Instance.State.DOWN);
        assertEquals(Instance.State.DOWN, deregistered.getState());

        Service found = control.getService(serviceName);
        assertEquals(Instance.State.DOWN, found.getInstances().iterator().next().getState());
    }

    @Test(expected = ServiceException.class)
    public void addLinkTargetNotFound() throws Exception {
        String source = "source-service";
        registerService("source-id-123", source);

        control.addLink(source, "invlaid-target-service");
    }

    @Test
    public void addLink() throws Exception {
        String source = "source-service";
        registerService("source-id-123", source);

        String target = "target-service";
        registerService("target-id-123344", target);

        Service updatedTarget = control.addLink(source, target);

        assertEquals(1, updatedTarget.getLinks().size());
        assertEquals(target, updatedTarget.getName());
        assertEquals(source, updatedTarget.getLinks().iterator().next());

    }


    private Instance registerService(String id, String serviceName) throws ServiceException {
        Instance instance = new Instance();
        instance.setId(id);
        instance.setAddress("http://localhost:8080/" + UUID.randomUUID().toString().substring(0, 4));

        return control.register(serviceName, instance);
    }


}