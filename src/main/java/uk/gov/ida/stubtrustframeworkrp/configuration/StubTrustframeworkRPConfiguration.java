package uk.gov.ida.stubtrustframeworkrp.configuration;

import io.dropwizard.Configuration;

public class StubTrustframeworkRPConfiguration extends Configuration {

    private String stubBrokerURI;
    private String verifiableCredentialsURI;

    public String getStubBrokerURI() {
        return stubBrokerURI;
    }

    public String getVerifiableCredentialsURI() {
        return verifiableCredentialsURI;
    }
}
