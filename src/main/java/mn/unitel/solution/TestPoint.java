package mn.unitel.solution;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("/zendesk")
public class TestPoint {

    //mock zendesk endpoint
    @POST
    public String receive(@QueryParam("access_token")String token,
                          @QueryParam("pageId")String pageId, String payload) {

        return "done";

    }
}
