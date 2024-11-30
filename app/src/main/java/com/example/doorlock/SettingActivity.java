package com.example.doorlock;

import static java.lang.Thread.sleep;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.doorlock.login.AccountSetActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.concurrent.Executor;

public class SettingActivity extends Fragment {
    ImageButton to_unlock_btn,help_btn,set_btn;
    boolean i=true;
    String fp="close";
    String data_door;

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference conditionRef = mRootRef.child("fp");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.setting_page, container, false);

        help_btn=(ImageButton)rootView.findViewById(R.id.help_btn);
        help_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentL = new Intent(getActivity(), HelpActivity.class);
                startActivity(intentL);
            }
        });

        set_btn=(ImageButton)rootView.findViewById(R.id.set_btn);
        set_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentK = new Intent(getActivity(), AccountSetActivity.class);
                startActivity(intentK);
            }
        });

        to_unlock_btn=(ImageButton)rootView.findViewById(R.id.to_unlock_btn);
        to_unlock_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View V){
                if(i){ //잠금상태
                    //Database 추가
                    conditionRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            data_door=snapshot.getValue(String.class);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    //지문인식 시작
                    BiometricManager biometricManager = BiometricManager.from(requireActivity().getApplicationContext());
                    switch (biometricManager.canAuthenticate()) {
                        case BiometricManager.BIOMETRIC_SUCCESS:        //지문인식 가능 -> 사용
                            break;

                        case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                            Toast.makeText(requireActivity().getApplicationContext(), "지문인식 센서가 없습니다", Toast.LENGTH_SHORT).show();
                            to_unlock_btn.setVisibility(View.GONE);
                            break;

                        case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                            Toast.makeText(requireActivity().getApplicationContext(), "지문인식센서를 사용할 수 없어요", Toast.LENGTH_SHORT).show();
                            to_unlock_btn.setVisibility(View.GONE);
                            break;

                        case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                            Toast.makeText(requireActivity().getApplicationContext(), "지문인식을 저장할 수 없어요, 설정에 가서 확인해주세요", Toast.LENGTH_SHORT).show();
                            break;
                        case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                            break;
                        case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                            break;
                        case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                            break;
                    }
                    Executor executors = ContextCompat.getMainExecutor(requireActivity().getApplicationContext());

                    final BiometricPrompt biometricPrompt = new BiometricPrompt(SettingActivity.this, executors, new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                        }

                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {   //지문인식을 성공하면
                            super.onAuthenticationSucceeded(result);
                            fp = "success";     //파이어베이스 RealTime값 fp="success"로 설정 -> 라즈베리파이에서 사용 : 도어락 열림
                            conditionRef.setValue(fp);

                            //10초 뒤
                            try {
                                sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            fp = "fail";        //파이어베이스 RealTime값 fp="fail"로 설정 -> 라즈베리파이에서 사용 : 도어락 잠김
                            conditionRef.setValue(fp);
                        }
                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                        }
                    });  //결과 알려줌

                    final BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder() //지문인식 알림창
                            .setTitle("로그인")
                            .setDescription("지문을 인식해주세요.")
                            .setNegativeButtonText("취소")
                            .build();
                    to_unlock_btn.setOnClickListener(new View.OnClickListener() {   //도어락 열기 버튼 클릭시
                        @Override
                        public void onClick(View v) {
                            biometricPrompt.authenticate(promptInfo);   // 지문인식 알림창 출력
                        }
                    });
                    i=false;
                }
                else {   //열림상태
                    i = true;
                }
            }
        });
        return rootView;
    }
}