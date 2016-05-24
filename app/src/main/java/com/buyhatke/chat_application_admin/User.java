package com.buyhatke.chat_application_admin;
public class User {
    private String email;
    private String fullName;
    private String id;
    public User() {}

    public String getId() {
        return id;
    }

    public User(String fullName, String email, String id) {
        this.fullName = fullName;
        this.email = email;
        this.id=id;
    }
    public String getEmail() {
        return email;
    }
    public String getFullName() {
        return fullName;
    }
}