package com.example.doorlock.login;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.doorlock.LoginActivity;
import com.example.doorlock.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class AccountSetActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth; //파이어베이스 인증 처리
    private DatabaseReference mDatabaseRef; //실시간 데이터베이스
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accountset_page);

        //Firebase 계정 getInstance()
        mFirebaseAuth = FirebaseAuth.getInstance();

        //accountset_page.xml 창 버튼 설정
        Button btn_logout = findViewById(R.id.btn_logout); //로그아웃 버튼
        Button btn_delete = findViewById(R.id.btn_delete); //계정 탈퇴 버튼

        //회원 비밀번호 가져오기
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

        /*"로그아웃" 버튼 클릭 시*/
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //로그아웃 확인 창 출력
                AlertDialog.Builder builder = new AlertDialog.Builder(AccountSetActivity.this);
                builder.setTitle("로그아웃");
                builder.setMessage("로그아웃 하시겠습니까?");
                //예 눌렀을 때 로그아웃 진행
                builder.setNegativeButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mFirebaseAuth.signOut(); //Firebase 로그아웃
                        //로그인 창으로 이동
                        Intent intent = new Intent(AccountSetActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish(); //현재 액티비티 파괴
                        Toast.makeText(AccountSetActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                //아니요 눌렀을 때 로그아웃 취소
                builder.setPositiveButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(AccountSetActivity.this, "로그아웃이 취소되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.create().show();
            }
        });

        /*"계정 탈퇴" 버튼 클릭 시*/
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //계정 탈퇴 확인 창 출력
                AlertDialog.Builder builder = new AlertDialog.Builder(AccountSetActivity.this);
                builder.setTitle("계정 탈퇴");
                builder.setMessage("계정 탈퇴 하시겠습니까?");
                //예 눌렀을 때 계정 탈퇴 진행
                builder.setNegativeButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //계정 탈퇴하기
                        mFirebaseAuth.getCurrentUser().delete(); //Auth에서 계정 탈퇴
                        //Realtime Database 사용자 구분 Uid 삭제
                        mDatabaseRef = FirebaseDatabase.getInstance().getReference("User");
                        mDatabaseRef.child(firebaseUser.getUid()).removeValue();

                        //로그인 창으로 이동
                        Intent intent = new Intent(AccountSetActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish(); //현재 액티비티 파괴
                        Toast.makeText(AccountSetActivity.this, "계정 탈퇴되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
                //아니요 눌렀을 때 계정 탈퇴 취소
                builder.setPositiveButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(AccountSetActivity.this, "계정 탈퇴 취소", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.create().show();
            }
        });
    }
}