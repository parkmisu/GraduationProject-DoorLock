package com.example.doorlock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doorlock.login.RegisterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;     //파이어베이스 인증 처리
    private DatabaseReference mDatabaseRef; //실시간 데이터베이스
    private EditText mEtEmail, mEtPwd;      //로그인 입력필드
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        //Realtime Database 사용자 구분
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("User");

        //login_page.xml의 로그인 입력 필드 변수 설정
        mEtEmail = findViewById(R.id.et_email); //이메일 입력 창
        mEtPwd = findViewById(R.id.et_pwd);     //비밀번호 입력 창

        //자동 로그인 변수 설정
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();

        //사용자가 1번이라도 로그인을 했거나 회원가입을 진행하여 가입했을 경우 자동 로그인인
       if(currentUser != null){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        //로그인 버튼 클릭시
        Button btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //로그인 요청
                String strEmail = mEtEmail.getText().toString();
                String strPwd = mEtPwd.getText().toString();

                //아이디를 입력하지 않고 "로그인" 버튼 클릭시 토스트 메세지 출력
                if(strEmail.length()==0){
                    Toast.makeText(LoginActivity.this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    mEtEmail.requestFocus();
                    return;
                }
                //비밀번호를 입력하지 않고 "로그인"버튼 클릭시 토스트 메세지 출력
                if (strPwd.length() == 0) {
                    Toast.makeText(LoginActivity.this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    mEtPwd.requestFocus();
                    return;
                }
                mFirebaseAuth.signInWithEmailAndPassword(strEmail, strPwd).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                           //로그인 성공
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish(); //현재 액티비티 파괴
                        }else { //로그인 실패
                            Toast.makeText(LoginActivity.this, "로그인 실패. 아이디나 비밀번호를 확인해 주세요", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        //회원가입 버튼 클릭 시
        Button btn_register = findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //회원가입 화면으로 이동
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}