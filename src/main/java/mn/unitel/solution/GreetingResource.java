package mn.unitel.solution;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import mn.unitel.solution.Client.RasaClient;
import mn.unitel.solution.Switchboard.TakeControl;
import mn.unitel.solution.Switchboard.ZendeskHandover;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.time.LocalDateTime;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;


@Path("/webhooks/facebook/webhook")
public class GreetingResource {
    // Config config;
    @Inject
    Init init;

    @Inject
    @RestClient
    ZendeskHandover zendeskCall;

    @Inject
    @RestClient
    TakeControl takeControl;

    @ConfigProperty(name = "zendesk.token",defaultValue = "1")
    String token;

    private static final Logger logger = Logger.getLogger("rasa");


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@QueryParam("id") String id) {
        return init.check(id);
        // return "Hello RESTEasy";
    }

    Uni<String> send(DataStore dataStore) {

        PageInfo info = init.getPagesInfo().get(dataStore.recipientId);

        return RestClientBuilder.newBuilder().baseUri(URI.create(info.url)).build(RasaClient.class).send(dataStore.getValue(), dataStore.sha1, dataStore.sha256);


    }


    @POST
    @Path("takeControl")
    @Consumes(MediaType.APPLICATION_JSON)
    public String takeControl(String data, @HeaderParam("X-Hub-Signature") String sha1, @HeaderParam("X-Hub-Signature-256") String sha2) {
        try {
            DataStore dataStore = new DataStore(data, sha1, sha2);
            PageInfo info = init.pagesInfo.get(dataStore.getRecipientId());
            takeControl.call(info.accessToken,String.format(init.takeControlRequest, dataStore.senderId));
            logger.info("chatbot took control");

            return "success";

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString();
            logger.errorv(sStackTrace);
        }
        return "failed";
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String wait(String data, @HeaderParam("X-Hub-Signature") String sha1, @HeaderParam("X-Hub-Signature-256") String sha2) throws IOException {
        DataStore dataStore = new DataStore(data, sha1, sha2);
        if (init.getLoaded()){
            try {
                PageInfo info = init.pagesInfo.get(dataStore.getRecipientId());
                //Request handover
                if(info.operation.equals("1")){
                    zendeskCall.call(info.accessToken,String.format(init.switchboardHandoverRequest,
                            dataStore.senderId,dataStore.senderId, LocalDateTime.now(),dataStore.senderId,dataStore.senderId));
                    Uni.createFrom().item(dataStore).onItem().call(x -> send(x)).onFailure().recoverWithNull().subscribe().with(System.out::println);
                    logger.info("called handoverAPI & rasaClient");
                }
                else {
                    Uni.createFrom().item(dataStore).onItem().call(x -> send(x)).onFailure().recoverWithNull().subscribe().with(System.out::println);
                }
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String sStackTrace = sw.toString();
                logger.errorv(sStackTrace);
            }
        }

        // init.push(dataStore);
        else {
            System.out.println("not loaded");
        }

        return "success";
    }

    @PUT
    public String changeMode(@QueryParam("id") String id, @QueryParam("mode") String mode) {
        String result = init.changeMode(id, mode);
        init.readConfiguration();
        return result;
    }


}