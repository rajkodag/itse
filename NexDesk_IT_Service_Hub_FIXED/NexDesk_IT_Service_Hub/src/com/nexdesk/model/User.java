package com.nexdesk.model;
public class User {
    private final int id; private final String name,email,password,role;
    public User(int id,String name,String email,String password,String role){
        this.id=id;this.name=name;this.email=email;this.password=password;this.role=role;
    }
    public int getId(){return id;} public String getName(){return name;} public String getEmail(){return email;}
    public String getPassword(){return password;} public String getRole(){return role;}
}
