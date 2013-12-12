package com.meeba.google.objects;

import java.io.Serializable;

/**
 * Created by Eidan on 11/8/13.
 */
public class Event implements Serializable {
    private int eid;
    private String where;
    private String when;
    private String created_at;
    private User host;

    public Event(int eid, String where, String when, String created_at, User host) {
        this.eid = eid;
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

    @Override
    public String toString() {
        return "Event{" +
                "eid=" + eid +
                ", where='" + where + '\'' +
                ", when='" + when + '\'' +
                ", created_at='" + created_at + '\'' +
                ", host=" + host +
                '}';
    }
}
