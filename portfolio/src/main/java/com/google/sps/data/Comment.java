package com.google.sps.data;

import java.lang.String;

public final class Comment {

    private final String name;
    private final String email;
    private final String message;
    private final long timestamp;
    private final long numLikes;
    private final long id;

    public Comment(String name, String email, String message, long timestamp, long numLikes, long id) {
        this.name = name;
        this.email = email;
        this.message = message;
        this.timestamp = timestamp;
        this.numLikes = numLikes;
        this.id = id;
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

    public long getNumLikes() {
        return this.numLikes;
    }

    public long getId() {
        return this.id;
    }

}
