Online Travel Reservation System

This project implements a Java Swing + MySQL travel reservation system for the Principles of Information & Data Management final project.

Required setup:
1. Install MySQL Server and MySQL Connector/J.
2. Import project.sql into MySQL:
   mysql -u root -p < project.sql
3. Open ProjectFrame.java and update DB_USER and DB_PASSWORD if your local MySQL account is not root with a blank password.
4. Compile and run with the MySQL Connector/J jar on the classpath.

Example compile/run commands:
javac -cp ".:mysql-connector-j-8.4.0.jar" ProjectFrame.java
java -cp ".:mysql-connector-j-8.4.0.jar" ProjectFrame

Demo credentials:

Admin:
Email: admin@example.com
Password: admin123

Customer Representative:
Email: rep@example.com
Password: rep123

Customer:
Email: customer@example.com
Password: customer123

Other sample customers:
Email: ava@example.com
Password: ava123

Email: noah@example.com
Password: noah123

Implemented features:
- Customer account creation and login.
- Customer flight search by origin and destination.
- Customer one-way reservation creation with seat assignment and fare calculation.
- Customer reservation viewing and cancellation.
- Customer waiting list join/remove.
- Customer Q&A browsing, keyword search, and question posting.
- Customer representative flight, customer, reservation, and waiting list screens.
- Customer representative airport and aircraft management.
- Customer representative Q&A reply screen.
- Admin employee management.
- Admin reservation listing by flight/customer.
- Admin sales report by month.
- Admin revenue reports by flight and customer.
- Admin best customer and most active flight reports.
- MySQL schema with primary keys, foreign keys, checks, indexes, and seed data.

Important notes:
- Passwords are stored as plain text because the course project focuses on database functionality, not production authentication security.
- The included project.sql file is a full schema + data dump and can be imported directly.
- If your MySQL server uses a different user, password, host, or port, update DB_URL, DB_USER, and DB_PASSWORD at the top of ProjectFrame.java.
