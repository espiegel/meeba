package com.meeba.google.objects;

import java.io.Serializable;

/**
 * Created by Eidan on 11/8/13.
 */
public class Event implements Serializable {
    private int eid;
    private String title;
    private String where;
    private String when;
    private String created_at;
    private User host;
    private String event_picture;

    public Event(int eid,String title, String where, String when, String created_at, User host) {
        this.eid = eid;
        this.title = title;
        this.where = where;
        this.when = when;
        this.created_at = created_at;
        this.host = host;
    }

    public User getHost() { return host; }
    public void setHost(User host) { this.host = host; }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public int getEid() {
        return eid;
    }

    public void setEid(int eid) {
        this.eid = eid;
    }

    public String getTitle(){return title;}

    public void setTitle(String title) {this.title = title;}

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public String getEvent_picture() {
        return event_picture;
    }

    public void setEvent_picture(String event_picture) {
        this.event_picture = event_picture;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eid=" + eid +
                ", title='" + title + '\'' +
                ", where='" + where + '\'' +
                ", when='" + when + '\'' +
                ", created_at='" + created_at + '\'' +
                ", host=" + host +
                ", event_picture='" + event_picture + '\'' +
                '}';
    }
}
