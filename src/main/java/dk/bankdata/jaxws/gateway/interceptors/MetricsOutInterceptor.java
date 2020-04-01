package dk.bankdata.jaxws.gateway.interceptors;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class MetricsOutInterceptor  extends AbstractPhaseInterceptor<Message> {
    private Histogram prometheusHistogram;
    private Counter totalCounter;
    private Counter failureCounter;
    private String traceUrl;
    private String service;

    public MetricsOutInterceptor(Histogram prometheusHistogram, String traceUrl, String service,
                                 Counter failureCounter, Counter totalCounter) {
        super(Phase.SETUP);
        this.prometheusHistogram = prometheusHistogram;
        this.traceUrl = traceUrl;
        this.service = service;
        this.totalCounter = totalCounter;
        this.failureCounter = failureCounter;
    }

    public void handleMessage(Message message) {
        Histogram.Timer timer = prometheusHistogram.labels(service, traceUrl).startTimer();
        message.getExchange().put("requestPrometheusTimer", timer);

        totalCounter.inc();
    }


    public void handleFault(Message message) {
        // This will be called in case normal execution was aborted, e.g. if connection fails
        // If we have not made it past the TracingInInterceptor, the span will still be open, so we close it here
        Histogram.Timer timer = (Histogram.Timer) message.getExchange().get("requestPrometheusTimer");

        if (timer != null) {
            timer.observeDuration();
        }

        failureCounter.inc();
    }

}
