package com.example.doorlock;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.doorlock.help.Fragment1;
import com.example.doorlock.help.Fragment2;
import com.example.doorlock.help.Fragment3;
import com.example.doorlock.help.PageAdapter;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.ArrayList;

public class HelpActivity extends AppCompatActivity{
    PageAdapter pageAdapter ;
    ViewPager2 viewPager2;
    ArrayList<Fragment> fragList = new ArrayList<Fragment>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.help_page);

        viewPager2=findViewById(R.id.viewPager2);

        Fragment1 fragment1 =new Fragment1();
        Fragment2 fragment2 =new Fragment2();
        Fragment3 fragment3 =new Fragment3();

        fragList.add(fragment1);
        fragList.add(fragment2);
        fragList.add(fragment3);

        pageAdapter =new PageAdapter(this,fragList);
        viewPager2.setAdapter(pageAdapter);

        DotsIndicator indicator =findViewById(R.id.dots_indicator);
        indicator.setViewPager2(viewPager2);

    }

    //확인 버튼 클릭
    public void mOnClose(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", "Close Popup");
        setResult(RESULT_OK, intent);

        //액티비티(팝업) 닫기
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        return event.getAction() != MotionEvent.ACTION_OUTSIDE;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
    }
}


