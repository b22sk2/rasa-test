package mn.unitel.solution.Switchboard;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@RegisterRestClient(baseUri = "https://graph.facebook.com/v14.0/me/take_thread_control?access_token=EAAhcipyDxDMBACFQ18Dfv5TjimH8mxzAtT6LPIrvl6ZAoWFjYGFJo9qnGzIJZAPNtZBdEak7LZAw3Fzi183bJCcRybuI3qub8jbRIlg1khhsPZBB9iQ0uaBl0vicMTH4cKGljxDtmRYUQIQebMBYgE8n9jSnZA4WODzjVKWVZBpEyvIPpJQ1O7dBoFLpDAlZCnDKvtlAKvf2YQZDZD")
public interface TakeControl {
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    String call(@QueryParam("access_token") String token, String request);
}
