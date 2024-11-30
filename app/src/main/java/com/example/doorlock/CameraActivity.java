package com.example.doorlock;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
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
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener{
    final static int TAKE_PICTURE = 1;
    final static int GET_GALLERY_IMAGE = 2;

    Button takePicture, save, gallery;
    ImageView imageview;
    Uri selectedImageUri;
    EditText fileN;

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference conditionRef = mRootRef.child("name");    //데이터베이스 RealTime의 name값 선언
    DatabaseReference conditionRef2 = mRootRef.child("save"); //데이터베이스 RealTime의 save값 선언
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    String sOldValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_page);

        takePicture = findViewById(R.id.takePicture);
        save = findViewById(R.id.save);
        gallery = findViewById(R.id.gallery);
        imageview = findViewById(R.id.imageView);
        fileN = findViewById(R.id.fileN);

        takePicture.setOnClickListener(this);
        gallery.setOnClickListener(this);
        save.setOnClickListener(this);

        //데이터베이스 초기화
        firebaseDatabase=FirebaseDatabase.getInstance();
        //레퍼런스 초기화
        databaseReference=firebaseDatabase.getReference().child("Data");

        //Database 추가
        conditionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        //카메라 권한 요청
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    ==checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){}
            else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},1 );
            }
        }
    }

    // 권한 요청
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED){
            Log.d("로그", "Permission: " + permissions[0] + " was " + grantResults[0]);
        }
    }

    // 버튼 이벤트 리스너
    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.takePicture: // 카메라 사용
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, TAKE_PICTURE);
                break;
            case R.id.gallery: // 사진첩 사용
                intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, GET_GALLERY_IMAGE);
                break;
            case R.id.save: // 저장 클릭시
                clickUpload(); //파이어베이스에 사진 업로드
                selectedImageUri = null;
                finish();
                Toast.makeText(getApplicationContext(), "사진 저장이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 사진 촬영 완료 후 응답
        if (requestCode == TAKE_PICTURE) {
            if (resultCode == RESULT_OK && data.hasExtra("data")) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                if (bitmap != null)
                    imageview.setImageBitmap(bitmap);

                String imageSaveUri = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "사진 저장", "찍은 사진이 저장되었습니다.");
                selectedImageUri = Uri.parse(imageSaveUri);
                Log.d(TAG, "CameraActivity - onActivityResult() called" + selectedImageUri);
            }
        }
        // 갤러리에서 이미지 가져온 후의 응답
        else if (requestCode == GET_GALLERY_IMAGE) {
            if (resultCode == RESULT_OK && data.getData() != null) {
                selectedImageUri = data.getData();
                Log.d(TAG, "CameraActivity - onActivityResult() called" + selectedImageUri);
                Log.d(TAG, "CameraActivity - onActivityResult() called" + getRealPathFromURI(selectedImageUri));

                Glide.with(this)
                        .load(getRealPathFromURI(selectedImageUri))
                        .into(imageview);
            }
        }
    }

    // 절대 경로로 변경
    public String getRealPathFromURI(Uri contentUri) {

        String[] proj = { MediaStore.Images.Media.DATA };

        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        cursor.moveToNext();
        @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
        Uri uri = Uri.fromFile(new File(path));

        cursor.close();
        return path;
    }

    // 파이어베이스 업로드 함수
    public void clickUpload() {

        FirebaseStorage firebaseStorage= FirebaseStorage.getInstance();

        String name1=fileN.getText().toString();
        conditionRef.setValue(name1);
        String name =fileN.getText().toString();
        String filename= name.toString()+ ".jpg";

        //키 생성
        String sKey = databaseReference.push().getKey();

        //값 넘기기
        Intent intent=new Intent(CameraActivity.this,ListActivity.class);   //사용자 확인 기능에서 등록된 사용자 id를 출력하기 위해서
        intent.putExtra("ID값",name);
        intent.putExtra("키값",sKey);

        //sKey가 null이 아니면 sKey값으로 데이터 저장
        if (sKey !=null) {
            databaseReference.child(sKey).child("value").setValue(name);
            //입력창 초기화
            fileN.setText("");
        }
        else{
            //equalTo-해당값 반환
            //orderByChild-value값으로 정렬
            Query query = databaseReference.orderByChild("value").equalTo(sOldValue);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                        //수정된 입력창의 값으로 데이터 수정
                        dataSnapshot.getRef().child("value").setValue(name);
                        //입력창 초기화
                        fileN.setText("");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        // 폴더 생성 + 이미지 주소
        StorageReference imgRef= firebaseStorage.getReference("uploads/"+filename);

        // 업로드 결과를 받아오기 - uploadTask
        UploadTask uploadTask =imgRef.putFile(selectedImageUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(CameraActivity.this, "success upload", Toast.LENGTH_SHORT).show();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "CameraActivity - onFailure() called");
                    }
                });
        conditionRef2.setValue("On");   //파이어베이스 RealTime의 save 값 변환 -> 라즈베리파이에서 사용
    }
}