package com.silverorange.videoplayer;

public class Video {
    private String url;
    private String title;
    private String name;
    private String date;
    private String description;

    public Video (String url, String title, String name, String date, String description) {
        this.url = url;
        this.title = title;
        this.name = name;
        this.date = date;
        this.description = description;
    }
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
