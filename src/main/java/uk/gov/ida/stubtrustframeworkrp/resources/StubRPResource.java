package uk.gov.ida.stubtrustframeworkrp.resources;

import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.SignedJWT;
import io.dropwizard.views.View;
import net.minidev.json.JSONObject;
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
import java.io.IOException;
import java.text.ParseException;

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
            @FormParam("jsonResponse") String response, @FormParam("httpStatus") String httpStatus ) throws ParseException, IOException {



        if (httpStatus.equals("200")) {
            JSONObject jsonObject = JSONObjectUtils.parse(response);
            SignedJWT signedJWT = SignedJWT.parse(jsonObject.get("jws").toString());


            return Response.ok(signedJWT.getJWTClaimsSet().toJSONObject()).build();
        } else {
            return Response.ok(response + httpStatus).build();
        }


    }
}
