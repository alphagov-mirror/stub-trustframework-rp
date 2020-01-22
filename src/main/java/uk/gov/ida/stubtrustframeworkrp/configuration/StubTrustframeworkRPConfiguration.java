package uk.gov.ida.stubtrustframeworkrp.configuration;

import io.dropwizard.Configuration;

public class StubTrustframeworkRPConfiguration extends Configuration {

    private String serviceProviderURI;
    private String verifiableCredentialsURI;
    private String trustframeworkRP;
    private String rp;

    public String getServiceProviderURI() {
        return serviceProviderURI;
    }

    public String getVerifiableCredentialsURI() {
        return verifiableCredentialsURI;
    }

    public String getTrustframeworkRP() {
        return trustframeworkRP;
    }

    public String getRp() {
        return rp;
    }
}
