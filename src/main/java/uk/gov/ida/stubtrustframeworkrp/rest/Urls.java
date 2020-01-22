package uk.gov.ida.stubtrustframeworkrp.rest;

public interface Urls {

    interface ServiceProvider {
        String REQUEST_URI = "/formPost/generateAuthenticationRequest";
        String AUTHN_RESPONSE_URI = "/formPost/validateAuthenticationResponse";
    }
}
