package com.xingchen.android.tv.bean;

/** 字幕匹配用轻量 TMDB 分集 */
public class TmdbEpisode {
    private int id;
    private int seasonNumber;
    private int episodeNumber;
    private String name;

    public int getId() { return id; }
    public int getSeasonNumber() { return seasonNumber; }
    public int getEpisodeNumber() { return episodeNumber; }
    public int getNumber() { return episodeNumber; }
    public String getName() { return name == null ? "" : name; }
}
