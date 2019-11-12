package uk.gov.ida.stubtrustframeworkrp.resources;

import io.dropwizard.views.View;
import uk.gov.ida.stubtrustframeworkrp.configuration.StubTrustframeworkRPConfiguration;
import uk.gov.ida.stubtrustframeworkrp.rest.Urls;
import uk.gov.ida.stubtrustframeworkrp.views.StartPageView;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Path("/")
public class StubRPResource {

    private final StubTrustframeworkRPConfiguration configuration;

    public StubRPResource(StubTrustframeworkRPConfiguration configuration) {
        this.configuration = configuration;
    }

    @GET
    @Path("/")
    public View startPage() {
        return new  StartPageView();
    }

    @GET
    @Path("/sendRequest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendRequest() {

        return Response
                .status(302)
                .location(UriBuilder.fromUri(configuration.getStubBrokerURI()).path(Urls.StubBroker.REQUEST_URI).build())
                .build();
    }

    @POST
    @Path("/response")
    public Response receiveResponse(
            @FormParam("jsonResponse") String response
    ) {

       return Response.ok(response).build();
    }
}
