package com.meeba.google.Objects;

/**
 * Created by Eidan on 11/8/13.
 */
public class Event {
    private int eid;
    private int host_uid;
    private String where;
    private String when;
    private String created_at;

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

    public Event(int eid, int hostUid, String where, String when) {
        this.eid = eid;
        this.host_uid = hostUid;
        this.where = where;
        this.when = when;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eid=" + eid +
                ", host_uid=" + host_uid +
                ", where='" + where + '\'' +
                ", when='" + when + '\'' +
                ", created_at='" + created_at + '\'' +
                '}';
    }
}
