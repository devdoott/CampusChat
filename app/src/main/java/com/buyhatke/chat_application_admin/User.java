package com.buyhatke.chat_application_admin;

import android.content.Context;
import android.content.Intent;

import com.google.firebase.storage.StorageReference;

public class User implements Comparable<User>{
    private String fullName;
    private String id;
    private String packageName;
    private Integer unreadMessages;
    private Long lastTime;
    public User() {}

    public String getId() {
        return id;
    }
    public User(String packageName, String fullName, String id, Integer unreadMessages,Long lastTime) {
        this.fullName = fullName;
        this.id=id;
        this.packageName=packageName;
        this.unreadMessages=unreadMessages;
        this.lastTime=lastTime;
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
    @Override
    public int compareTo(User another) {
        System.out.println("compareToooooooo "+this.toString() );
        if(id.equals(another.getId()))return 0;
        if((this.unreadMessages==0&&another.getUnreadMessages()==0)||(this.unreadMessages!=0&&another.getUnreadMessages()!=0)){
            if(this.lastTime<another.getLastTime())
                return  1;
            else if(this.lastTime>another.lastTime) {
                return  -1;
            }
            else return 0;
        }
        else {
            if(this.unreadMessages!=0)
                return -1;
            else if (another.getUnreadMessages()!=0){
                return 1;
            }
            else {
                return 0;
            }


        }
    }
    public String getFullName() {
        return fullName;
    }

    public String getPackageName() {
        return packageName;
    }

    public Integer getUnreadMessages() {
        return unreadMessages;
    }

    public Long getLastTime() {
        return lastTime;
    }
}