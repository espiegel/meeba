package com.meeba.google.Objects;

/**
 * Created by Eidan on 11/8/13.
 */
public class Event {
    private int eid;
    private int hostUid;
    private String where;
    private String when;

    public int getEid() {
        return eid;
    }

    public void setEid(int eid) {
        this.eid = eid;
    }

    public int getHostUid() {
        return hostUid;
    }

    public void setHostUid(int hostUid) {
        this.hostUid = hostUid;
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
        this.hostUid = hostUid;
        this.where = where;
        this.when = when;
    }
}
