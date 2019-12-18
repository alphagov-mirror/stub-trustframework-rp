package uk.gov.ida.stubtrustframeworkrp.views;

import io.dropwizard.views.View;

public class InvalidResponseView extends View {

    private String error;

    public InvalidResponseView(String error) {
        super("invalidresponse-view.mustache");
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
