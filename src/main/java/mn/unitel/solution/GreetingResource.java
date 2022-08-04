package mn.unitel.solution;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;



@Path("/webhooks/facebook/webhook")
public class GreetingResource {
    // Config config;
    @Inject
    Init init;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@QueryParam("id") String id) {
        return init.check(id);
        // return "Hello RESTEasy";
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String wait(String data,@HeaderParam("X-Hub-Signature") String sha1 , @HeaderParam("X-Hub-Signature-256") String sha2) throws IOException {
       DataStore dataStore = new DataStore(data,sha1,sha2);


        init.push(dataStore);
       
        return "success";
    }
    @PUT
    public String changeMode(@QueryParam("id") String id, @QueryParam("mode") String mode){
        String result = init.changeMode(id, mode);
        init.readConfiguration();
        return result;
    }

    
}