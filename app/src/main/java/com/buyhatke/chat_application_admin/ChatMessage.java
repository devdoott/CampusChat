package com.buyhatke.chat_application_admin;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Created by devdoot on 30/5/16.
 */
public class ChatMessage implements Comparable<ChatMessage>  {
    private String message;
    private int type;
    private Bitmap image;
    private boolean isMe;
    private Long time;
    private int seen;
    private Uri imageUri=null;
    ChatMessage(String message, boolean isMe, long time, int seen){
        this.message=message;
        this.isMe=isMe;
        this.time=time;
        this.seen=seen;
        this.type=0;
        this.image=null;
        this.imageUri=null;
    }
    ChatMessage(Uri message, boolean isMe, long time, int seen){

        this.imageUri=message;
        this.isMe=isMe;
        this.time=time;
        this.seen=seen;
        this.type=1;
        this.message=null;
    }
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ChatMessage)) {
            return false;
        }
        ChatMessage c = (ChatMessage) o;
        return time.equals(c.getTime())&&isMe==c.isMe();
    }
    public String getMessage() {
        return message;
    }
    public boolean isMe() {
        return isMe;
    }

    @Override
    public int compareTo(ChatMessage another) {
        if(this.getTime()>another.getTime())
            return 1;
        else if(this.getTime()<another.getTime())
            return  -1;
        else
            return 0;
    }

    public Long getTime() {
        return time;
    }

    public int isSeen() {
        return seen;
    }

    public void setSeen(int seen) {
        this.seen = seen;
    }

    public int getType() {
        return type;
    }

    public Bitmap getImage() {
        return image;
    }

    public int getSeen() {
        return seen;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public void setMe(boolean me) {
        isMe = me;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }
}