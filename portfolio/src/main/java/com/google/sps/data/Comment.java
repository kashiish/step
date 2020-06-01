package com.google.sps.data;

import java.lang.String;

public final class Comment {

    private final String name;
    private final String email;
    private final String message;

    public Comment(String name, String email, String message) {
        this.name = name;
        this.email = email;
        this.message = message;
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

}