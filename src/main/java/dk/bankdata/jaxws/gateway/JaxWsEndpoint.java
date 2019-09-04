package dk.bankdata.jaxws.gateway;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import javax.xml.ws.Service;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Qualifier
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface JaxWsEndpoint {
    @Nonbinding
    Class<? extends Service> service();
}
