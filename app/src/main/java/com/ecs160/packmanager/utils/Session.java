package com.ecs160.packmanager.utils;

import java.util.UUID;

/**
 * Session class. Stores details on the current session (id) and keeps a tab on the
 * user that is logged in. The Session object is destroyed once the user logs out and
 * is recreated when a user is logged in.
 */
public class Session {

    private String sessionId;
    private User loggedInUser; // The user that is logged into this session.

    public Session(User user) {
        sessionId = UUID.randomUUID().toString();
        loggedInUser = user;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(User user) {
        loggedInUser = user;
    }

}
