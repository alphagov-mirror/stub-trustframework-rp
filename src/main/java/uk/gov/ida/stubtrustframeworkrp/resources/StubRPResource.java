package uk.gov.ida.stubtrustframeworkrp.resources;

import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.SignedJWT;
import io.dropwizard.views.View;
import net.minidev.json.JSONObject;
import uk.gov.ida.stubtrustframeworkrp.configuration.StubTrustframeworkRPConfiguration;
import uk.gov.ida.stubtrustframeworkrp.rest.Urls;
import uk.gov.ida.stubtrustframeworkrp.service.ResponseService;
import uk.gov.ida.stubtrustframeworkrp.views.StartPageView;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.text.ParseException;
import java.util.Collection;
import java.util.stream.Collectors;

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
            @FormParam("jsonResponse") String response, @FormParam("httpStatus") String httpStatus ) throws ParseException {

        if (httpStatus.equals("200") && !(response.length() == 0)) {
            JSONObject jsonResponse = JSONObjectUtils.parse(response);
            Collection<String> errors = responseService.validateResponse(jsonResponse);
            if (errors.isEmpty()) {
                return Response.ok(SignedJWT.parse(jsonResponse.get("jws").toString()).getJWTClaimsSet().toString()).build();
            }
                return Response.ok("The following errors have occurred in the response: " + errors.stream().collect(Collectors.joining(" , "))).build();
        } else {
            return Response.ok("Error in response with HttpStatus: " + httpStatus + " Response Body: " + response).build();
        }
    }
}
