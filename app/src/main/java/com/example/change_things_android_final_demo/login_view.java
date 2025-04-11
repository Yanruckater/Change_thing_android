package com.example.change_things_android_final_demo;

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
        mbuttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(login_view.this, "登入成功!!", Toast.LENGTH_SHORT).show();

            }
        });

    }
}