package mn.unitel.solution.Switchboard;

import io.smallrye.mutiny.Uni;
import mn.unitel.solution.Client.RasaClient;
import mn.unitel.solution.DataStore;
import mn.unitel.solution.GreetingResource;
import mn.unitel.solution.Init;
import mn.unitel.solution.PageInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.net.URI;

@Path("switchboard")
public class AgentToCustomer {

    @Inject
    Init init;
    @ConfigProperty(name = "zendesk.token",defaultValue = "1")
    String token;

    Uni<String> send(DataStore dataStore) {

        PageInfo info = init.getPagesInfo().get(dataStore.getRecipientId());
        return RestClientBuilder.newBuilder().baseUri(URI.create(info.getUrl())).build(RasaClient.class)
                .send(dataStore.getValue(), dataStore.getSha1(), dataStore.getSha256());

    }

    @Path("message")
    @POST
    public String agentSend(String data, @HeaderParam("X-Hub-Signature") String sha1, @HeaderParam("X-Hub-Signature-256") String sha2){
        DataStore dataStore = new DataStore(data, sha1, sha2);
        if (init.getLoaded())
            Uni.createFrom().item(dataStore).onItem().call(x -> send(x)).onFailure().recoverWithNull().subscribe().with(System.out::println);
            // init.push(dataStore);
        else {
            System.out.println("not loaded");
        }

        return "success";

    }


}
