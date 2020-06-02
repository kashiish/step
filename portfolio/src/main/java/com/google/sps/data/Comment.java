package com.google.sps.data;

import java.lang.String;

public final class Comment {

    private final String name;
    private final String email;
    private final String message;
    private final long timestamp;

    public Comment(String name, String email, String message, long timestamp) {
        this.name = name;
        this.email = email;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }

    public String getMessage() {
        return this.message;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

}
