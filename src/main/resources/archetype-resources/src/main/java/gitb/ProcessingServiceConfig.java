package ${package}.gitb;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;

/**
 * Configuration class responsible for creating the Spring beans required by the service.
 */
@Configuration
public class ProcessingServiceConfig {

    @Autowired
    private Bus cxfBus = null;

    @Autowired
    private ProcessingServiceImpl processingServiceImplementation = null;

    /**
     * The CXF endpoint that will serve service calls.
     *
     * @return The endpoint.
     */
    @Bean
    public Endpoint processingService() {
        EndpointImpl endpoint = new EndpointImpl(cxfBus, processingServiceImplementation);
        endpoint.setServiceName(new QName("http://www.gitb.com/ps/v1/", "ProcessingServiceService"));
        endpoint.setEndpointName(new QName("http://www.gitb.com/ps/v1/", "ProcessingServicePort"));
        endpoint.publish("/process");
        return endpoint;
    }

}
