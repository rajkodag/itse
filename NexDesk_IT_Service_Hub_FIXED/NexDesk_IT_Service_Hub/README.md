# NexDesk – IT Service Hub

Java 17 + Oracle XE 21c + JDBC + built-in Java HTTP server.

## Features
- Sign up
- Login
- Admin and user roles
- Create tickets
- Search/filter tickets
- Technician assignment
- Ticket status updates
- Oracle persistence
- `/api/health` connection test

## 1. Create Oracle application user

In SQL*Plus:

```sql
CONNECT SYS/<SYS_PASSWORD>@localhost:1521/XEPDB1 AS SYSDBA

CREATE USER ITHELPDESK IDENTIFIED BY ITHELPDESK123;
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE VIEW TO ITHELPDESK;
ALTER USER ITHELPDESK QUOTA UNLIMITED ON USERS;
```

If ITHELPDESK already exists, do not create it again.

## 2. Create NexDesk tables

Connect as:

```sql
CONNECT ITHELPDESK/ITHELPDESK123@localhost:1521/XEPDB1
```

Then run:

```sql
@sql/nexdesk_oracle.sql
```

Do not type foreign-key constraints separately. The script contains complete CREATE TABLE statements.

Verify:

```sql
SELECT USER FROM DUAL;

SELECT TABLE_NAME
FROM USER_TABLES
WHERE TABLE_NAME LIKE 'NX_%'
ORDER BY TABLE_NAME;

SELECT SEQUENCE_NAME
FROM USER_SEQUENCES
WHERE SEQUENCE_NAME LIKE 'NX_%'
ORDER BY SEQUENCE_NAME;
```

## 3. JDBC

Place Oracle `ojdbc11.jar` in:

```text
lib\ojdbc11.jar
```

## 4. Run

From the project folder:

```powershell
.un.bat
```

Open:

```text
http://localhost:8080
```

Test Oracle:

```text
http://localhost:8080/api/health
```

Expected:

```json
{"status":"OK","database":"Oracle connected"}
```

## Demo login

Admin:

```text
admin@nexdesk.com
admin123
```

User:

```text
demo@nexdesk.com
demo123
```

New users can use **Create an account**.

## If ORA-00942 appears

Run:

```sql
CONNECT ITHELPDESK/ITHELPDESK123@localhost:1521/XEPDB1

SELECT USER FROM DUAL;

SELECT TABLE_NAME
FROM USER_TABLES
WHERE TABLE_NAME='NX_USERS';
```

The result must contain `NX_USERS`.

If it does not, run:

```sql
@sql/nexdesk_oracle.sql
```

again while connected as ITHELPDESK.
