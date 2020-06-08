package com.google.sps.data;

import java.lang.String;

public final class Comment {

    private final String name;
    private final String message;
    private final String email;
    private final long timestamp;
    private final long numLikes;
    private final long id;

    public Comment(String name, String message, String email, long timestamp, long numLikes, long id) {
        this.name = name;
        this.message = message;
        this.email = email;
        this.timestamp = timestamp;
        this.numLikes = numLikes;
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public String getMessage() {
        return this.message;
    }

    public String getEmail() {
        return this.email;
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
