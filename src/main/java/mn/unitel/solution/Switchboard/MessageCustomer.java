package mn.unitel.solution.Switchboard;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@RegisterRestClient(baseUri = "https://graph.facebook.com/v15.0/me/messages")
public interface MessageCustomer {
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    String call(@QueryParam("access_token") String token, String request);
}
