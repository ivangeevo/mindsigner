package hive.ivangeevo.mindsigner.xmindsigner;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
@Produces(MediaType.TEXT_PLAIN)
public class CSHttpHandler {
    @GET
    public String sayHello() {
        return "Hello, world!";
    }
}
