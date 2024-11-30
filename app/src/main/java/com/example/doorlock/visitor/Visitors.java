package com.example.doorlock.visitor;

import android.net.Uri;

/*출입 기록 정보 모델 클래스*/
public class Visitors {

    private Uri imgUri; //출입 기록 이미지
    private String mStrName; //출입 기록 시간
    private String mStrFy; //출입 기록의 인물이 등록된 사용자인지 구별하기 위한 변수

    /*Visitors 생성자*/
    public Visitors(Uri imgUri, String mStrName, String mStrFy) {
        this.imgUri = imgUri;
        this.mStrName = mStrName;
        this.mStrFy = mStrFy;
    }

    /*getter setter 설정 --> 출입 기록 불러오기.*/
    //출입 기록 -> 이미지 getter, setter
    public Uri getImgUri() {
        return imgUri;
    }
    public void setImgUri(Uri imgUri) {
        this.imgUri = imgUri;
    }

    //출입 기록 -> 시간 출력 텍스트 getter, setter
    public String getmStrName() {
        return mStrName;
    }
    public void setmStrName(String mStrName) {
        this.mStrName = mStrName;
    }

    //출입 기록 -> 등록된 사용자 여부 구별을 위한 변수 getter, setter
    public String getmStrFy() { return mStrFy; }
    public void setmStrFy(String mStrFy) { this.mStrFy = mStrFy; }
}
