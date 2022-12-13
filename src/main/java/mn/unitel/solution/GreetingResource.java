package mn.unitel.solution;

import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.hash.ReactiveHashCommands;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import mn.unitel.solution.Client.RasaClient;
import mn.unitel.solution.Switchboard.RedisService;
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
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    @ConfigProperty(name = "access.token",defaultValue = "1")
    String accessToken;

    @Inject
    RedisService redisService;

    private static final Logger logger = Logger.getLogger("rasa");



    @GET
    @Path("11")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@QueryParam("id") String id) {
        return init.check(id);
        // return "Hello RESTEasy";
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello1(@QueryParam("hub.mode")String mode, @QueryParam("hub.verify_token")String verifyToken,
                         @QueryParam("hub.challenge")String challenge, String request) {
        if(accessToken.equals(verifyToken)){
            return challenge;
        }
        return "failed";
    }


    Uni<String> send(DataStore dataStore) {

        PageInfo info = init.getPagesInfo().get(dataStore.recipientId);

        return RestClientBuilder.newBuilder().baseUri(URI.create(info.url)).build(RasaClient.class).send(dataStore.getValue(), dataStore.sha1, dataStore.sha256);


    }


    @POST
    @Path("takeControl")
    @Consumes(MediaType.APPLICATION_JSON)
    public String takeControl(@QueryParam("access_token")String zendeskAccessToken,
                              @QueryParam("pageId")String pageId,String data) {
        if(!accessToken.equals(zendeskAccessToken)){
            return "failed";
        }
        String recipientId = null;
        String metadata = null;
        try {
            JsonObject jObject = new JsonObject(data);
            recipientId = jObject.getJsonObject("recipient").getString("id");
            PageInfo info = init.getPagesInfo().get(recipientId);


            if(redisService.get(recipientId.concat(".").concat(pageId)) != null){
                redisService.del(recipientId.concat(".").concat(pageId));
                logger.info("chatbot took control");
            }
            else {
                System.out.println("No handover in progress");
            }
//            takeControl.call(info.accessToken,String.format(init.takeControlRequest, recipientId));



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
        if (!init.getLoaded()){
            System.out.println("not loaded");
            return "failed";
        }
        try {
            PageInfo info = init.getPagesInfo().get(dataStore.getRecipientId());
            String postback = null;
            JsonObject jsonObject = new JsonObject(dataStore.getValue());
            JsonObject payload = jsonObject.getJsonArray("entry").getJsonObject(0)
                    .getJsonArray("messaging").getJsonObject(0);

            if(payload.getJsonObject("postback") != null){
                postback = payload.getJsonObject("postback").getString("payload");
            }

            //redirect msg to zendesk
            if(redisService.get(dataStore.getSenderId().concat(".").concat(dataStore.recipientId)) != null){
                String msg = payload.getJsonObject("message").getString("text");
//                System.out.println(String.format(init.switchboardHandoverRequest,
//                        dataStore.getRecipientId(),msg, LocalDateTime.now(),dataStore.getSenderId(),dataStore.senderId));
//                    zendeskCall.call(accessToken,info.id,String.format(init.switchboardHandoverRequest,
//                            dataStore.getRecipientId(),msg, LocalDateTime.now(),dataStore.getSenderId(),dataStore.senderId));
                System.out.println("Msg sent to operator");
            }
            else {
                if(postback == null){
//                        Uni.createFrom().item(dataStore).onItem().call(x -> send(x)).onFailure().recoverWithNull().subscribe().with(System.out::println);
                    System.out.println("rasa msg");
                }
                else {
                    //Human handover
                    if (postback.equals("/human_handover")) {
                        redisService.set(dataStore.getSenderId().concat(".").concat(dataStore.recipientId), "hand_over");
//                        System.out.println(String.format(init.switchboardHandoverRequest,
//                                dataStore.getRecipientId(),"handover", LocalDateTime.now(),dataStore.getSenderId(),dataStore.senderId));
//                        zendeskCall.call(accessToken,info.id,String.format(init.switchboardHandoverRequest,
//                                dataStore.getRecipientId(),"handover", LocalDateTime.now(),dataStore.getSenderId(),dataStore.senderId));
                        logger.info("called handoverAPI & rasaClient");
                    }
                }
                // init.push(dataStore);
            }

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString();
            logger.errorv(sStackTrace);
            return "failed";
        }
        return "success";
    }

    @POST
    @Path("passtojava")
    @Consumes(MediaType.APPLICATION_JSON)
    public String process(@QueryParam("access_token")String token,String data) {
        if(!accessToken.equals(token))
            return "failed";

        try {
            JsonObject jsonObject = new JsonObject(data);
            String senderId = jsonObject.getString("sender");
            String recipientId = jsonObject.getJsonObject("recipient").getString("id");
            String metadata = jsonObject.getString("metadata");



            redisService.set(senderId.concat(".").concat(recipientId), "hand_over");
            System.out.println(String.format(init.switchboardHandoverRequest,
                    recipientId,metadata, LocalDateTime.now(),senderId,senderId));
//            zendeskCall.call(accessToken,recipientId,String.format(init.switchboardHandoverRequest,
//                    recipientId,metadata, LocalDateTime.now(),senderId,senderId));
            logger.info("called handoverAPI & rasaClient");


        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String sStackTrace = sw.toString();
            logger.errorv(sStackTrace);
            return "failed";
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