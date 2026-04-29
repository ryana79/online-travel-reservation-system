# Online Travel Reservation System

Java Swing and MySQL travel reservation system for the Principles of Information & Data Management final project.

## Setup

1. Install MySQL Server and MySQL Connector/J.
2. Import the database dump:

   ```sh
   mysql -u root -p < project.sql
   ```

3. If your local MySQL account is not `root` with a blank password, update `DB_USER` and `DB_PASSWORD` near the top of `ProjectFrame.java`.
4. Compile and run with the MySQL Connector/J jar on the classpath:

   ```sh
   javac -cp ".:mysql-connector-j-8.4.0.jar" ProjectFrame.java
   java -cp ".:mysql-connector-j-8.4.0.jar" ProjectFrame
   ```

You can also run:

```sh
sh build.sh mysql-connector-j-8.4.0.jar
```

## Demo Credentials

Admin:

- Email: `admin@example.com`
- Password: `admin123`

Customer representative:

- Email: `rep@example.com`
- Password: `rep123`

Customer:

- Email: `customer@example.com`
- Password: `customer123`

Other sample customers:

- Email: `ava@example.com`, password: `ava123`
- Email: `noah@example.com`, password: `noah123`

## Implemented Features

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

## Notes

- Passwords are stored as plain text because the course project focuses on database functionality, not production authentication security.
- The included `project.sql` file is a full schema and seed data dump that can be imported directly.
- See `ER_Diagram.md` for the database diagram.
- See `DEMO_VIDEO_INSTRUCTIONS.md` for a suggested demo video outline.
