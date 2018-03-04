package com.example.user.chatroom;

import java.util.Date;

/**
 * Created by user on 2018/3/3.
 */

class UserData {

    private String username;
    private String email;
    private String color;

    public UserData(){}

    public UserData(String userName, String email, String color){
        this.username = userName;
        this.email = email;
        this.color = color;
    }

    public void setUsername(String inputUserName){
        this.username = inputUserName;
    }
    public void setEmail(String inputemail){
        this.email = inputemail;
    }
    public void setColor(String inputcolor){ this.color = inputcolor; }

    public String getUsername(){ return username; }
    public String getEmail(){ return email; }
    public String getColor(){ return color; }
}
