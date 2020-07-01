package dk.bankdata.jaxws.gateway.interceptors;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class TracingInInterceptor extends AbstractPhaseInterceptor<Message> {
    public TracingInInterceptor() {
        super(Phase.POST_INVOKE);
    }

    public void handleMessage(Message message) {
        Span requestSpan = (Span) message.getExchange().get("requestTracingSpan");
        if (requestSpan != null) {
            Tracer tracer = GlobalTracer.get();
            try (Scope scope = tracer.scopeManager().activate(requestSpan)) {
                Integer responseStatus = (Integer) message.get(Message.RESPONSE_CODE);
                requestSpan.setTag("http.status_code", responseStatus);
                requestSpan.finish();
                message.getExchange().remove("requestTracingSpan");
            }
        }
    }

    public void handleFault(Message messageParam) {
        // This should only be called if we have made it successfully past this stage
        // in which case the span has already been closed.
    }
}