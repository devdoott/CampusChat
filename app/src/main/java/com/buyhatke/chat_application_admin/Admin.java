package com.buyhatke.chat_application_admin;

import android.content.Context;
import android.content.Intent;

import com.google.firebase.storage.StorageReference;

public class Admin{
    private String fullName;
    private String id;
    private String packageName;

    public String getEmail() {
        return email;
    }

    private String email;
    public Admin() {}

    public String getId() {
        return id;
    }
    public Admin(String packageName, String fullName, String email,String id) {
        this.fullName = fullName;
        this.id=id;
        this.packageName=packageName;
        this.email=email;
    }
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        User c = (User) o;

        System.out.println("Equals.................."+this.id);

        System.out.println("Equals.................."+c.getId());
        return id.equals(c.getId());
    }
    public String getFullName() {
        return fullName;
    }

    public String getPackageName() {
        return packageName;
    }
}