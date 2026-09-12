package com.nexdesk.model;
public class Technician {
    private final int id; private final String name,specialization,email;
    public Technician(int id,String name,String specialization,String email){
        this.id=id;this.name=name;this.specialization=specialization;this.email=email;
    }
    public int getId(){return id;} public String getName(){return name;}
    public String getSpecialization(){return specialization;} public String getEmail(){return email;}
}
