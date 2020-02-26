package uk.gov.ida.stubtrustframeworkrp.views;

import io.dropwizard.views.View;
import uk.gov.ida.stubtrustframeworkrp.dto.Address;

import java.util.Map;
import java.util.Set;

public class IdentityValidatedView extends View {

    private boolean isDBS;
    private Address address;
    Set<Map.Entry<String,String>> claimsSet;
    private String rawJSON;

    public IdentityValidatedView(String rpName, Address address, Map<String,String> claims, String rawJSON) {
        super("identityvalidated-view.mustache");
        this.isDBS = rpName.equals("dbs");
        this.address = address;
        this.claimsSet = claims != null ? claims.entrySet() : null;
        this.rawJSON = rawJSON;
    }

    public boolean getHasAddress() { return address != null; }

    public boolean getHasClaimsSet() { return claimsSet != null; }

    public boolean getDBS() {
        return isDBS;
    }

    public Address getAddress() {
        return address;
    }

    public Set<Map.Entry<String,String>> getClaimsSet() { return claimsSet; }

    public String getRawJSON() { return rawJSON; }
}
