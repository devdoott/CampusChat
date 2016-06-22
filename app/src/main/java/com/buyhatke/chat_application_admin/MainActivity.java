package com.buyhatke.chat_application_admin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twopanelayout);
        FragmentManager fragmentManager=getSupportFragmentManager();
        Fragment fragment=fragmentManager.findFragmentById(R.id.list_fragment_container);
        if(fragment==null){
            fragment= new MainFragment();
            fragmentManager.beginTransaction().add(R.id.list_fragment_container,fragment).commit();
        }
       /* setContentView(R.layout.activity_main);
        State.setFirebaseStorage();
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(false);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(MainActivity.this, mUsers);
        mRecyclerView.setAdapter(mAdapter);*/
        //getUsers();

    }

    @Override
    protected void onStart() {
        super.onStart();
        //updateView();
        //getUsers();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:{
                if(item.getTitle().toString().equals(getString(R.string.goOffline))){
                    item.setTitle("Going offline..");

                    State.getDatabaseReference().child(getPackageName().replace('.','/')).child("presence").runTransaction(new Transaction.Handler() {
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
                    State.getDatabaseReference().child(getPackageName().replace('.','/')).child("presence").runTransaction(new Transaction.Handler() {
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
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chatmenu, menu);
        return true;
    }

}
