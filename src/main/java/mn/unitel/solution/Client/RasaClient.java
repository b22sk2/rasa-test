package mn.unitel.solution.Client;


import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestHeader;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@RegisterRestClient(baseUri = "http://10.21.68.25:5007/webhooks/rest/webhook")
public interface RasaClient {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<String> send(String data, @RestHeader("X-Hub-Signature") String sha1, @RestHeader("X-Hub-Signature-256") String sha2);
}
