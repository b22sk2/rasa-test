package mn.unitel.solution;/*
 * @created_at 28/07/2022 7:32 PM
 * @project rasa-gw
 * @author baasankhuu.d
 */

import io.vertx.core.json.JsonObject;
import org.jboss.logging.Logger;

public class DataStore {

    private static final Logger logger = Logger.getLogger("rasa");
    String value ;
    String sha1;
    String sha256;
    String recipientId;
    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    String senderId;
    public DataStore(String value, String sha1, String sha256) {
        this.value = value;
        this.sha1 = sha1;
        this.sha256 = sha256;
        try{ 
            JsonObject jObject = new JsonObject(value);
            JsonObject messaging = jObject.getJsonArray("entry").getJsonObject(0).getJsonArray("messaging").getJsonObject(0);
            String recipientId = messaging.getJsonObject("recipient").getString("id"); 
            String senderId =  messaging.getJsonObject("sender").getString("id");
            this.recipientId = recipientId;
            this.senderId = senderId;
            logger.info("recieved recipienId&senderId - " + recipientId + "-"+senderId);
        }
        catch(Exception ex){
           
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }
}
