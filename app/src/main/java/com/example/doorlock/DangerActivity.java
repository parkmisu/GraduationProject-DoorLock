package com.example.doorlock;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.example.doorlock.visitor.VisitorListAdapter;
import com.example.doorlock.visitor.Visitors;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class DangerActivity extends Fragment {
    //화재 감지
    private TextView fire_time;         //화재 감지 알람 시간 text
    private TextView blank;             //이미지 출력 위치를 맞추기 위한 blank text
    private ImageButton fire_alarm;     //화재 알람 이미지
    private ImageButton fireok_btn;     //화재 알람 삭제 이미지
    private ImageButton GridDeleteBtn;  // 출입 기록 전체 삭제 버튼
    //Firebase 선언
    private DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();  //Firebase getReference
    private DatabaseReference conditionRef = mRootRef.child("message"); //화재 감지 여부
    private DatabaseReference conditionRef2 = mRootRef.child("fire_time"); //화재 시간 가져오기
    private DatabaseReference mDatabaseRef; //실시간 데이터베이스

    //출입 기록 리스트 초기화
    private DatabaseReference conditionRef3 = mRootRef.child("Visit"); //DataBase '출입 기록' 변수 불러옴
    List<Visitors> visitorList = new ArrayList<>(); //리스트 초기화
    private VisitorListAdapter gridAdapter;     // grid list view



    ArrayList<String> del_list=new ArrayList<>(); // '전체 삭제' 기능을 위해 del_list에 파일 이름 저장

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.danger_page, container, false);

        /*danger_page.xml의 출력 필드 변수 설정*/
        GridView gridView = rootView.findViewById(R.id.gridview); //출입 기록 GridView
        fire_time = rootView.findViewById(R.id.fire_time); //화재 감지 시간 text
        blank=rootView.findViewById(R.id.blank); //blank text
        fireok_btn = rootView.findViewById(R.id.fireok_btn); //화재 알람 삭제 버튼
        fire_alarm = rootView.findViewById(R.id.fire_image); //화재 알람 이미지
        GridDeleteBtn = rootView.findViewById(R.id.grid_delete_btn); //출입 기록 삭제 버튼
        /*출입 기록 Adapter setting*/
        gridAdapter = new VisitorListAdapter(getContext());
        gridView.setAdapter(gridAdapter);

        /*--------------------화재 알람--------------------*/
        //화재 감지 시, 푸시 알람을 위한 변수 설정
        Bitmap mLargeIconForNoti = BitmapFactory.decodeResource(getResources(), R.drawable.fire_alarm);
        PendingIntent mPendingIntent = PendingIntent.getActivity(getActivity(), 0,
                new Intent(getActivity().getApplicationContext(), MainActivity.class),PendingIntent.FLAG_ONE_SHOT);

        /*Database의 화재감지 변수 불러오기*/
        conditionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                if (value.equals("Fire Detected")) {  //화재 감지 시
                    //Database의 화재 발생 시간 불러오기
                    conditionRef2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //화재 감지 시간 출력
                            String value2 = snapshot.getValue(String.class);
                            fire_time.setText(value2);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                    fire_time.setVisibility(View.VISIBLE); //화재 감지 시간 출력
                    blank.setVisibility(View.GONE); //blank text 숨김
                    fire_alarm.setVisibility(View.VISIBLE); //화재 알람 출력
                    fireok_btn.setVisibility(View.VISIBLE); //화재 알람 삭제 버튼 출력

                    /*화재 알람 삭제 버튼 클릭 시*/
                    fireok_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //화재 알람 삭제 확인 창 출력
                            AlertDialog.Builder fbuilder = new AlertDialog.Builder(getContext());
                            fbuilder.setTitle("화재 알림");
                            fbuilder.setMessage("삭제하시겠습니까?");
                            //예 눌렀을때의 이벤트 처리
                            fbuilder.setNegativeButton("예", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    conditionRef.setValue("safe"); //Database의 화재 감지 변수 "safe"로 변경
                                    fire_time.setText(""); //시간 비우기
                                    fire_time.setVisibility(View.INVISIBLE); //화재 감지 시간 숨김
                                    fire_alarm.setVisibility(View.INVISIBLE); //화재 알람 이미지 숨김
                                    fireok_btn.setVisibility(View.INVISIBLE); //화재 알람 삭제 버튼
                                    blank.setVisibility(View.VISIBLE); //blank text 출력
                                }
                            });
                            //아니요 눌렀을때의 이벤트 처리
                            fbuilder.setPositiveButton("아니요", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            fbuilder.show();
                        }
                    });

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                        NotificationChannel notificationChannel = new NotificationChannel("notification", "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
                        notificationChannel.setDescription("channel description");
                        notificationChannel.enableLights(true);
                        notificationChannel.enableVibration(true);
                        notificationChannel.setLockscreenVisibility(Notification.DEFAULT_SOUND);
                        mNotificationManager.createNotificationChannel(notificationChannel);
                    }
                        /*화재 감지 시, 푸시 알람*/
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity(), NOTIFICATION_SERVICE)
                                .setSmallIcon(R.drawable.fire_alarm)                        //알림 시 보여지는 화재 이미지 아이콘
                                .setContentTitle("주의")                                    //알림 제목 텍스트
                                .setContentText("화재가 감지되었습니다!")                      //알림 본문 텍스트
                                .setDefaults(Notification.DEFAULT_SOUND)                    //소리로 푸시 알림, 진동 = DEFAULT_VIBRATE
                                .setLargeIcon(mLargeIconForNoti)
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)           //헤드업 알림
                                .setAutoCancel(true)                                        //사용자가 알림창을 터치하였을 때 알림창 사라짐
                                .setContentIntent(mPendingIntent);                          //창 넘어감
                        //생성한 notification 등록
                        NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
                        mNotificationManager.notify(0, mBuilder.build());
                    } else {
                    }
            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
        /*-------화재 알림 끝-----*/

        /*------- 출입기록 -----*/
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference pathReference = storageReference.child("Visitors");
        if (pathReference == null) {
            Toast.makeText(getActivity(), "사진 가져오기 실패", Toast.LENGTH_SHORT).show();
        } else {
            conditionRef3.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        //데이터 가져오기
                        String PngName = dataSnapshot.child("time").getValue(String.class);         //사진 이름
                        String fy = dataSnapshot.child("fy").getValue(String.class);                 //등록된 사용자 여부
                        //출입 기록 이미지 불러오기
                        StorageReference submitProfile = storageReference.child("Visitors/" + PngName + ".jpg");
                        submitProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                visitorList.clear(); //list clear
                                visitorList.add(new Visitors(uri, PngName, fy));
                                gridAdapter.setListItem(visitorList);               // 리스트 어댑터 셋팅
                                gridAdapter.setSortASC();                           //출입 기록 시간에 따라 내림차순 정렬
                                gridAdapter.notifyDataSetChanged();                 // 리스트뷰 새로고침
                                del_list.add(PngName);                              // 배열에 시간값 저장
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
        /*------출입기록 끝-------*/

        /*출입 기록 전체 삭제 버튼 클릭 시*/
        GridDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //출입 기록 삭제 확인 창 출력
                AlertDialog.Builder fbuilder2 = new AlertDialog.Builder(getContext());
                fbuilder2.setTitle("출입 기록");
                fbuilder2.setMessage("전체 삭제하시겠습니까?");
                //YES 눌렀을때 출입 기록 전체 삭제 진행
                fbuilder2.setNegativeButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        visitorList.clear();
                        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Visit"); //Database에 출입 기록 목록이 저장되어 있는 변수 불러옴
                        mDatabaseRef.removeValue(); //출입 기록 목록 전체 삭제

                        // 배열로 이름값 불러와서 strorage 경로 설정하고 삭제
                        for(int k=0; k < del_list.size(); k++){
                            //사진 삭제
                            storageReference.child("Visitors/"+del_list.get(k)+".jpg").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });
                        }
                        del_list.clear(); // 배열 비우기
                    }
                    //아니요 눌렀을때 전체 삭제 취소
                });fbuilder2.setPositiveButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });fbuilder2.show();
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}