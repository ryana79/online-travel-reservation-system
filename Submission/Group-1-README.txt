Online Travel Reservation System
Group 1 - CS336 / Principles of Information & Data Management - Final Submission

Required setup:
1. Install MySQL Server and MySQL Connector/J.
2. Import project.sql into MySQL:
   mysql -u root -p < project.sql
3. Open ProjectFrame.java and update DB_USER and DB_PASSWORD if your local MySQL account is not root with a blank password.
4. Either run the provided project.jar (the connector jar must sit next to it) or compile and run with the connector on the classpath.

Compile/run from source:
   javac -cp ".:mysql-connector-j-8.4.0.jar" ProjectFrame.java
   java  -cp ".:mysql-connector-j-8.4.0.jar" ProjectFrame

Run the prebuilt jar:
   java -jar project.jar

Demo credentials:

Admin:
   Email:    admin@example.com
   Password: admin123

Customer Representative:
   Email:    rep@example.com
   Password: rep123

Customer:
   Email:    customer@example.com
   Password: customer123

Other sample customers:
   ava@example.com   / ava123
   noah@example.com  / noah123

Where to find each checklist item in the UI:

Customer (login as Customer):
- "Search and Book" tab:
    * Trip Type: one-way / round-trip
    * Depart Date and Return Date fields
    * "Flexible (+/- 3 days)" checkbox for flexible date search
    * Sort By: price, take-off time, landing time, duration
    * Airline filter and Max Price filter
    * "Book Selected Outbound" / "Book Selected Return" / "Join Waitlist"
- "My Reservations" tab: split into Upcoming and Past sub-tabs.
    * Cancel is only allowed for business or first class on Upcoming.
- "Waiting List" tab: shows alert_sent flag (set automatically when a seat opens).
- "Questions" tab: browse, keyword search, and post questions.

Customer Representative (login as Employee):
- "Flights", "Airports", "Aircraft": full add/edit/delete.
- "Customers": full add/edit/delete.
- "Book / Edit on Behalf" tab: search/book any flight on behalf of a customer (enter the customer's email).
- "Reservations" tab: filter by customer name/email, airline, or flight #; edit class/date/meal; cancel.
- "Waiting Lists" tab: filter by airline, flight #, and flight date.
- "Flights by Airport" tab: lists departing AND arriving flights for any airport code.
- "Questions" tab: reply to customer questions.

Admin (login as Employee with admin role):
- "Employees": add/edit/delete customer reps and admins.
- "Customers": add/edit/delete customers (also satisfies "Add/Edit/Delete a customer representative or customer").
- "Reports" tab:
    * Sales Month input -> "Sales for Month" produces sales for the chosen YYYY-MM.
    * Revenue by Flight, Revenue by Airline (filter input), Revenue by Customer.
    * Best Customer (most total revenue).
    * Most Active Flights.
    * Reservations filter by Flight # and/or Customer Name.
- "Flights" tab: read-only flight reference for admins.

Waiting-list alert:
- When a customer or representative cancels a reservation, the system finds the
  earliest waitlisted customer for the same airline + flight + flight date whose
  alert_sent flag is FALSE, marks the flag TRUE, and shows a confirmation popup
  identifying the customer who has been notified.

Important notes:
- Passwords are stored as plain text because the course project focuses on database functionality, not production authentication security.
- The included project.sql is a full schema + data dump and can be imported directly.
- If your MySQL server uses a different user, password, host, or port, update DB_URL, DB_USER, and DB_PASSWORD at the top of ProjectFrame.java.
