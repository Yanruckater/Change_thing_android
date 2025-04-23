package com.example.change_things_android_final_demo.recyclerfile;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.change_things_android_final_demo.R;

public class DetailActivity extends AppCompatActivity {
    private ImageView imageView;
    private String name, exchangeItem, price, status;
    private int image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);       //設定畫面


        ImageView imageView = findViewById(R.id.detailImage);
        TextView nameView = findViewById(R.id.detailName);
        TextView exchangeView = findViewById(R.id.detailExchange);
        TextView priceView = findViewById(R.id.detailPrice);
        TextView statusView = findViewById(R.id.detailStatus);

        // 取得資料
        String name = getIntent().getStringExtra("name");
        String exchangeItem = getIntent().getStringExtra("exchange");
        String price = getIntent().getStringExtra("price");
        String status = getIntent().getStringExtra("status");
        int image = getIntent().getIntExtra("image", 0);

        // 顯示
        imageView.setImageResource(image);
        nameView.setText(name);
        exchangeView.setText("希望交換物：" + exchangeItem);
        priceView.setText("價格：" + price);
        statusView.setText("狀態：" + status);
    }
}