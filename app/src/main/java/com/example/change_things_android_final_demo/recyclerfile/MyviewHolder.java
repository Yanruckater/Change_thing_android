package com.example.change_things_android_final_demo.recyclerfile;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.change_things_android_final_demo.R;

public class MyviewHolder extends RecyclerView.ViewHolder {
    ImageView productImage;
    TextView productName, textViewExchange, productPrice, productStatus;

    public MyviewHolder(@NonNull View itemView) {
        super(itemView);
        productImage = itemView.findViewById(R.id.productImage);
        productName = itemView.findViewById(R.id.productName);
        textViewExchange = itemView.findViewById(R.id.textViewExchange);
        productPrice = itemView.findViewById(R.id.productPrice);
        productStatus = itemView.findViewById(R.id.productStatus);
    }
}
