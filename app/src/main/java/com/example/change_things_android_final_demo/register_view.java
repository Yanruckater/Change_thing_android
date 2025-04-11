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

public class register_view extends AppCompatActivity {
    public Button mbuttonRegister;
    private TextView mtextViewEmail, mtextViewPassword, mtextViewPassword2;
    ImageView mimageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mbuttonRegister = findViewById(R.id.button4);
        mtextViewEmail = findViewById(R.id.editTextEmailAddress2);
        mtextViewPassword = findViewById(R.id.editregisterPassword);
        mtextViewPassword2 = findViewById(R.id.editregisterPassword2);
        mimageView = findViewById(R.id.imageView2);

        mimageView.setImageDrawable(getResources().getDrawable(R.drawable.baseline_account_circle_24));
        mbuttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(register_view.this, "註冊成功!!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}