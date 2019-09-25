package dk.bankdata.jaxws.gateway.domain;

/**
 * Providing values configurable from environment.
 */
public class Environment {
    private final String soapScheme;
    private final String soapHost;
    private final int soapPort;

    public Environment(String soapScheme, String soapHost, int soapPort) {
        this.soapScheme = soapScheme;
        this.soapHost = soapHost;
        this.soapPort = soapPort;
    }

    public String getSoapScheme() {
        return soapScheme;
    }

    public String getSoapHost() {
        return soapHost;
    }

    public int getSoapPort() {
        return soapPort;
    }
}