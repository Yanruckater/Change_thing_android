package com.example.change_things_android_final_demo.recyclerfile;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.change_things_android_final_demo.R;

public class DetailActivity extends AppCompatActivity {
    private ImageView imageView;
    private String name, exchangeItem, price, status, location;
    private String image;

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
        TextView locationView = findViewById(R.id.detailLocation);

        // 取得資料
        String name = getIntent().getStringExtra("name");
        String exchangeItem = getIntent().getStringExtra("exchangeItem");
        String price = getIntent().getStringExtra("price");
        String status = getIntent().getStringExtra("status");
        String imageUri = getIntent().getStringExtra("image");
        String location = getIntent().getStringExtra("location");
        Glide.with(this).load(imageUri).placeholder(R.drawable.loading).into(imageView);

        // 顯示
        nameView.setText(name);
        exchangeView.setText("希望交換物：" + exchangeItem);
        priceView.setText("價格：" + price);
        statusView.setText("狀態：" + status);
        locationView.setText("地點：" + location);


    }
}