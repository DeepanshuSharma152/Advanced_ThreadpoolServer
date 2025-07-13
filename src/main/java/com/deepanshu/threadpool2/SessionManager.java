package com.deepanshu.threadpool2;

import java.util.HashMap;
import java.util.Map;

/**
 * SessionManager handles simple in‑memory session management.
 *
 * ⚠️ This is a lightweight session system:
 *   • Suitable for small‑scale applications or prototyping.
 *   • Not persistent — all sessions are lost when the app restarts.
 *   • Not thread‑safe — consider synchronizing access in a multi‑threaded environment.
 *
 * Why Map<String, String>?
 *   • Key: session ID (usually a unique token like UUID).
 *   • Value: associated username (or user ID).
 */

public class SessionManager {


    // Stores session ID → username mappings
    private static final Map<String,String> sessions=new HashMap<>();
    /**
     * Creates a session by storing session ID with associated username.
     * @param sessionId unique session identifier (e.g. UUID)
     * @param username  username to bind to the session
     */
    public static void createSession(String sessionId,String username){
        sessions.put(sessionId,username);
    }

    /**
     * Retrieves username tied to a session ID.
     * @param sessionId session identifier
     * @return username if session exists, else null
     */
    public static String getUsername(String sessionId){
        return sessions.get(sessionId);
    }

    /**
     * Removes the session from the active session store.
     * @param sessionId session identifier to be invalidated
     */
    public static void destroySession(String sessionId){
        sessions.remove(sessionId);
    }
}
