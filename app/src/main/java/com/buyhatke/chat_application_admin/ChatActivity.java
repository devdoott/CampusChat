package com.buyhatke.chat_application_admin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ChatActivity extends AppCompatActivity {
    private Button mSend;
    private EditText mTypemessage;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private  ArrayList<ChatMessage>mMessages=new ArrayList<>();
    private int inchild=0;
    private int outchild=0;
    private long lastin=0 ;
    private TextView mTextView;
    private String userid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatlayout);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mRecyclerView.scrollToPosition(mMessages.size()-1);
            }
        });
        mTextView=(TextView)findViewById(R.id.customersupport);
        Intent intent=getIntent();
        mTextView.setText(intent.getStringExtra("email"));
        userid=intent.getStringExtra("id");
       // mRecyclerView.setTransc
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(Color.parseColor("#ffffff"), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        lastin = sharedPref.getLong(getString(R.string.lastin),0);
        final   Firebase firebase=State.getfirebase();
        Firebase incoming_messages=firebase.child(userid).child("messages_to_admin");
        incoming_messages.keepSynced(true);
        Firebase outgoing_messages=firebase.child(userid).child("messages_from_admin");
        outgoing_messages.keepSynced(true);
        get_messages(incoming_messages,outgoing_messages);
        mTypemessage=(EditText)findViewById(R.id.typemessage);
        mTypemessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mRecyclerView.scrollToPosition(mMessages.size()-1);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mSend=(Button)findViewById(R.id.send);
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
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(getString(R.string.lastin), lastin);
        editor.commit();
    }


    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        lastin = sharedPref.getLong(getString(R.string.lastin),0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(getString(R.string.lastin), lastin);
    }

    @Override
    protected void onPause() {
        super.onPause();
        View view = ChatActivity.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(getString(R.string.lastin), lastin);
    }

    @Override
    protected void onResume() {

        super.onResume();

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        lastin = sharedPref.getLong(getString(R.string.lastin),0);
    }

    private class ChatMessage implements Comparable<ChatMessage>  {
        private String message;
        private boolean isMe;
        private long time;
        ChatMessage(String message, boolean isMe, long time){
            this.message=message;
            this.isMe=isMe;
            this.time=time;
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

        public long getTime() {
            return time;
        }

      }
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private ArrayList<ChatMessage>messages;
        private Activity context;
        public  class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView txtMessage;
            public TextView txtInfo;
            private ImageView mImageview;
            public LinearLayout content;
            public LinearLayout contentWithBG;
            public ViewHolder(View v) {
                super(v);
                this.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
                this.content = (LinearLayout) v.findViewById(R.id.content);
                this.contentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
                this.txtInfo = (TextView) v.findViewById(R.id.txtInfo);
                this.mImageview=(ImageView)v.findViewById(R.id.image);
            }
        }
        private void setAlignment(MyAdapter.ViewHolder holder, boolean isMe) {
            if (isMe) {

                holder.txtMessage.setTextColor(Color.parseColor("#ffffff"));
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

                holder.txtMessage.setTextColor(Color.parseColor("#000000"));
              //  holder.mImageview.setImageResource(R.mipmap.customer_service);
                holder.contentWithBG.setBackgroundResource(R.drawable.rounded_rectangle);

                LinearLayout.LayoutParams layoutParams =
                        (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
                layoutParams.gravity = Gravity.LEFT;
                holder.contentWithBG.setLayoutParams(layoutParams);

                RelativeLayout.LayoutParams lp =
                        (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
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
            ChatMessage cm=messages.get(position);
            setAlignment(holder, cm.isMe());
            holder.txtMessage.setText(cm.getMessage());
            holder.txtInfo.setText(getServerTime(cm.getTime()));
        }
        @Override
        public int getItemCount() {
            if (messages != null) {
                return messages.size();
            } else {
                return 0;
            }
        }
    }
    private  final void get_messages(final Firebase firebasein,final Firebase firebaseout){
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
                    if(lastin<ll){
                        lastin=ll;
                        long tt=System.currentTimeMillis();
                        firebasein.child(ds.getKey()).child("admin_time").setValue(String.valueOf(tt));
                        firebasein.child(ds.getKey()).child("seen").setValue(String.valueOf(3));
                        mMessages.add(new ChatMessage(ds.child("message").getValue().toString(),false,tt));

                    }
                    else{
                        firebasein.child(ds.getKey()).child("seen").setValue(String.valueOf(3));
                        mMessages.add(new ChatMessage(ds.child("message").getValue().toString(),false,Long.parseLong(ds.child("admin_time").getValue().toString())));
                    }
                }
                set_outgoing_messages(firebasein,firebaseout);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
    private void updateView(){
        mLayoutManager.scrollToPosition(mMessages.size()-1);
        mAdapter = new MyAdapter(ChatActivity.this,mMessages);
        mRecyclerView.setAdapter(mAdapter);
    }
    private   void set_outgoing_messages(final Firebase firebasein, final Firebase firebaseout){
        firebaseout.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot>i= dataSnapshot.getChildren().iterator();

                while(i.hasNext()){
                    DataSnapshot ds=i.next();
                    mMessages.add(new ChatMessage(ds.child("message").getValue().toString(),true,Long.parseLong(ds.child("time").getValue().toString())));
                }
                Collections.sort(mMessages);
                updateView();
               get_new_messages(firebasein);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void get_new_messages(final Firebase firebasein){
        firebasein.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot==null||dataSnapshot.child("time")==null||dataSnapshot.child("time").getValue()==null){
                    return;
                }
            long lastintime=Long.parseLong( dataSnapshot.child("time").getValue().toString());
            if(lastin<lastintime){
                lastin=lastintime;
                final long tt=System.currentTimeMillis();
                final String new_message= dataSnapshot.child("message").getValue().toString();
                firebasein.child(dataSnapshot.getKey()).child("admin_time").setValue(String.valueOf(tt));

                firebasein.child(dataSnapshot.getKey()).child("seen").setValue(String.valueOf(3));
                mMessages.add(new ChatMessage(new_message,false,tt));
                updateView();
            }}
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
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
    private void writeToUser(String message){
        Firebase firebase=State.getfirebase();
       Firebase fb= firebase.child(userid).child("messages_from_admin").push();
        //Firebase fbadmin=State.getfirebase().child("messages_to_admin").push();
        Map<String,String> am=new HashMap<>();
        am.put("message",message);
        long tt=System.currentTimeMillis();
        am.put("time",String.valueOf(tt) );
        fb.setValue(am);
       // am.put("user",firebase.getAuth().getUid());
        //fbadmin.setValue(am);
        mMessages.add(new ChatMessage(message,true,tt));
        updateView();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chatmenu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
            case R.id.logout:
                logoutUser();
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
    private void logoutUser(){
    }}