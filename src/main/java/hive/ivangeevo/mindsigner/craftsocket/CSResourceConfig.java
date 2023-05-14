package hive.ivangeevo.mindsigner.craftsocket;

import org.glassfish.jersey.server.ResourceConfig;

public class CSResourceConfig extends ResourceConfig {
    public CSResourceConfig() {
        register(CSHttpHandler.class);
    }
}
