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
        adapter = new MyAdapter(getContext(), items,"home"); // 使用 getContext()
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
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot dataSnapshot : userSnapshot.getChildren()) { // 假設 userSnapshot 是 User 的節點
                        String name = dataSnapshot.child("caption").getValue(String.class);
                        String price = "售價: " + dataSnapshot.child("itemprice").getValue(String.class);
                        String exchange = "希望交換物: " + dataSnapshot.child("itemchange").getValue(String.class);
                        String status = "可交換"; // 暫時寫死
                        String image = dataSnapshot.child("imageURL").getValue(String.class);
                        String location = dataSnapshot.child("location").getValue(String.class);
                        String userImage = dataSnapshot.child("userImage").getValue(String.class);
                        String userName = dataSnapshot.child("userName").getValue(String.class);
                        String itemkey = dataSnapshot.getKey();

                        // 避免加入空的資料
                        if (name != null && image != null) {
                            items.add(new itme_recycler(name, exchange, price, status, image, location, userName, userImage, itemkey));
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
                    Toast.makeText(getContext(), "已成功登出!" , Toast.LENGTH_SHORT).show();
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