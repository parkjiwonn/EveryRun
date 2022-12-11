package com.example.everyrun.RetrofitData;

import com.google.gson.annotations.SerializedName;

public class JoinData {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("join_id")
    private int join_id;

    @SerializedName("user_email")
    private String user_email;

    @SerializedName("crew_id")
    private int crew_id;

    @SerializedName("join_date")
    private String join_date;

    @SerializedName("user_name")
    private String user_name;

    @SerializedName("user_photo")
    private String user_photo;

    @SerializedName("join_num")
    private int join_num;

    public int getJoin_num() {
        return join_num;
    }

    public void setJoin_num(int join_num) {
        this.join_num = join_num;
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

    public int getJoin_id() {
        return join_id;
    }

    public void setJoin_id(int join_id) {
        this.join_id = join_id;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public int getCrew_id() {
        return crew_id;
    }

    public void setCrew_id(int crew_id) {
        this.crew_id = crew_id;
    }

    public String getJoin_date() {
        return join_date;
    }

    public void setJoin_date(String join_date) {
        this.join_date = join_date;
    }


    // toString()을 Override 해주지 않으면 객체 주소값을 출력함
    @Override
    public String toString() {
        return  "{" +
                "status=" + status +
                ", message=" + message +
                ", join_id=" + join_id +
                ", user_email=" + user_email +
                ", crew_id=" + crew_id +
                ", join_date=" + join_date +
                ", user_name=" + user_name +
                ", user_photo=" + user_photo +
                ", join_num=" + join_num +
                '}';
    }
}
