package mn.unitel.solution;/*
 * @created_at 28/07/2022 6:44 PM
 * @project rasa-gw
 * @author baasankhuu.d
 */

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@RegisterRestClient(baseUri = "http://10.21.68.25:5007/webhooks/facebook/webhook")
public interface RasaClient {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String send(String data);
}
