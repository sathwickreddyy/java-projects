package com.example.notificationsystem.templates;

public class User {
    private String uname;
    private String uemail;
    private String course;
    private String id;
    private String DOB;
    private String section;

    public User(String uname, String uemail, String id, String DOB,String course,String section) {
        this.uname = uname;
        this.uemail = uemail;
        this.id = id;
        this.DOB = DOB;
        this.course=course;
        this.section=section;
    }

    public String getUname() {
        return uname;
    }

    public String getSection(){
        return section;
    }

    public String getUemail() {
        return uemail;
    }

    public String getId() {
        return id;
    }

    public String getDOB() {
        return DOB;
    }

    public String getCourse(){
        return course;
    }
}
