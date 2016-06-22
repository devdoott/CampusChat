package com.buyhatke.chatfirebaseadminlibrary;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by devdoot on 6/6/16.
 */
public abstract class Messaging implements RecyclerViewChat{

    private ArrayList<ChatMessage>mMessages=new ArrayList<>();
    public static final int NOT_SENT=0;
    public static final int SENT=1;
    public static final int RECEIVED=2;
    public static final int SEEN=3;
    private Context mContext;
    private DatabaseReference mUserDatabase;
    private String mUid;
    private StorageReference mUserStorage;
    private StorageReference storageReference;
    private Integer unreadMessages;
    private DatabaseReference newMessagesForAdmin;
    Integer previousUnreadMessages=0;

    public Messaging(Context mContext, String mUid , DatabaseReference mUserDatabase, StorageReference mUserStorage , DatabaseReference newMessagesForAdmin)throws NullPointerException{
        if(mContext==null||mUid==null||mUserDatabase==null||mUserStorage==null){
            throw new NullPointerException("Null values for arguments not allowed");
        }else{
            this.mContext=mContext;
            this.mUserDatabase=mUserDatabase;
            this.mUserStorage=mUserStorage;
            this.mUid=mUid;
            this.unreadMessages=0;
            this.newMessagesForAdmin=newMessagesForAdmin;
        }

    }

    public void setUnreadMessages(){
        if(unreadMessages==0)return;
        ArrayMap<String,Object>am=new ArrayMap<>();
        am.put("unread",0);
        am.put("lastTime",System.currentTimeMillis());
        newMessagesForAdmin.updateChildren(am);
        unreadMessages=0;
    }

    public ArrayList<ChatMessage> startMessaging(final DatabaseReference firebasein, final DatabaseReference firebaseout){

        get_messages(firebasein,firebaseout);
        newMessagesForAdmin.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getKey().equals("unread")){
                    previousUnreadMessages=Integer.parseInt(dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getKey().equals("unread")){
                    previousUnreadMessages=Integer.parseInt(dataSnapshot.getValue().toString());
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return mMessages;
    }

    public int getNumberOfMessages(){

        return this.mMessages.size();
    }

    private class MessagingTaskin extends AsyncTask<Void,Void,Void>{
        private DatabaseReference firebasein;
        private DatabaseReference firebaseout;
        private DataSnapshot dataSnapshot;

        public MessagingTaskin(DatabaseReference firebasein, DatabaseReference firebaseout, DataSnapshot dataSnapshot) {
            this.firebasein = firebasein;
            this.firebaseout = firebaseout;
            this.dataSnapshot = dataSnapshot;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Iterator<DataSnapshot> i= dataSnapshot.getChildren().iterator();
            System.out.println("Getting incoming messages......................................");

            while(i.hasNext()){

                DataSnapshot ds=i.next();
                final String type=ds.child("type").getValue().toString();
                final String message=ds.child("message").getValue().toString();

                if(ds==null||ds.child("time")==null||ds.child("time").getValue()==null){
                    continue;
                }
                if(!ds.child("admin_time").exists()){
                    final long time=System.currentTimeMillis();
                    Map<String,Object> am=new ArrayMap<>();
                    am.put("admin_time",String.valueOf(time));
                    am.put("seen",String.valueOf(SEEN));
                    firebasein.child(ds.getKey()).updateChildren(am);
                    if(type.equals("text")) {
                        mMessages.add(new ChatMessage(message,false,time,NOT_SENT));
                    } else if(type.equals("image")){
                        Uri imageUri=null;String name=ds.child("name").getValue().toString();
                        final ChatMessage chatMessage=new ChatMessage(imageUri,false,time,NOT_SENT);
                        mMessages.add(chatMessage);
                        if((imageUri=ImageHandler.ImageExists(name,mContext))==null){
                           storageReference=mUserStorage.getRoot().child(message.replace('%','/'));
                            try {
                                final File image=ImageHandler.createImageFile(name,mContext);
                                storageReference.getFile(image).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        chatMessage.setImageUri(Uri.fromFile(image));
                                        updateView(mMessages.indexOf(mMessages.indexOf(chatMessage)));
                                    }

                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        mMessages.add(new ChatMessage("Cannot download image.",false,time,NOT_SENT));
                                        updateView();
                                        Toast.makeText(mContext,e.toString(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            chatMessage.setImageUri(imageUri);
                            updateView(mMessages.indexOf(chatMessage));
                        }
                    }

                } else{

                    Map<String,Object>am=new ArrayMap<>();
                    am.put("seen",String.valueOf(SEEN));
                    firebasein.child(ds.getKey()).updateChildren(am);
                    final Long time=Long.parseLong(ds.child("admin_time").getValue().toString());

                    if(type.equals("text"))
                        mMessages.add(new ChatMessage(message,false,time,NOT_SENT));
                    else if(type.equals("image")){
                        Uri imageUri=null;
                        String name=ds.child("name").getValue().toString();
                        final ChatMessage chatMessage=new ChatMessage(imageUri,false,time,NOT_SENT);
                        mMessages.add(chatMessage);
                        if((imageUri=ImageHandler.ImageExists(name,mContext))==null){
                           storageReference=mUserStorage.getRoot().child(message.replace('%','/'));
                            try {
                                final File image=ImageHandler.createImageFile(name,mContext);
                                storageReference.getFile(image).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                                        chatMessage.setImageUri(Uri.fromFile(image));
                                        updateView(mMessages.indexOf(chatMessage));
                                    }


                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        mMessages.add(new ChatMessage("Cannot download image.",false,time,NOT_SENT));
                                        updateView();

                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }else {
                            chatMessage.setImageUri(imageUri);
                        }
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            set_outgoing_messages(firebasein,firebaseout);
        }

    }
    private class MessagingTaskout extends AsyncTask<Void,Void,Void>{
        private DatabaseReference firebasein;
        private DatabaseReference firebaseout;
        private DataSnapshot dataSnapshot;

        public MessagingTaskout(DatabaseReference firebasein, DatabaseReference firebaseout, DataSnapshot dataSnapshot) {
            this.firebasein = firebasein;
            this.firebaseout = firebaseout;
            this.dataSnapshot = dataSnapshot;
        }

        @Override
        protected Void doInBackground(Void... params) { Iterator<DataSnapshot>i= dataSnapshot.getChildren().iterator();

            while(i.hasNext()){

                DataSnapshot ds=i.next();
                final Long time = Long.parseLong(ds.child("time").getValue().toString());
                final String message = ds.child("message").getValue().toString();
                final String type = ds.child("type").getValue().toString();


                if(ds.child("seen").exists()) {

                    final Integer status = Integer.parseInt(ds.child("seen").getValue().toString());
                    if(type.equals("text")) {
                        mMessages.add(new ChatMessage(message, true,time,status));
                    }else if(type.toString().equals("image")){

                        Uri imageUri=null;
                        String name=ds.child("name").getValue().toString();
                        final ChatMessage chatMessage=new ChatMessage(imageUri,true,time,status);

                        mMessages.add(chatMessage);
                        if((imageUri=ImageHandler.ImageExists(name,mContext))==null){
                            storageReference=mUserStorage.getRoot().child(message.replace('%','/'));
                            try {
                                final File image=ImageHandler.createImageFile(name,mContext);

                                storageReference.getFile(image).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        chatMessage.setImageUri(Uri.fromFile(image));
                                        updateView(mMessages.indexOf(chatMessage));
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        mMessages.add(new ChatMessage("Cannot download image.",true,time,status));
                                        updateView();
                                        Toast.makeText(mContext,e.toString(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            chatMessage.setImageUri(imageUri);
                        }
                    }
                }else{
                    System.out.println(dataSnapshot.toString());
                    firebaseout.child(ds.getKey()).child("seen").setValue(SENT);
                    if(type.equals("text"))
                        mMessages.add(new ChatMessage(message.replace('%','/'),true,time,NOT_SENT));
                    else if(type.equals("image")){

                        Uri imageUri=null;
                        String name=dataSnapshot.child("name").getValue().toString();
                        final ChatMessage chatMessage=new ChatMessage(imageUri,true,time,NOT_SENT);

                        mMessages.add(chatMessage);
                        if((imageUri=ImageHandler.ImageExists(name,mContext))==null){

                            storageReference=mUserStorage.getRoot().child(message.replace('%','/'));

                            try {
                                final File image=ImageHandler.createImageFile(name,mContext);
                                storageReference.getFile(image).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        chatMessage.setImageUri(Uri.fromFile(image));
                                        updateView(mMessages.indexOf(chatMessage));

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        mMessages.add(new ChatMessage("Cannot download image.",true,time,NOT_SENT));
                                        updateView();
                                        Toast.makeText(mContext,e.toString(),Toast.LENGTH_SHORT).show();

                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } else {
                            chatMessage.setImageUri(imageUri);
                        }
                    }
                }

            }
            Collections.sort(mMessages);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateView();
            get_new_messages(firebasein);
            get_seen(firebaseout);
        }
    }


    private   final void get_messages(final DatabaseReference firebasein, final DatabaseReference firebaseout){
        firebasein.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               new MessagingTaskin(firebasein,firebaseout,dataSnapshot).execute();
            }

            @Override
            public void onCancelled(DatabaseError DatabaseError) {

            }
        });
    }

    private   void set_outgoing_messages(final DatabaseReference firebasein, final DatabaseReference firebaseout){
        firebaseout.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                new MessagingTaskout(firebasein,firebaseout,dataSnapshot).execute();
            }

            @Override
            public void onCancelled(DatabaseError DatabaseError) {

            }
        });
    }

    private void get_seen(final DatabaseReference firebaseout){
        firebaseout.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                try {
                    if (dataSnapshot.child("seen").exists()) {
                        mMessages.get(mMessages.indexOf(new ChatMessage((String)null,true,Long.parseLong(dataSnapshot.child("time").getValue().toString()), NOT_SENT))
                        ).setSeen(Integer.parseInt(dataSnapshot.child("seen").getValue().toString()));

                        updateView();
                    }
                } catch (IndexOutOfBoundsException e) {
                    //Wait to syncronize .On sychronize onChildAdded will be called automatically
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError DatabaseError) {

            }
        });

    }

    private void get_new_messages(final DatabaseReference firebasein){
        firebasein.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot==null||dataSnapshot.child("time")==null||dataSnapshot.child("time").getValue()==null){
                    return;
                }
                if(!dataSnapshot.child("admin_time").exists()){

                    final long tt=System.currentTimeMillis();
                    final String new_message= dataSnapshot.child("message").getValue().toString();
                    final String type=dataSnapshot.child("type").getValue().toString();

                    if(type.equals("text")) {
                        mMessages.add(new ChatMessage(new_message,false,tt,NOT_SENT));

                        Map<String,Object>am=new ArrayMap<>();
                        am.put("admin_time",String.valueOf(tt));
                        firebasein.child(dataSnapshot.getKey()).updateChildren(am, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError DatabaseError, DatabaseReference firebase) {
                                firebasein.child(dataSnapshot.getKey()).child("seen").setValue(String.valueOf(SEEN));
                            }
                        });
                        updateView();
                    } else if(type.equals("image")){
                        String name=dataSnapshot.child("name").getValue().toString();
                        final Long time=tt;
                        Uri imageUri=null;
                        final ChatMessage chatMessage=new ChatMessage(imageUri,false,time,NOT_SENT);

                        if((imageUri=ImageHandler.ImageExists(name,mContext))==null){
                            StorageReference storageReference=mUserStorage.getRoot().child(new_message.replace('%','/'));

                            try {
                                final File image=ImageHandler.createImageFile(name,mContext);
                                storageReference.getFile(image).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {

                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                       chatMessage.setImageUri(Uri.fromFile(image));
                                        mMessages.add(chatMessage);
                                        Map<String,Object>am=new ArrayMap<>();
                                        am.put("admin_time",String.valueOf(tt));
                                        updateView(mMessages.indexOf(chatMessage));
                                        updateView(mMessages.indexOf(chatMessage));
                                        firebasein.child(dataSnapshot.getKey()).updateChildren(am, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError DatabaseError, DatabaseReference firebase) {
                                                firebasein.child(dataSnapshot.getKey()).child("seen").setValue(String.valueOf(SEEN));
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        mMessages.remove(chatMessage);
                                        mMessages.add(new ChatMessage("Cannot download image.",false,time,NOT_SENT));
                                        Toast.makeText(mContext,e.toString(),Toast.LENGTH_SHORT).show();
                                        updateView();
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            chatMessage.setImageUri(imageUri);
                            mMessages.add(chatMessage);
                            updateView(mMessages.indexOf(chatMessage));
                            Map<String,Object>am=new ArrayMap<>();
                            am.put("admin_time",String.valueOf(tt));
                            firebasein.child(dataSnapshot.getKey()).updateChildren(am, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError DatabaseError, DatabaseReference firebase) {
                                    firebasein.child(dataSnapshot.getKey()).child("seen").setValue(String.valueOf(SEEN));
                                }
                            });
                        }
                    }

                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError DatabaseError) {

            }
        });
    }

    public void writeToUser(String message){
        long tt=System.currentTimeMillis();
        ChatMessage chatMessage=new ChatMessage(message,true,tt,NOT_SENT);
        mMessages.add(chatMessage);
        final DatabaseReference fb= mUserDatabase.child(mUid).child("messages_from_admin").push();
        Map<String,String> am=new HashMap<>();
        am.put("message",message);
        am.put("time",String.valueOf(tt) );
        am.put("type","text");
        fb.setValue(am, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError DatabaseError, DatabaseReference firebase) {
                fb.child("seen").setValue(String.valueOf(SENT));
                unreadMessages++;
                setUnreadMessages();
            }
        });
        updateView(mMessages.indexOf(chatMessage));
    }

    public void writeToUser(final Uri imageUri){
        final long tt=System.currentTimeMillis();
        ChatMessage chatMessage=new ChatMessage(imageUri,true,tt,NOT_SENT);
        mMessages.add(chatMessage);
        updateView(mMessages.indexOf(chatMessage));
        final StorageReference userStorage = mUserStorage.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("messages_from_admin").child(imageUri.getLastPathSegment());
        userStorage.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(mContext,"Uploaded image.......",Toast.LENGTH_LONG).show();
                Map<String,Object> am=new HashMap<>();
                final DatabaseReference fb= mUserDatabase.child(mUid).child("messages_from_admin").push();
                am.put("message",userStorage.getPath().replace('/','%'));
                am.put("name",imageUri.getLastPathSegment());
                am.put("time",String.valueOf(tt) );
                am.put("type","image");
                updateView();
                fb.setValue(am, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError DatabaseError, DatabaseReference firebase) {
                        unreadMessages++;
                        fb.child("seen").setValue(String.valueOf(SENT));
                        setUnreadMessages();
                    }
                });
            }
        });
    }

    public ArrayList<ChatMessage> getMessages() {
        return mMessages;
    }
}