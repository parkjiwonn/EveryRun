package com.example.everyrun.RetrofitData;

import com.google.gson.annotations.SerializedName;

public class UserInfoData {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("user_id")
    private int user_id;

    @SerializedName("user_email")
    private String user_email;

    @SerializedName("user_pwd")
    private String user_pwd;

    @SerializedName("user_name")
    private String user_name;

    @SerializedName("user_photo")
    private String user_photo;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getUser_pwd() {
        return user_pwd;
    }

    public void setUser_pwd(String user_pwd) {
        this.user_pwd = user_pwd;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_photo() {
        return user_photo;
    }

    public void setUser_photo(String user_photo) {
        this.user_photo = user_photo;
    }

    // toString()을 Override 해주지 않으면 객체 주소값을 출력함
    @Override
    public String toString() {
        return  "{" +
                "status=" + status +
                ", message=" + message +
                ", user_id=" + user_id +
                ", user_email=" + user_email +
                ", user_pwd=" + user_pwd +
                ", user_name=" + user_name +
                ", user_photo=" + user_photo +

                '}';
    }
}
