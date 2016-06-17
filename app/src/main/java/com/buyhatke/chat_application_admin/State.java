package com.buyhatke.chat_application_admin;

import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.net.URLEncoder;

/**
 * Created by devdoot on 16/5/16.
 */
public class State {
   private static DatabaseReference DatabaseReference=null;
    private static FirebaseStorage firebaseStorage =null;
    private  static FirebaseDatabase database=null;
    private static StorageReference storageReference=null;
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
        System.out.println("gs://intense-torch-2537.appspot.com"+ URLEncoder.encode(s));
        return  firebaseStorage.getReferenceFromUrl("gs://intense-torch-2537.appspot.com"+s);
    }

}
