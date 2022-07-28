package mn.unitel.solution;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/webhooks/facebook/webhook")
public class GreetingResource {
    @Inject
    Init init;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello RESTEasy";
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String wait(String data) {
        init.push(data);
        System.out.println(data);
        return "OK";
    }
}