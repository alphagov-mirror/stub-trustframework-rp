package uk.gov.ida.stubtrustframeworkrp;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import uk.gov.ida.stubtrustframeworkrp.configuration.StubTrustframeworkRPConfiguration;
import uk.gov.ida.stubtrustframeworkrp.resources.StubRPResource;
import uk.gov.ida.stubtrustframeworkrp.service.ResponseService;

public class StubTrustframeworkRPApplication extends Application<StubTrustframeworkRPConfiguration> {

    public static void main(String[] args) throws Exception {
        new StubTrustframeworkRPApplication().run(args);
    }

    @Override
    public void run(StubTrustframeworkRPConfiguration configuration, Environment environment) {
        environment.jersey().register(new StubRPResource(configuration, new ResponseService(configuration)));
    }

    @Override
    public void initialize(final Bootstrap<StubTrustframeworkRPConfiguration> bootstrap) {
        bootstrap.addBundle(new ViewBundle<>());
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)));
    }
}
