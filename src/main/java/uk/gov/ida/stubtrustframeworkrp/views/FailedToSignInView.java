package uk.gov.ida.stubtrustframeworkrp.views;

import io.dropwizard.views.View;

public class FailedToSignInView extends View {

    private String contractedIdpUri;

    public FailedToSignInView(String rp, String contractedIdpUri) {

        super(rp + "-failed-to-sign-in-view.mustache");
        this.contractedIdpUri = contractedIdpUri;
    }

    public String getContractedIdpUri() {
        return contractedIdpUri;
    }
}
