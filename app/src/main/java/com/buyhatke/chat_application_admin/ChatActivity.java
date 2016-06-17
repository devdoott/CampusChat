package com.buyhatke.chat_application_admin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
public class ChatActivity extends AppCompatActivity {
    private ImageButton mSend;
    private EditText mTypemessage;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private  ArrayList<ChatMessage>mMessages=new ArrayList<>();
    private TextView mTextView;
    private String muserid;
    private DatabaseReference user=null;
    private final int NOT_SENT=0;
    private final int SENT=1;
    private final int RECEIVED=2;
    private final int SEEN=3;
    private ImageButton mPic;
    private LinearLayout mInput;
    static final int REQUEST_IMAGE_CAPTURE = 10;
    static final int REQUEST_IMAGE_PICKING = 11;
    private Uri mCurrentFilePath=null;
    private long lastTime;
    private DatabaseReference newMessagesForAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastTime=0;
        setContentView(R.layout.chatlayout);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(false);
        State.setFirebaseStorage();
        mTextView=(TextView)findViewById(R.id.customersupport);
        Intent intent=getIntent();
        mTextView.setText(intent.getStringExtra("name"));
        muserid=intent.getStringExtra("id");
        user=State.getDatabaseReference().child("com%buyhatke%chat_application");
        newMessagesForAdmin=State.getDatabaseReference().child("newMessagesForAdmin").child("com%buyhatke%chat_application").child(muserid);
       // mRecyclerView.setTransc
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        final   DatabaseReference databaseReference=user;
        DatabaseReference incoming_messages=databaseReference.child(muserid).child("messages_to_admin");
        incoming_messages.keepSynced(true);
        DatabaseReference outgoing_messages=databaseReference.child(muserid).child("messages_from_admin");
        outgoing_messages.keepSynced(true);
        get_messages(incoming_messages,outgoing_messages);
        mTypemessage=(EditText)findViewById(R.id.typemessage);
        mInput=(LinearLayout)findViewById(R.id.input);
        mInput.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(oldTop!=top)
                    mRecyclerView.scrollToPosition(mMessages.size()-1);
            }
        });
        mSend=(ImageButton)findViewById(R.id.send);
        mPic=(ImageButton) findViewById(R.id.pic);
        mPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder ad=new AlertDialog.Builder(ChatActivity.this);
                LayoutInflater inflater = (LayoutInflater)ChatActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                ad.setView(inflater.inflate(R.layout.dialog, null));
                final   AlertDialog  al= ad.create();
                al.show();
                Button mTP=(Button)al.findViewById(R.id.takepic);
                Button mCG=(Button)al.findViewById(R.id.choose);
                mTP.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        al.cancel();
                        dispatchTakePictureIntent();

                    }
                });
                mCG.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    al.cancel();
                    dispatchGallery();
                    }
                });


            }
        });
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkString(mTypemessage.getText().toString())==false)return ;

/*                View view = ChatActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }*/
            writeToUser(mTypemessage.getText().toString().trim());
                mTypemessage.setText(null);
            }
        });
    }
    private boolean checkString(String s){
        if(s==null||s.trim().equals("")){
            return false;
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        newMessagesForAdmin.child("unread").setValue(0);
    }


    @Override
    protected void onStart() {
        super.onStart();
        newMessagesForAdmin.child("unread").setValue(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        View view = ChatActivity.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private ArrayList<ChatMessage>messages;
        private Activity context;
        public  class ViewHolder extends RecyclerView.ViewHolder {
            public TextView txtMessage;
            public TextView txtInfo;
            private ImageView mImageview;
            public LinearLayout content;
            public LinearLayout contentWithBG;
            private ImageView mImagemessage;
            public ViewHolder(View v) {
                super(v);
                this.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
                this.content = (LinearLayout) v.findViewById(R.id.content);
                this.contentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
                this.txtInfo = (TextView) v.findViewById(R.id.txtInfo);
                this.mImageview=(ImageView)v.findViewById(R.id.image);
                this.mImagemessage=(ImageView)v.findViewById(R.id.imageMessage);
            }
        }
        private void setAlignment(MyAdapter.ViewHolder holder, boolean isMe) {
            if (isMe) {

                holder.txtMessage.setTextColor(Color.parseColor("#000000"));
                holder.contentWithBG.setBackgroundResource(R.drawable.rounded_rectangle_out);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
                layoutParams.gravity = Gravity.RIGHT;
                holder.contentWithBG.setLayoutParams(layoutParams);
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
                lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                holder.content.setLayoutParams(lp);
                layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
                layoutParams.gravity = Gravity.RIGHT;
                holder.txtMessage.setLayoutParams(layoutParams);
                layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
                layoutParams.gravity = Gravity.RIGHT;
                holder.txtInfo.setLayoutParams(layoutParams);
            } else {
                holder.txtMessage.setTextColor(Color.parseColor("#ffffff"));
                holder.contentWithBG.setBackgroundResource(R.drawable.rounded_rectangle);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
                layoutParams.gravity = Gravity.LEFT;
                holder.contentWithBG.setLayoutParams(layoutParams);
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
                lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                holder.content.setLayoutParams(lp);
                layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
                layoutParams.gravity = Gravity.LEFT;
                holder.txtMessage.setLayoutParams(layoutParams);
                layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
                layoutParams.gravity = Gravity.LEFT;
                holder.txtInfo.setLayoutParams(layoutParams);
            }
        }

        public MyAdapter(Activity context, ArrayList<ChatMessage>messages) {
            this.context = context;
            this.messages=messages;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.textviewlayoutchat, null);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final ChatMessage cm=messages.get(position);
            setAlignment(holder, cm.isMe());
            if(cm.getType()==1){
                Picasso.with(context).load(cm.getImageUri()).placeholder(R.drawable.loading).into(holder.mImagemessage);
                holder.txtMessage.setText(null);
                if(holder.itemView!=null){
                    holder.mImagemessage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent=new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(cm.getImageUri(), "image/*");
                            startActivity(intent);
                        }
                    });
                }
            }else if (cm.getType()==0){
                holder.txtMessage.setText(cm.getMessage());
            }
            holder.txtInfo.setText(getServerTime(cm.getTime()));
            int seen=cm.isSeen();
            if(seen==SEEN){
                final Drawable dtick = getResources().getDrawable(R.mipmap.seen);
                dtick.setColorFilter(Color.parseColor("#4caf50"),PorterDuff.Mode.SRC_ATOP);
                holder.mImageview.setImageDrawable(dtick);
                holder.mImageview.setVisibility(View.VISIBLE);
            }else if(seen==RECEIVED){
                final Drawable dtick = getResources().getDrawable(R.mipmap.seen);
                dtick.setColorFilter(Color.parseColor("#85a3a9"),PorterDuff.Mode.SRC_ATOP);
                holder.mImageview.setImageDrawable(dtick);
                holder.mImageview.setVisibility(View.VISIBLE);
            }else if(seen==SENT){
                final Drawable dtick = getDrawable(R.mipmap.sent);
                dtick.setColorFilter(Color.parseColor("#85a3a9"),PorterDuff.Mode.SRC_ATOP);
                holder.mImageview.setImageDrawable(dtick);
                holder.mImageview.setVisibility(View.VISIBLE);
            }else if(seen==NOT_SENT){
                holder.mImageview.setImageDrawable(null);

            }
        }
        @Override
        public int getItemCount() {
            if (messages != null) {
                return messages.size();
            } else {
                return 0;
            }
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            super.onViewRecycled(holder);
            holder.mImagemessage.setOnClickListener(null);
        }
    }
        private  final void get_messages(final DatabaseReference firebasein,final DatabaseReference firebaseout){
            firebasein.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterator<DataSnapshot>i= dataSnapshot.getChildren().iterator();
                    while(i.hasNext()){

                        DataSnapshot ds=i.next();
                        if(ds==null||ds.child("time")==null||ds.child("time").getValue()==null){
                            continue;
                        }
                        long ll=Long.parseLong(ds.child("time").getValue().toString());
                        if(!ds.child("admin_time").exists()){
                            long tt=System.currentTimeMillis();
                            Map<String,Object>am=new ArrayMap<>();
                            am.put("admin_time",String.valueOf(tt));
                            am.put("seen",String.valueOf(3));
                            firebasein.child(ds.getKey()).updateChildren(am);
                            if(ds.child("type").getValue().toString().equals("text")) {
                                mMessages.add(new ChatMessage(ds.child("message").getValue().toString(), false, tt, NOT_SENT));
                            }else if(ds.child("type").getValue().toString().equals("image")){
                                final Long time=tt;
                                Uri imageUri=null;String name=ds.child("name").getValue().toString();
                                final ChatMessage chatMessage=new ChatMessage(imageUri,false,time,NOT_SENT);
                                mMessages.add(chatMessage);
                                if((imageUri=ImageExists(name))==null){
                                    StorageReference storageReference=State.getReference(ds.child("message").getValue().toString().replace('%','/'));
                                try {
                                    final File image=createImageFile(name);
                                    storageReference.getFile(image).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            chatMessage.setImageUri(Uri.fromFile(image));
                                            updateView();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //mMessages.add(new ChatMessage("Cannot download image.",false,time,NOT_SENT));
                                        }
                                    });
                                } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                           // Picasso.with(ChatActivity.this).load(ds.child("message").getValue().toString()).into( getTarget(ds.child("name").getValue().toString(),tt,mMessages,false,NOT_SENT));
                        else {
                                chatMessage.setImageUri(imageUri);
                                updateView();
                            //mMessages.add(new ChatMessage(imageUri,false,tt,NOT_SENT));
                        }
                        }

                    }
                    else{
                        Map<String,Object>am=new ArrayMap<>();
                        //am.put("admin_time",String.valueOf(tt));
                        am.put("seen",String.valueOf(3));
                        firebasein.child(ds.getKey()).updateChildren(am);
                        if(ds.child("type").getValue().toString().equals("text"))

                            mMessages.add(new ChatMessage(ds.child("message").getValue().toString(),false,Long.parseLong(ds.child("admin_time").getValue().toString()),NOT_SENT));
                        else if(ds.child("type").getValue().toString().equals("image")){
                            final Long time=Long.parseLong(ds.child("admin_time").getValue().toString());
                            Uri imageUri=null;String name=ds.child("name").getValue().toString();
                            final ChatMessage chatMessage=new ChatMessage(imageUri,false,time,NOT_SENT);
                            mMessages.add(chatMessage);
                            if((imageUri=ImageExists(name))==null){
                                //Picasso.with(ChatActivity.this).load().into( getTarget(ds.child("name").getValue().toString(),Long.parseLong(ds.child("time").getValue().toString()),mMessages,true,NOT_SENT));

                                    StorageReference storageReference = State.getReference(ds.child("message").getValue().toString().replace('%', '/'));


                                try {
                                    final File image=createImageFile(name);
                                    storageReference.getFile(image).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                           chatMessage.setImageUri(Uri.fromFile(image));
                                            updateView();
                                            // mMessages.add(new ChatMessage(Uri.fromFile(image),false,time,NOT_SENT));
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                           // mMessages.add(new ChatMessage("Cannot download image.",false,time,NOT_SENT));
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                               // Picasso.with(ChatActivity.this).load(ds.child("message").getValue(Uri.class)).into( getTarget(ds.child("name").getValue().toString(),Long.parseLong(ds.child("admin_time").getValue().toString()),mMessages,false,NOT_SENT));
                            else {
                                chatMessage.setImageUri(imageUri);
                                updateView();
                                //mMessages.add(new ChatMessage(imageUri,false,Long.parseLong(ds.child("admin_time").getValue().toString()),NOT_SENT));
                            }
                        }
                    }
                }
                set_outgoing_messages(firebasein,firebaseout);
            }

            @Override
            public void onCancelled(DatabaseError DatabaseError) {

            }
        });
    }

    private void updateView(int position){
        mAdapter.notifyItemChanged(position);
        mLayoutManager.scrollToPosition(mMessages.size()-1);
    }
    private void updateView(){
        mLayoutManager.scrollToPosition(mMessages.size()-1);
        mAdapter = new MyAdapter(ChatActivity.this,mMessages);
        mRecyclerView.setAdapter(mAdapter);
    }
    private   void set_outgoing_messages(final DatabaseReference firebasein, final DatabaseReference firebaseout){
        firebaseout.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot>i= dataSnapshot.getChildren().iterator();
                DataSnapshot lastds;
                while(i.hasNext()){
                    DataSnapshot ds=i.next();
                    if(ds.child("seen").exists()) {
                        if(ds.child("type").getValue().toString().equals("text"))
                            mMessages.add(new ChatMessage(ds.child("message").getValue().toString(), true, Long.parseLong(ds.child("time").getValue().toString()), Integer.parseInt(ds.child("seen").getValue().toString())));
                        else if(ds.child("type").getValue().toString().equals("image")){
                            final Long time=Long.parseLong(ds.child("time").getValue().toString());
                            final Integer status=Integer.parseInt(ds.child("seen").getValue().toString());
                            Uri imageUri=null;String name=ds.child("name").getValue().toString();
                            final ChatMessage chatMessage=new ChatMessage(imageUri,true,time,status);
                            mMessages.add(chatMessage);
                            if((imageUri=ImageExists(name))==null){
                                //Picasso.with(ChatActivity.this).load().into( getTarget(ds.child("name").getValue().toString(),Long.parseLong(ds.child("time").getValue().toString()),mMessages,true,NOT_SENT));
                                StorageReference storageReference=State.getReference(ds.child("message").getValue().toString().replace('%','/'));
                                try {
                                    final File image=createImageFile(name);

                                    storageReference.getFile(image).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            chatMessage.setImageUri(Uri.fromFile(image));
                                            updateView();
                                            //mMessages.add(new ChatMessage(Uri.fromFile(image),true,time,status));
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //mMessages.add(new ChatMessage("Cannot download image.",true,time,status));
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                               // Picasso.with(ChatActivity.this).load(ds.child("message").getValue().toString()).into( getTarget(ds.child("name").getValue().toString(),Long.parseLong(ds.child("time").getValue().toString()),mMessages,true,Integer.parseInt(ds.child("seen").getValue().toString())));
                            else {
                                chatMessage.setImageUri(imageUri);
                                updateView();
                                //mMessages.add(new ChatMessage(imageUri,true,Long.parseLong(ds.child("time").getValue().toString()),Integer.parseInt(ds.child("seen").getValue().toString())));
                            } }
                    }else{

                        if(ds.child("type").getValue().toString().equals("text"))

                            mMessages.add(new ChatMessage(ds.child("message").getValue().toString(),true,Long.parseLong(ds.child("time").getValue().toString()),NOT_SENT));
                        else if(ds.child("type").getValue().toString().equals("image")){
                            final Long time=Long.parseLong(ds.child("time").getValue().toString());
                            Uri imageUri=null;String name=dataSnapshot.child("name").getValue().toString();
                            final ChatMessage chatMessage=new ChatMessage(imageUri,true,time,NOT_SENT);
                            mMessages.add(chatMessage);
                            if((imageUri=ImageExists(name))==null){
                                //Picasso.with(ChatActivity.this).load().into( getTarget(ds.child("name").getValue().toString(),Long.parseLong(ds.child("time").getValue().toString()),mMessages,true,NOT_SENT));
                                StorageReference storageReference=State.getReference(ds.child("message").getValue().toString().replace('%','/'));
                                try {
                                    final File image=createImageFile(name);
                                    storageReference.getFile(image).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            chatMessage.setImageUri(Uri.fromFile(image));
                                            updateView();
                                            //mMessages.add(new ChatMessage(Uri.fromFile(image),true,time,NOT_SENT));
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                           // mMessages.add(new ChatMessage("Cannot download image.",true,time,NOT_SENT));
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                                //Picasso.with(ChatActivity.this).load(ds.child("message").getValue().toString()).into( getTarget(ds.child("name").getValue().toString(),Long.parseLong(ds.child("time").getValue().toString()),mMessages,true,NOT_SENT));
                            else {
                                chatMessage.setImageUri(imageUri);
                                updateView();
                            }
                        }}
                }
                Collections.sort(mMessages);
                updateView();
                get_new_messages(firebasein);
                get_seen(firebaseout);

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
                        mMessages.get(mMessages.indexOf(new ChatMessage(dataSnapshot.child("message").getValue().toString(), true, Long.parseLong(dataSnapshot.child("time").getValue().toString()), NOT_SENT))).setSeen(Integer.parseInt(dataSnapshot.child("seen").getValue().toString()));
                        updateView();
                    }
                } catch (IndexOutOfBoundsException e) {

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

    //target to save
    private static Target getTarget(final String url,final  Long time,final ArrayList<ChatMessage>mMessages,final  boolean isMe,final Integer sent){
        Target target = new Target(){

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator+"BuyHatkeImages"+File.separator+ url);
                        try {
                            file.createNewFile();
                            FileOutputStream ostream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, ostream);
                            ostream.flush();
                            ostream.close();

                            mMessages.add(new ChatMessage(Uri.fromFile(file),isMe, time,sent));
                        } catch (IOException e) {
                            Log.e("IOException", e.getLocalizedMessage());
                        }
                    }
                }).start();

            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        return target;
    }
    private void get_new_messages(final DatabaseReference firebasein){
        firebasein.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot==null||dataSnapshot.child("time")==null||dataSnapshot.child("time").getValue()==null){
                    return;
                }
                if(!dataSnapshot.child("admin_time").exists()){
                  //  lastin=lastintime;
                    final long tt=System.currentTimeMillis();
                    final String new_message= dataSnapshot.child("message").getValue().toString();
                    Uri imageUri=null;
                    if(dataSnapshot.child("type").getValue().toString().equals("text")){
                        mMessages.add(new ChatMessage(new_message,false,tt,NOT_SENT));
                        lastTime=System.currentTimeMillis();
                        updateView();
                    }
                    else if(dataSnapshot.child("type").getValue().toString().equals("image")){
                        String name=dataSnapshot.child("name").getValue().toString();

                        final Long time=tt;
                        final ChatMessage chatMessage=new ChatMessage(imageUri,false,time,NOT_SENT);
                        mMessages.add(chatMessage);
                        if((imageUri=ImageExists(name))==null){
                            //Picasso.with(ChatActivity.this).load().into( getTarget(ds.child("name").getValue().toString(),Long.parseLong(ds.child("time").getValue().toString()),mMessages,true,NOT_SENT));
                            StorageReference storageReference=State.getReference(dataSnapshot.child("message").getValue().toString().replace('%','/'));
                            try {
                                final File image=createImageFile(name);
                                storageReference.getFile(image).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                       chatMessage.setImageUri(Uri.fromFile(image));

                                        lastTime=System.currentTimeMillis();
                                        updateView(mMessages.indexOf(chatMessage));
                                        // mMessages.add(new ChatMessage(Uri.fromFile(image),false,time,NOT_SENT));
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        //mMessages.add(new ChatMessage("Cannot download image.",false,time,NOT_SENT));
                                    }
                                });
                            } catch (IOException e) {
                                Toast.makeText(ChatActivity.this,"Unable to create File.",Toast.LENGTH_LONG).show();
                            }
                        }
                       // Picasso.with(ChatActivity.this).load(dataSnapshot.child("message").getValue(Uri.class)).into( getTarget(dataSnapshot.child("name").getValue().toString(),tt,mMessages,false,NOT_SENT));
                        else {
                        chatMessage.setImageUri(imageUri);

                            lastTime=System.currentTimeMillis();
                            updateView(mMessages.indexOf(chatMessage));
                            // mMessages.add(new ChatMessage(imageUri,false,tt,NOT_SENT));
                    }}
                    Map<String,Object>am=new ArrayMap<>();
                    am.put("admin_time",String.valueOf(tt));
                    firebasein.child(dataSnapshot.getKey()).updateChildren(am, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError DatabaseError, DatabaseReference firebase) {
                            firebasein.child(dataSnapshot.getKey()).child("seen").setValue("3");
                        }
                    });
                 //   updateView();
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
    private void writeToUser(String message){
        long tt=System.currentTimeMillis();
        mMessages.add(new ChatMessage(message,true,tt,NOT_SENT));
      final DatabaseReference fb= user.child(muserid).child("messages_from_admin").push();
        //Firebase fbadmin=State.getfirebase().child("messages_to_admin").push();
        Map<String,String> am=new HashMap<>();
        am.put("message",message);
        am.put("time",String.valueOf(tt) );
        am.put("type","text");

        fb.setValue(am, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError DatabaseError, DatabaseReference firebase) {
                fb.child("seen").setValue(String.valueOf(SENT));

                newMessagesForAdmin.child("lastTime").setValue(System.currentTimeMillis());
            }
        });

        lastTime=System.currentTimeMillis();
        updateView();
    }
    private void writeToUser(final Uri imageUri){

try {
    final StorageReference userStorage = State.getStorageReference().child(muserid).child("messages_from_admin").child(imageUri.getLastPathSegment());
    userStorage.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            Map<String,Object> am=new HashMap<>();
            long tt=System.currentTimeMillis();

            final DatabaseReference fb= user.child(muserid).child("messages_from_admin").push();
            mMessages.add(new ChatMessage(imageUri,true,tt,NOT_SENT));
            am.put("message",userStorage.getPath().replace('/','%'));
            am.put("name",imageUri.getLastPathSegment());
            am.put("time",String.valueOf(tt) );
            am.put("type","image");
            updateView();
            fb.setValue(am, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError DatabaseError, DatabaseReference firebase) {
                    fb.child("seen").setValue(String.valueOf(SENT));

                    newMessagesForAdmin.child("lastTime").setValue(System.currentTimeMillis());
                }
            });
        }
    });
}
catch (NullPointerException e){
    Toast.makeText(this,"reference is :  "+State.getStorageReference().toString(),Toast.LENGTH_LONG).show();
    return;
}


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chatmenu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
            case R.id.presence:
               FirebaseAuth.getInstance().signOut();
              /*if(item.getTitle().toString().equals("Go offline")){
                  item.setTitle("Going offline..");
                  State.getDatabaseReference().child(getPackageName().replace('.','/')).child("presence").setValue("0", new DatabaseReference.CompletionListener() {
                      @Override
                      public void onComplete(DatabaseError DatabaseError, DatabaseReference firebase) {
                          item.setTitle(R.string.goOnline);
                      }
                  });
              }
              else if(item.getTitle().toString().equals(getString(R.string.goOnline))){
                  item.setTitle("Going online..");
                  State.getDatabaseReference().child(getPackageName().replace('.','/')).child("presence").setValue("1", new DatabaseReference.CompletionListener() {
                      @Override
                      public void onComplete(DatabaseError DatabaseError, DatabaseReference firebase) {
                          item.setTitle(R.string.goOffline);
                      }
                  });
              }*/
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private String getServerTime(long timestamp){
        Calendar cal = Calendar.getInstance(TimeZone.getDefault(), Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        DateFormat format=DateFormat.getDateTimeInstance();
        return ( format.format((cal.getTime()))).toString();

    }
    private File createImageFile()throws IOException{
        String timestamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File imageFile= new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+File.separator+"BuyHatkeImages");
        if(!imageFile.exists())
            imageFile.mkdirs();

        File image= File.createTempFile("admin_"+timestamp+"-0",".jpg",imageFile);
        return image;
    }
    private File createImageFile(String name)throws IOException{
        //  String timestamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File imageFile= new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+File.separator+"BuyHatkeImages");
        if(!imageFile.exists())
            imageFile.mkdirs();
        File image;
        //if(name==null)

        image= File.createTempFile(name.substring(0,name.indexOf('-'))+"-0",name.substring(name.lastIndexOf('.')),imageFile);

        return image;
    }
    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try{
            File imageFile=createImageFile();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
            mCurrentFilePath=Uri.fromFile(imageFile);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                Toast.makeText(this,"here".toString(),Toast.LENGTH_LONG).show();
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
        catch (IOException e){
            Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
        }

    }
    private void galleryPic(){

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
  //          Bundle extras = data.getExtras();
//            final Bitmap imageBitmap = (Bitmap) extras.get("data");
            AlertDialog.Builder ad=new AlertDialog.Builder(this).setView(R.layout.dialog_take_picture);
            LayoutInflater inflater = (LayoutInflater)ChatActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
            ad.setView(inflater.inflate(R.layout.dialog_take_picture, null));
            final AlertDialog alertDialog=ad.create();
            alertDialog.show();
            ImageView mImage=(ImageView)alertDialog.findViewById(R.id.pic);
            if(mImage==null)
                System.out.println("mImage.......is NULL...............................................................................");

            if(mCurrentFilePath==null)
                System.out.println("mCurrentFilePath.......is NULL...............................................................................");
            imageTransform(new File(mCurrentFilePath.getPath()));
            mImage.setImageBitmap(BitmapFactory.decodeFile(mCurrentFilePath.getPath()));
            Button mOk=(Button)alertDialog.findViewById(R.id.ok);
            Button mRetake=(Button)alertDialog.findViewById(R.id.retake);
            mOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.cancel();
                    writeToUser(mCurrentFilePath);
                }
            });
            mRetake.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new File(mCurrentFilePath.getPath()).delete();
                    alertDialog.cancel();
                    dispatchTakePictureIntent();
                }
            });
        }
       else if(requestCode==REQUEST_IMAGE_PICKING&&resultCode==RESULT_OK){
            if(data.getData()!=null){

                BitmapFactory.Options options=new BitmapFactory.Options();
                options.inJustDecodeBounds=true;
                BitmapFactory.decodeFile(getPath(data.getData()),options);
                options.inSampleSize=options.outWidth/480;
                options.inJustDecodeBounds=false;
                Bitmap bitmap=BitmapFactory.decodeFile(getPath(data.getData()),options);
                try {
                    File image=createImageFile();
                    FileOutputStream fileOutputStream=new FileOutputStream(image);

                    bitmap.compress(Bitmap.CompressFormat.JPEG,50,fileOutputStream);
                    writeToUser(Uri.fromFile(image));
                    bitmap.recycle();
                } catch (IOException e) {
                    bitmap.recycle();
                    e.printStackTrace();
                }
            }
        }
    }

    private Uri ImageExists(String name){

        File imageFile= new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+File.separator+"BuyHatkeImages");
        try {
        File [] matches=findFilesForId(imageFile,name.substring(0,name.indexOf('-')));
        if(matches.length>0){
            System.out.println("match found............................");
            return Uri.fromFile(matches[0]);}
        else {
            System.out.println("NOOOOOOOOOOOOOOOOOOOOOOOOOO.................match found............................");

            return null;}
        }catch (StringIndexOutOfBoundsException e){

            System.out.println("NULLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL.................match found............................");
                     return null;
                }


    }
    private void imageTransform(File image){
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(image.getPath(),options);
        options.inSampleSize=options.outWidth/480;
        options.inJustDecodeBounds=false;
       Bitmap bitmap=BitmapFactory.decodeFile(image.getPath(),options);

        try {
            FileOutputStream fs=new FileOutputStream(image);

            bitmap.compress(Bitmap.CompressFormat.JPEG,50,fs);
            bitmap.recycle();
        } catch (FileNotFoundException e) {
            bitmap.recycle();
            e.printStackTrace();
        }

    }
    private void dispatchGallery(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*"); PackageManager packageManager = ChatActivity.this.getPackageManager();
        if(photoPickerIntent.resolveActivity(packageManager) != null){
            startActivityForResult(photoPickerIntent, REQUEST_IMAGE_PICKING);
        }
    }
    public String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }
    private static File[] findFilesForId(File dir, final String id) {
        return dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if(pathname.getName().matches(id+"-(.*)")){
                    System.out.println("MMMMAAAATTTTCCCCHHHHEEEESSSS......................");
                    return  true;
                }else
                {
                    System.out.println("NNNNNNNNNNNNNNNNNNNNNNOOOOOOOOOOOOOOOOOOOOOOOOOOOO...................");
                    return false;
                }
            }
        });
    }


}