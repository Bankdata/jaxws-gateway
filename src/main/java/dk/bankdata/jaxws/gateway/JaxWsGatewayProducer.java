package dk.bankdata.jaxws.gateway;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

public class JaxWsGatewayProducer {
    @Produces
    @JaxWsEndpoint(service = Service.class)
    public <T> JaxWsGateway<T> create(InjectionPoint inject) {
        JaxWsEndpoint jaxWsEndpoint = inject.getAnnotated().getAnnotation(JaxWsEndpoint.class);

        try {
            Service service = jaxWsEndpoint.service().newInstance();

            if (!(inject.getType() instanceof ParameterizedType)) {
                throw new IllegalArgumentException("Injection point must be of type JaxWsGateway<port>");
            }

            ParameterizedType parameterizedType = (ParameterizedType) inject.getType();
            T port = service.getPort((Class<T>) parameterizedType.getActualTypeArguments()[0]);

            BindingProvider provider = (BindingProvider) port;

            Map<String, Object> requestContext = provider.getRequestContext();
            String strUrl = (String)requestContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
            URI uri = URI.create(strUrl);

            String urlPath = uri.getPath();

            URL endpointUrl;

            try {
                endpointUrl = buildEndpointUrl(urlPath);
            } catch (MalformedURLException e) {
                throw new RuntimeException("Unable to create soap url.", e);
            }

            requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointUrl.toString());

            return new JaxWsGateway<>(port);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Unable to create port instance", e);
        }
    }

    private URL buildEndpointUrl(String urlPath) throws MalformedURLException {
        String soapScheme = loadSystemEnvironmentVariable("SOAP_SCHEME");
        String soapHost = loadSystemEnvironmentVariable("SOAP_HOST");
        int soapPort = Integer.parseInt(loadSystemEnvironmentVariable("SOAP_PORT"));

        return new URL(soapScheme, soapHost, soapPort, urlPath);
    }

    private String loadSystemEnvironmentVariable(String variableName) {
        String value = System.getenv(variableName);

        if (value == null || value.isEmpty()) {
            throw new RuntimeException("Expected environment variable: " + variableName);
        }

        return value;
    }

}
