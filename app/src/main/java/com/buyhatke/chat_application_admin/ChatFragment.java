package com.buyhatke.chat_application_admin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.buyhatke.chatfirebaseadminlibrary.ChatMessage;
import com.buyhatke.chatfirebaseadminlibrary.ImageHandler;
import com.buyhatke.chatfirebaseadminlibrary.Messaging;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by devdoot on 21/6/16.
 */
public class ChatFragment extends Fragment {
    private ImageButton mSend;
    private EditText mTypemessage;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private LinearLayout mInput;
    private DatabaseReference muserFirebase=null;
    private String muserid=null;
    private ImageView mPic;
    private Uri mCurrentFilePath;
    static final int REQUEST_IMAGE_CAPTURE = 10;
    static final int REQUEST_IMAGE_PICKING=11;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private Messaging mMessenger;
    private long lastTime;
    private DatabaseReference newMessagesForAdmin;
    private TextView mTextView;
    private String mUserName;

    public ChatFragment() {
        super();
    }
    public static ChatFragment newChatFragment(String name,String id){
        ChatFragment chatFragment=new ChatFragment();
        chatFragment.setMuserid(id);
        chatFragment.setmUserName(name);
        return chatFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.chatlayout,container,false);

        checkDatabaseAndStorageInitialised();
        Toolbar myToolbar = (Toolbar) view.findViewById(R.id.my_toolbar);
        /*Fragment.setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);*/
        mTypemessage=(EditText)view.findViewById(R.id.typemessage);
        mInput=(LinearLayout)view.findViewById(R.id.input);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mTypemessage=(EditText)view.findViewById(R.id.typemessage);
        mTextView=(TextView)view.findViewById(R.id.customersupport);
        mTextView.setText(mUserName);
        mInput=(LinearLayout)view.findViewById(R.id.input);
        mInput.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(top!=oldTop)
                    mRecyclerView.scrollToPosition(mMessenger.getNumberOfMessages()-1);
            }
        });
        muserFirebase=State.getDatabaseReference().child("com%buyhatke%chat_application");
        newMessagesForAdmin=State.getDatabaseReference().child("newMessagesForAdmin").child("com%buyhatke%chat_application").child(muserid);
        final   DatabaseReference databaseReference=muserFirebase;
        DatabaseReference incoming_messages=databaseReference.child(muserid).child("messages_to_admin");
        incoming_messages.keepSynced(true);
        DatabaseReference outgoing_messages=databaseReference.child(muserid).child("messages_from_admin");
        outgoing_messages.keepSynced(true);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                System.out.println(mRecyclerView.getScrollState()+"////////////////////////////////////////////////////////////");
            }
        });
        mMessenger=new Messaging(getActivity(),muserid,databaseReference,State.getStorageReference(),newMessagesForAdmin) {
            @Override
            public void updateView() {
                mAdapter.notifyDataSetChanged();
                mLayoutManager.scrollToPosition(this.getNumberOfMessages()-1);
            }

            @Override
            public void updateView(int position) {
                mAdapter.notifyItemChanged(position);
                mLayoutManager.scrollToPosition(this.getNumberOfMessages()-1);

            }
        };
        mAdapter = new MyAdapter(getActivity(), mMessenger.getMessages());
        mRecyclerView.setAdapter(mAdapter);
        mMessenger.startMessaging(incoming_messages,outgoing_messages);
        mSend=(ImageButton)view.findViewById(R.id.send);
        mPic=(ImageView)view.findViewById(R.id.pic);
        mPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder ad=new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                mMessenger.writeToUser(mTypemessage.getText().toString().trim());
                mTypemessage.setText(null);
            }
        });
        mAuthStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser()==null){
                    Intent intent=new Intent(getActivity(),LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    getActivity().finish();
                    State.getFirebaseAuth().removeAuthStateListener(this);
                }
            }
        };

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mTypemessage.setCursorVisible(true);
        checkDatabaseAndStorageInitialised();

        newMessagesForAdmin.child("unread").setValue(0);
        if(State.getFirebaseAuth()==null)
            System.out.println("Nilllllllllllllllllllllllllllllllllllllllllllllllll");
        else
            State.getFirebaseAuth().addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkDatabaseAndStorageInitialised();
    }

    @Override
    public void onStop() {
        super.onStop();

        newMessagesForAdmin.child("unread").setValue(0);
        State.getFirebaseAuth().removeAuthStateListener(mAuthStateListener);
        mMessenger.setUnreadMessages();
    }

    private boolean checkString(String s){
        if(s==null||s.trim().equals("")){
            return false;
        }
        return true;
    }

    public void setMuserid(String muserid) {
        this.muserid = muserid;
    }

    public void setmUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private ArrayList<ChatMessage> messages;
        private Activity context;
        public  class ViewHolder extends RecyclerView.ViewHolder {
            public TextView txtMessage;
            private TextView txtInfo;
            private ImageView mImageview;
            public LinearLayout mcontent;
            public LinearLayout mcontentWithBG;
            private ImageView mImagemessage;
            public ViewHolder(View v) {
                super(v);
                this.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
                this.mcontent = (LinearLayout) v.findViewById(R.id.content);
                this.mcontentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
                this.txtInfo = (TextView) v.findViewById(R.id.txtInfo);
                this.mImageview=(ImageView)v.findViewById(R.id.image);
                this.mImagemessage=(ImageView)v.findViewById(R.id.imageMessage);
            }
        }

        private void setAlignment(MyAdapter.ViewHolder holder, boolean isMe) {
            if (isMe) {
                holder.txtMessage.setTextColor(Color.parseColor("#000000"));
                holder.mcontentWithBG.setBackgroundResource(R.drawable.rounded_rectangle_out);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.mcontentWithBG.getLayoutParams();
                layoutParams.gravity = Gravity.RIGHT;
                holder.mcontentWithBG.setLayoutParams(layoutParams);
                RelativeLayout.LayoutParams lp =
                        (RelativeLayout.LayoutParams) holder.mcontent.getLayoutParams();
                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
                lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                holder.mcontent.setLayoutParams(lp);
                layoutParams=(LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
                layoutParams.gravity = Gravity.RIGHT;
                holder.txtMessage.setLayoutParams(layoutParams);
                layoutParams = (LinearLayout.LayoutParams) holder.txtInfo.getLayoutParams();
                layoutParams.gravity = Gravity.RIGHT;
                holder.txtInfo.setLayoutParams(layoutParams);
            } else {
                holder.txtMessage.setTextColor(Color.parseColor("#ffffff"));
                holder.mcontentWithBG.setBackgroundResource(R.drawable.rounded_rectangle);
                holder.mImageview.setVisibility(View.INVISIBLE);
                LinearLayout.LayoutParams layoutParams =
                        (LinearLayout.LayoutParams) holder.mcontentWithBG.getLayoutParams();
                layoutParams.gravity = Gravity.LEFT;
                holder.mcontentWithBG.setLayoutParams(layoutParams);
                RelativeLayout.LayoutParams lp =
                        (RelativeLayout.LayoutParams) holder.mcontent.getLayoutParams();
                lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                holder.mcontent.setLayoutParams(lp);
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
                if(cm.getImageUri()!=null)
                    Picasso.with(context).load(cm.getImageUri()).placeholder(R.drawable.loading).into(holder.mImagemessage);
                else
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
            }else{
                holder.mImagemessage.setImageBitmap(null);
                holder.txtMessage.setText(cm.getMessage());
            }
            holder.txtInfo.setText(getServerTime(cm.getTime()));
            int seen=cm.isSeen();
            if(seen==Messaging.SEEN){
                final Drawable dtick = getResources().getDrawable(R.mipmap.seen);
                dtick.setColorFilter(Color.parseColor("#4caf50"), PorterDuff.Mode.SRC_ATOP);
                holder.mImageview.setImageDrawable(dtick);
                holder.mImageview.setVisibility(View.VISIBLE);
            }else if(seen==Messaging.RECEIVED){
                final Drawable dtick = getResources().getDrawable(R.mipmap.seen);
                dtick.setColorFilter(Color.parseColor("#85a3a9"),PorterDuff.Mode.SRC_ATOP);
                holder.mImageview.setImageDrawable(dtick);
                holder.mImageview.setVisibility(View.VISIBLE);
            }else if(seen==Messaging.SENT){
                final Drawable dtick = getResources().getDrawable(R.mipmap.sent);
                dtick.setColorFilter(Color.parseColor("#85a3a9"),PorterDuff.Mode.SRC_ATOP);
                holder.mImageview.setImageDrawable(dtick);
                holder.mImageview.setVisibility(View.VISIBLE);
            }else if(seen==Messaging.NOT_SENT){
                holder.mImageview.setVisibility(View.INVISIBLE);

            }
        }   @Override
        public int getItemCount() {
            if (messages != null) {
                return messages.size();
            } else {
                return 0;
            }
        }

        private String getServerTime(long timestamp){
            Calendar cal = Calendar.getInstance(TimeZone.getDefault(), Locale.ENGLISH);
            cal.setTimeInMillis(timestamp);
            Calendar now = Calendar.getInstance();final String timeFormatString = "h:mm aa";
            DateFormat format=new SimpleDateFormat(timeFormatString);
            final String dateTimeFormatString = "EEEE, MMMM d, h:mm aa";
            if(now.get(Calendar.DATE) == cal.get(Calendar.DATE) ){
                return "Today " + format.format( cal.getTime());
            }else if(now.get(Calendar.DATE) - cal.get(Calendar.DATE) == 1 ){
                return "Yesterday " + format.format( cal.getTime());
            }else if(now.get(Calendar.YEAR) == cal.get(Calendar.YEAR)){
                return new SimpleDateFormat(dateTimeFormatString).format(cal.getTime());
            }else
                return new SimpleDateFormat("MMMM dd yyyy, h:mm aa").format(cal);
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            super.onViewRecycled(holder);
            holder.mImagemessage.setOnClickListener(null);
        }
    }

    private void checkDatabaseAndStorageInitialised(){
        if(State.getDatabaseReference()==null) {
            State.setDatabaseReference();
        }if(State.getFirebaseAuth()==null){
            State.setFirebaseAuth(FirebaseAuth.getInstance());
        }if(State.getFirebaseStorage()==null) {
            State.setFirebaseStorage();
        }
    }
    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try{
            File imageFile= ImageHandler.createImageFile(getActivity());
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
            mCurrentFilePath=Uri.fromFile(imageFile);
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                this.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
        catch (IOException e){
            Toast.makeText(getActivity(),e.toString(),Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("AHOYyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy.......................");
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode ==Activity.RESULT_OK ) {

            System.out.print("Checking camera image ..........................");
            checkCameraOutput(mCurrentFilePath);
        }
        else if(requestCode==REQUEST_IMAGE_PICKING&&resultCode==Activity.RESULT_OK){
            if(data.getData()!=null){
                Uri imageUri=ImageHandler.copyAndTransformImage(data.getData(),getActivity());
                if(imageUri!=null){
                    System.out.print("Checking gallery image ..........................");
                    checkGalleryOutput(imageUri);
                }
            }
        }
        else{
            System.out.println("......................................................................rewerwerwer");
            super.onActivityResult(requestCode,resultCode,data);
        }
    }

    private void dispatchGallery(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        PackageManager packageManager = getActivity().getPackageManager();
        if(photoPickerIntent.resolveActivity(packageManager) != null){
            System.out.println("Outtttttttttttttttttt Galeryyyyyyyyyyyyyyy");
           this.startActivityForResult(photoPickerIntent, REQUEST_IMAGE_PICKING);
        }
    }
    private void checkGalleryOutput(final Uri imageUri){
        AlertDialog.Builder ad=new AlertDialog.Builder(getActivity()).setView(R.layout.dialog_take_picture);
        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ad.setView(inflater.inflate(R.layout.dialog_take_picture, null));
        final AlertDialog alertDialog=ad.create();
        alertDialog.show();
        final ImageView mImage=(ImageView)alertDialog.findViewById(R.id.pic);
        ImageView mCancel=(ImageView)alertDialog.findViewById(R.id.cancel);
        alertDialog.setCanceledOnTouchOutside(false);
        ImageView mRotate=(ImageView)alertDialog.findViewById(R.id.rotate);
        ImageHandler.imageTransform(new File(imageUri.getPath()));
        //mImage.setImageBitmap(BitmapFactory.decodeFile());
        final Bitmap bMap = BitmapFactory.decodeFile(imageUri.getPath());
        mImage.setImageBitmap(bMap);
        final Matrix matrix = new Matrix();
        final int[]multiple=new int[1];
        multiple[0]=0;
        Button mOk=(Button)alertDialog.findViewById(R.id.ok);
        Button mRetake=(Button)alertDialog.findViewById(R.id.retake);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new File(mCurrentFilePath.getPath()).delete();
                bMap.recycle();
                alertDialog.cancel();
            }
        });
        mRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                matrix.postRotate((float) 90);
                multiple[0]++;
                Bitmap bMapRotate = Bitmap.createBitmap(bMap, 0, 0, bMap.getWidth(), bMap.getHeight(), matrix, true);
                mImage.setImageBitmap(bMapRotate);
            }
        });
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.cancel();
                if(multiple[0]%4!=0){
                    ImageHandler.imageRotate(imageUri,matrix);
                }
                bMap.recycle();
                mMessenger.writeToUser(imageUri);
            }
        });
        mRetake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.cancel();
                new File(imageUri.getPath()).delete();
                bMap.recycle();
                dispatchGallery();
            }
        });
    }
    private void checkCameraOutput(final Uri imageUri){
        AlertDialog.Builder ad=new AlertDialog.Builder(getActivity()).setView(R.layout.dialog_take_picture);
        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ad.setView(inflater.inflate(R.layout.dialog_take_picture, null));
        final AlertDialog alertDialog=ad.create();
        alertDialog.show();
        final ImageView mImage=(ImageView)alertDialog.findViewById(R.id.pic);
        ImageView mCancel=(ImageView)alertDialog.findViewById(R.id.cancel);
        alertDialog.setCanceledOnTouchOutside(false);
        ImageView mRotate=(ImageView)alertDialog.findViewById(R.id.rotate);
        ImageHandler.imageTransform(new File(imageUri.getPath()));
        //mImage.setImageBitmap(BitmapFactory.decodeFile());
        final Bitmap bMap = BitmapFactory.decodeFile(imageUri.getPath());
        mImage.setImageBitmap(bMap);
        final Matrix matrix = new Matrix();
        final int[]multiple=new int[1];
        multiple[0]=0;
        Button mOk=(Button)alertDialog.findViewById(R.id.ok);
        Button mRetake=(Button)alertDialog.findViewById(R.id.retake);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new File(mCurrentFilePath.getPath()).delete();
                bMap.recycle();
                alertDialog.cancel();
            }
        });
        mRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                matrix.postRotate((float) 90);
                multiple[0]++;
                Bitmap bMapRotate = Bitmap.createBitmap(bMap, 0, 0, bMap.getWidth(), bMap.getHeight(), matrix, true);
                mImage.setImageBitmap(bMapRotate);
            }
        });
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.cancel();
                if(multiple[0]%4!=0){
                    ImageHandler.imageRotate(imageUri,matrix);
                }
                bMap.recycle();
                mMessenger.writeToUser(imageUri);
            }
        });
        mRetake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.cancel();
                new File(imageUri.getPath()).delete();
                bMap.recycle();
                dispatchTakePictureIntent();
            }
        });
    }


}
