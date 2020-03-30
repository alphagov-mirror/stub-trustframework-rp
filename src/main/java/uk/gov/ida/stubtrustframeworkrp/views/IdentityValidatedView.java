package uk.gov.ida.stubtrustframeworkrp.views;

import io.dropwizard.views.View;
import uk.gov.ida.stubtrustframeworkrp.dto.Address;
import uk.gov.ida.stubtrustframeworkrp.dto.IdentityAttributes;

public class IdentityValidatedView extends View {

    private Address address;
    private IdentityAttributes identityAttributes;
    private String brokerJWT;

    public IdentityValidatedView(String rpName, Address address, IdentityAttributes identityAttributes) {
        super("identityvalidated-view.mustache");
        this.address = address;
        this.identityAttributes = identityAttributes;
    }

    public IdentityValidatedView(IdentityAttributes identityAttributes,
                                 String brokerJWT) {
        super("identityvalidated-view.mustache");
        this.identityAttributes = identityAttributes;
        this.brokerJWT = brokerJWT;
    }

    public IdentityAttributes getIdentityAttributes() {
        return identityAttributes;
    }

    public boolean hasIdentityAttributes() {
        return identityAttributes != null;
    }

    public boolean getHasAddress() { return address != null; }

    public Address getAddress() {
        return address;
    }

    public String getBrokerJWT() {
        return brokerJWT;
    }
}
