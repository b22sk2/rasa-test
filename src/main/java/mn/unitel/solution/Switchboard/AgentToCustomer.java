package mn.unitel.solution.Switchboard;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import mn.unitel.solution.Client.RasaClient;
import mn.unitel.solution.DataStore;
import mn.unitel.solution.Init;
import mn.unitel.solution.PageInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;


@Path("switchboard")
public class AgentToCustomer {
    private static final Logger logger = Logger.getLogger("rasa");

    @Inject
    Init init;
    @ConfigProperty(name = "access.token",defaultValue = "1")
    String accessToken;


    public Uni<String> send(JsonObject jsonPayload) {

        return RestClientBuilder.newBuilder().baseUri(URI.create("https://graph.facebook.com/v15.0/me/messages"))
                .build(MessageCustomer.class).call(jsonPayload.getString("pageToken"),jsonPayload.getString("payload"));
    }

    @POST
    @Path("message")
    public String agentSend(@QueryParam("access_token")String zendeskAccessToken,
                            @QueryParam("pageId")String pageId, String data){
        if(!accessToken.equals(zendeskAccessToken)){
            return "failed";
        }
        try {
            JsonObject jsonObject = new JsonObject(data);
            String recipientId = jsonObject.getJsonObject("recipient").getString("id");
            String message = jsonObject.getJsonObject("message").getString("text");
            String pageToken = init.getPageToken(pageId);
            String payload = String.format(init.messageRequest,recipientId,message);

            JsonObject jsonPayload = new JsonObject();
            jsonPayload.put("pageToken",pageToken);
            jsonPayload.put("payload",payload);

            Uni.createFrom().item(jsonPayload).onItem().call(x -> send(x)).onFailure().recoverWithNull().subscribe().with(System.out::println);
//            messageCustomer.call(init.getPageToken(pageId),String.format(init.messageRequest,recipientId,message));
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString();
            logger.errorv(sStackTrace);
            return "Bad Request, status code 400";
        }

        return "success";

    }
}
