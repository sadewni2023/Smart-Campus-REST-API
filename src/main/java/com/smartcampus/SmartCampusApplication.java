package com.smartcampus;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {
    public SmartCampusApplication() {
        // Register resources, filters, and exception mappers
        packages("com.smartcampus.resources", 
                 "com.smartcampus.exceptions.mappers", 
                 "com.smartcampus.filters");
        
        // Register Jackson for JSON support
        register(org.glassfish.jersey.jackson.JacksonFeature.class);
    }
}
