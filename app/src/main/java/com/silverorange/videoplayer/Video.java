package com.silverorange.videoplayer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public Date getDate() throws ParseException {
        String [] dateTime = date.split("T");
        String [] dividedDate = dateTime[0].split("-");
        String dateFormatted = "";

        for (int i = 0; i < dividedDate.length; i++)
            if (i != dividedDate.length - 1)
                dateFormatted = dateFormatted + dividedDate[i] + "/";
            else
                dateFormatted = dateFormatted + dividedDate[i];

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        Date strDate = sdf.parse(dateFormatted);
        return strDate;
    }

    public void setDate(String date) {
        this.date = date;
    }

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
