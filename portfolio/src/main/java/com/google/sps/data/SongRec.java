package com.google.sps.data;

import java.lang.String;

public final class SongRec {

    private final String name;
    private final long numLikes;
    private final long id;

    public SongRec(String name, long numLikes, long id) {
        this.name = name;
        this.numLikes = numLikes;
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public long getNumLikes() {
        return this.numLikes;
    }

    public long getId() {
        return this.id;
    }

}
