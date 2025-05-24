package com.example.change_things_android_final_demo.recyclerfile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.change_things_android_final_demo.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity2 extends Fragment {
    private ImageView imageView;
    private String name, exchangeItem, price, status, location;
    private String image;

    private MyAdapter adapter;
    private List<itme_recycler> items;

    public DetailActivity2() {
        // Required empty public constructor
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_detail2, container, false);
        FloatingActionButton deleteBtn = view.findViewById(R.id.buttondelect);
        FloatingActionButton editBtn = view.findViewById(R.id.buttonedit);

        ImageView imageView = view.findViewById(R.id.detailImage);
        TextView nameView = view.findViewById(R.id.detailName);
        TextView exchangeView = view.findViewById(R.id.detailExchange);
        TextView priceView = view.findViewById(R.id.detailPrice);
        TextView statusView = view.findViewById(R.id.detailStatus);
        TextView locationView = view.findViewById(R.id.detailLocation);

        ImageView userImage = view.findViewById(R.id.userAvatar);
        TextView userName = view.findViewById(R.id.userName);

        items = new ArrayList<>();
        adapter = new MyAdapter(getContext(), items,"home");


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
            priceView.setText("售價:"+price);
            statusView.setText("狀態:" + status);
            locationView.setText("地點:" + location);
            userName.setText(UploaderUserName);
        }

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String itemkey = getArguments().getString("itemkey");

                if(itemkey != null && !itemkey.isEmpty()){
                    new AlertDialog.Builder(getContext()).setTitle("確認刪除").setMessage("確定要刪除嗎？").setPositiveButton("確定", (dialog, which) -> {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Images").child(uid).child(itemkey);

                        ref.removeValue().addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "刪除成功", Toast.LENGTH_SHORT).show();
                            requireActivity().onBackPressed();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "刪除失敗", Toast.LENGTH_SHORT).show();
                        });
                    }).setNegativeButton("取消", null).show();
                }else {
                    Toast.makeText(getContext(), "無法取得刪除目標", Toast.LENGTH_SHORT).show();
                }
            }
        });

        editBtn.setOnClickListener(v ->  {
            Bundle bundle = new Bundle();
            bundle.putString("name",nameView.getText().toString());

            String exchangeText = exchangeView.getText().toString();
            String exchangeItem = exchangeText.replaceFirst("希望交換物[:：]\\s*", ""); // 處理中英文冒號與空格
            bundle.putString("exchangeItem", exchangeItem);

            String priceText = priceView.getText().toString();
            String price = priceText.replaceAll("[^\\d.]", ""); // 只保留數字
            bundle.putString("price", price);

            bundle.putString("status",statusView.getText().toString().replace("狀態：",""));
            bundle.putString("location",locationView.getText().toString().replace("地點：",""));
            bundle.putString("image",image);
            bundle.putString("itemkey",getArguments().getString("itemkey"));

            Navigation.findNavController(v).navigate(R.id.nav_edit,bundle);
            Toast.makeText(getContext(), "進入編輯頁面", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}