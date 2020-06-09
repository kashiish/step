package com.google.sps.data;

import java.lang.String;

public final class Comment {

    //Builder class
    public static final class CommentBuilder {

        private String name;
        private String message;
        private String email;
        private long timestamp;
        private long numLikes;
        private boolean isLiked;
        private String languageCode;
        private long id;

        public CommentBuilder() {
            //set default values
            this.name = "Anonymous";
            this.email = "anonymous";
            this.numLikes = 0;
            this.isLiked = false;
            this.languageCode = "en";
        }

        public CommentBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public CommentBuilder setMessage(String message) {
            this.message = message;
            return this;
        }

        public CommentBuilder setEmail(String email) {
            this.email = email;
            return this;
        }

        public CommentBuilder setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public CommentBuilder setNumLikes(long numLikes) {
            this.numLikes = numLikes;
            return this;
        }

        public CommentBuilder setIsLiked(boolean isLiked) {
            this.isLiked = isLiked;
            return this;
        }
        
        public CommentBuilder setLanguageCode(String languageCode) {
            this.languageCode = languageCode;
            return this;
        }

        public CommentBuilder setId(long id) {
            this.id = id;
            return this;
        }

        public Comment build() {
            //if the comment is missing any of these fields, we cannot create a new Comment
            if(this.message == null || this.timestamp == 0 || this.id == 0) {
                throw new NullPointerException();
            }

            return new Comment(this);

        }

    }

    private final String name;
    private final String message;
    private final String email;
    private final long timestamp;
    private final long numLikes;
    private final boolean isLiked;
    private final String languageCode;
    private final long id;

    private Comment(CommentBuilder builder) {
        this.name = builder.name;
        this.message = builder.message;
        this.email = builder.email;
        this.timestamp = builder.timestamp;
        this.numLikes = builder.numLikes;
        this.isLiked = builder.isLiked;
        this.languageCode = builder.languageCode;
        this.id = builder.id;
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

    public boolean isLiked() {
        return this.isLiked;
    }

    public String getLanguageCode() {
        return this.languageCode;
    }

    public long getId() {
        return this.id;
    }

}
