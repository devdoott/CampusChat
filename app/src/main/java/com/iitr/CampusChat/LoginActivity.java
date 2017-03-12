package com.buyhatke.chat_application_admin;

import android.content.Intent;
import android.support.annotation.NonNull;
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

public class LoginActivity extends AppCompatActivity {

    private Button mLogin;
    private EditText mEmail;
    private EditText mPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mLogin=(Button)findViewById(R.id.button2);
        mEmail=(EditText)findViewById(R.id.editText);
        mPassword=(EditText)findViewById(R.id.editText2);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_user(mEmail.getText().toString(),mPassword.getText().toString());
            }
        });

    }

    private void login_user(String email,String password){
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //System.out.println(+"////////////////////////////////////////////////////////////////////////////////////////////////");
                    Toast.makeText(LoginActivity.this,"User ID: " + task.getResult().getUser().getUid() + ", Provider: " + task.getResult().getUser().getProviders(),Toast.LENGTH_LONG).show();
                    invalidateOptionsMenu();
                    Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(intent);
                }
                else{

                    Toast.makeText(LoginActivity.this,task.getException().toString(),Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}