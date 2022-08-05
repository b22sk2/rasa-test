package mn.unitel.solution.Client;


import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RegisterRestClient
public interface RasaClient {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String send(String data, @HeaderParam("X-Hub-Signature") String sha1, @HeaderParam("X-Hub-Signature-256") String sha2);
}
