package domain;

import environment.Environment;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

public class JaxWsGatewayProducer {
    @Inject
    Environment environment;

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

            String scheme = environment.getSoapScheme();
            String host = environment.getSoapHost();
            int portNo = environment.getSoapPort();

            URL endpointUrl;

            try {
                endpointUrl = new URL(scheme, host, portNo, urlPath);
            } catch (MalformedURLException e) {
                throw new RuntimeException("Unable to create url from " +
                        scheme + "|" +
                        host + "|" +
                        portNo + "|" +
                        urlPath + "|", e);
            }

            requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointUrl.toString());

            return new JaxWsGateway<>(port);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Unable to create port instance", e);
        }
    }
}
