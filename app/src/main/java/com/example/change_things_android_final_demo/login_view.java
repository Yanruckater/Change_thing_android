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
    private TextView mtextViewEmail, mtextViewPassword;
    ImageView mimageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mbuttonLogin = findViewById(R.id.button);
        mtextViewEmail = findViewById(R.id.editTextEmailAddress);
        mtextViewPassword = findViewById(R.id.editTextPassword);
        mimageView = findViewById(R.id.imageView);

        mimageView.setImageDrawable(getResources().getDrawable(R.drawable.baseline_account_circle_24));

        //初始化firebase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // 設定登入按鈕點擊事件
        mbuttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mtextViewEmail.getText().toString().trim();
                String password = mtextViewPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(login_view.this, "請輸入帳號與密碼", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 登入成功
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(login_view.this, "登入成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(login_view.this, UploadItemActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // 登入失敗
                        Toast.makeText(login_view.this, "登入失敗：" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}