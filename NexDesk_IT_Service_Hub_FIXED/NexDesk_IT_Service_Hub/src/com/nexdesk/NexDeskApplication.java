package com.nexdesk;
import com.nexdesk.db.DatabaseManager;import com.nexdesk.web.ApiServer;import java.sql.Connection;
public class NexDeskApplication{
 public static void main(String[]args){
  try(Connection ignored=DatabaseManager.getConnection()){
   System.out.println("==============================================");System.out.println("        NEXDESK - IT SERVICE HUB");System.out.println("        Java + Oracle Prototype");System.out.println("==============================================");
   System.out.println("Oracle connected: "+DatabaseManager.getUrl());System.out.println("Open: http://localhost:8080");System.out.println("Admin: admin@nexdesk.com / admin123");System.out.println("User : demo@nexdesk.com / demo123");
   new ApiServer().start();
  }catch(Exception e){System.err.println("Could not start NexDesk.");e.printStackTrace();System.exit(1);}
 }
}
