package environment;


import static org.truth0.Truth.ASSERT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EnvironmentTest {
    @Test
    public void shouldCreateEnvironment() {
        Environment environment = new Environment("some-scheme", "some-host", 12345);

        String soapScheme = environment.getSoapScheme();
        String soapHost = environment.getSoapHost();
        int soapPort = environment.getSoapPort();

        ASSERT.that(soapScheme).isEqualTo("some-scheme");
        ASSERT.that(soapHost).isEqualTo("some-host");
        ASSERT.that(soapPort).isEqualTo(12345);
    }
}