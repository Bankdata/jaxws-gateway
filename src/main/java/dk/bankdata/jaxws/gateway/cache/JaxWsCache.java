package dk.bankdata.jaxws.gateway.cache;

import dk.bankdata.jaxws.gateway.domain.Environment;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@SuppressWarnings("VariableDeclarationUsageDistance")
public class JaxWsCache {
    @Inject
    Environment environment;

    private Map<String, Object> portMap = new HashMap<>();
    private static final Logger LOG = LoggerFactory.getLogger(JaxWsCache.class);

    public JaxWsCache port(Class<? extends Service> service, Class<?> portType) {
        try {
            LOG.info("Provider class is " + javax.xml.ws.spi.Provider.provider().getClass());

            long start = System.currentTimeMillis();
            Service cacheService = service.newInstance();
            Object port = cacheService.getPort(portType);

            BindingProvider provider = (BindingProvider) port;
            Map<String, Object> requestContext = provider.getRequestContext();
            requestContext.put("thread.local.request.context", true);

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

            portMap.put(portType.getName(), port);

            long stop = System.currentTimeMillis();
            long duration = stop - start;
            LOG.info("Took " + duration + " ms to cache " + portType.getName());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public <T> T getPort(String key) {
        return (T) portMap.get(key);
    }
}
