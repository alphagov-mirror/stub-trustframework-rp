package uk.gov.ida.stubtrustframeworkrp.views;

import io.dropwizard.views.View;
import uk.gov.ida.stubtrustframeworkrp.dto.Address;
import uk.gov.ida.stubtrustframeworkrp.dto.IdentityAttributes;

import java.util.Map;
import java.util.Set;

public class IdentityValidatedView extends View {

    private Address address;
    private IdentityAttributes identityAttributes;
    Set<Map.Entry<String,String>> claimsSet;

    public IdentityValidatedView(String rpName, Address address, IdentityAttributes identityAttributes) {
        super("identityvalidated-view.mustache");
        this.address = address;
        this.identityAttributes = identityAttributes;
    }

    public IdentityAttributes getIdentityAttributes() {
        return identityAttributes;
    }

    public boolean hasIdentityAttributes() {
        return identityAttributes != null;
    }

    public boolean getHasAddress() { return address != null; }

    public boolean getHasClaimsSet() { return claimsSet != null; }


    public Address getAddress() {
        return address;
    }

    public Set<Map.Entry<String,String>> getClaimsSet() { return claimsSet; }
}
