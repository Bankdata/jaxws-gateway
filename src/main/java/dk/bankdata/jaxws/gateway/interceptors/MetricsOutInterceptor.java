package dk.bankdata.jaxws.gateway.interceptors;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class MetricsOutInterceptor  extends AbstractPhaseInterceptor<Message> {
    private Histogram prometheusHistogram;
    private Counter failureCounter;
    private Counter.Child counterChild;
    private final String traceUrl;
    private final String service;

    public MetricsOutInterceptor(Histogram prometheusHistogram, String traceUrl, String service,
                                 Counter failureCounter) {
        super(Phase.SETUP);
        this.prometheusHistogram = prometheusHistogram;
        this.traceUrl = traceUrl;
        this.service = service;
        this.failureCounter = failureCounter;
        this.counterChild = failureCounter.labels(service, traceUrl);
    }

    public void handleMessage(Message message) {
        Histogram.Timer timer = prometheusHistogram.labels(service, traceUrl).startTimer();
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
