package dk.bankdata.jaxws.gateway.cache;

import dk.bankdata.jaxws.gateway.domain.Environment;
import dk.bankdata.jaxws.gateway.interceptors.MetricsInInterceptor;
import dk.bankdata.jaxws.gateway.interceptors.MetricsOutInterceptor;
import dk.bankdata.jaxws.gateway.interceptors.TracingInInterceptor;
import dk.bankdata.jaxws.gateway.interceptors.TracingOutInterceptor;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.spi.ProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@SuppressWarnings("VariableDeclarationUsageDistance")
public class JaxWsCache {
    @Inject
    Environment environment;

    private Map<String, Object> portMap = new HashMap<>();
    private static final Logger LOG = LoggerFactory.getLogger(JaxWsCache.class);

    private final Counter wsFailureCounter = Counter.build()
            .name("ws_failure_counter")
            .labelNames("service", "operation")
            .help("Number of failed web service requests")
            .register();

    private final Histogram wsRequestHistogram = Histogram.build()
            .name("ws_request_latency_seconds")
            .labelNames("service", "operation")
            .help("Metrics for all web service downstream requests.")
            .register();

    public JaxWsCache(){}

    public JaxWsCache port(Class<? extends Service> service, Class<?> portType) {
        try {
            checkCxfAvailability();

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

            Client cxfClient = ClientProxy.getClient(port);

            cxfClient.getInInterceptors().add(new TracingInInterceptor());
            cxfClient.getOutInterceptors().add(new TracingOutInterceptor());

            cxfClient.getInInterceptors().add(new MetricsInInterceptor());
            cxfClient.getOutInterceptors().add(new MetricsOutInterceptor(wsRequestHistogram, urlPath,
                    cacheService.getServiceName().getLocalPart(), wsFailureCounter));

            portMap.put(portType.getName(), port);

            long stop = System.currentTimeMillis();
            long duration = stop - start;
            LOG.info("Took " + duration + " ms to cache " + portType.getName());
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    private void checkCxfAvailability() {
        try {
            ProviderImpl.provider().getClass();
        } catch (java.lang.NoClassDefFoundError e) {
            throw new RuntimeException("org.apache.cxf provider not available. " +
                    "Please add the following to your gradle.build \r\n" +
                    "compile group: 'org.apache.cxf', name: 'cxf-rt-frontend-jaxws', version: '3.3.3' \r\n" +
                    "compile group: 'org.apache.cxf', name: 'cxf-rt-transports-http', version: '3.3.3' \r\n" +
                    "Version is the minimum tested - newer version may be available! \r\n");
        }
    }

    public <T> T getPort(String key) {
        return (T) portMap.get(key);
    }
}
