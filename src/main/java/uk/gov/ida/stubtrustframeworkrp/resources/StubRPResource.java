package uk.gov.ida.stubtrustframeworkrp.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.SignedJWT;
import io.dropwizard.views.View;
import net.minidev.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;
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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
            @FormParam("jsonResponse") String response, @FormParam("httpStatus") String httpStatus ) throws ParseException, JsonProcessingException {

        if (httpStatus.equals("200")) {
            JSONObject jsonObject = JSONObjectUtils.parse(response);
            SignedJWT signedJWT = SignedJWT.parse(jsonObject.get("jws").toString());
            if (validateVerifiableCredentials(signedJWT)) {
                return Response.ok(signedJWT.getJWTClaimsSet().toJSONObject().toJSONString()).build();
            }
                return Response.ok("Unable to validate signature of VerifiableCredentials").build();
        } else {
            return Response.ok(response + httpStatus).build();
        }
    }

    //This will be validated by the RP and the logic in the Node app needs to be reversed engineered into here
    private boolean validateVerifiableCredentials(SignedJWT signedJWT) throws ParseException {
        HttpClient httpClient = HttpClient.newBuilder()
                .build();

        String jsonString = signedJWT.getJWTClaimsSet().toJSONObject().toJSONString().replace("\\", "");

        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .uri(UriBuilder.fromUri(configuration.getVerifiableCredentialsURI()).path("/verify").build())
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (response.statusCode() == HttpStatus.OK_200) {
            return true;
        }
        return false;
    }
}
