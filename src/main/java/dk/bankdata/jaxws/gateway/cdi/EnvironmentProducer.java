package dk.bankdata.jaxws.gateway.cdi;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

import dk.bankdata.jaxws.gateway.domain.Environment;

@ApplicationScoped
public class EnvironmentProducer {

    private Environment environment;

    @PostConstruct
    protected void createEnvironment() {
        String soapScheme = loadSystemEnvironmentVariable("SOAP_SCHEME");
        String soapHost = loadSystemEnvironmentVariable("SOAP_HOST");
        int soapPort = Integer.parseInt(loadSystemEnvironmentVariable("SOAP_PORT"));

        environment = new Environment(soapScheme, soapHost, soapPort);
    }

    @Produces
    @Dependent
    public Environment create() {
        return environment;
    }

    private static String loadSystemEnvironmentVariable(String variableName) {
        String value = System.getenv(variableName);
        if (value == null || value.isEmpty()) {
            throw new RuntimeException("Expected environment variable: " + variableName);
        }
        return value;
    }
}