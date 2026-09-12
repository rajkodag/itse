package com.nexdesk.model;
import java.sql.Timestamp;
public class Ticket {
    private int id,createdBy; private String title,description,category,priority,status,creatorName,creatorEmail,technicianName,technicianSpecialization;
    private Integer assignedTo; private Timestamp createdAt,updatedAt;
    public int getId(){return id;} public void setId(int x){id=x;}
    public int getCreatedBy(){return createdBy;} public void setCreatedBy(int x){createdBy=x;}
    public String getTitle(){return title;} public void setTitle(String x){title=x;}
    public String getDescription(){return description;} public void setDescription(String x){description=x;}
    public String getCategory(){return category;} public void setCategory(String x){category=x;}
    public String getPriority(){return priority;} public void setPriority(String x){priority=x;}
    public String getStatus(){return status;} public void setStatus(String x){status=x;}
    public String getCreatorName(){return creatorName;} public void setCreatorName(String x){creatorName=x;}
    public String getCreatorEmail(){return creatorEmail;} public void setCreatorEmail(String x){creatorEmail=x;}
    public Integer getAssignedTo(){return assignedTo;} public void setAssignedTo(Integer x){assignedTo=x;}
    public String getTechnicianName(){return technicianName;} public void setTechnicianName(String x){technicianName=x;}
    public String getTechnicianSpecialization(){return technicianSpecialization;} public void setTechnicianSpecialization(String x){technicianSpecialization=x;}
    public Timestamp getCreatedAt(){return createdAt;} public void setCreatedAt(Timestamp x){createdAt=x;}
    public Timestamp getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Timestamp x){updatedAt=x;}
}
