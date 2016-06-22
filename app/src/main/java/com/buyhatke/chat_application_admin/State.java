package com.buyhatke.chat_application_admin;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.net.URLEncoder;

/**
 * Created by devdoot on 16/5/16.
 */
public class State {
    private static com.google.firebase.database.DatabaseReference DatabaseReference=null;
    private static FirebaseStorage firebaseStorage =null;
    private  static FirebaseDatabase database=null;
    private static StorageReference storageReference=null;
    private static FirebaseAuth firebaseAuth=null;
    public static DatabaseReference getDatabaseReference(){

        return DatabaseReference;
    }
    public static void setDatabaseReference() {


        database = FirebaseDatabase.getInstance();
        database.getInstance().setPersistenceEnabled(true);
        DatabaseReference = database.getReferenceFromUrl("https://intense-torch-2537.firebaseio.com/");

    }
    public static void setFirebaseStorage(){
        firebaseStorage = firebaseStorage.getInstance();
        storageReference= firebaseStorage.getReferenceFromUrl("gs://intense-torch-2537.appspot.com").child("images");
    }
    public static StorageReference getStorageReference(){
        return storageReference;
    }
    public static StorageReference getReference(String s){
        if(firebaseStorage==null)
            setFirebaseStorage();
        return  firebaseStorage.getReferenceFromUrl("gs://intense-torch-2537.appspot.com"+s);
    }

    public static void setDatabaseReference(com.google.firebase.database.DatabaseReference databaseReference) {
        DatabaseReference = databaseReference;
    }

    public static FirebaseStorage getFirebaseStorage() {
        return firebaseStorage;
    }

    public static void setFirebaseStorage(FirebaseStorage firebaseStorage) {
        State.firebaseStorage = firebaseStorage;
    }

    public static FirebaseDatabase getDatabase() {
        return database;
    }

    public static void setDatabase(FirebaseDatabase database) {
        State.database = database;
    }

    public static void setStorageReference(StorageReference storageReference) {
        State.storageReference = storageReference;
    }

    public static FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public static void setFirebaseAuth(FirebaseAuth firebaseAuth) {
        State.firebaseAuth = firebaseAuth;
    }
}

