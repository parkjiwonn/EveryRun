package com.example.everyrun.RetrofitData;

import com.google.gson.annotations.SerializedName;

public class CrewData {
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("crew_id")
    private int crew_id;

    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    @SerializedName("banner")
    private String banner;

    @SerializedName("area")
    private int area;

    @SerializedName("member")
    private int member;

    @SerializedName("total")
    private int total;

    @SerializedName("current")
    private int current;

    @SerializedName("reader")
    private String reader;

    public String getReader() {
        return reader;
    }

    public void setReader(String reader) {
        this.reader = reader;
    }

    public int getCrew_id() {
        return crew_id;
    }

    public void setCrew_id(int crew_id) {
        this.crew_id = crew_id;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public int getMember() {
        return member;
    }

    public void setMember(int member) {
        this.member = member;
    }

    // toString()을 Override 해주지 않으면 객체 주소값을 출력함
    @Override
    public String toString() {
        return  "{" +
                "status=" + status +
                ", message=" + message +
                ", title=" + title +
                ", content=" + content +
                ", banner=" + banner +
                ", area=" + area +
                ", member=" + member +
                ", total=" + total +
                ", current=" + current +
                ", crew_id=" + crew_id +
                ", reader=" + reader +

                '}';
    }
}
