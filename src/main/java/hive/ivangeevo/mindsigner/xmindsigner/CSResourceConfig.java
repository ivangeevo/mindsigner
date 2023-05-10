package hive.ivangeevo.mindsigner.xmindsigner;

import org.glassfish.jersey.server.ResourceConfig;

public class CSResourceConfig extends ResourceConfig {
    public CSResourceConfig() {
        register(CSHttpHandler.class);
    }
}
