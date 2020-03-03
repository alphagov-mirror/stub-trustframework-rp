package uk.gov.ida.stubtrustframeworkrp.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jwt.SignedJWT;
import net.minidev.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.internal.util.Base64;
import uk.gov.ida.stubtrustframeworkrp.configuration.StubTrustframeworkRPConfiguration;
import uk.gov.ida.stubtrustframeworkrp.dto.OidcResponseBody;

import javax.ws.rs.core.UriBuilder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class ResponseService {

    private final StubTrustframeworkRPConfiguration configuration;
    private final RedisService redisService;

    public ResponseService(StubTrustframeworkRPConfiguration configuration, RedisService redisService) {
        this.configuration = configuration;
        this.redisService = redisService;
    }

    public String getStateFromSession(String transactionID) {
        String state = redisService.get(transactionID);
        if (state == null || state.length() <1) {
            throw new RuntimeException("State not found in datastore");
        }
        return state;
    }

    public String getNonceFromSession(String state) {
        String nonce = redisService.get("state::" + state);
        if (nonce == null || nonce.length() < 1) {
            throw new RuntimeException("Nonce not found in data store");
        }
        return nonce;
    }

    public String getTransactionIDFromResponse(String responseBody) {
        Map<String, String> response = QueryParameterHelper.splitQuery(responseBody);
        return response.get("transactionID");
    }

    public OidcResponseBody generateOidcResponse(String responseBody, String state, String nonce) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("oidcResponse", responseBody);
        jsonObject.put("state", state);
        jsonObject.put("nonce", nonce);

        OidcResponseBody oidcResponseBody;
        try {
            oidcResponseBody = new ObjectMapper()
                    .readerFor(OidcResponseBody.class)
                    .readValue(jsonObject.toJSONString());
        } catch (IOException e) {
            throw new RuntimeException("Unable to read value when mapping JSON to OidcResponseBody");
        }

        return oidcResponseBody;
    }

    public Collection<String> validateResponse(JSONObject jsonResponse) throws ParseException {
        Collection<String> errors = new HashSet<>();

        SignedJWT signedJWT = SignedJWT.parse(jsonResponse.get("jws").toString());
        PublicKey publicKey = getPublicKeyFromString(jsonResponse.get("public_key").toString());
        boolean hasValidatedJWTSuccessfully = validateJWTSignature(publicKey, signedJWT);
        boolean hasValidatedVCSuccessfully = validateVerifiableCredentials(signedJWT);

        if (!hasValidatedJWTSuccessfully) {
            errors.add("The JWT signature has not been validated successfully");
        } else if (!hasValidatedVCSuccessfully) {
            errors.add("The Verifiable Credential has not been validated successfully");
        }
        return errors;
    }

    //This will be validated by the RP and the logic in the Node app needs to be reversed engineered into here
    private boolean validateVerifiableCredentials(SignedJWT signedJWT) throws ParseException {

        String jsonString = signedJWT.getJWTClaimsSet().toJSONObject().toJSONString().replace("\\", "");

        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .uri(UriBuilder.fromUri(configuration.getVerifiableCredentialsURI()).path("/verify").build())
                .build();

        HttpResponse<String> response;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (response.statusCode() == HttpStatus.OK_200) {
            return true;
        }
        return false;
    }

    public boolean validateSignatureOfJWT(SignedJWT signedJWT, String component, String issuer) {
        URI directoryEndpoint = UriBuilder.fromUri(configuration.getDirectoryURI()).path("organisation").path(component).path(issuer).path("certificate/signing").build();
        PublicKey publicKey = getPublicKeyFromDirectory(directoryEndpoint);
        boolean validSignature = validateJWTSignature(publicKey, signedJWT);

        return validSignature;
    }

    private PublicKey getPublicKeyFromDirectory(URI directoryEndpoint) {
        HttpResponse<String> response = sendHttpRequest(directoryEndpoint);

        String responseString = response.body();

        JSONObject jsonResponse;
        try {
            jsonResponse = JSONObjectUtils.parse(responseString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        responseString = jsonResponse.get("signing").toString();

        responseString = responseString.replaceAll("\\n", "").replace("-----BEGIN CERTIFICATE-----", "").replace("-----END CERTIFICATE-----", "");
        byte[] encodedPublicKey = Base64.decode(responseString.getBytes());

        X509Certificate cert;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(encodedPublicKey);
            cert = (X509Certificate) cf.generateCertificate(in);
            return cert.getPublicKey();
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean validateJWTSignature(PublicKey publicKey, SignedJWT signedJWT) {
        RSASSAVerifier verifier = new RSASSAVerifier((RSAPublicKey) publicKey);
        boolean isVerified;

        try {
            isVerified = signedJWT.verify(verifier);
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }

        return isVerified;
    }

    private PublicKey getPublicKeyFromString(String publicKey) {
        publicKey = publicKey.replaceAll("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
        byte[] encodedPublicKey = Base64.decode(publicKey.getBytes());

        try {
            X509EncodedKeySpec x509publicKey = new X509EncodedKeySpec(encodedPublicKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(x509publicKey);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpResponse<String> sendHttpRequest(URI uri) {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(uri)
                .build();

        try {
            return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {

            throw new RuntimeException(e);
        }
    }
}
