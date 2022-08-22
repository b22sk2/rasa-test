package mn.unitel.solution;

import io.smallrye.mutiny.Uni;
import mn.unitel.solution.Client.RasaClient;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;


@Path("/webhooks/facebook/webhook")
public class GreetingResource {
    // Config config;
    @Inject
    Init init;

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
    @Consumes(MediaType.APPLICATION_JSON)
    public String wait(String data, @HeaderParam("X-Hub-Signature") String sha1, @HeaderParam("X-Hub-Signature-256") String sha2) throws IOException {
        DataStore dataStore = new DataStore(data, sha1, sha2);
        if (init.getLoaded())
            Uni.createFrom().item(dataStore).onItem().call(x -> send(x)).onFailure().recoverWithNull().subscribe().with(System.out::println);
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