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

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class register_view extends AppCompatActivity {
    public Button mbuttonRegister;
    private TextView mtextViewEmail, mtextViewPassword, mtextViewPassword2;
    ImageView mimageView;

    private FirebaseAuth mAuth;

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

        mAuth = FirebaseAuth.getInstance();


        mimageView.setImageDrawable(getResources().getDrawable(R.drawable.baseline_account_circle_24));
        mbuttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mtextViewEmail.getText().toString().trim();
                String password = mtextViewPassword.getText().toString().trim();
                String password2 = mtextViewPassword2.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || password2.isEmpty()) {
                    Toast.makeText(register_view.this, "請完整填寫所有欄位", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(password2)) {
                    Toast.makeText(register_view.this, "兩次密碼不一致", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(register_view.this, "註冊成功", Toast.LENGTH_SHORT).show();
                                // 導向登入畫面
                                Intent intent = new Intent(register_view.this, login_view.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(register_view.this, "註冊失敗：" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });





    }
}