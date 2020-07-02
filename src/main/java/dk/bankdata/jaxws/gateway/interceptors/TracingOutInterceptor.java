package dk.bankdata.jaxws.gateway.interceptors;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class TracingOutInterceptor extends AbstractPhaseInterceptor<Message> {
    public TracingOutInterceptor() {
        super(Phase.SETUP);
    }


    public void handleMessage(Message message) {
        Tracer tracer = GlobalTracer.get();
        if (tracer != null) {
            String endpoint = (String) message.get(Message.ENDPOINT_ADDRESS);
            Span requestSpan = tracer
                    .buildSpan("WS Request")
                    .withTag("span.kind", "client")
                    .withTag("http.url", endpoint)
                    .start();
            try (Scope scope = tracer.scopeManager().activate(requestSpan, false)) {
                message.getExchange().put("requestTracingSpan", requestSpan);
            }

        }
    }

    public void handleFault(Message message) {
        // This will be called in case normal execution was aborted, e.g. if connection fails
        // If we have not made it past the TracingInInterceptor, the span will still be open, so we close it here
        Span requestSpan = (Span) message.getExchange().get("requestTracingSpan");

        if (requestSpan != null) {
            Tracer tracer = GlobalTracer.get();
            try (Scope scope = tracer.scopeManager().activate(requestSpan, false)) {
                requestSpan.setTag("error", true);
                requestSpan.finish();
                message.getExchange().remove("requestTracingSpan");
            }
        }
    }
}