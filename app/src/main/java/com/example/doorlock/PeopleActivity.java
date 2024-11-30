package com.example.doorlock;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PeopleActivity extends Fragment {
    ImageButton enroll_btn, enrolllist_btn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.people_page, container, false);

        enroll_btn = (ImageButton) rootView.findViewById(R.id.enroll_btn);          //사용자 등록 버튼
        enroll_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CameraActivity.class);    //사용자 등록 화면으로 전환
                startActivity(intent);
            }
        });

        enrolllist_btn = (ImageButton) rootView.findViewById(R.id.inout_btn);       //사용자 확인 버튼
        enrolllist_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(getActivity(), ListActivity.class);     //사용자 확인 액티비티로 변환
                startActivity(intent2);
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}