package mn.unitel.solution.Switchboard;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
@RegisterRestClient(baseUri = "http://127.0.0.1/pass_thread_control")
public interface ZendeskHandover {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    String  call(@QueryParam("access_token") String token,
                 @QueryParam("pageId") String pageId, String request);
}
