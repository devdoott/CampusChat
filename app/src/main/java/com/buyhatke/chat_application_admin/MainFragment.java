package com.buyhatke.chat_application_admin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by devdoot on 21/6/16.
 */
public class MainFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<User> mUsers=new ArrayList<>();
    private RecyclerView.Adapter mAdapter;
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private ArrayList<User> users;
        private Activity context;
        public  class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView txtMessage;
            public TextView txtInfo;
            private ImageView mImageview;
            public LinearLayout content;
            public RelativeLayout contentWithBG;
            public LinearLayout unread;
            public ViewHolder(View v) {
                super(v);
                //ViewHolder holder = new ViewHolder();
                this.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
                this.content = (LinearLayout) v.findViewById(R.id.content);
                this.contentWithBG = (RelativeLayout) v.findViewById(R.id.contentWithBackground);
                this.txtInfo = (TextView) v.findViewById(R.id.txtInfo);
                this.unread=(LinearLayout)v.findViewById(R.id.unread);
                //this.mImageview=(ImageView)v.findViewById(R.id.image);
                // return holder;

            }
        }


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
                    User user=(User)v.getTag();
                    getActivity().getSupportFragmentManager().beginTransaction().add(R.id.chat_fragment_container,ChatFragment.newChatFragment(user.getFullName(),user.getId())).commit();

                }
            });
            ViewHolder vh = new ViewHolder(v);

            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            User cm=users.get(position);
            holder.itemView.setTag(cm);
            // setAlignment(holder, cm.isMe());
            holder.txtMessage.setText(cm.getFullName());
            if(cm.getUnreadMessages()!=0){
                holder.unread.setVisibility(View.VISIBLE);
                holder.txtInfo.setVisibility(View.VISIBLE);
                holder.txtInfo.setText(String.valueOf(cm.getUnreadMessages()));
            }
            else{
                holder.unread.setVisibility(View.INVISIBLE);
                holder.txtInfo.setVisibility(View.INVISIBLE);
            }
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
        mAdapter.notifyDataSetChanged();
    }

    public MainFragment() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.activity_main,container,false);
        State.setFirebaseStorage();
        Toolbar myToolbar = (Toolbar) view.findViewById(R.id.my_toolbar);
       /* getActivity().setSupportActionBar(myToolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);*/
        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(false);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(getActivity(), mUsers);
        mRecyclerView.setAdapter(mAdapter);
        getUsers();
        return view;
    }


    private void getUsers(){
        if(State.getDatabaseReference()==null)
            State.setDatabaseReference();
        State.getDatabaseReference().child("newMessagesForAdmin").child("com%buyhatke%chat_application").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator=dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()){
                    DataSnapshot ds=iterator.next();
                    System.out.println(ds.toString()+"//////////////////////////////////////////////////////////////////////////");
                    if(ds.child("fullName").getValue()!=null){

                        System.out.println("inside if\t"+ds.toString()+"//////////////////////////////////////////////////////////////////////////");
                        mUsers.add(new User(getActivity().getPackageName().replace('.','%'),ds.child("fullName").getValue().toString(),ds.getKey(),
                                Integer.parseInt(ds.child("unread").getValue().toString()),
                                Long.parseLong(ds.child("lastTime").getValue().toString())));


                    }
                }
                Collections.sort(mUsers);
                updateView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        State.getDatabaseReference().child("newMessagesForAdmin").child("com%buyhatke%chat_application").orderByChild("lastTime").limitToLast(100).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot ds, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot ds, String s) {
                if(ds.child("fullName").getValue()!=null&&Integer.parseInt(ds.child("unread").getValue().toString())!=0){

                    System.out.println("inside changed    "+ds.toString()+"//////////////////////////////////////////////////////////////////////////");
                    User user=new User(getActivity().getPackageName().replace('.','%'),ds.child("fullName").getValue().toString(),ds.getKey(),
                            Integer.parseInt(ds.child("unread").getValue().toString()),Long.parseLong(ds.child("lastTime").getValue().toString()));
                    int index=mUsers.indexOf(user);
                    System.out.println("INDEX : "+index+"            ////////////////////////////////////" );
                    if(index!=-1)
                        mUsers.remove(index);
                    mUsers.add(0,user);

                    updateView();
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

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:{
                if(item.getTitle().toString().equals(getString(R.string.goOffline))){
                    item.setTitle("Going offline..");

                    State.getDatabaseReference().child(getActivity().getPackageName().replace('.','/')).child("presence").runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            if(mutableData.getValue()==null||mutableData.getValue().toString()=="1"){
                                mutableData.setValue("0");
                                System.out.println("Now offline 0 + ///////////////////////////////////////////////////////////////////////////////////////////////////");
                            }
                            else{
                                mutableData.setValue(String.valueOf(Integer.parseInt(mutableData.getValue().toString())-1));
                                System.out.println("Now offline -1 + ///////////////////////////////////////////////////////////////////////////////////////////////////");
                            }
                            return null;
                        }

                        @Override
                        public void onComplete(DatabaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {
                            item.setTitle(R.string.goOnline);
                        }
                    });
                }
                else if(item.getTitle().toString().equals(getString(R.string.goOnline))){
                    item.setTitle("Going online..");
                    State.getDatabaseReference().child(getActivity().getPackageName().replace('.','/')).child("presence").runTransaction(new Transaction.Handler() {
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {
                            if(mutableData.getValue()==null||mutableData.getValue().toString()=="0"){
                                mutableData.setValue(1);
                                System.out.printf("Now online %s + //////////////////////////////////////////////////////////////////////////////////////////////////\n",mutableData.getValue().toString());
                            }
                            else{
                                mutableData.setValue((Long) mutableData.getValue()+1);
                                System.out.printf("Now online %s + //////////////////////////////////////////////////////////////////////////////////////////////////\n",mutableData.getValue().toString());
                            }
                            return null;
                        }

                        @Override
                        public void onComplete(DatabaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {
                            item.setTitle(R.string.goOffline);
                        }
                    });
                }
                return true;}
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
