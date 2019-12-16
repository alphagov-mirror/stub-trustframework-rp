package uk.gov.ida.stubtrustframeworkrp.views;

import io.dropwizard.views.View;

public class IdentityValidatedView extends View {

    private boolean isDBS;

    public IdentityValidatedView(String rpName) {
        super("identityvalidated-view.mustache");
        this.isDBS = rpName.equals("dbs");
    }

    public boolean getDBS() {
        return isDBS;
    }
}
