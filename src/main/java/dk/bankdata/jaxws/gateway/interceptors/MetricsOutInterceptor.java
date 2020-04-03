package dk.bankdata.jaxws.gateway.interceptors;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class MetricsOutInterceptor  extends AbstractPhaseInterceptor<Message> {
    private Histogram.Child histogramChild;
    private Counter.Child counterChild;

    public MetricsOutInterceptor(Histogram prometheusHistogram, String operation, String service,
                                 Counter failureCounter) {
        super(Phase.SETUP);
        this.histogramChild = prometheusHistogram.labels(service, operation);
        this.counterChild = failureCounter.labels(service, operation);
    }

    public void handleMessage(Message message) {
        Histogram.Timer timer = histogramChild.startTimer();
        message.getExchange().put("requestPrometheusTimer", timer);
    }

    public void handleFault(Message message) {
        Histogram.Timer timer = (Histogram.Timer) message.getExchange().get("requestPrometheusTimer");

        if (timer != null) {
            timer.observeDuration();
        }

        counterChild.inc();
    }
}
