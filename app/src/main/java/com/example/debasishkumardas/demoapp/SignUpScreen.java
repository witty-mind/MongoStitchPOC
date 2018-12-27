package com.example.debasishkumardas.demoapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mongodb.lang.NonNull;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.core.auth.providers.userpassword.UserPasswordAuthProviderClient;
import com.mongodb.stitch.core.auth.providers.userpassword.UserPasswordCredential;

public class SignUpScreen extends AppCompatActivity {

    EditText etFullname, etPassword, etEmail, etPhoneNumber;
    Button btnSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_screen);

        etFullname = (EditText) findViewById(R.id.etFullName);
        etEmail = (EditText) findViewById(R.id.etUserId);
        etPhoneNumber = (EditText) findViewById(R.id.etPhone);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateUserData();
            }
        });

    }

    void validateUserData(){
        if(TextUtils.isEmpty(etFullname.getText().toString().trim())){
            Toast.makeText(this, "Please Enter Your FullName", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(etEmail.getText().toString().trim())){
            Toast.makeText(this, "Please Enter Your Email", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(etPhoneNumber.getText().toString().trim())){
            Toast.makeText(this, "Please Enter Your Phone Number", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(etPassword.getText().toString().trim())){
            Toast.makeText(this, "Please Enter Your Password", Toast.LENGTH_SHORT).show();
        }else {
            onSuccessfulValidations(etEmail.getText().toString().trim(),
                    etPassword.getText().toString().trim());
        }
    }

    void onSuccessfulValidations(String userName, String password){
        UserPasswordAuthProviderClient emailPassClient = Stitch.getDefaultAppClient().getAuth().getProviderClient(
                UserPasswordAuthProviderClient.factory);

        emailPassClient.registerWithEmail(userName, password)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                           @Override
                                           public void onComplete(@NonNull final Task<Void> task) {
                                               if (task.isSuccessful()) {
                                                   Log.d("stitch", "Successfully sent account confirmation email");
                                               } else {
                                                   Log.e("stitch", "Error registering new user:", task.getException());
                                               }
                                           }
                                       }
                );
    }
}
