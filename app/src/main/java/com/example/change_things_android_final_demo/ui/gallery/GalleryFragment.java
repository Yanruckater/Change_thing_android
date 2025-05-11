package com.example.change_things_android_final_demo.ui.gallery; // 或你的 SlideshowFragment 包名

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // 如果要用 SlideshowViewModel
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// 引入你的 Binding 類別 (基於 activity_own_recycler_view.xml)
import com.example.change_things_android_final_demo.databinding.ActivityOwnRecyclerViewBinding;

import com.example.change_things_android_final_demo.Google_map_api;
import com.example.change_things_android_final_demo.UploadItemActivity;
import com.example.change_things_android_final_demo.recyclerfile.MyAdapter;
import com.example.change_things_android_final_demo.recyclerfile.itme_recycler;
import com.google.android.material.floatingactionbutton.FloatingActionButton; // 確認引入
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {

    // ViewBinding 物件
    private ActivityOwnRecyclerViewBinding binding; // 假設佈局檔是 activity_own_recycler_view.xml

    // RecyclerView 相關
    private MyAdapter adapter;
    private List<itme_recycler> items;

    // Firebase 相關
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private String uid;

    // SlideshowViewModel (可選, 目前未使用於列表邏輯)
    // private SlideshowViewModel slideshowViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // slideshowViewModel = new ViewModelProvider(this).get(SlideshowViewModel.class); // 如果使用ViewModel

        binding = ActivityOwnRecyclerViewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化元件 (透過 ViewBinding)
        // RecyclerView ID 假設是 recyclerview
        // FAB ID 假設是 fab, fab1

        binding.recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        items = new ArrayList<>();
        adapter = new MyAdapter(getContext(), items);
        binding.recyclerview.setAdapter(adapter);

        // 初始化 Firebase
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            // 獲取該用戶的商品
            databaseReference = FirebaseDatabase.getInstance().getReference("Images").child(uid);
            loadDataFromFirebaseForCurrentUser();
        } else {
            Toast.makeText(getContext(), "使用者未登入，無法查看個人商品", Toast.LENGTH_LONG).show();
            // 你可能想在這裡清空列表或顯示一個提示訊息
            items.clear();
            adapter.notifyDataSetChanged();
        }

        // 注意: own_recycler_view.java 中沒有 fab2 的點擊事件。
        // 如果你的 activity_own_recycler_view.xml 中有 fab2, 你需要決定是否為它添加事件。
        // 例如: if (binding.fab2 != null) { binding.fab2.setOnClickListener(...); }
    }

    private void loadDataFromFirebaseForCurrentUser() {
        if (valueEventListener != null && databaseReference != null) {
            databaseReference.removeEventListener(valueEventListener);
        }

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                items.clear();
                // 因為 databaseReference 已經是 .child(uid)，所以這裡的 snapshot 直接是該 uid 下的商品列表
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) { // 遍歷該用戶的所有商品
                    String name = dataSnapshot.child("caption").getValue(String.class);
                    // String desc = dataSnapshot.child("text").getValue(String.class); // 未使用
                    String price = "售價: " + dataSnapshot.child("itemprice").getValue(String.class);
                    String exchange = "希望交換物: " + dataSnapshot.child("itemchange").getValue(String.class);
                    String status = "可交換"; // 暫時寫死
                    String image = dataSnapshot.child("imageURL").getValue(String.class);
                    String location = dataSnapshot.child("location").getValue(String.class);

                    if (name != null && image != null) {
                        items.add(new itme_recycler(name, exchange, price, status, image, location));
                    }
                }
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "個人商品資料讀取失敗: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };
        databaseReference.addValueEventListener(valueEventListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (databaseReference != null && valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }
        binding = null; // 釋放 ViewBinding
    }
}