package com.google.sps.data;

import java.lang.String;

public final class LoginStatus {

    private final boolean loggedIn;
    private final String url;

    public LoginStatus(boolean loggedIn, String url) {

        this.loggedIn = loggedIn;
        this.url = url;
    }

    public boolean isLoggedIn() {
        return this.loggedIn;
    }

    public String getUrl() {
        return this.url;
    }

}
