package dk.bankdata.jaxws.gateway.cdi;

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

import dk.bankdata.jaxws.gateway.cache.JaxWsCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JaxWsGatewayProducer {
    @Inject
    JaxWsCache jaxWsCache;

    private static final Logger LOG = LoggerFactory.getLogger(JaxWsGatewayProducer.class);

    @Produces
    @JaxWsEndpoint()
    public <T> JaxWsGateway<T> create(InjectionPoint inject) {
        if (!(inject.getType() instanceof ParameterizedType)) {
            throw new IllegalArgumentException("Injection point must be of type JaxWsGateway<port>");
        }

        ParameterizedType parameterizedType = (ParameterizedType) inject.getType();
        T port = jaxWsCache.getPort(parameterizedType.getActualTypeArguments()[0].getTypeName());

        return new JaxWsGateway<>(port);
    }
}
