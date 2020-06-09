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
        private long id;

        public CommentBuilder() {
            //set default values
            this.name = "Anonymous";
            this.email = "anonymous";
            this.numLikes = 0;
            this.isLiked = false;
        }

        public CommentBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CommentBuilder message(String message) {
            this.message = message;
            return this;
        }

        public CommentBuilder email(String email) {
            this.email = email;
            return this;
        }

        public CommentBuilder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public CommentBuilder numLikes(long numLikes) {
            this.numLikes = numLikes;
            return this;
        }

        public CommentBuilder isLiked(boolean isLiked) {
            this.isLiked = isLiked;
            return this;
        }

        public CommentBuilder id(long id) {
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
    private final long id;

    private Comment(CommentBuilder builder) {
        this.name = builder.name;
        this.message = builder.message;
        this.email = builder.email;
        this.timestamp = builder.timestamp;
        this.numLikes = builder.numLikes;
        this.isLiked = builder.isLiked;
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

    public long getId() {
        return this.id;
    }

}
