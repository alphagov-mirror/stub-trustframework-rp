package uk.gov.ida.stubtrustframeworkrp.configuration;

import io.dropwizard.Configuration;

public class StubTrustframeworkRPConfiguration extends Configuration {

    private String stubBrokerURI;

    public String getStubBrokerURI() {
        return stubBrokerURI;
    }
}
