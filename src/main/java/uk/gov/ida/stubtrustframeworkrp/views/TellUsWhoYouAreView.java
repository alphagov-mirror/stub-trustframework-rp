package uk.gov.ida.stubtrustframeworkrp.views;

import io.dropwizard.views.View;

public class TellUsWhoYouAreView extends View {

    private String rp;

    public TellUsWhoYouAreView(String rp) {
        super("telluswhoyouare-view.mustache");
        this.rp = rp;
    }

    public boolean isDWP() {
        return rp.equals("dwp");
    }
}
