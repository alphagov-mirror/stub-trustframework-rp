package uk.gov.ida.stubtrustframeworkrp.views;

import io.dropwizard.views.View;

public class RPStartView extends View {

    public RPStartView(String rp) {

        super(rp + "-start-view.mustache");
    }
}
