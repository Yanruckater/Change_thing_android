package com.example.change_things_android_final_demo;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

//使用者資訊
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.example.change_things_android_final_demo.databinding.ActivityMainnavigationBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class  Navigation_drawer_view extends AppCompatActivity {

    private NavigationView navigationView;

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainnavigationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        binding = ActivityMainnavigationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        navigationView = binding.navView;

        setSupportActionBar(binding.appBarMainnavigation.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView1 = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_mainnavigation);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView1, navController);

        //使用者頭像與帳號名稱抓取
        View headerView = navigationView1.getHeaderView(0);

        TextView userNameText = headerView.findViewById(R.id.userNameText);
        TextView userEmailText = headerView.findViewById(R.id.userEmailText);
        ImageView profileImage = headerView.findViewById(R.id.imageView);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Images").child(uid);

            userEmailText.setText(user.getEmail());
            if (user.getDisplayName() != null)
                userNameText.setText(user.getDisplayName());

            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).into(profileImage);
            } else {
                profileImage.setImageResource(R.mipmap.ic_launcher_round); // 預設圖
            }
        }

        //登出
        navigationView1.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(Navigation_drawer_view.this, login_view.class)); // 登入頁面
                finish();
                return true;
            }

            NavigationUI.onNavDestinationSelected(item, navController);
            drawer.closeDrawers();
            return true;
        });

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,R.id.nav_gallery,R.id.nav_slideshow).setOpenableLayout(drawer).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 不顯示
        return false;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_mainnavigation);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void onBackPressed() {
        NavController navController = Navigation.findNavController(this,R.id.nav_host_fragment_content_mainnavigation);
        int currentDestinationId = navController.getCurrentDestination().getId();

        if(currentDestinationId == R.id.nav_home){
            Toast.makeText(this,"請用登出鍵來登出",Toast.LENGTH_SHORT).show();
        }else {
            super.onBackPressed();
        }
    }

    public void updateNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        ImageView navHeaderImage = headerView.findViewById(R.id.imageView);
        TextView userNameText = headerView.findViewById(R.id.userNameText);
        TextView userEmailText = headerView.findViewById(R.id.userEmailText);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (user.getDisplayName() != null) {
                userNameText.setText(user.getDisplayName());
            }
            userEmailText.setText(user.getEmail());

            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .into(navHeaderImage);
            } else {
                navHeaderImage.setImageResource(R.mipmap.ic_launcher_round);
            }
        }
    }


}
