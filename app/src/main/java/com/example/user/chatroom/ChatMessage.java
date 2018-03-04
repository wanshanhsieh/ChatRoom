package com.example.user.chatroom;

import java.util.Date;

/**
 * Created by user on 2018/3/1.
 */

class ChatMessage {

    private String msgText;
    private String msgUser;
    private long msgTime;

    public ChatMessage(){} // for Firebase

    public ChatMessage(String msgText, String msgUser){
        this.msgText = msgText;
        this.msgUser = msgUser;
        // Get current time
        msgTime = new Date().getTime();
    }

    public String getmsgText(){
        return msgText;
    }

    public void setmsgText(String inputMsgText){
        this.msgText = inputMsgText;
    }

    public String getmsgUser(){
        return msgUser;
    }

    public void setmsgUser(String inputMsgUser){
        this.msgUser = inputMsgUser;
    }

    public long getmsgTime(){
        return msgTime;
    }

    public void setmsgTime(){
        this.msgTime = msgTime;
    }
}
