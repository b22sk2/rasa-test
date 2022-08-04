package mn.unitel.solution.Client;

import javax.interceptor.AroundTimeout;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(baseUri = "https://graph.facebook.com/v14.0/me/pass_thread_control")
public interface Handover {
    @POST
    @AroundTimeout()
    public String send(@QueryParam("access_token") String accessToken, String request);
}
