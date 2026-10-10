package com.xingchen.android.tv.bean;

/** 字幕匹配用轻量 TMDB 条目（可后续接完整 TMDB 模块） */
public class TmdbItem {
    private int id;
    private String title;
    private String originalTitle;
    private String originalLanguage;
    private String mediaType;
    private String releaseDate;
    private String firstAirDate;
    private java.util.List<String> originCountry;

    public int getId() { return id; }
    public TmdbItem setId(int id) { this.id = id; return this; }
    public String getTitle() { return title == null ? "" : title; }
    public TmdbItem setTitle(String title) { this.title = title; return this; }
    public String getOriginalTitle() { return originalTitle == null ? "" : originalTitle; }
    public TmdbItem setOriginalTitle(String originalTitle) { this.originalTitle = originalTitle; return this; }
    public String getOriginalLanguage() { return originalLanguage == null ? "" : originalLanguage; }
    public TmdbItem setOriginalLanguage(String originalLanguage) { this.originalLanguage = originalLanguage; return this; }
    public String getMediaType() { return mediaType == null ? "" : mediaType; }
    public TmdbItem setMediaType(String mediaType) { this.mediaType = mediaType; return this; }
    public String getReleaseDate() { return releaseDate == null ? "" : releaseDate; }
    public String getFirstAirDate() { return firstAirDate == null ? "" : firstAirDate; }
    public java.util.List<String> getOriginCountry() { return originCountry == null ? java.util.Collections.emptyList() : originCountry; }
}
