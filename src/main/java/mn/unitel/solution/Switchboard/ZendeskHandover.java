package mn.unitel.solution.Switchboard;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
@RegisterRestClient(baseUri = "https://81bc-202-70-46-20.jp.ngrok.io/zendesk")
public interface ZendeskHandover {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<String> call(@QueryParam("access_token") String token,
                            @QueryParam("pageId") String pageId, String request);
}
