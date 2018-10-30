package environment;

import javax.enterprise.inject.Any;

@Any
public class Environment {
    private String soapScheme;
    private String soapHost;
    private int soapPort;

    Environment() {}

    Environment(String soapScheme, String soapHost, int soapPort) {
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
