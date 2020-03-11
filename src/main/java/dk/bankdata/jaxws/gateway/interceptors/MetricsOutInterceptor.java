package dk.bankdata.jaxws.gateway.interceptors;

import io.prometheus.client.Histogram;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class MetricsOutInterceptor  extends AbstractPhaseInterceptor<Message> {
    private Histogram prometheusHistogram;
    private String bdwsUrl;

    public MetricsOutInterceptor(Histogram prometheusHistogram, String bdwsUrl) {
        super(Phase.SETUP);
        this.prometheusHistogram = prometheusHistogram;
        this.bdwsUrl = bdwsUrl;
    }

    public void handleMessage(Message message) {
        Histogram.Timer timer = prometheusHistogram.labels(bdwsUrl).startTimer();
        message.getExchange().put("requestPrometheusTimer", timer);
    }

    public void handleFault(Message message) {
        // This will be called in case normal execution was aborted, e.g. if connection fails
        // If we have not made it past the TracingInInterceptor, the span will still be open, so we close it here
        Histogram.Timer timer = (Histogram.Timer) message.getExchange().get("requestPrometheusTimer");
        if (timer != null) {
            timer.observeDuration();
        }
    }

}
