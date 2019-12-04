package uk.gov.ida.stubtrustframeworkrp.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import net.minidev.json.JSONObject;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.internal.util.Base64;
import uk.gov.ida.stubtrustframeworkrp.configuration.StubTrustframeworkRPConfiguration;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;

public class ResponseService {

    private final StubTrustframeworkRPConfiguration configuration;

    public ResponseService(StubTrustframeworkRPConfiguration configuration) {
        this.configuration = configuration;
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
}
