package com.example.administrator.wangyi_music.info;

/**
 * Created by Administrator on 2017/6/20.
 */

public class PlayList {
    public final long id;
    public final String name;
    public final int songCount;
    public final String albumArt;
    public final String author;

    public PlayList() {
        this.id = -1;
        this.name = "";
        this.songCount = -1;
        this.albumArt = "";
        this.author = "";
    }


    public PlayList(long id, String name, int songCount, String albumArt, String author) {
        this.id = id;
        this.name = name;
        this.songCount = songCount;
        this.albumArt = albumArt;
        this.author = author;
    }
}
