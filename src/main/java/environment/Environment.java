package environment;

import javax.inject.Named;

@Named
public class Environment {
    private String soapScheme;
    private String soapHost;
    private int soapPort;

    public Environment() {}

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
