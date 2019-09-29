package dk.bankdata.jaxws.gateway.cdi;

import dk.bankdata.jaxws.gateway.cache.JaxWsCache;

import java.lang.reflect.ParameterizedType;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class JaxWsGatewayProducer {
    @Inject
    private JaxWsCache jaxWsCache;

    private static final Logger LOG = LoggerFactory.getLogger(JaxWsGatewayProducer.class);

    @Produces
    @JaxWsEndpoint
    public <T> JaxWsGateway<T> create(InjectionPoint inject) {
        if (!(inject.getType() instanceof ParameterizedType)) {
            throw new IllegalArgumentException("Injection point must be of type JaxWsGateway<port>");
        }

        ParameterizedType parameterizedType = (ParameterizedType) inject.getType();
        T port = jaxWsCache.getPort(parameterizedType.getActualTypeArguments()[0].getTypeName());

        return new JaxWsGateway<>(port);
    }
}
