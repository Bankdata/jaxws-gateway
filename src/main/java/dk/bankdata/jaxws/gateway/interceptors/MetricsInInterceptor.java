package dk.bankdata.jaxws.gateway.interceptors;

import io.prometheus.client.Histogram;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class MetricsInInterceptor  extends AbstractPhaseInterceptor<Message> {
    public MetricsInInterceptor() {
        super(Phase.POST_INVOKE);
    }

    public void handleMessage(Message message) {
        Histogram.Timer timer = (Histogram.Timer) message.getExchange().get("requestPrometheusTimer");
        if (timer != null) {
            timer.observeDuration();
        }
    }

    public void handleFault(Message messageParam) {

    }
}
