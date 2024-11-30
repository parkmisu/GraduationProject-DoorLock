package com.example.doorlock.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doorlock.LoginActivity;
import com.example.doorlock.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/*회원가입*/
public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth; //파이어베이스 인증 처리
    private DatabaseReference mDatabaseRef; //실시간 데이터베이스
    private EditText mEtEmail, mEtPwd, mEtPwdConfirm; //회원가입 입력필드
    private Button mBtnRegister; //회원가입 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page);

        //Realtime Database 사용자 구분
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("User");

        //register_page.xml의 회원가입 입력 필드 변수 설정
        mEtEmail = findViewById(R.id.et_email); //이메일 입력 창
        mEtPwd = findViewById(R.id.et_pwd); //비밀번호 입력 창
        mEtPwdConfirm = findViewById(R.id.et_pwd_confirm); //비밀번호 확인 창
        mBtnRegister = findViewById(R.id.btn_join); //가입 버튼

        /*"가입" 버튼 클릭 시*/
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //회원가입 처리 시작
                String strEmail = mEtEmail.getText().toString();
                String strPwd = mEtPwd.getText().toString();
                String strPwdConfirm = mEtPwdConfirm.getText().toString();

                //아이디를 입력하지 않고 "로그인" 버튼 클릭시 토스트 메세지 출력
                if(strEmail.length()==0){
                    Toast.makeText(RegisterActivity.this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    mEtEmail.requestFocus();
                    return;
                }
                //비밀번호를 입력하지 않고 "로그인"버튼 클릭 시 토스트 메세지 출력
                if (strPwd.length() == 0) {
                    Toast.makeText(RegisterActivity.this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    mEtPwd.requestFocus();
                    return;
                }
                //비밀번호 확인을 입력하지 않고 "로그인"버튼 클릭 시 토스트 메세지 출력
                if (strPwdConfirm.length() == 0) {
                    Toast.makeText(RegisterActivity.this, "비밀번호 확인을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    mEtPwd.requestFocus();
                    return;
                }
                //"가입"버튼 클릭 시 비밀번호와 비밀번호 확인이 일치하지 않을 경우 토스트메세지 출력
                if (strPwd.equals(strPwdConfirm) == false) {
                    Toast.makeText(RegisterActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    mEtPwdConfirm.requestFocus();
                    return;
                }
                //Firebase 등록 진행
                mFirebaseAuth.createUserWithEmailAndPassword(strEmail, strPwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //비밀번호와 비밀번호 확인이 일치하여 회원가입에 성공했을 경우
                        if(strPwd.equals(strPwdConfirm) == true&&task.isSuccessful()){
                            //Firebase에 사용자 등록
                            FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                            //UserAccount.java에 사용자 등록
                            com.example.doorlock.login.UserAccount account = new UserAccount();
                            account.setIdToken(firebaseUser.getUid()); //Firebase에 등록된 Uid UserAccount에 등록
                            account.setEmailId(firebaseUser.getEmail()); //Firebase에 등록된 Uid UserAccount에 등록
                            account.setPassword(strPwd); //Firebase에 등록된 Uid UserAccount에 등록

                            //Realtime Database에서 사용자를 구분하기 위하여, 등록된 사용자마다 데이터 변수들 셋팅
                            com.example.doorlock.login.UserAccount2 account2 = new UserAccount2();
                            account2.setDoor("close");
                            account2.setFP("fail");
                            account2.setMessage("null");
                            account2.setName("null");
                            account2.setSet_value("null");

                            //가입 성공 시, LoginActivity로 이동
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);

                            //가입 성공 시, Firebase Realtime Database에서 Uid로 사용자 구분
                            mDatabaseRef.child(firebaseUser.getUid()).setValue(account2);
                            Toast.makeText(RegisterActivity.this, "회원가입에 성공하였습니다.",Toast.LENGTH_SHORT).show();
                        }else{ //회원가입에 실패했을 경우
                            Toast.makeText(RegisterActivity.this, "회원가입에 실패하였습니다.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }
}