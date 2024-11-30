package com.example.doorlock;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    PeopleActivity peopletab;
    SettingActivity settingtab;
    DangerActivity dangertab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        peopletab = new PeopleActivity();
        settingtab = new SettingActivity();
        dangertab = new DangerActivity();

        getSupportFragmentManager().beginTransaction().replace(R.id.content_layout, settingtab).commit();

        BottomNavigationView bottom_bar = findViewById(R.id.nav_view);
        bottom_bar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_1: //첫번째 매뉴 선택
                        getSupportFragmentManager().beginTransaction().replace(R.id.content_layout, settingtab).commit();
                        return true;
                    case R.id.navigation_2: //두번째 메뉴 선택
                        getSupportFragmentManager().beginTransaction().replace(R.id.content_layout, peopletab).commit();
                        return true;
                    case R.id.navigation_3: //세번쨰 메뉴 선택
                        getSupportFragmentManager().beginTransaction().replace(R.id.content_layout, dangertab).commit();
                        return true;
                }
                return false;
            }
        });
    }
}