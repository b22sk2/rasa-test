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

import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.io.PrintWriter;
import java.io.StringWriter;


@Path("switchboard")
public class AgentToCustomer {
    private static final Logger logger = Logger.getLogger("rasa");

    @Inject
    Init init;
    @ConfigProperty(name = "access.token",defaultValue = "1")
    String accessToken;

    @Inject
    @RestClient
    MessageCustomer messageCustomer;



    @POST
    @Path("message")
    public String agentSend(@QueryParam("access_token")String zendeskAccessToken,
                            @QueryParam("pageId")String pageId, String data){
        if(!accessToken.equals(zendeskAccessToken))
            return "failed";

        try {
            JsonObject jsonObject = new JsonObject(data);
            String recipientId = jsonObject.getJsonObject("recipient").getString("id");
            String message = jsonObject.getJsonObject("message").getString("text");
//            System.out.println("page id = "+init.getPageToken(pageId));
//            System.out.println(String.format(init.messageRequest,recipientId,message));
            messageCustomer.call(init.getPageToken(pageId),String.format(init.messageRequest,recipientId,message));
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
