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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    EditText nEmail, nPassword;
    Button LoginBtn;
    TextView GotoRegister;
    ProgressBar pb;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        nEmail= (EditText) findViewById(R.id.login_email);
        nPassword= (EditText) findViewById(R.id.login_password);
        GotoRegister = (TextView) findViewById(R.id.goto_register);
        LoginBtn = (Button) findViewById(R.id.login_btn);

        fAuth = FirebaseAuth.getInstance();
        pb = findViewById(R.id.progressBar_login);

        GotoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });

        LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    String email = nEmail.getText().toString().trim();
                    String password = nPassword.getText().toString().trim();

                    if (TextUtils.isEmpty(email))
                        nEmail.setError("Email is required");

                    if (TextUtils.isEmpty(password))
                        nPassword.setError("Password is required");

                    pb.setVisibility(View.VISIBLE);

                    try {
                        fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {
                                    Toast.makeText(Login.this, "Logged in successfully!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(Login.this, MainActivity.class));
                                } else{
                                    pb.setVisibility(View.INVISIBLE);
                                    Toast.makeText(Login.this, "Failed to login/n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();

                                }
                            }
                        });
                    } catch (Exception e) {
                        pb.setVisibility(View.INVISIBLE);
                        Toast.makeText(Login.this, e.toString(), Toast.LENGTH_LONG).show();
                    }
                }



        });


    }
}