package uk.gov.ida.stubtrustframeworkrp.configuration;

import io.dropwizard.Configuration;

public class StubTrustframeworkRPConfiguration extends Configuration {

    private String serviceProviderURI;
    private String verifiableCredentialsURI;
    private String trustframeworkRP;
    private String rp;
    private String brokerURI;
    private boolean usingServiceProvider;
    private String redisURI;
    private String contractedIdpURI;

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

    public String getBrokerURI() {
        return brokerURI;
    }

    public boolean isUsingServiceProvider() {
        return usingServiceProvider;
    }

    public String getRedisURI() {
        return redisURI;
    }

    public String getContractedIdpURI() {
        return contractedIdpURI;
    }
}
