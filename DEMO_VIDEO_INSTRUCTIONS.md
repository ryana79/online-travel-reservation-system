# Demo Video Instructions

Use this outline to record a clear project demo. Aim for 6-10 minutes unless the professor gave a different time limit.

## Before Recording

1. Import `project.sql` into MySQL.
2. Download MySQL Connector/J and place the jar in the project folder.
3. Compile and launch the app:

   ```sh
   javac -cp ".:mysql-connector-j-8.4.0.jar" ProjectFrame.java
   java -cp ".:mysql-connector-j-8.4.0.jar" ProjectFrame
   ```

4. Keep `README.md`, `ER_Diagram.md`, and the running application open.

## Suggested Video Flow

1. Introduce the project as an Online Travel Reservation System built with Java Swing and MySQL.
2. Briefly show `ER_Diagram.md` and explain the main entities: customers, employees, airlines, aircraft, airports, flights, tickets, ticket-flight legs, and waiting lists.
3. Show the MySQL import worked by opening the application and logging in with seeded credentials.
4. Customer demo:
   - Log in as `customer@example.com` / `customer123`.
   - Search for flights by departure and arrival airport.
   - Create a reservation and point out seat assignment, class, fare, and booking fee.
   - View reservations.
   - Join or remove from a waiting list.
   - Browse/search Q&A and post a question.
5. Customer representative demo:
   - Log in as `rep@example.com` / `rep123`.
   - Show flight, airport, aircraft, customer, reservation, waiting list, and Q&A management screens.
   - Add or update one small record if time allows.
6. Admin demo:
   - Log in as `admin@example.com` / `admin123`.
   - Show employee management.
   - Show reservation lookup by flight/customer.
   - Show monthly sales, revenue by flight/customer, best customer, and most active flight reports.
7. Close by summarizing how the app satisfies the database requirements: relational schema, constraints, foreign keys, seed data, CRUD operations, transactions/reporting screens, and role-based workflows.

## Recording Tips

- Narrate what each screen proves rather than reading every field.
- Use sample data from the seed database so the demo is repeatable.
- If something is slow, pause briefly instead of clicking repeatedly.
- Do not spend much time on setup in the video; mention that setup instructions are in `README.md`.
- If the professor requires every team member to speak, split sections by role: schema overview, customer workflow, representative workflow, and admin/reporting workflow.
