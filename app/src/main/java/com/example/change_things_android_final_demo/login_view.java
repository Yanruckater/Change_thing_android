package com.example.change_things_android_final_demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class login_view extends AppCompatActivity {

    public Button mbuttonLogin;
    private TextView mtextViewEmail, mtextViewPassword, mtextViewRegister;
    ImageView mimageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.google.firebase.FirebaseApp.initializeApp(this);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_view);


        mbuttonLogin = findViewById(R.id.button);
        mtextViewEmail = findViewById(R.id.editTextEmailAddress);
        mtextViewPassword = findViewById(R.id.editTextPassword);
        mimageView = findViewById(R.id.imageView);
        mtextViewRegister = findViewById(R.id.registerText);

        mimageView.setImageDrawable(getResources().getDrawable(R.drawable.app_logo));

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        mbuttonLogin.setOnClickListener(v -> {
            String email = mtextViewEmail.getText().toString().trim();
            String password = mtextViewPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(login_view.this, "請輸入帳號與密碼", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    Toast.makeText(login_view.this, "登入成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(login_view.this, Navigation_drawer_view.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(login_view.this, "登入失敗：" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        mtextViewRegister.setOnClickListener(v -> {
            Intent intent = new Intent(login_view.this, register_view.class);
            startActivity(intent);
        });
    }
}
