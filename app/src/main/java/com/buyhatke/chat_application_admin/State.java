package com.buyhatke.chat_application_admin;

import com.firebase.client.Firebase;

/**
 * Created by devdoot on 16/5/16.
 */
public class State {
   private static Firebase firebase=null;
    private static boolean persistence =false;
    public static Firebase getfirebase(){

        return firebase;
    }
    public static void setFirebase(String url){

        firebase=new Firebase(url);
    }

    public static boolean isPersistence() {
        return persistence;
    }

    public static void setPersistence(boolean persistence) {
        State.persistence = persistence;
    }
}
