package mn.unitel.solution;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import mn.unitel.solution.Client.RasaClient;
import mn.unitel.solution.Switchboard.MessageCustomer;
import mn.unitel.solution.Switchboard.RedisService;
import mn.unitel.solution.Switchboard.ZendeskHandover;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
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


    @ConfigProperty(name = "access.token",defaultValue = "1")
    String accessToken;

    @Inject
    RedisService redisService;

    private static final Logger logger = Logger.getLogger("rasa");



//    @GET
//    @Path("11")
//    @Produces(MediaType.TEXT_PLAIN)
//    public String hello(@QueryParam("id") String id) {
//        return init.check(id);
//        // return "Hello RESTEasy";
//    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String webhookVerify(@QueryParam("hub.mode")String mode, @QueryParam("hub.verify_token")String verifyToken,
                                @QueryParam("hub.challenge")String challenge, String request) {
        if(accessToken.equals(verifyToken)){
            return challenge;
        }
        return "failed";
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

                JsonObject jsonPayload = new JsonObject();
                jsonPayload.put("token",init.getPageToken(info.getId()));
                jsonPayload.put("pageId",info.getId());
                jsonPayload.put("payload",String.format(init.switchboardHandoverRequest,
                        dataStore.getRecipientId(),msg, LocalDateTime.now(),dataStore.getSenderId(),dataStore.getSenderId()));

                Uni.createFrom().item(jsonPayload).onItem().call(x -> zendeskCall(x)).onFailure().recoverWithNull().subscribe().with(System.out::println);

                System.out.println("Msg sent to operator");
            }
            else {
                //sending msg to rasa
                if(postback == null){
                    Uni.createFrom().item(dataStore).onItem().call(x -> send(x)).onFailure().recoverWithNull().subscribe().with(System.out::println);
                    System.out.println("rasa msg");
                }
                else {
                    //Human handover --> from persistent menu
                    if (postback.equals("/human_handover")) {
                        //store 'userId.pageId'
                        redisService.set(dataStore.getSenderId().concat(".").concat(dataStore.recipientId), "hand_over");

                        JsonObject jsonPayload = new JsonObject();
                        jsonPayload.put("token",accessToken);
                        jsonPayload.put("pageId",info.getId());
                        jsonPayload.put("payload",String.format(init.switchboardHandoverRequest,
                                dataStore.getRecipientId(),"hand_over", LocalDateTime.now(),dataStore.getSenderId(),dataStore.senderId));
                        Uni.createFrom().item(jsonPayload).onItem().call(x -> zendeskCall(x)).onFailure().recoverWithNull().subscribe().with(System.out::println);

                        //Send confirmation msg to customer
                        sendConfirmationToCustomer(init.getPageToken(info.getId()), dataStore.senderId);
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

    //Rasa human_handover intent calls this
    @POST
    @Path("passtojava")
    @Consumes(MediaType.APPLICATION_JSON)
    public String process(@QueryParam("access_token")String token,String data) {
        if(!accessToken.equals(token))
            return "failed";

        try {
            //Human handover --> from rasa
            JsonObject jsonObject = new JsonObject(data);
            String senderId = jsonObject.getString("sender");
            String recipientId = jsonObject.getJsonObject("recipient").getString("id");
            JsonObject metadata = jsonObject.getJsonObject("metadata");
            redisService.set(senderId.concat(".").concat(recipientId), "hand_over");
            PageInfo info = init.getPagesInfo().get(recipientId);

            JsonObject jsonPayload = new JsonObject();
            jsonPayload.put("token",accessToken);
            jsonPayload.put("pageId",info.getId());
            jsonPayload.put("payload",String.format(init.switchboardHandoverRequest,
                    metadata.getString("message_id"),metadata.getString("text"), LocalDateTime.now(),senderId,senderId));

            Uni.createFrom().item(jsonPayload).onItem().call(x -> zendeskCall(x)).onFailure().recoverWithNull().subscribe().with(System.out::println);
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



    Uni<String> send(DataStore dataStore) {

        PageInfo info = init.getPagesInfo().get(dataStore.recipientId);

        return RestClientBuilder.newBuilder().baseUri(URI.create(info.url)).build(RasaClient.class).send(dataStore.getValue(), dataStore.sha1, dataStore.sha256);

    }
    //sending payload to zendesk (user msg)
    Uni<String> zendeskCall(JsonObject jsonPayload) {
        return RestClientBuilder.newBuilder().baseUri(URI.create("https://81bc-202-70-46-20.jp.ngrok.io/zendesk"))
                .build(ZendeskHandover.class).call(jsonPayload.getString("token"),
                        jsonPayload.getString("pageId"),jsonPayload.getString("payload"));
    }
    public Uni<String> sendToCustomer(JsonObject jsonPayload) {

        return RestClientBuilder.newBuilder().baseUri(URI.create("https://graph.facebook.com/v15.0/me/messages"))
                .build(MessageCustomer.class).call(jsonPayload.getString("pageToken"),jsonPayload.getString("payload"));
    }
    public String sendConfirmationToCustomer(String token, String recipientId){
        String payload = String.format(init.messageRequest,recipientId,
                "За, таныг ажилтан руу шилжүүллээ. Ажилтан тун удахгүй хариу өгөх тул та түр хүлээгээрэй 🙂");

        JsonObject jsonPayload = new JsonObject();
        jsonPayload.put("pageToken",token);
        jsonPayload.put("payload",payload);
        System.out.println(jsonPayload);


        Uni.createFrom().item(jsonPayload).onItem().call(x -> sendToCustomer(x)).onFailure().recoverWithNull().subscribe().with(System.out::println);

        return "success";
    }

    @PUT
    public String changeMode(@QueryParam("id") String id, @QueryParam("mode") String mode) {
        String result = init.changeMode(id, mode);
        init.readConfiguration();
        return result;
    }


}