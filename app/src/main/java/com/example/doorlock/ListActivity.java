package com.example.doorlock;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    ArrayList<String> arrayList=new ArrayList<>();
    ArrayAdapter<String> adapter;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private ListView listView;

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference conditionRef = mRootRef.child("name");
    DatabaseReference conditionRemove = mRootRef.child("remove");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_page);

        listView=findViewById(R.id.list_View);

        //어뎁터 초기화
        adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,arrayList); //
        //데이터베이스 초기화
        firebaseDatabase=FirebaseDatabase.getInstance();
        //레퍼런스 초기화
        databaseReference=firebaseDatabase.getReference().child("Data");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        //데이터 조회
        getValue();
        //값 불러오기
        Intent intent=getIntent();
        String sName=intent.getStringExtra("ID값");
        String sKey= intent.getStringExtra("키값");

        //아이템 짧게 누를 경우
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String sName = arrayList.get(i);                                            // 선택한 아이템의 위치 불러오기
                Intent intentL = new Intent(getApplicationContext(), PopupActivity.class); // 아이템 클릭시 팝업창으로 해당 이미지 출력
                intentL.putExtra("ID값",sName);                                      // 선택한 아이템의 이름값을 PopupActivity로 전달
                startActivity(intentL);
            }
        });

        //아이템 길게 누를 경우
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?>adapterView, View view, int position, long id){
                String sValue = arrayList.get(position);                                     // 선택한 아이템의 위치 불러오기

                AlertDialog.Builder builder=new AlertDialog.Builder(ListActivity.this);
                builder.setTitle("삭제");
                builder.setMessage("삭제하시겠습니까?");
                builder.setNegativeButton("예", new DialogInterface.OnClickListener() { //삭제 선택
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //쿼리 초기화
                        Query query=databaseReference.orderByChild("value").equalTo(sValue);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot :snapshot.getChildren()){
                                    //데이터 삭제
                                    dataSnapshot.getRef().removeValue();
                                    //삭제할 이미지 이름
                                    StorageReference desertRef = storageRef.child("uploads/"+sValue+".jpg");
                                    conditionRef.setValue(sValue);      //데이터베이스 RealTime의 name값을 삭제 한 이름 값으로 변경 -> 라즈베리에서 사용
                                    conditionRemove.setValue("On");     //데이터베이스 RealTime값 변경 -> 라즈베리에서 사용

                                    //파일 삭제
                                    desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
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
                                Toast.makeText(ListActivity.this, "error:"+error.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                builder.setPositiveButton("아니요", new DialogInterface.OnClickListener() { //삭제 안함 선택
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //닫기
                        dialogInterface.dismiss();
                    }
                });
                //보여주기
                builder.show();
                conditionRemove.setValue("off");
                return true;
            }
        });
    }

    //파이어 베이스에서 데이터 가져오기
    private void getValue(){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //리스트 초기화
                arrayList.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    //데이터 가져오기 - value 이름으로 된 값을 변수에 담는다.
                    String sValue = dataSnapshot.child("value").getValue(String.class);
                    //리스트에 변수를 담는다
                    arrayList.add(sValue);
                }
                //리스트뷰 어뎁터 설정
                listView.setAdapter(adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ListActivity.this,"error:"+error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}
