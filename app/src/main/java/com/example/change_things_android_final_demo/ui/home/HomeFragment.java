package com.example.change_things_android_final_demo.ui.home; // 或者你的 Fragment 存放的包名

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// 引入你的 Binding 類別 (名稱取決於你的 XML 檔案名)
// 如果你的 XML 是 activity_recycler_view.xml, Binding 類別就是 ActivityRecyclerViewBinding
import com.example.change_things_android_final_demo.databinding.ActivityRecyclerViewBinding;

import com.example.change_things_android_final_demo.Google_map_api; // 假設這是你要跳轉的 Activity
import com.example.change_things_android_final_demo.UploadItemActivity; // 假設這是你要跳轉的 Activity
import com.example.change_things_android_final_demo.own_recycler_view;  // 假設這是你要跳轉的 Activity
import com.example.change_things_android_final_demo.recyclerfile.MyAdapter;
import com.example.change_things_android_final_demo.recyclerfile.itme_recycler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    // ViewBinding 物件
    private ActivityRecyclerViewBinding binding; // 如果XML是activity_recycler_view.xml

    // RecyclerView 相關
    private MyAdapter adapter;
    private List<itme_recycler> items;

    // Firebase 相關
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private String uid;


    // ViewModel (如果你有使用 GalleryViewModel)
    // private GalleryViewModel galleryViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // galleryViewModel = new ViewModelProvider(this).get(GalleryViewModel.class); // 如果使用ViewModel

        // 使用 ViewBinding 來載入佈局
        binding = ActivityRecyclerViewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化元件 (透過 ViewBinding)
        // RecyclerView 的 ID 假設在 XML 中是 recyclerview
        // FAB 的 ID 假設在 XML 中是 fab, fab1, fab2

        // 初始化 RecyclerView
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        items = new ArrayList<>();
        adapter = new MyAdapter(getContext(), items); // 使用 getContext()
        binding.recyclerview.setAdapter(adapter);

        // 初始化 Firebase
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            // 注意路徑 "Images" 可能需要調整為 "Image"，根據你之前的程式碼和 Firebase 結構
            databaseReference = FirebaseDatabase.getInstance().getReference("Images"); // 或 "Image"
            loadDataFromFirebase();
        } else {
            // 處理用戶未登入的情況，例如顯示提示或導向登入頁面
            Toast.makeText(getContext(), "使用者未登入", Toast.LENGTH_LONG).show();
        }


        // 設定 FAB 的點擊事件
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UploadItemActivity.class); // 使用 getActivity()
                startActivity(intent);
            }
        });

    }

    private void loadDataFromFirebase() {
        if (valueEventListener != null && databaseReference != null) {
            databaseReference.removeEventListener(valueEventListener); // 先移除舊的監聽器以防重複添加
        }

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                items.clear(); // 清空列表
                // 你的 Firebase 資料結構是 Images -> UserID -> ItemID -> ItemDetails
                // 所以需要遍歷兩層 snapshot.getChildren()
                for (DataSnapshot userSnapshot : snapshot.getChildren()) { // 第一層是 UserID (或 Images 下直接是 ItemID?)
                    // 如果你的 Images 節點下直接就是商品列表 (沒有按 UserID 分組)，那麼這裡的遍歷方式需要調整
                    // 根據你 recycler_view.java 的邏輯，看起來是 Images -> UserID -> ItemID
                    // 但你那裡的 databaseReference 是 "Images"，然後遍歷 snapshot.getChildren() 兩次
                    // 這暗示 Images 下直接是各個 User 的節點，每個 User 節點下才是他們的商品。
                    // 如果 Images 下就是商品列表 (沒有 UserID 這層)，那麼只需要一層 for loop。
                    // 假設 Images -> UserID -> ItemID
                    // for(DataSnapshot userdataSnapuse : snapshot.getChildren()) { // 這是原來的，可能代表 UserID
                    //    for (DataSnapshot dataSnapshot : userdataSnapuse.getChildren()) { // 這是 ItemID
                    for (DataSnapshot dataSnapshot : userSnapshot.getChildren()) { // 假設 userSnapshot 是 User 的節點
                        String name = dataSnapshot.child("caption").getValue(String.class);
                        // String desc = dataSnapshot.child("text").getValue(String.class); // 在 recycler_view.java 中沒用到
                        String price = "售價: " + dataSnapshot.child("itemprice").getValue(String.class);
                        String exchange = "希望交換物: " + dataSnapshot.child("itemchange").getValue(String.class);
                        String status = "可交換"; // 暫時寫死
                        String image = dataSnapshot.child("imageURL").getValue(String.class);
                        String location = dataSnapshot.child("location").getValue(String.class);

                        // 避免加入空的資料
                        if (name != null && image != null) {
                            items.add(new itme_recycler(name, exchange, price, status, image, location));
                        }
                    }
                }
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "資料讀取失敗: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };
        databaseReference.addValueEventListener(valueEventListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 移除 Firebase 監聽器
        if (databaseReference != null && valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }
        // 釋放 ViewBinding
        binding = null;
    }
}