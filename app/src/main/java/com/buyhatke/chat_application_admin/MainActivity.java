package com.buyhatke.chat_application_admin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import android.support.v7.widget.RecyclerView.OnItemTouchListener;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<User>mUsers=new ArrayList<>();
    private RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!State.isPersistence()) {
            Firebase.setAndroidContext(this);
            Firebase.getDefaultConfig().setPersistenceEnabled(true);
            State.setPersistence(true);
            State.setFirebase("https://intense-torch-2537.firebaseio.com/");
        }
    //    setContentView(R.layout.chatlayout);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
       // getSupportActionBar().setHomeButtonEnabled(true);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(false);
        // mRecyclerView.setTransc
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        getUsers();

    }
    private void getUsers(){
        State.getfirebase().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
              Iterator< DataSnapshot> i= dataSnapshot.getChildren().iterator();
                DataSnapshot ds;
              while(i.hasNext()){
                  ds=i.next();
                  if(!ds.getKey().toString().equals("messages_to_admin")){
                      mUsers.add(new User(ds.child("fullName").getValue().toString(),ds.child("email").getValue().toString(),ds.getKey().toString()));
                  }
              }
                updateView();

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private ArrayList<User> users;
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
                //ViewHolder holder = new ViewHolder();
                this.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
                this.content = (LinearLayout) v.findViewById(R.id.content);
                this.contentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
                this.txtInfo = (TextView) v.findViewById(R.id.txtInfo);
                //this.mImageview=(ImageView)v.findViewById(R.id.image);
                // return holder;

            }
        }
       /* private void setAlignment(MyAdapter.ViewHolder holder, boolean isMe) {
            if (isMe) {

                holder.txtMessage.setTextColor(Color.parseColor("#ffffff"));
                holder.contentWithBG.setBackgroundResource(R.drawable.rounded_rectangle_out);

//                holder.mImageview.setImageBitmap(null);
                LinearLayout.LayoutParams layoutParams =
                        (LinearLayout.LayoutParams) holder.contentWithBG.getLayoutParams();
                layoutParams.gravity = Gravity.RIGHT;
                holder.contentWithBG.setLayoutParams(layoutParams);
                RelativeLayout.LayoutParams lp =
                        (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
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
        }*/


        public MyAdapter(Activity context, ArrayList<User>users) {
            this.context = context;
            this.users=users;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.textviewlayout, null);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent =new Intent(MainActivity.this,ChatActivity.class);
                    User user=(User)v.getTag();
                    intent.putExtra("name",user.getFullName());
                    intent.putExtra("email",user.getEmail());
                    intent.putExtra("id",user.getId());
                    startActivity(intent);
                }
            });
            ViewHolder vh = new ViewHolder(v);

            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            User cm=users.get(position);
            holder.itemView.setTag(cm);
            holder.contentWithBG.setBackgroundResource(R.drawable.rounded_rectangle_out);
            holder.txtMessage.setTextColor(Color.parseColor("#ffffff"));
           // setAlignment(holder, cm.isMe());
            holder.txtMessage.setText(cm.getFullName());
            holder.txtInfo.setText(cm.getEmail());
        }
        @Override
        public int getItemCount() {
            if (users != null) {
                return users.size();
            } else {
                return 0;
            }
        }
    }

    private void updateView(){
        // mLayoutManager.scrollToPosition(mUsers.size()-1);
        mAdapter = new MyAdapter(MainActivity.this,mUsers);
        mRecyclerView.setAdapter(mAdapter);
    }
}
