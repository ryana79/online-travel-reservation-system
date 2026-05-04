# Demo Video Shot List - Group 1

The earlier 13MB video predates several features added for the official
ProjectChecklistSp26.pdf items. To get full credit on the demo, the video
should show the items below in roughly this order. Aim to stay under 10
minutes - do not narrate setup, jump straight into the running app.

Open the application before recording and have the customer / rep / admin
demo accounts ready to log into.

## Shot list

### 1. Schema overview (about 30 seconds)
- Open `Group-1-ER_Diagram.pdf` and call out the main entities: airline,
  aircraft, airport, flight, customer, employee, ticket, ticket_flight,
  waiting_list, question.
- Mention primary keys, foreign keys, check constraints, and the
  alert_sent flag on waiting_list.

### 2. Customer (login as customer@example.com / customer123)

Search and Book tab:
- Set From=EWR, To=LAX, Class=economy, Trip Type=one-way, set a depart
  date that is a Mon-Fri in the next 1-2 weeks (so AA 101 matches).
- Click "Search Flights" and let the table populate.
- Show "Sort By" -> Price (low-high) and click Search again. Then sort by
  Departure time. Then by Duration.
- Set "Max Price ($)" to a small value and search again to show the
  filter, then clear it.
- Set "Airline filter" to AA and search to show airline filtering, then
  clear it.
- Switch Trip Type to round-trip, set Return Date to a Mon-Fri date a few
  days later, and search again. Show outbound + return tables.
- Tick "Flexible (+/- 3 days)" and search to show 7-day window.
- Pick an outbound row, click "Book Selected Outbound". Pick a return row,
  click "Book Selected Return".

My Reservations tab:
- Show the Upcoming sub-tab; mention the new ticket appears there.
- Click the Past sub-tab to demonstrate it exists (it can be empty).
- Select an economy reservation and click Cancel - show the rejection
  popup ("only business or first class can be cancelled").
- Select a business or first class reservation and cancel it - confirm
  the alert popup names the waitlisted customer.

Waiting List tab:
- Show the existing waitlist row, point out the alert_sent column.
- Add to a full flight from Search and Book to demonstrate it appears.

Questions tab:
- Search for a keyword, post a question, show it appears.

### 3. Customer Representative (login as rep@example.com / rep123)

Flights tab: add a new flight, edit a flight, delete a flight.
Airports tab: add a new airport (e.g. BOS).
Aircraft tab: add a new aircraft for any airline.
Customers tab: edit one customer record briefly.
Book / Edit on Behalf tab: enter customer email, search a route, book a
flight on behalf of the customer.
Reservations tab: filter by customer name and/or flight #, edit class or
date or meal on a leg, cancel a leg.
Waiting Lists tab: filter by airline + flight # + flight date.
Flights by Airport tab: enter EWR, click "Show Flights for Airport" to
display departing AND arriving flights together.
Questions tab: select an unanswered question, type a reply, save.

### 4. Admin (login as admin@example.com / admin123)

Employees tab: add and edit an employee row.
Customers tab: briefly show add / edit / delete works for admin too.
Reports tab:
- Set Sales Month to 2026-04 and click "Sales for Month".
- Click "Revenue by Flight".
- Type AA in the airline filter and click "Revenue by Airline" to filter.
- Click "Revenue by Customer".
- Click "Best Customer (most revenue)".
- Click "Most Active Flights".
- Type a flight number or customer name in the filters and click
  "Reservations" to show filtered listing.

### 5. Wrap (about 15 seconds)
- Mention all checklist items have been demonstrated and that
  ProjectChecklist PDF lists them.

## Recording tips
- Resize the window so all tabs fit on one screen.
- Pause briefly between dialogs so the grader can read the popups.
- Do not include the MySQL setup in the video - the README covers it.
- Save / export the video as MP4 named `Group-1-Demo.mp4` and place it in
  the `Submission/` folder before uploading.
