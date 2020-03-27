package uk.gov.ida.stubtrustframeworkrp.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.SignedJWT;
import io.dropwizard.views.View;
import net.minidev.json.JSONObject;
import uk.gov.ida.stubtrustframeworkrp.configuration.StubTrustframeworkRPConfiguration;
import uk.gov.ida.stubtrustframeworkrp.dto.Address;
import uk.gov.ida.stubtrustframeworkrp.dto.IdentityAttributes;
import uk.gov.ida.stubtrustframeworkrp.dto.OidcResponseBody;
import uk.gov.ida.stubtrustframeworkrp.rest.Urls;
import uk.gov.ida.stubtrustframeworkrp.services.QueryParameterHelper;
import uk.gov.ida.stubtrustframeworkrp.services.ResponseService;
import uk.gov.ida.stubtrustframeworkrp.views.FailedToSignInView;
import uk.gov.ida.stubtrustframeworkrp.views.FailedToSignUpView;
import uk.gov.ida.stubtrustframeworkrp.views.IdentityValidatedView;
import uk.gov.ida.stubtrustframeworkrp.views.InvalidResponseView;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/")
public class StubRpResponseResource {

    private final StubTrustframeworkRPConfiguration configuration;
    private final ResponseService responseService;

    public StubRpResponseResource(StubTrustframeworkRPConfiguration configuration, ResponseService responseService) {
        this.configuration = configuration;
        this.responseService = responseService;
    }

    @POST
    @Path("/authenticationResponse")
    public View handleAuthenticationResponse(String responseBody) throws ParseException {
        if (responseBody.contains("error")) {
            Map<String, String> responseMap = QueryParameterHelper.splitQuery(responseBody);
            String error = responseMap.get("error");
            if (error.equals("unmet_authentication_requirements")) {
                return new FailedToSignInView(
                        configuration.getRp(),
                        configuration.getContractedIdpURI());
            }
            return new InvalidResponseView(responseMap.get("error") + ": " + responseMap.get("error_description"));
        }

        String transactionID = responseService.getTransactionIDFromResponse(responseBody);
        String state = responseService.getStateFromSession(transactionID);
        String nonce = responseService.getNonceFromSession(state);
        OidcResponseBody oidcResponseBody = new OidcResponseBody(responseBody, state, nonce);

        String userCredentials = sendAuthenticationResponseToServiceProvider(oidcResponseBody);

        JSONObject jsonResponse = JSONObjectUtils.parse(userCredentials);
        if (jsonResponse.get("jws") == null) {
            return new InvalidResponseView(jsonResponse.toJSONString());
        }
        SignedJWT brokerJWT = SignedJWT.parse(jsonResponse.get("jws").toString());

        boolean validSignature = responseService.validateSignatureOfJWT(
                brokerJWT, "broker", brokerJWT.getHeader().getKeyID());

        if (!validSignature) {
            return new InvalidResponseView("Invalid signature of Broker JWT");
        }
        JSONObject brokerClaimSet = brokerJWT.getJWTClaimsSet().toJSONObject();

        IdentityAttributes identityAttributes;
        if (brokerClaimSet.get("_claim_names") != null) {
            identityAttributes = parseAggregatedClaims(brokerClaimSet);
        } else if (brokerClaimSet.containsKey("vp")) {
            identityAttributes = parseVerifiablePresentation(brokerClaimSet);
        } else {
            identityAttributes = mapClaimsToIdentityAttributes(brokerClaimSet);
        }

        return new IdentityValidatedView(identityAttributes);
    }

    @POST
    @Path("/response")
    public View receiveResponse(
            @FormParam("jsonResponse") String response, @FormParam("httpStatus") String httpStatus) {

        if (httpStatus.equals("200") && !(response.length() == 0) && !response.contains("error")) {
            JSONObject jsonResponse;
            JSONObject jsonObject;
            Address address;
            try {
                jsonResponse = JSONObjectUtils.parse(response);
                jsonObject = SignedJWT.parse(jsonResponse.get("jws").toString()).getJWTClaimsSet().toJSONObject();
                address = deserializeAddressFromJWT(jsonObject);

                Map<String, String> claims = new HashMap<>();
                for (String key : jsonObject.keySet()) {
                    String value = jsonObject.get(key).toString();
                    claims.put(key, value);
                }

                return new IdentityValidatedView(configuration.getRp(), address, null);

            } catch (ParseException | IOException e) {
                return new InvalidResponseView(e.toString());
            }
        }
        return new InvalidResponseView("Status: " + httpStatus + " with response: " + response);
    }

    @GET
    @Path("/failed-to-sign-up")
    public View FailedToSignUpPage() {
        return new FailedToSignUpView();
    }

    private IdentityAttributes parseAggregatedClaims(JSONObject jsonObject) {
        JSONObject claims = new JSONObject();
        JSONObject addressJsonObject;

        JSONObject claimSources = (JSONObject) jsonObject.get("_claim_sources");
        JSONObject claimNames = (JSONObject) jsonObject.get("_claim_names");

        List<String> distinctClaimNameValues = claimNames.values()
                .stream().distinct()
                .map(Object::toString)
                .collect(Collectors.toList());

        for (String distinctClaimName : distinctClaimNameValues) {
            JSONObject claimSourceNameJson = (JSONObject) claimSources.get(distinctClaimName);
            JSONObject jsonClaims;
            try {
                SignedJWT signedJWT = SignedJWT.parse(claimSourceNameJson.get("JWT").toString());
                boolean validSignature = responseService
                        .validateSignatureOfJWT(signedJWT, "idp", signedJWT.getHeader().getKeyID());
                if (!validSignature) {
                    throw new RuntimeException("Invalid Signature ahhhhh");
                }
                jsonClaims = signedJWT.getJWTClaimsSet().toJSONObject();

                if (jsonClaims.containsKey("vc")) {
                    try {
                        JSONObject credential = JSONObjectUtils.parse(jsonClaims.get("vc").toString());
                        JSONObject credentialSubject = JSONObjectUtils.parse(credential.get("credentialSubject").toString());
                        addressJsonObject = (JSONObject) credentialSubject.get("address");
                        claims.put("address", addressJsonObject);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    List<String> claimsInJWT = claimNames.entrySet()
                            .stream()
                            .filter(t -> t.getValue().equals(distinctClaimName))
                            .map(a -> a.getKey())
                            .collect(Collectors.toList());

                    for (String jsonClaimKey : claimsInJWT) {
                        String value = jsonClaims.get(jsonClaimKey).toString();
                        claims.put(jsonClaimKey, value);
                    }
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return mapClaimsToIdentityAttributes(claims);
    }

    private IdentityAttributes parseVerifiablePresentation(JSONObject jsonObject) {
        JSONObject claims = new JSONObject();
        JSONObject verifiablePresentation = (JSONObject) jsonObject.get("vp");
        List<String> verifiableCredentialList = (List<String>) verifiablePresentation.get("verifiableCredential");

        for (String vc : verifiableCredentialList) {
            try {
                SignedJWT signedJWT = SignedJWT.parse(vc);
                boolean validSignature = responseService
                        .validateSignatureOfJWT(signedJWT, "idp", signedJWT.getJWTClaimsSet().getIssuer());
                if (!validSignature) {
                    throw new RuntimeException("Invalid JWT Signature");
                }
                JSONObject jsonClaims = signedJWT.getJWTClaimsSet().toJSONObject();
                JSONObject credential = JSONObjectUtils.parse(jsonClaims.get("vc").toString());
                JSONObject credentialSubject = JSONObjectUtils.parse(credential.get("credentialSubject").toString());
                if (credentialSubject.containsKey("address")) {
                    claims.put("address", credentialSubject.get("address"));
                } else if (credentialSubject.containsKey("ho_positive_verification_notice")) {
                    claims.put("ho_positive_verification_notice", credentialSubject.containsKey("ho_positive_verification_notice"));
                } else if (credentialSubject.containsKey("bankAccount")) {
                    JSONObject bankAccount = (JSONObject) credentialSubject.get("bankAccount");
                    claims.put("bank_account_number", bankAccount.get("bank_account_number"));
                }  else if (credentialSubject.containsKey("addressCovers5Years")) {
                    JSONObject bankAccount = (JSONObject) credentialSubject.get("addressCovers5Years");
                    claims.put("address_covers_5_years", bankAccount.get("address_covers_5_years"));
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return mapClaimsToIdentityAttributes(claims);
    }

    private String sendAuthenticationResponseToServiceProvider(OidcResponseBody oidcResponseBody) {
        URI uri = UriBuilder.fromUri(configuration.getServiceProviderURI())
                .path(Urls.ServiceProvider.AUTHN_RESPONSE_URI)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String oidcResponseToString;
        try {
            oidcResponseToString = objectMapper.writeValueAsString(oidcResponseBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(oidcResponseToString))
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

    private IdentityAttributes mapClaimsToIdentityAttributes(JSONObject claims) {
        IdentityAttributes identityAttributes;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            identityAttributes = objectMapper.readValue(claims.toJSONString(), IdentityAttributes.class);
        } catch (IOException e) {
            throw new RuntimeException("Cant map to IdentityAttributes object", e);
        }

        return identityAttributes;
    }

    private Address deserializeAddressFromJWT(JSONObject jwtJson) throws IOException, ParseException {
        if (!jwtJson.containsKey("vc")) {
            return null;
        }
        JSONObject credential = JSONObjectUtils.parse(jwtJson.get("vc").toString());
        JSONObject credentialSubject = JSONObjectUtils.parse(credential.get("credentialSubject").toString());
        JSONObject jsonAddress = JSONObjectUtils.parse(credentialSubject.get("address").toString());
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonAddress.toJSONString(), Address.class);
    }

}
