package com.example.change_things_android_final_demo.recyclerfile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.change_things_android_final_demo.R;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyviewHolder> {

    Context context;
    List<itme_recycler> items;
    String sourceFragment;

    public MyAdapter(Context context, List<itme_recycler> items,String sourceFragment) {
        this.context = context;
        this.items = items;
        this.sourceFragment = sourceFragment;
    }

    @NonNull
    @Override
    public MyviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyviewHolder(LayoutInflater.from(context).inflate(R.layout.item_view_recycler, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyviewHolder holder, int position) {

        Glide.with(context).load(items.get(position).getImage()).placeholder(R.drawable.loading).into(holder.productImage);

        holder.productName.setText(items.get(position).getName());
        holder.textViewExchange.setText(items.get(position).getExchangeItem());
        holder.productPrice.setText(items.get(position).getPrice());
        holder.productStatus.setText(items.get(position).getStatus());

        //點擊顯示詳細資訊
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();

            bundle.putString("name", items.get(position).getName());
            bundle.putString("exchangeItem", items.get(position).getExchangeItem());
            bundle.putString("price", items.get(position).getPrice());
            bundle.putString("status", items.get(position).getStatus());
            bundle.putString("image", items.get(position).getImage());
            bundle.putString("location", items.get(position).getLocation());
            bundle.putString("userImage", items.get(position).getUserImage());
            bundle.putString("userName", items.get(position).getUserName());
            bundle.putString("itemkey", items.get(position).getItemkey());

            if("gallery".equals(sourceFragment)){
                Navigation.findNavController(v).navigate(R.id.nav_detail2,bundle);
            }else if("home".equals(sourceFragment)){
                Navigation.findNavController(v).navigate(R.id.nav_detail,bundle);
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
