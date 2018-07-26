package ${package}.gitb;

import com.gitb.core.*;
import com.gitb.ps.*;
import com.gitb.ps.Void;
import com.gitb.tr.TestResultType;
import ${package}.process.ProcessHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Spring component that realises the processing service.
 */
@Component
public class ProcessingServiceImpl implements ProcessingService {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ProcessingServiceImpl.class);

    /** The name of the uppercase operation. */
    public static final String OPERATION__UPPERCASE = "uppercase";
    /** The name of the lowercase operation. */
    public static final String OPERATION__LOWERCASE = "lowercase";
    /** The name of the text input that will be treated by each operation. */
    public static final String INPUT__INPUT = "input";
    /** The name of the output to return the processing result. */
    public static final String OUTPUT__OUTPUT = "output";

    @Value("${service.id}")
    private String serviceId = null;

    @Value("${service.version}")
    private String serviceVersion = null;

    @Autowired
    private SessionManager sessionManager = null;

    @Autowired
    private ProcessHandler processHandler = null;

    /**
     * The purpose of the getModuleDefinition call is to inform its caller on how the service is supposed to be called.
     *
     * In this case its main purpose is to define the service's supported operations and their respective input
     * and output parameters:
     * <ul>
     *     <li>Operation 'uppercase' with one input for the text to process and one output with the result.</li>
     *     <li>Operation 'lowercase' with one input for the text to process and one output with the result.</li>
     * </ul>
     * Note that defining output messages is optional as any and all output will be send back to the test bed regardless.
     * It is important however to define all expected inputs here as these are checked by the test bed before making the
     * actual call. You may define inputs as required in which case the test bed itself will check that they are provided
     * before actually making the call. Alternatively you can define inputs as optional and check them as part of the
     * processing operation.
     *
     * @param parameters No parameters are expected.
     * @return The response.
     */
    @Override
    public GetModuleDefinitionResponse getModuleDefinition(Void parameters) {
        GetModuleDefinitionResponse response = new GetModuleDefinitionResponse();
        response.setModule(new ProcessingModule());
        response.getModule().setId(serviceId);
        response.getModule().setMetadata(new Metadata());
        response.getModule().getMetadata().setName(response.getModule().getId());
        response.getModule().getMetadata().setVersion(serviceVersion);
        response.getModule().setConfigs(new ConfigurationParameters());
        TypedParameter inputText = Utils.createParameter(INPUT__INPUT, "string", UsageEnumeration.R, ConfigurationType.SIMPLE, "The input text to process");
        TypedParameter outputText = Utils.createParameter(OUTPUT__OUTPUT, "string", UsageEnumeration.R, ConfigurationType.SIMPLE, "The output result");
        response.getModule().getOperation().add(Utils.createProcessingOperation(OPERATION__UPPERCASE, Arrays.asList(inputText), Arrays.asList(outputText)));
        response.getModule().getOperation().add(Utils.createProcessingOperation(OPERATION__LOWERCASE, Arrays.asList(inputText), Arrays.asList(outputText)));
        return response;
    }

    /**
     * The purpose of the process operation is to execute one of the service's supported operations.
     *
     * What would typically take place here is as follows:
     * <ol>
     *    <li>Check that the requested operation is indeed supported by the service.</li>
     *    <li>For the requested operation collect and check the provided input parameters.</li>
     *    <li>Perform the requested operation and return the result to the test bed.</li>
     * </ol>
     * Note that a good practice is to decouple the actual processing from the GITB service implementation as
     * this is simply one API to access it through. In the current trivial example this is no really needed but
     * it is still done to illustrate this practice.
     *
     * @param processRequest The requested operation and input parameters.
     * @return The result.
     */
    @Override
    public ProcessResponse process(ProcessRequest processRequest) {
        String operation = processRequest.getOperation();
        if (operation == null) {
            throw new IllegalArgumentException("No processing operation provided");
        }
        List<AnyContent> input = Utils.getInputsForName(processRequest.getInput(), INPUT__INPUT);
        if (input.size() != 1) {
            throw new IllegalArgumentException("No processing operation provided");
        }
        String result;
        // The actual processing is decoupled and occurs via the processHandler component.
        switch (operation) {
            case OPERATION__UPPERCASE:
                result = processHandler.upperCase(input.get(0).getValue());
                break;
            case OPERATION__LOWERCASE:
                result = processHandler.lowerCase(input.get(0).getValue());
                break;
            default:
                throw new IllegalArgumentException(String.format("Unexpected operation [%s]. Expected [%s] or [%s].", operation, OPERATION__UPPERCASE, OPERATION__LOWERCASE));
        }
        ProcessResponse response = new ProcessResponse();
        response.setReport(Utils.createReport(TestResultType.SUCCESS));
        response.getOutput().add(Utils.createAnyContentSimple(OUTPUT__OUTPUT, result, ValueEmbeddingEnumeration.STRING));
        LOG.info("Completed operation [{}]. Input was [{}], output was [{}].", operation, input.get(0).getValue(), result);
        return response;
    }

    /**
     * The purpose of the beginTransaction operation is to begin a unique processing session.
     *
     * The key step that takes place in this operation is the creation and recording of a processing session. In the
     * current sample implementation this is not really needed as operations are one-off calls. It could be the case
     * however that we want a long running conversation with the processing service to leverage previous results.
     * As an example consider a processing service that extracts a ZIP archive's contents. As part of a single, long-running
     * session a first 'initialize' operation could provide the archive to process and then with subsequent 'extract'
     * operation calls its individual files could be returned. Using a long-running session to do this allows us to:
     * <ul>
     *     <li>Provide the file once and not as part of every call.</li>
     *     <li>Extract the file and cache contents as part of the session (through the sessionManager component).</li>
     *     <li>Clean up resources such as temporary files once the session completes.</li>
     * </ul>
     *
     * @param beginTransactionRequest Optional configuration parameters to consider when starting a processing transaction.
     * @return The response with the generated session ID for the processing transaction.
     */
    @Override
    public BeginTransactionResponse beginTransaction(BeginTransactionRequest beginTransactionRequest) {
        BeginTransactionResponse response = new BeginTransactionResponse();
        String sessionId = sessionManager.createSession();
        response.setSessionId(sessionId);
        LOG.info("Starting processing session [{}]", sessionId);
        return response;
    }

    /**
     * The purpose of the endTransaction operation is to complete an ongoing processing session.
     *
     * The main actions to be taken as part of this operation are to remove the provided session identifier (if this
     * was being recorded to begin with), and to perform any custom cleanup tasks.
     *
     * @param parameters The identifier of the session to terminate.
     * @return A void response.
     */
    @Override
    public Void endTransaction(BasicRequest parameters) {
        String sessionId = parameters.getSessionId();
        LOG.info("Ending processing session [{}]", sessionId);
        sessionManager.destroySession(sessionId);
        return new Void();
    }

}
