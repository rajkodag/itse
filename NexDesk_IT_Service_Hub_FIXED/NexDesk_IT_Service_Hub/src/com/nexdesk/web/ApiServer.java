package com.nexdesk.web;

import com.nexdesk.db.DatabaseManager;
import com.nexdesk.model.*;
import com.nexdesk.service.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;

public class ApiServer {

    private final UserService users = new UserService();
    private final TicketService tickets = new TicketService();
    private final TechnicianService techs = new TechnicianService();

    public void start() throws IOException {

        // Test Oracle connection before starting the server
        try (var connection = DatabaseManager.getConnection()) {
            System.out.println(
                "Oracle connected: " +
                connection.getMetaData().getURL()
            );
        } catch (SQLException ex) {
            throw new IOException(
                "Oracle connection failed: " + ex.getMessage(),
                ex
            );
        }

        HttpServer server = HttpServer.create(
            new InetSocketAddress(8080), 0
        );

        server.createContext("/", this::handle);

        server.setExecutor(
            Executors.newFixedThreadPool(12)
        );

        server.start();

        System.out.println("==============================================");
        System.out.println("        NEXDESK - IT SERVICE HUB");
        System.out.println("==============================================");
        System.out.println("NexDesk running at http://localhost:8080");
    }

    private void handle(HttpExchange exchange) throws IOException {

        try {

            String path = exchange.getRequestURI().getPath();

            // Frontend files
            if (path.equals("/") || path.equals("/index.html")) {
                file(exchange, "/index.html", "text/html");
                return;
            }

            if (path.equals("/app.js")) {
                file(exchange, "/app.js", "application/javascript");
                return;
            }

            if (path.equals("/style.css")) {
                file(exchange, "/style.css", "text/css");
                return;
            }

            // API
            if (path.startsWith("/api/")) {
                api(exchange, path);
                return;
            }

            send(
                exchange,
                404,
                "{\"error\":\"Not found\"}"
            );

        } catch (Exception ex) {

            ex.printStackTrace();

            send(
                exchange,
                500,
                "{\"error\":\"" +
                    Json.esc(root(ex)) +
                    "\"}"
            );
        }
    }

    private void api(
            HttpExchange exchange,
            String path) throws IOException {

        String method = exchange.getRequestMethod();

        Map<String, String> query =
            parse(exchange.getRequestURI().getRawQuery());

        /*
         * LOGIN
         */
        if (path.equals("/api/login")
                && method.equalsIgnoreCase("POST")) {

            String email = query.get("email");
            String password = query.get("password");

            if (email == null || password == null) {

                send(
                    exchange,
                    400,
                    "{\"error\":\"Email and password are required.\"}"
                );

                return;
            }

            User user = users.login(email, password);

            if (user == null) {

                send(
                    exchange,
                    401,
                    "{\"error\":\"Invalid email or password.\"}"
                );

                return;
            }

            send(
                exchange,
                200,
                user(user)
            );

            return;
        }

        /*
         * SIGN UP
         */
        if (path.equals("/api/signup")
                && method.equalsIgnoreCase("POST")) {

            String name = query.get("name");
            String email = query.get("email");
            String password = query.get("password");

            if (name == null ||
                email == null ||
                password == null) {

                send(
                    exchange,
                    400,
                    "{\"error\":\"Name, email and password are required.\"}"
                );

                return;
            }

            try {

                User newUser =
                    users.signup(name, email, password);

                send(
                    exchange,
                    201,
                    user(newUser)
                );

            } catch (IllegalArgumentException ex) {

                send(
                    exchange,
                    400,
                    "{\"error\":\"" +
                        Json.esc(ex.getMessage()) +
                        "\"}"
                );
            }

            return;
        }

        /*
         * GET ALL TICKETS
         *
         * /api/tickets
         *
         * OR
         *
         * /api/tickets?userId=2
         */
        if (path.equals("/api/tickets")
                && method.equalsIgnoreCase("GET")) {

            List<Ticket> result;

            String userId = query.get("userId");

            if (userId == null || userId.isBlank()) {

                result = tickets.all();

            } else {

                result =
                    tickets.byUser(
                        Integer.parseInt(userId)
                    );
            }

            send(
                exchange,
                200,
                tickets(result)
            );

            return;
        }

        /*
         * CREATE TICKET
         */
        if (path.equals("/api/tickets")
                && method.equalsIgnoreCase("POST")) {

            String title = query.get("title");
            String description = query.get("description");
            String category = query.get("category");
            String priority = query.get("priority");
            String userId = query.get("userId");

            if (title == null ||
                title.isBlank() ||
                userId == null) {

                send(
                    exchange,
                    400,
                    "{\"error\":\"Title and userId are required.\"}"
                );

                return;
            }

            Ticket ticket = new Ticket();

            ticket.setTitle(title);
            ticket.setDescription(
                description == null ? "" : description
            );
            ticket.setCategory(
                category == null ? "General" : category
            );
            ticket.setPriority(
                priority == null ? "MEDIUM" : priority
            );
            ticket.setCreatedBy(
                Integer.parseInt(userId)
            );

            int ticketId =
                tickets.create(ticket);

            send(
                exchange,
                201,
                "{\"ticketId\":" + ticketId + "}"
            );

            return;
        }

        /*
         * SEARCH SINGLE TICKET
         *
         * /api/ticket?id=1
         */
        if (path.equals("/api/ticket")
                && method.equalsIgnoreCase("GET")) {

            String id = query.get("id");

            if (id == null) {

                send(
                    exchange,
                    400,
                    "{\"error\":\"Ticket ID is required.\"}"
                );

                return;
            }

            Ticket ticket =
                tickets.find(
                    Integer.parseInt(id)
                );

            if (ticket == null) {

                send(
                    exchange,
                    404,
                    "{\"error\":\"Ticket not found.\"}"
                );

                return;
            }

            send(
                exchange,
                200,
                ticket(ticket)
            );

            return;
        }

        /*
         * GET TECHNICIANS
         */
        if (path.equals("/api/technicians")
                && method.equalsIgnoreCase("GET")) {

            StringBuilder result =
                new StringBuilder("[");

            boolean first = true;

            for (Technician technician : techs.all()) {

                if (!first) {
                    result.append(",");
                }

                first = false;

                result.append("{")
                    .append(
                        Json.num(
                            "id",
                            technician.getId()
                        )
                    )
                    .append(",")
                    .append(
                        Json.str(
                            "name",
                            technician.getName()
                        )
                    )
                    .append(",")
                    .append(
                        Json.str(
                            "specialization",
                            technician.getSpecialization()
                        )
                    )
                    .append(",")
                    .append(
                        Json.str(
                            "email",
                            technician.getEmail()
                        )
                    )
                    .append("}");
            }

            result.append("]");

            send(
                exchange,
                200,
                result.toString()
            );

            return;
        }

        /*
         * ASSIGN TICKET
         *
         * /api/assign?ticketId=1&techId=1
         */
        if (path.equals("/api/assign")
                && method.equalsIgnoreCase("POST")) {

            String ticketId =
                query.get("ticketId");

            String techId =
                query.get("techId");

            if (ticketId == null || techId == null) {

                send(
                    exchange,
                    400,
                    "{\"error\":\"ticketId and techId are required.\"}"
                );

                return;
            }

            tickets.assign(
                Integer.parseInt(ticketId),
                Integer.parseInt(techId)
            );

            send(
                exchange,
                200,
                "{\"message\":\"Ticket assigned successfully.\"}"
            );

            return;
        }

        /*
         * UPDATE STATUS
         *
         * /api/status?ticketId=1&status=RESOLVED
         */
        if (path.equals("/api/status")
                && method.equalsIgnoreCase("POST")) {

            String ticketId =
                query.get("ticketId");

            String status =
                query.get("status");

            if (ticketId == null || status == null) {

                send(
                    exchange,
                    400,
                    "{\"error\":\"ticketId and status are required.\"}"
                );

                return;
            }

            tickets.status(
                Integer.parseInt(ticketId),
                status
            );

            send(
                exchange,
                200,
                "{\"message\":\"Status updated successfully.\"}"
            );

            return;
        }

        /*
         * UNKNOWN API
         */
        send(
            exchange,
            404,
            "{\"error\":\"API endpoint not found\"}"
        );
    }

    /*
     * Convert User to JSON
     */
    private static String user(User u) {

        return "{"
            + Json.num("id", u.getId())
            + ","
            + Json.str("name", u.getName())
            + ","
            + Json.str("email", u.getEmail())
            + ","
            + Json.str("role", u.getRole())
            + "}";
    }

    /*
     * Convert Ticket to JSON
     */
    private static String ticket(Ticket t) {

        return "{"
            + Json.num("id", t.getId())
            + ","
            + Json.str("title", t.getTitle())
            + ","
            + Json.str("description", t.getDescription())
            + ","
            + Json.str("category", t.getCategory())
            + ","
            + Json.str("priority", t.getPriority())
            + ","
            + Json.str("status", t.getStatus())
            + ","
            + Json.num("createdBy", t.getCreatedBy())
            + ","
            + Json.str(
                "creatorName",
                t.getCreatorName()
            )
            + ","
            + Json.str(
                "creatorEmail",
                t.getCreatorEmail()
            )
            + ","
            + Json.nullableNum(
                "assignedTo",
                t.getAssignedTo()
            )
            + ","
            + Json.str(
                "technicianName",
                t.getTechnicianName()
            )
            + ","
            + Json.str(
                "technicianSpecialization",
                t.getTechnicianSpecialization()
            )
            + ","
            + Json.str(
                "createdAt",
                String.valueOf(t.getCreatedAt())
            )
            + ","
            + Json.str(
                "updatedAt",
                String.valueOf(t.getUpdatedAt())
            )
            + "}";
    }

    /*
     * Convert Ticket List to JSON
     */
    private static String tickets(List<Ticket> list) {

        StringBuilder result =
            new StringBuilder("[");

        for (int i = 0; i < list.size(); i++) {

            if (i > 0) {
                result.append(",");
            }

            result.append(
                ticket(list.get(i))
            );
        }

        result.append("]");

        return result.toString();
    }

    /*
     * Parse URL parameters
     */
    private static Map<String, String> parse(
            String raw) {

        Map<String, String> result =
            new HashMap<>();

        if (raw == null || raw.isBlank()) {
            return result;
        }

        for (String part : raw.split("&")) {

            int index = part.indexOf("=");

            String key;
            String value;

            if (index < 0) {

                key = part;
                value = "";

            } else {

                key = part.substring(0, index);
                value = part.substring(index + 1);
            }

            result.put(
                URLDecoder.decode(
                    key,
                    StandardCharsets.UTF_8
                ),
                URLDecoder.decode(
                    value,
                    StandardCharsets.UTF_8
                )
            );
        }

        return result;
    }

    /*
     * Serve frontend files
     */
    private static void file(
            HttpExchange exchange,
            String name,
            String contentType)
            throws IOException {

        try (
            InputStream input =
                ApiServer.class.getResourceAsStream(
                    "/web" + name
                )
        ) {

            if (input == null) {

                send(
                    exchange,
                    404,
                    "{\"error\":\"File not found\"}"
                );

                return;
            }

            byte[] data =
                input.readAllBytes();

            exchange.getResponseHeaders()
                .set(
                    "Content-Type",
                    contentType + "; charset=utf-8"
                );

            exchange.sendResponseHeaders(
                200,
                data.length
            );

            try (
                OutputStream output =
                    exchange.getResponseBody()
            ) {

                output.write(data);
            }
        }
    }

    /*
     * Send response
     */
    private static void send(
            HttpExchange exchange,
            int code,
            String body)
            throws IOException {

        byte[] data =
            body.getBytes(
                StandardCharsets.UTF_8
            );

        exchange.getResponseHeaders()
            .set(
                "Content-Type",
                "application/json; charset=utf-8"
            );

        exchange.sendResponseHeaders(
            code,
            data.length
        );

        try (
            OutputStream output =
                exchange.getResponseBody()
        ) {

            output.write(data);
        }
    }

    /*
     * Get root error message
     */
    private static String root(Exception exception) {

        Throwable current = exception;

        while (current.getCause() != null) {
            current = current.getCause();
        }

        String message = current.getMessage();

        return message == null
            ? current.toString()
            : message;
    }
}