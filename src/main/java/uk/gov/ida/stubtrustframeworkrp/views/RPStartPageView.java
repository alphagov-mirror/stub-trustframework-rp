package uk.gov.ida.stubtrustframeworkrp.views;

import io.dropwizard.views.View;

public class RPStartPageView extends View {

    public RPStartPageView(String rp) {

        super(rp + "-startpage.mustache");
    }
}
