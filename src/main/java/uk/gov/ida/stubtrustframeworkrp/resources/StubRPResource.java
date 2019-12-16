package uk.gov.ida.stubtrustframeworkrp.resources;

import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.SignedJWT;
import io.dropwizard.views.View;
import net.minidev.json.JSONObject;
import uk.gov.ida.stubtrustframeworkrp.configuration.StubTrustframeworkRPConfiguration;
import uk.gov.ida.stubtrustframeworkrp.rest.Urls;
import uk.gov.ida.stubtrustframeworkrp.service.ResponseService;
import uk.gov.ida.stubtrustframeworkrp.views.TellUsWhoYouAreView;
import uk.gov.ida.stubtrustframeworkrp.views.RPStartView;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.text.ParseException;

@Path("/")
public class StubRPResource {

    private final StubTrustframeworkRPConfiguration configuration;
    private final ResponseService responseService;

    public StubRPResource(StubTrustframeworkRPConfiguration configuration, ResponseService responseService) {
        this.configuration = configuration;
        this.responseService = responseService;
    }

    @GET
    @Path("/")
    public View startPage() {
        return new RPStartView(configuration.getRp());
    }

    @GET
    @Path("/tellUsWhoYouAre")
    public View tellUsWhoYouAre() {
        return new TellUsWhoYouAreView();
    }

    @GET
    @Path("/sendRequest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendRequest() {
        URI uri = UriBuilder.fromUri(configuration.getStubBrokerURI()).path(Urls.StubBroker.REQUEST_URI).queryParam("response-uri", configuration.getTrustframeworkRP()).build();
        
        return Response
                .status(302)
                .location(uri)
                .build();
    }

    @POST
    @Path("/response")
    public Response receiveResponse(
            @FormParam("jsonResponse") String response, @FormParam("httpStatus") String httpStatus ) throws ParseException {

        if (httpStatus.equals("200") && !(response.length() == 0)) {
            JSONObject jsonResponse = JSONObjectUtils.parse(response);
            String parsedClaimSet = SignedJWT.parse(jsonResponse.get("jws").toString()).getJWTClaimsSet().toString();
            return Response.ok("The Response from Broker is: " + httpStatus + " Response Body: " + parsedClaimSet).build();
            }
        return Response.ok("Error Response from Broker: " + httpStatus + " Response Body: " + response).build();
    }
}
