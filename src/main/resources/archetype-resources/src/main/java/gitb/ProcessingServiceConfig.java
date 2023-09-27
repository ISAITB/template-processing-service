package ${package}.gitb;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.namespace.QName;

/**
 * Configuration class responsible for creating the Spring beans required by the service.
 */
@Configuration
public class ProcessingServiceConfig {

    /**
     * The CXF endpoint that will serve service calls.
     *
     * @return The endpoint.
     */
    @Bean
    public EndpointImpl processingService(Bus cxfBus, ProcessingServiceImpl processingServiceImplementation) {
        EndpointImpl endpoint = new EndpointImpl(cxfBus, processingServiceImplementation);
        endpoint.setServiceName(new QName("http://www.gitb.com/ps/v1/", "ProcessingServiceService"));
        endpoint.setEndpointName(new QName("http://www.gitb.com/ps/v1/", "ProcessingServicePort"));
        endpoint.publish("/process");
        return endpoint;
    }

}
