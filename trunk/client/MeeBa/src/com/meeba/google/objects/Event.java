package com.meeba.google.objects;

import java.io.Serializable;

/**
 * Created by Eidan on 11/8/13.
 */
public class Event implements Serializable {
    private int eid;
    private int host_uid;
    private String host_name;
    private String host_picture_url;
    private String where;
    private String when;
    private String created_at;

    public Event(int eid, int hostUid, String where, String when, String host_name) {
        this.eid = eid;
        this.host_uid = hostUid;
        this.where = where;
        this.when = when;
        this.host_name = host_name;
    }

    public String getHost_picture_url() {
        return host_picture_url;
    }

    public void setHost_picture_url(String host_picture_url) {
        this.host_picture_url = host_picture_url;
    }

    public String getHost_name() {
        return host_name;
    }

    public void setHost_name(String host_name) {
        this.host_name = host_name;
    }

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

    public int getHost_uid() {
        return host_uid;
    }

    public void setHost_uid(int host_uid) {
        this.host_uid = host_uid;
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
                ", host_uid=" + host_uid +
                ", host_name='" + host_name + '\'' +
                ", host_picture_url='" + host_picture_url + '\'' +
                ", where='" + where + '\'' +
                ", when='" + when + '\'' +
                ", created_at='" + created_at + '\'' +
                '}';
    }
}
