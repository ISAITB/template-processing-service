package ${package}.gitb;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Component used to store sessions and their state.
 *
 * This class is used to record a processing session to ensure that an overall context is maintained
 * across its different operation calls. Note that tracking sessions (i.e. use of this class) is of course
 * not required if operations are one-off independent calls.
 *
 * This implementation stores session information in memory. An alternative solution
 * that would be fault-tolerant could store session data in a DB.
 */
@Component
public class SessionManager {

    /** The map of in-memory active sessions. */
    private Map<String, Map<String, Object>> sessions = new ConcurrentHashMap<>();

    /**
     * Create a new session.
     *
     * @return The session ID that was generated.
     */
    public String createSession() {
        String sessionId = UUID.randomUUID().toString();
        Map<String, Object> sessionInfo = new HashMap<>();
        sessions.put(sessionId, sessionInfo);
        return sessionId;
    }

    /**
     * Remove the provided session from the list of tracked sessions.
     *
     * @param sessionId The session ID to remove.
     */
    public void destroySession(String sessionId) {
        sessions.remove(sessionId);
    }

    /**
     * Get a given item of information linked to a specific session.
     *
     * @param sessionId The session ID we want to lookup.
     * @param infoKey The key of the value that we want to retrieve.
     * @return The retrieved value.
     */
    public Object getSessionInfo(String sessionId, String infoKey) {
        Object value = null;
        if (sessions.containsKey(sessionId)) {
            value = sessions.get(sessionId).get(infoKey);
        }
        return value;
    }

    /**
     * Set the given information item for a session.
     *
     * @param sessionId The session ID to set the information for.
     * @param infoKey The information key.
     * @param infoValue The information value.
     */
    public void setSessionInfo(String sessionId, String infoKey, Object infoValue) {
        sessions.get(sessionId).put(infoKey, infoValue);
    }

    /**
     * Get all the active sessions.
     *
     * @return An unmodifiable map of the sessions.
     */
    public Map<String, Map<String, Object>> getAllSessions() {
        return Collections.unmodifiableMap(sessions);
    }

}
