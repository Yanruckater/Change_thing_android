package com.example.change_things_android_final_demo.recyclerfile;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.change_things_android_final_demo.R;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyviewHolder> {

    Context context;
    List<itme_recycler> items;

    public MyAdapter(Context context, List<itme_recycler> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public MyviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyviewHolder(LayoutInflater.from(context).inflate(R.layout.item_view_recycler, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyviewHolder holder, int position) {
        holder.productImage.setImageResource(items.get(position).getImage());
        holder.productName.setText(items.get(position).getName());
        holder.textViewExchange.setText(items.get(position).getExchangeItem());
        holder.productPrice.setText(items.get(position).getPrice());
        holder.productStatus.setText(items.get(position).getStatus());

        //點擊顯示詳細資訊
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("name", items.get(position).getName());
            intent.putExtra("exchangeItem", items.get(position).getExchangeItem());
            intent.putExtra("price", items.get(position).getPrice());
            intent.putExtra("status", items.get(position).getStatus());
            intent.putExtra("image", items.get(position).getImage());


            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
