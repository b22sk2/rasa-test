package mn.unitel.solution.Switchboard;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("fb/webhook")
public class WebhookVerify {
    @ConfigProperty(name = "zendesk.token",defaultValue = "1")
    String token;
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello1(@QueryParam("hub.mode")String mode, @QueryParam("hub.verify_token")String verifyToken,
                         @QueryParam("hub.challenge")String challenge, String request) {
        if(token.equals(verifyToken)){
            return challenge;
        }
        return "failed";
    }
}
