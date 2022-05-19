package com.silverorange.videoplayer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.time.*;

/**
 * This class holds all the data necessary to represent a video
 */
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

    /**
     * @return the date for the video
     * @throws ParseException
     */
    public Date getDate() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date strDate = sdf.parse(date);
        return strDate;
    }

    public void setDate(String date) {
        this.date = date;
    }

    /**
     * @param otherDate
     * @return if the given video is made after the other video
     * @throws ParseException
     */
    public boolean isDateAfter (Date otherDate) throws ParseException {
        return getDate().after(otherDate);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
