package environment;

import javax.enterprise.inject.Produces;

public class EnvironmentProducer {

    @Produces
    public Environment create() {
        String soapScheme = loadSystemEnvironmentVariable("SOAP_SCHEME");
        String soapHost = loadSystemEnvironmentVariable("SOAP_HOST");
        int soapPort = Integer.valueOf(loadSystemEnvironmentVariable("SOAP_PORT"));

        return new Environment(soapScheme, soapHost, soapPort);
    }

    private String loadSystemEnvironmentVariable(String variableName) {
        String value = System.getenv(variableName);

        if (value == null || value.isEmpty()) {
            throw new RuntimeException("Expected environment variable: " + variableName);
        }

        return value;
    }
}