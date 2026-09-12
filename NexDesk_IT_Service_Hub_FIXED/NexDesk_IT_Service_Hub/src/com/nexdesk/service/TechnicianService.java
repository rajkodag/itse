package com.nexdesk.service;
import com.nexdesk.db.DatabaseManager; import com.nexdesk.model.Technician; import java.sql.*; import java.util.*;
public class TechnicianService{
    public List<Technician> all(){
        List<Technician> list=new ArrayList<>();String sql="SELECT TECH_ID,TECH_NAME,SPECIALIZATION,EMAIL FROM NX_TECHNICIANS WHERE ACTIVE=1 ORDER BY TECH_NAME";
        try(Connection c=DatabaseManager.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){
            while(r.next())list.add(new Technician(r.getInt("TECH_ID"),r.getString("TECH_NAME"),r.getString("SPECIALIZATION"),r.getString("EMAIL")));return list;
        }catch(SQLException e){throw new RuntimeException(e);}
    }
}
