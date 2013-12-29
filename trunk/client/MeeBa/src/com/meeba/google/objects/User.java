package com.meeba.google.objects;

import java.io.Serializable;

/**
 * Created by Padi on 07/11/13.
 */
public class User implements Serializable {
    private int uid;
    private String email;
    private String name;
    private String phone_number;
    private String rid;
    private String created_at;
    private int invite_status;
    private boolean selected;
    private String picture_url;
    private int is_dummy;

    public User(int uid, String email, String name, String phone_number, String rid, String created_at, String picture_url,int is_dummy) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.phone_number = phone_number;
        this.rid = rid;
        this.created_at = created_at;
        this.picture_url = picture_url;
        this.selected = false;
        this.is_dummy=is_dummy;
    }

    public User(int uid, String email, String name, String phone_number, String rid, String created_at, String picture_url) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.phone_number = phone_number;
        this.rid = rid;
        this.created_at = created_at;
        this.picture_url = picture_url;
        this.selected = false;
    }

    public User(int uid, String email, String name, String phone_number, String rid, String created_at) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.phone_number = phone_number;
        this.rid = rid;
        this.created_at = created_at;
        this.selected = false;
    }

    public String getPicture_url() {
        return picture_url;
    }

    public void setPicture_url(String picture_url) {
        this.picture_url = picture_url;
    }

    public int getInvite_status() {
        return invite_status;
    }

    public void setInvite_status(int invite_status) {
        this.invite_status = invite_status;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }


    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (uid != user.uid) return false;
        if (!email.equals(user.email)) return false;
        if (!name.equals(user.name)) return false;
        if (!phone_number.equals(user.phone_number)) return false;
        if (!picture_url.equals(user.picture_url)) return false;
        if (!rid.equals(user.rid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uid;
        result = 31 * result + email.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + phone_number.hashCode();
        result = 31 * result + rid.hashCode();
        result = 31 * result + picture_url.hashCode();
        return result;
    }

    public int getIs_dummy() {
        return is_dummy;
    }

    public void setIs_dummy(int is_dummy) {
        this.is_dummy = is_dummy;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid=" + uid +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", rid='" + rid + '\'' +
                ", created_at='" + created_at + '\'' +
                ", invite_status=" + invite_status +
                ", selected=" + selected +
                ", picture_url='" + picture_url + '\'' +
                ", is_dummy=" + is_dummy +
                '}';
    }
}
