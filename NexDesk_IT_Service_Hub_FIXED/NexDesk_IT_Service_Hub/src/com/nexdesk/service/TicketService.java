package com.nexdesk.service;

import com.nexdesk.db.DatabaseManager;
import com.nexdesk.model.Ticket;

import java.sql.*;
import java.util.*;

public class TicketService {
    private static final String BASE =
            "SELECT t.TICKET_ID,t.TITLE,t.DESCRIPTION,t.CATEGORY,t.PRIORITY,t.STATUS," +
            "t.CREATED_BY,t.ASSIGNED_TO,t.CREATED_AT,t.UPDATED_AT," +
            "u.FULL_NAME AS CREATOR_NAME,u.EMAIL AS CREATOR_EMAIL," +
            "tech.TECH_NAME AS TECH_NAME,tech.SPECIALIZATION AS TECH_SPECIALIZATION " +
            "FROM NX_TICKETS t " +
            "JOIN NX_USERS u ON u.USER_ID=t.CREATED_BY " +
            "LEFT JOIN NX_TECHNICIANS tech ON tech.TECH_ID=t.ASSIGNED_TO";

    public int create(Ticket t) {
        String sql =
                "INSERT INTO NX_TICKETS " +
                "(TICKET_ID,TITLE,DESCRIPTION,CATEGORY,PRIORITY,STATUS,CREATED_BY) " +
                "VALUES(NX_TICKET_SEQ.NEXTVAL,?,?,?,?, 'OPEN',?) " +
                "RETURNING TICKET_ID INTO ?";

        try (Connection c = DatabaseManager.getConnection();
             CallableStatement p = c.prepareCall(sql)) {
            p.setString(1, t.getTitle());
            p.setString(2, t.getDescription());
            p.setString(3, t.getCategory());
            p.setString(4, t.getPriority());
            p.setInt(5, t.getCreatedBy());
            p.registerOutParameter(6, Types.NUMERIC);
            p.executeUpdate();
            return p.getInt(6);
        } catch (SQLException e) {
            throw new RuntimeException("Ticket creation failed: " + e.getMessage(), e);
        }
    }

    public List<Ticket> all() {
        return query(BASE + " ORDER BY t.TICKET_ID DESC", null);
    }

    public List<Ticket> byUser(int id) {
        return query(BASE + " WHERE t.CREATED_BY=? ORDER BY t.TICKET_ID DESC", id);
    }

    public Ticket find(int id) {
        List<Ticket> x = query(BASE + " WHERE t.TICKET_ID=?", id);
        return x.isEmpty() ? null : x.get(0);
    }

    public void assign(int ticketId, int techId) {
        update(
                "UPDATE NX_TICKETS SET ASSIGNED_TO=?,STATUS='ASSIGNED'," +
                "UPDATED_AT=CURRENT_TIMESTAMP WHERE TICKET_ID=?",
                techId, ticketId
        );
    }

    public void status(int ticketId, String status) {
        if (status == null ||
                !List.of("OPEN","ASSIGNED","IN_PROGRESS","RESOLVED","CLOSED")
                        .contains(status.toUpperCase())) {
            throw new IllegalArgumentException("Invalid status.");
        }

        update(
                "UPDATE NX_TICKETS SET STATUS=?,UPDATED_AT=CURRENT_TIMESTAMP " +
                "WHERE TICKET_ID=?",
                status.toUpperCase(), ticketId
        );
    }

    private void update(String sql, Object a, Object b) {
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            if (a instanceof Integer) p.setInt(1, (Integer) a);
            else p.setString(1, String.valueOf(a));
            if (b instanceof Integer) p.setInt(2, (Integer) b);
            else p.setString(2, String.valueOf(b));

            if (p.executeUpdate() == 0) {
                throw new IllegalArgumentException("Ticket not found.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database update failed: " + e.getMessage(), e);
        }
    }

    private List<Ticket> query(String sql, Integer param) {
        List<Ticket> list = new ArrayList<>();
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            if (param != null) p.setInt(1, param);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) list.add(map(r));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Ticket query failed: " + e.getMessage(), e);
        }
    }

    private Ticket map(ResultSet r) throws SQLException {
        Ticket t = new Ticket();
        t.setId(r.getInt("TICKET_ID"));
        t.setTitle(r.getString("TITLE"));
        t.setDescription(r.getString("DESCRIPTION"));
        t.setCategory(r.getString("CATEGORY"));
        t.setPriority(r.getString("PRIORITY"));
        t.setStatus(r.getString("STATUS"));
        t.setCreatedBy(r.getInt("CREATED_BY"));

        int assigned = r.getInt("ASSIGNED_TO");
        t.setAssignedTo(r.wasNull() ? null : assigned);

        t.setCreatorName(r.getString("CREATOR_NAME"));
        t.setCreatorEmail(r.getString("CREATOR_EMAIL"));
        t.setTechnicianName(r.getString("TECH_NAME"));
        t.setTechnicianSpecialization(r.getString("TECH_SPECIALIZATION"));
        t.setCreatedAt(r.getTimestamp("CREATED_AT"));
        t.setUpdatedAt(r.getTimestamp("UPDATED_AT"));
        return t;
    }
}
