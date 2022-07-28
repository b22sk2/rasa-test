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
    public String wait(String data,@HeaderParam("X-Hub-Signature") String sha1 , @HeaderParam("X-Hub-Signature-256") String sha2) {
       DataStore dataStore = new DataStore(data,sha1,sha2);
        init.push(dataStore);
        System.out.println(data);
        return "success";
    }
}