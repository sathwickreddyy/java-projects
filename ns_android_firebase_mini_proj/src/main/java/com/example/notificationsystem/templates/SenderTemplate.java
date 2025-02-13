package com.example.notificationsystem.templates;

public class SenderTemplate {
    private String name,email,dept,imageUri,id;

    public SenderTemplate(String name, String email, String dept,String id) {
        this.name = name;
        this.email = email;
        this.dept = dept;
        this.id=id;
    }

    public SenderTemplate(){

    }

    public SenderTemplate(String name, String email, String dept) {
        this.name = name;
        this.email = email;
        this.dept = dept;
    }

    public SenderTemplate(String name, String email, String dept, String id, String imageUri) {
        this.name = name;
        this.email = email;
        this.dept = dept;
        this.id=id;
        this.imageUri = imageUri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
