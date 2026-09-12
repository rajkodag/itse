NEXDESK DATABASE SETUP

1. CONNECT SYS/<password>@localhost:1521/XEPDB1 AS SYSDBA
2. Create ITHELPDESK if needed.
3. CONNECT ITHELPDESK/ITHELPDESK123@localhost:1521/XEPDB1
4. Run: @sql/nexdesk_oracle.sql
5. Verify NX_USERS, NX_TECHNICIANS and NX_TICKETS exist.
6. Run the Java project with .\run.bat
