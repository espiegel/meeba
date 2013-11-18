package com.meeba.google.objects;

/**
 * Created by Padi on 07/11/13.
 */
public class User {
    private int uid;
    private String email;
    private String name;
    private String phone_number;
    private String rid;
    private String created_at;

    public User(int uid, String email, String name, String phone_number, String rid, String created_at) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.phone_number = phone_number;
        this.rid = rid;
        this.created_at = created_at;
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

    @Override
    public String toString() {
        return "User{" +
                "uid=" + uid +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", rid='" + rid + '\'' +
                '}';
    }
}