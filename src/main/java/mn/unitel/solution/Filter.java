package mn.unitel.solution;/*
 * @created_at 28/07/2022 7:23 PM
 * @project rasa-gw
 * @author baasankhuu.d
 */

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class Filter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext context) {
    }
}