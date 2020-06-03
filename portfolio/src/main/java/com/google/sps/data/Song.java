package com.google.sps.data;

import java.lang.String;

public final class Song {

    private final String song;
    private final long numLikes;
    private final long id;

    public Song(String song, long numLikes, long id) {
        this.name = name;
        this.numLikes = numLikes;
        this.id = id;
    }

    public String getSong() {
        return this.song;
    }

    public long getNumLikes() {
        return this.numLikes;
    }

    public long getId() {
        return this.id;
    }

}
