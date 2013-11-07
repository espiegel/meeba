package com.meeba.google.Objects;

/**
 * Created by Padi on 07/11/13.
 */
public class User {
    private int uid;

    public User(String rid, int uid, String email, String name, String phone) {
        this.rid = rid;
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.phone = phone;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    private String email;
    private String name;
    private String phone;
    private String rid;
}
