package uk.gov.ida.stubtrustframeworkrp.views;

import io.dropwizard.views.View;
import uk.gov.ida.stubtrustframeworkrp.domain.Address;

public class IdentityValidatedView extends View {

    private boolean isDBS;
    private Address address;

    public IdentityValidatedView(String rpName, Address address) {
        super("identityvalidated-view.mustache");
        this.isDBS = rpName.equals("dbs");
        this.address = address;
    }

    public boolean getDBS() {
        return isDBS;
    }

    public Address getAddress() {
        return address;
    }
}
