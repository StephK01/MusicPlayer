package com.example.musicplayer;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    EditText nName, nEmail, nPassword, nPhone;
    Button RegisterBtn;
    TextView GotoLogin;
    ProgressBar pb;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nName= (EditText) findViewById(R.id.full_name);
        nEmail= (EditText) findViewById(R.id.login_email);
        nPassword= (EditText) findViewById(R.id.login_password);
        nPhone= (EditText) findViewById(R.id.phone_no);
        GotoLogin = (TextView) findViewById(R.id.already_registered);
        RegisterBtn = (Button) findViewById(R.id.login_btn);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        pb = findViewById(R.id.progressBar);

        if(fAuth.getCurrentUser()!=null) {
            startActivity(new Intent(Register.this, MainActivity.class));
            finish();
        }

        GotoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });


        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String name = nName.getText().toString();
                final String phoneNo = nPhone.getText().toString();
                final String email = nEmail.getText().toString().trim();
                String password = nPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email))
                    nEmail.setError("Email is required");

                if(TextUtils.isEmpty(password))
                    nPassword.setError("Password is required");

                if(password.length() < 8)
                    nPassword.setError("Password length should be more than 8 characters");

                pb.setVisibility(View.VISIBLE);

                try{
                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(Register.this,"User added successfully!",Toast.LENGTH_SHORT).show();
                            userID=fAuth.getCurrentUser().getUid();

                            DocumentReference documentReference = fStore.collection("users").document(userID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("FullName",name);
                            user.put("Phone",phoneNo);
                            user.put("Email",email);

                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(Register.this,"Data saved successfully",Toast.LENGTH_SHORT).show();
                                }
                            });

                            startActivity(new Intent(Register.this,MainActivity.class));
                        }
                        else{
                            pb.setVisibility(View.INVISIBLE);
                            Toast.makeText(Register.this,"Failed to add user/n"+ task.getException().getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    }
                });}
                catch (Exception e){
                    pb.setVisibility(View.INVISIBLE);
                    Toast.makeText(Register.this, e.toString(), Toast.LENGTH_LONG).show();
                }



            }

        });

    }
}