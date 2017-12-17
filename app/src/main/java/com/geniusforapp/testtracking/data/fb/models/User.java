package com.geniusforapp.testtracking.data.fb.models;

/**
 * @name TestTracking
 * Copyrights (c) 11/9/17 Created By Ahmad Najar
 **/

public class User {
    private String email;
    private String displayName;
    private String phone;


    public User() {
    }

    public User(String email, String displayName, String phone) {
        this.email = email;
        this.displayName = displayName;
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
