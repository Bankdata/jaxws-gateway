package domain;

/**
 * A wrapper for a port of a SOAP port. Unfortunately this is necessary
 * due to limitations in the CDI producers.
 *
 * @param <T> The port type
 */
public class JaxWsGateway<T> {

    private final T port;

    JaxWsGateway(T port) {
        this.port = port;
    }

    public T port() {
        return port;
    }
}