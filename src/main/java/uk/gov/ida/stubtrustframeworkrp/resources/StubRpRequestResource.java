package uk.gov.ida.stubtrustframeworkrp.resources;

import io.dropwizard.views.View;
import uk.gov.ida.stubtrustframeworkrp.configuration.StubTrustframeworkRPConfiguration;
import uk.gov.ida.stubtrustframeworkrp.rest.Urls;
import uk.gov.ida.stubtrustframeworkrp.services.RequestService;
import uk.gov.ida.stubtrustframeworkrp.views.RPStartView;
import uk.gov.ida.stubtrustframeworkrp.views.TellUsWhoYouAreView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static uk.gov.ida.stubtrustframeworkrp.services.QueryParameterHelper.splitQuery;

@Path("/")
public class StubRpRequestResource {

    private final StubTrustframeworkRPConfiguration configuration;
    private final RequestService requestService;

    public StubRpRequestResource(StubTrustframeworkRPConfiguration configuration, RequestService requestService) {
        this.configuration = configuration;
        this.requestService = requestService;
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

    @POST
    @Path("/sendRequest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendRequest(String claimParams) throws URISyntaxException {
        URI uri;
        Map<String, String> claimRequest = splitQuery(claimParams);

        if (configuration.isUsingServiceProvider()) {
            uri = new URI(generateRequestFromServiceProvider(claimRequest));
            requestService.storeNonceAndState(uri);
        } else {
            URI responseURI = UriBuilder.fromUri(configuration.getTrustframeworkRP()).path(Urls.RP.RESPONSE_URI).build();
            uri = UriBuilder.fromUri(configuration.getBrokerURI()).path(Urls.Broker.REQUEST_URI).queryParam("response-uri", responseURI).build();
        }

        return Response
                .status(302)
                .location(uri)
                .build();
    }

    private String generateRequestFromServiceProvider(Map<String, String> claimRequest) {
        URI uri = UriBuilder.fromUri(configuration.getServiceProviderURI())
                                    .path(Urls.ServiceProvider.REQUEST_URI)
                                    .queryParam("name", claimRequest.get("name"))
                                    .queryParam("family-name", claimRequest.get("family-name"))
                                    .queryParam("given-name", claimRequest.get("given-name"))
                                    .queryParam("middle-name", claimRequest.get("middle-name"))
                                    .queryParam("birthday", claimRequest.get("birthday"))
                                    .build();

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        HttpResponse<String> responseBody;
        try {
            responseBody = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return responseBody.body();
    }
}
