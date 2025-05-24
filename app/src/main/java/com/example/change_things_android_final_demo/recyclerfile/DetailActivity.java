package com.example.change_things_android_final_demo.recyclerfile;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.change_things_android_final_demo.R;

public class DetailActivity extends Fragment {
    private ImageView imageView;
    private String name, exchangeItem, price, status, location;
    private String image;

    private TextView nameView;

    public DetailActivity() {
        // Required empty public constructor
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_detail, container, false);

        ImageView imageView = view.findViewById(R.id.detailImage);
        TextView nameView = view.findViewById(R.id.detailName);
        TextView exchangeView = view.findViewById(R.id.detailExchange);
        TextView priceView = view.findViewById(R.id.detailPrice);
        TextView statusView = view.findViewById(R.id.detailStatus);
        TextView locationView = view.findViewById(R.id.detailLocation);

        ImageView userImage = view.findViewById(R.id.userAvatar);
        TextView userName = view.findViewById(R.id.userName);

        Bundle args = getArguments();
        if(args != null) {
            // 取得資料
            String name = args.getString("name");
            String exchangeItem = args.getString("exchangeItem");
            String price = args.getString("price");
            String status = args.getString("status");
            String imageUri = args.getString("image");
            String location = args.getString("location");
            String UploaderImage = args.getString("userImage");
            String UploaderUserName = args.getString("userName");


            Glide.with(this).load(imageUri).placeholder(R.drawable.loading).into(imageView);
            Glide.with(this).load(UploaderImage).placeholder(R.drawable.baseline_account_circle_24).into(userImage);
            // 顯示
            nameView.setText(name);
            exchangeView.setText(exchangeItem);
            priceView.setText(price);
            statusView.setText("狀態：" + status);
            locationView.setText("地點：" + location);
            userName.setText(UploaderUserName);
        }
        return view;
    }
}