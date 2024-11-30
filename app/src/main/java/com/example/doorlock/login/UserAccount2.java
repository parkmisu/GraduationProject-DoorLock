package com.example.doorlock.login;

/*사용자 계정 데이터베이스 정보 모델 클래스*/
public class UserAccount2 {
    //각 사용자에게 Firebase Database 변수 등록
    private String door;
    private String fp;
    private String message;
    private String name;
    private String set_value;

    public UserAccount2() {} //파이어베이스 사용을 위해, 빈 생성자 생성

    /*Firebase Database 변수들 getter setter 설정-->'변수 설정 및 변경'을 위함.*/
    public String getDoor(){return door;}
    public void setDoor(String door) {this.door=door;}

    public String getFP(){return fp;}
    public void setFP(String fp){this.fp=fp;}

    public String getMessage(){return message;}
    public void setMessage(String message){this.message=message;}

    public String getName(){return name;}
    public void setName(String name){this.name=name;}

    public String getSet_value(){return set_value;}
    public void setSet_value(String set_value){this.set_value=set_value;}
}
