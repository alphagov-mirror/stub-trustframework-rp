package uk.gov.ida.stubtrustframeworkrp.rest;

public interface Urls {

    interface ServiceProvider {
        String REQUEST_URI = "/formPost/generateAuthenticationRequest";
        String AUTHN_RESPONSE_URI = "/formPost/validateAuthenticationResponse";
    }

    interface Broker {
        String REQUEST_URI = "/picker";
    }

    interface RP {
        String RESPONSE_URI = "/response";
    }
}
