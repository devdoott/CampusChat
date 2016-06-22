package com.buyhatke.chat_application_admin;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private Button mRegister;
    private EditText mEmailid;
    private EditText mPassword;
    private EditText mName;
    private Button mLogin;
    private Button mRemoveAdmin;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mRegister=(Button)findViewById(R.id.button);
        mEmailid=(EditText)findViewById(R.id.emailid);
        mPassword=(EditText)findViewById(R.id.password);
        mName=(EditText)findViewById(R.id.name);
        mLogin=(Button)findViewById(R.id.loginuser);
        mRemoveAdmin=(Button)findViewById(R.id.removeadmin);
        if(State.getDatabaseReference()==null){
            State.setDatabaseReference();
        }
        mAuth=FirebaseAuth.getInstance();
        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user!=null){
                    Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                    startActivity(intent);
                }
            }
        };

        mAuth.addAuthStateListener(mAuthListener);
        final DatabaseReference firebase=State.getDatabaseReference();
       /* mRemoveAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              mA.removeUser(mEmailid.getText().toString(), mPassword.getText().toString(), new Firebase.ResultHandler() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(RegisterActivity.this,"Admin removed",Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onError(DatabaseError firebaseError) {
                        Toast.makeText(RegisterActivity.this,firebaseError.toString(),Toast.LENGTH_LONG).show();
                        // error encountered
                    }
                });
            }
        });*/
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createUser(mName.getText().toString(),mEmailid.getText().toString(),mPassword.getText().toString());
            }
        });



    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    private void createUser(final String name, final String emailId,final String password){
        if(mAuth==null)return;

        mAuth.createUserWithEmailAndPassword(emailId,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    String uid=null;
                    String packageName=getPackageName().replace('.','%');
                    Admin admin=new Admin(packageName,name,emailId,uid=task.getResult().getUser().getUid());
                    State.getDatabaseReference().child(packageName).child(uid).setValue(admin);

                    Toast.makeText(RegisterActivity.this,"Successfully created user account with uid: " +uid,Toast.LENGTH_SHORT).show();

                    mAuth.signInWithEmailAndPassword(emailId,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                //Log.w(TAG, "signInWithEmail", task.getException());
                                Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(RegisterActivity.this,"User ID: " + task.getResult().getUser().getUid()+"provider: " + task.getResult().getUser().getProviders(),Toast.LENGTH_LONG).show();
                                Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                                invalidateOptionsMenu();
                                startActivity(intent);
                            }

                        }
                    });
                }
            }
        });

    }

}

