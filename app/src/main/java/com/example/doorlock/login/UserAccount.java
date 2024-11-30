package com.example.doorlock.login;

/*사용자 계정 정보 모델 클래스*/
public class UserAccount {
    private String idToken; //firebase uid(고유 토큰정보)
    private String emailId; //이메일 아이디
    private String password; //비밀번호

    public UserAccount() {} //파이어베이스 사용을 위해, 빈 생성자 생성

    /*getter setter 설정 --> '계정 설정 및 불러오기'를 위함.*/
    //계정 Uid getter, setter
    public String getIdToken(){return idToken;}
    public void setIdToken(String idToken) {this.idToken=idToken;}

    //계정 이메일 getter, setter
    public String getEmailId(){return emailId;}
    public void setEmailId(String emailId){this.emailId=emailId;}

    //계정 비밀번호 getter, setter
    public String getPassword(){return password;}
    public void setPassword(String password){this.password=password;}
}

