import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;

public class ProjectFrame extends JFrame {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/travel_reservation";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    private static final BigDecimal DEFAULT_BOOKING_FEE = new BigDecimal("25.00");

    private final Font mainFont = new Font("Lucida Sans", Font.PLAIN, 14);
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private Connection con;

    private int currentCustomerId = -1;
    private int currentEmployeeId = -1;
    private String currentEmployeeRole = "";
    private String currentUserName = "";

    public ProjectFrame(Connection con) {
        this.con = con;
        setTitle("Online Travel Reservation System");
        setSize(1200, 760);
        setMinimumSize(new Dimension(1000, 640));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setContentPane(root);
        showLogin();
    }

    // ==================== Login & navigation ====================

    private void showLogin() {
        root.removeAll();

        JPanel login = new JPanel(new BorderLayout(10, 10));
        login.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("Online Travel Reservation System");
        title.setFont(new Font("Lucida Sans", Font.BOLD, 24));
        login.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 8));
        JTextField email = field();
        JPasswordField password = new JPasswordField();
        password.setFont(mainFont);
        JComboBox<String> userType = new JComboBox<>(new String[] {"Customer", "Employee"});

        fields.add(label("Email"));
        fields.add(email);
        fields.add(label("Password"));
        fields.add(password);
        fields.add(label("Login Type"));
        fields.add(userType);
        login.add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton createAccount = new JButton("Create Customer Account");
        JButton loginButton = new JButton("Login");
        buttons.add(createAccount);
        buttons.add(loginButton);
        login.add(buttons, BorderLayout.SOUTH);

        loginButton.addActionListener(e -> {
            String typedEmail = email.getText().trim();
            String typedPassword = new String(password.getPassword());
            if ("Customer".equals(userType.getSelectedItem())) {
                loginCustomer(typedEmail, typedPassword);
            } else {
                loginEmployee(typedEmail, typedPassword);
            }
        });

        createAccount.addActionListener(e -> showCreateCustomerDialog());

        root.add(login, "login");
        cards.show(root, "login");
        revalidate();
        repaint();
    }

    private void loginCustomer(String email, String password) {
        String sql = "SELECT customer_id, first_name, last_name FROM customer WHERE email = ? AND password = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    currentCustomerId = rs.getInt("customer_id");
                    currentEmployeeRole = "";
                    currentUserName = rs.getString("first_name") + " " + rs.getString("last_name");
                    showApplication();
                } else {
                    message("Invalid customer email or password.");
                }
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void loginEmployee(String email, String password) {
        String sql = "SELECT employee_id, first_name, last_name, role FROM employee WHERE email = ? AND password = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    currentCustomerId = -1;
                    currentEmployeeId = rs.getInt("employee_id");
                    currentEmployeeRole = rs.getString("role");
                    currentUserName = rs.getString("first_name") + " " + rs.getString("last_name");
                    showApplication();
                } else {
                    message("Invalid employee email or password.");
                }
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void showApplication() {
        JPanel page = new JPanel(new BorderLayout(10, 10));
        page.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel header = new JPanel(new BorderLayout());
        JLabel welcome = label("Signed in as " + currentUserName + roleSuffix());
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> showLogin());
        header.add(welcome, BorderLayout.WEST);
        header.add(logout, BorderLayout.EAST);
        page.add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        if (currentCustomerId > 0) {
            tabs.addTab("Search and Book", customerSearchAndBookPanel(currentCustomerId, false));
            tabs.addTab("My Reservations", customerReservationsPanel(currentCustomerId));
            tabs.addTab("Waiting List", waitingListPanel(currentCustomerId));
            tabs.addTab("Questions", customerQuestionsPanel());
        } else if ("customer_rep".equals(currentEmployeeRole)) {
            tabs.addTab("Flights", flightsPanel(false));
            tabs.addTab("Airports", airportsPanel());
            tabs.addTab("Aircraft", aircraftPanel());
            tabs.addTab("Customers", customersPanel());
            tabs.addTab("Book / Edit on Behalf", customerSearchAndBookPanel(-1, true));
            tabs.addTab("Reservations", representativeReservationsPanel());
            tabs.addTab("Waiting Lists", repWaitingListsPanel());
            tabs.addTab("Flights by Airport", flightsByAirportPanel());
            tabs.addTab("Questions", representativeQuestionsPanel());
        } else if ("admin".equals(currentEmployeeRole)) {
            tabs.addTab("Employees", employeesPanel());
            tabs.addTab("Customers", customersPanel());
            tabs.addTab("Reports", reportsPanel());
            tabs.addTab("Flights", flightsPanel(true));
        }
        page.add(tabs, BorderLayout.CENTER);

        root.add(page, "app");
        cards.show(root, "app");
        revalidate();
        repaint();
    }

    private String roleSuffix() {
        if (currentCustomerId > 0) return " (customer)";
        if ("admin".equals(currentEmployeeRole)) return " (admin)";
        return " (customer representative)";
    }

    // ==================== Customer search / book ====================
    // This panel is also reused by the rep "Book on Behalf" tab when repMode=true.

    private JPanel customerSearchAndBookPanel(int customerIdForBooking, boolean repMode) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        JPanel form = new JPanel(new GridLayout(0, 4, 8, 8));
        JTextField customerEmail = field("");
        JTextField from = field("EWR");
        JTextField to = field("LAX");
        JComboBox<String> tripType = new JComboBox<>(new String[] {"one-way", "round-trip"});
        JTextField departDate = field(LocalDate.now().plusWeeks(2).toString());
        JTextField returnDate = field(LocalDate.now().plusWeeks(3).toString());
        JCheckBox flexible = new JCheckBox("Flexible (+/- 3 days)");
        flexible.setFont(mainFont);
        JComboBox<String> seatClass = new JComboBox<>(new String[] {"economy", "business", "first"});
        JTextField meal = field("");

        JTextField airlineFilter = field("");
        JTextField maxPrice = field("");
        JComboBox<String> sortBy = new JComboBox<>(new String[] {
            "Price (low-high)", "Price (high-low)", "Departure time", "Arrival time", "Duration"
        });

        if (repMode) {
            addField(form, "Customer Email", customerEmail);
        } else {
            form.add(new JLabel());
            form.add(new JLabel());
        }
        addField(form, "Trip Type", tripType);
        addField(form, "Class", seatClass);
        addField(form, "From (e.g. EWR)", from);
        addField(form, "To (e.g. LAX)", to);
        addField(form, "Depart Date (YYYY-MM-DD)", departDate);
        addField(form, "Return Date (YYYY-MM-DD)", returnDate);
        form.add(label("Flexible Dates"));
        form.add(flexible);
        addField(form, "Special Meal", meal);
        addField(form, "Airline filter (e.g. AA,DL)", airlineFilter);
        addField(form, "Max Price ($)", maxPrice);
        addField(form, "Sort By", sortBy);

        JTable outboundTable = table();
        JTable returnTable = table();
        JLabel outboundLabel = label("Outbound flights");
        JLabel returnLabel = label("Return flights");
        returnLabel.setVisible(false);
        returnTable.setVisible(false);

        JButton search = new JButton("Search Flights");
        JButton book = new JButton("Book Selected Outbound");
        JButton bookReturn = new JButton("Book Selected Return");
        JButton waitlist = new JButton("Join Waitlist (selected outbound)");
        bookReturn.setVisible(false);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(search);
        actions.add(waitlist);
        actions.add(book);
        actions.add(bookReturn);

        tripType.addActionListener(e -> {
            boolean rt = "round-trip".equals(tripType.getSelectedItem());
            returnLabel.setVisible(rt);
            returnTable.setVisible(rt);
            bookReturn.setVisible(rt);
            panel.revalidate();
            panel.repaint();
        });

        search.addActionListener(e -> runFlightSearch(
            outboundTable, returnTable, tripType, from.getText(), to.getText(),
            departDate.getText(), returnDate.getText(), flexible.isSelected(),
            (String) seatClass.getSelectedItem(),
            airlineFilter.getText(), maxPrice.getText(), (String) sortBy.getSelectedItem()
        ));

        book.addActionListener(e -> {
            int customerId = repMode ? lookupCustomerByEmail(customerEmail.getText()) : customerIdForBooking;
            if (customerId <= 0) return;
            bookFromTable(outboundTable, customerId, (String) seatClass.getSelectedItem(), meal.getText(),
                "round-trip".equals(tripType.getSelectedItem()) ? "round-trip" : "one-way", 1);
        });

        bookReturn.addActionListener(e -> {
            int customerId = repMode ? lookupCustomerByEmail(customerEmail.getText()) : customerIdForBooking;
            if (customerId <= 0) return;
            bookFromTable(returnTable, customerId, (String) seatClass.getSelectedItem(), meal.getText(),
                "round-trip", 2);
        });

        waitlist.addActionListener(e -> {
            int customerId = repMode ? lookupCustomerByEmail(customerEmail.getText()) : customerIdForBooking;
            if (customerId <= 0) return;
            int row = outboundTable.getSelectedRow();
            if (row < 0) {
                message("Select a flight first.");
                return;
            }
            try {
                addWaitlist(customerId,
                    value(outboundTable, row, "airline_id"),
                    value(outboundTable, row, "flight_number"),
                    LocalDate.parse(value(outboundTable, row, "operating_date")));
                message("Added to waiting list.");
            } catch (Exception ex) {
                showError(ex);
            }
        });

        JPanel center = new JPanel(new GridLayout(0, 1, 8, 8));
        JPanel outPanel = new JPanel(new BorderLayout(4, 4));
        outPanel.add(outboundLabel, BorderLayout.NORTH);
        outPanel.add(new JScrollPane(outboundTable), BorderLayout.CENTER);
        JPanel retPanel = new JPanel(new BorderLayout(4, 4));
        retPanel.add(returnLabel, BorderLayout.NORTH);
        retPanel.add(new JScrollPane(returnTable), BorderLayout.CENTER);
        center.add(outPanel);
        center.add(retPanel);

        panel.add(form, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void runFlightSearch(JTable outboundTable, JTable returnTable, JComboBox<String> tripType,
            String from, String to, String departDateText, String returnDateText, boolean flexible,
            String seatClass, String airlineFilter, String maxPriceText, String sortBy) {
        try {
            LocalDate departDate = LocalDate.parse(departDateText.trim());
            outboundTable.setModel(searchFlights(from, to, departDate, flexible, seatClass, airlineFilter, maxPriceText, sortBy));
            if ("round-trip".equals(tripType.getSelectedItem())) {
                LocalDate returnDate = LocalDate.parse(returnDateText.trim());
                returnTable.setModel(searchFlights(to, from, returnDate, flexible, seatClass, airlineFilter, maxPriceText, sortBy));
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private DefaultTableModel searchFlights(String fromAirport, String toAirport, LocalDate centerDate,
            boolean flexible, String seatClass, String airlineFilter, String maxPriceText, String sortBy) throws SQLException {
        // We pull flights for the route, then in Java expand each candidate against the selected
        // dates (with +/- 3 day flex) and filter against operating_days. We compute remaining seats
        // for the requested class so the customer sees real availability before booking.
        String capCol = "num_" + seatClass + "_seats";
        String fareCol = seatClass + "_fare";
        String mainSql =
            "SELECT f.airline_id, a.name AS airline, f.flight_number, f.aircraft_id, " +
            "f.departure_airport, f.arrival_airport, f.departure_time, f.arrival_time, " +
            "f.operating_days, f.economy_fare, f.business_fare, f.first_fare, ac." + capCol + " AS capacity " +
            "FROM flight f JOIN airline a ON f.airline_id = a.airline_id " +
            "JOIN aircraft ac ON f.aircraft_id = ac.aircraft_id " +
            "WHERE f.departure_airport = ? AND f.arrival_airport = ?";

        Set<String> airlineSet = parseAirlineFilter(airlineFilter);
        BigDecimal cap = parseMaxPrice(maxPriceText);

        Vector<String> columns = new Vector<>(Arrays.asList(
            "airline_id", "airline", "flight_number", "operating_date", "departure_time",
            "arrival_time", "duration_minutes", seatClass + "_fare", "seats_remaining"
        ));
        // Keep these hidden columns at the end (still needed for booking lookups)
        columns.add("departure_airport");
        columns.add("arrival_airport");
        columns.add("economy_fare");
        columns.add("business_fare");
        columns.add("first_fare");

        List<Object[]> rows = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(mainSql)) {
            ps.setString(1, fromAirport.trim().toUpperCase());
            ps.setString(2, toAirport.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                int dayWindow = flexible ? 3 : 0;
                while (rs.next()) {
                    String airline = rs.getString("airline_id");
                    if (!airlineSet.isEmpty() && !airlineSet.contains(airline)) continue;
                    BigDecimal fare = rs.getBigDecimal(fareCol);
                    if (cap != null && fare.compareTo(cap) > 0) continue;

                    Set<DayOfWeek> opDays = parseOperatingDays(rs.getString("operating_days"));
                    Time depTime = rs.getTime("departure_time");
                    Time arrTime = rs.getTime("arrival_time");
                    long duration = computeDurationMinutes(depTime, arrTime);
                    int capacity = rs.getInt("capacity");

                    for (int offset = -dayWindow; offset <= dayWindow; offset++) {
                        LocalDate flightDate = centerDate.plusDays(offset);
                        if (!opDays.contains(flightDate.getDayOfWeek())) continue;

                        int sold = countSeatsSold(airline, rs.getString("flight_number"), flightDate, seatClass);
                        int remaining = capacity - sold;

                        Object[] row = new Object[columns.size()];
                        row[0] = airline;
                        row[1] = rs.getString("airline");
                        row[2] = rs.getString("flight_number");
                        row[3] = flightDate.toString();
                        row[4] = depTime.toString();
                        row[5] = arrTime.toString();
                        row[6] = duration;
                        row[7] = fare;
                        row[8] = remaining;
                        row[9] = rs.getString("departure_airport");
                        row[10] = rs.getString("arrival_airport");
                        row[11] = rs.getBigDecimal("economy_fare");
                        row[12] = rs.getBigDecimal("business_fare");
                        row[13] = rs.getBigDecimal("first_fare");
                        rows.add(row);
                    }
                }
            }
        }

        sortFlightRows(rows, sortBy);

        Vector<Vector<Object>> data = new Vector<>();
        for (Object[] arr : rows) {
            Vector<Object> v = new Vector<>();
            for (Object o : arr) v.add(o);
            data.add(v);
        }
        return new DefaultTableModel(data, columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private void sortFlightRows(List<Object[]> rows, String sortBy) {
        if (sortBy == null) return;
        switch (sortBy) {
            case "Price (low-high)":
                rows.sort((a, b) -> ((BigDecimal) a[7]).compareTo((BigDecimal) b[7]));
                break;
            case "Price (high-low)":
                rows.sort((a, b) -> ((BigDecimal) b[7]).compareTo((BigDecimal) a[7]));
                break;
            case "Departure time":
                rows.sort((a, b) -> ((String) a[4]).compareTo((String) b[4]));
                break;
            case "Arrival time":
                rows.sort((a, b) -> ((String) a[5]).compareTo((String) b[5]));
                break;
            case "Duration":
                rows.sort((a, b) -> Long.compare((Long) a[6], (Long) b[6]));
                break;
            default:
                break;
        }
    }

    private Set<String> parseAirlineFilter(String text) {
        Set<String> out = new HashSet<>();
        if (text == null) return out;
        for (String part : text.split(",")) {
            String trimmed = part.trim().toUpperCase();
            if (!trimmed.isEmpty()) out.add(trimmed);
        }
        return out;
    }

    private BigDecimal parseMaxPrice(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try { return new BigDecimal(text.trim()); } catch (NumberFormatException ex) { return null; }
    }

    private Set<DayOfWeek> parseOperatingDays(String operatingDays) {
        Set<DayOfWeek> set = new HashSet<>();
        if (operatingDays == null) return set;
        String trimmed = operatingDays.trim().toLowerCase(Locale.ROOT);
        if (trimmed.equals("daily") || trimmed.equals("everyday") || trimmed.equals("all")) {
            for (DayOfWeek d : DayOfWeek.values()) set.add(d);
            return set;
        }
        for (String part : operatingDays.split(",")) {
            String key = part.trim().toLowerCase(Locale.ROOT);
            switch (key) {
                case "mon": case "monday": set.add(DayOfWeek.MONDAY); break;
                case "tue": case "tuesday": set.add(DayOfWeek.TUESDAY); break;
                case "wed": case "wednesday": set.add(DayOfWeek.WEDNESDAY); break;
                case "thu": case "thursday": set.add(DayOfWeek.THURSDAY); break;
                case "fri": case "friday": set.add(DayOfWeek.FRIDAY); break;
                case "sat": case "saturday": set.add(DayOfWeek.SATURDAY); break;
                case "sun": case "sunday": set.add(DayOfWeek.SUNDAY); break;
                default: break;
            }
        }
        return set;
    }

    private long computeDurationMinutes(Time dep, Time arr) {
        long depMin = (dep.getTime() / 60000L);
        long arrMin = (arr.getTime() / 60000L);
        long diff = arrMin - depMin;
        if (diff < 0) diff += 24 * 60;
        return diff;
    }

    private int countSeatsSold(String airline, String flight, LocalDate date, String seatClass) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT COUNT(*) FROM ticket_flight WHERE airline_id = ? AND flight_number = ? AND departure_date = ? AND class = ?")) {
            ps.setString(1, airline);
            ps.setString(2, flight);
            ps.setDate(3, Date.valueOf(date));
            ps.setString(4, seatClass);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void bookFromTable(JTable table, int customerId, String seatClass, String meal,
            String ticketType, int legNumber) {
        int row = table.getSelectedRow();
        if (row < 0) {
            message("Select a flight first.");
            return;
        }
        try {
            String airline = value(table, row, "airline_id");
            String flight = value(table, row, "flight_number");
            LocalDate date = LocalDate.parse(value(table, row, "operating_date"));
            int remaining = Integer.parseInt(value(table, row, "seats_remaining"));
            if (remaining <= 0) {
                addWaitlist(customerId, airline, flight, date);
                message("Flight is full. Added to the waiting list instead.");
                return;
            }
            BigDecimal fare = new BigDecimal(value(table, row, seatClass + "_fare"));

            con.setAutoCommit(false);
            int ticketNumber;
            if (legNumber == 1) {
                try (PreparedStatement t = con.prepareStatement(
                        "INSERT INTO ticket (customer_id, ticket_type, total_fare, booking_fee) VALUES (?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    t.setInt(1, customerId);
                    t.setString(2, ticketType);
                    t.setBigDecimal(3, fare);
                    t.setBigDecimal(4, DEFAULT_BOOKING_FEE);
                    t.executeUpdate();
                    try (ResultSet keys = t.getGeneratedKeys()) {
                        keys.next();
                        ticketNumber = keys.getInt(1);
                    }
                }
            } else {
                ticketNumber = lookupOpenRoundTripTicket(customerId);
                if (ticketNumber < 0) {
                    rollback();
                    message("Book the outbound leg first, then the return leg.");
                    return;
                }
                try (PreparedStatement t = con.prepareStatement(
                        "UPDATE ticket SET total_fare = total_fare + ? WHERE ticket_number = ?")) {
                    t.setBigDecimal(1, fare);
                    t.setInt(2, ticketNumber);
                    t.executeUpdate();
                }
            }

            try (PreparedStatement leg = con.prepareStatement(
                    "INSERT INTO ticket_flight (ticket_number, leg_number, airline_id, flight_number, departure_date, seat_number, class, special_meal) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                leg.setInt(1, ticketNumber);
                leg.setInt(2, legNumber);
                leg.setString(3, airline);
                leg.setString(4, flight);
                leg.setDate(5, Date.valueOf(date));
                leg.setString(6, nextSeatNumber(airline, flight, date, seatClass));
                leg.setString(7, seatClass);
                leg.setString(8, meal == null || meal.trim().isEmpty() ? null : meal.trim());
                leg.executeUpdate();
            }
            con.commit();
            message("Reservation saved. Ticket number: " + ticketNumber + (legNumber > 1 ? " (return leg added)" : ""));
        } catch (Exception ex) {
            rollback();
            showError(ex);
        } finally {
            resetAutoCommit();
        }
    }

    private int lookupOpenRoundTripTicket(int customerId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT t.ticket_number FROM ticket t WHERE t.customer_id = ? AND t.ticket_type = 'round-trip' " +
                "AND (SELECT COUNT(*) FROM ticket_flight tf WHERE tf.ticket_number = t.ticket_number) = 1 " +
                "ORDER BY t.purchase_datetime DESC LIMIT 1")) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    private String nextSeatNumber(String airline, String flight, LocalDate date, String seatClass) throws SQLException {
        String sql = "SELECT COUNT(*) + 1 AS next_seat FROM ticket_flight WHERE airline_id = ? AND flight_number = ? AND departure_date = ? AND class = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, airline);
            ps.setString(2, flight);
            ps.setDate(3, Date.valueOf(date));
            ps.setString(4, seatClass);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return seatClass.substring(0, 1).toUpperCase() + rs.getInt("next_seat");
            }
        }
    }

    private int lookupCustomerByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            message("Enter the customer's email first.");
            return -1;
        }
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT customer_id FROM customer WHERE email = ?")) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
                message("No customer found for that email.");
                return -1;
            }
        } catch (SQLException ex) {
            showError(ex);
            return -1;
        }
    }

    // ==================== Customer reservations / waiting list ====================

    private JPanel customerReservationsPanel(int customerId) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JTabbedPane sub = new JTabbedPane();
        JTable upcoming = table();
        JTable past = table();
        sub.addTab("Upcoming Flights", reservationsSubPanel(upcoming, customerId, true));
        sub.addTab("Past Flights", reservationsSubPanel(past, customerId, false));
        panel.add(sub, BorderLayout.CENTER);
        return panel;
    }

    private JPanel reservationsSubPanel(JTable table, int customerId, boolean upcoming) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JButton refresh = new JButton("Refresh");
        JButton cancel = new JButton(upcoming ? "Cancel Selected (business / first only)" : "Past flights cannot be cancelled");
        cancel.setEnabled(upcoming);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(refresh);
        actions.add(cancel);
        refresh.addActionListener(e -> loadCustomerReservations(table, customerId, upcoming));
        cancel.addActionListener(e -> cancelSelectedTicketLeg(table, customerId, true));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        loadCustomerReservations(table, customerId, upcoming);
        return panel;
    }

    private void loadCustomerReservations(JTable table, int customerId, boolean upcoming) {
        String comparison = upcoming ? ">=" : "<";
        setModel(table,
            "SELECT t.ticket_number, t.ticket_type, t.total_fare, t.booking_fee, t.purchase_datetime, " +
            "tf.leg_number, tf.airline_id, tf.flight_number, tf.departure_date, tf.seat_number, tf.class, tf.special_meal " +
            "FROM ticket t JOIN ticket_flight tf ON t.ticket_number = tf.ticket_number " +
            "WHERE t.customer_id = ? AND tf.departure_date " + comparison + " CURDATE() " +
            "ORDER BY tf.departure_date, t.ticket_number, tf.leg_number",
            customerId);
    }

    private JPanel waitingListPanel(int customerId) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JTable table = table();
        JButton refresh = new JButton("Refresh");
        JButton remove = new JButton("Remove Selected Waitlist Entry");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(refresh);
        actions.add(remove);
        refresh.addActionListener(e -> loadCustomerWaitlist(table, customerId));
        remove.addActionListener(e -> removeSelectedWaitlist(table, customerId));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        loadCustomerWaitlist(table, customerId);
        return panel;
    }

    private void loadCustomerWaitlist(JTable table, int customerId) {
        setModel(table,
            "SELECT w.airline_id, w.flight_number, w.flight_date, w.request_datetime, w.alert_sent, " +
            "f.departure_airport, f.arrival_airport " +
            "FROM waiting_list w JOIN flight f ON w.airline_id = f.airline_id AND w.flight_number = f.flight_number " +
            "WHERE w.customer_id = ? ORDER BY w.request_datetime",
            customerId);
    }

    private void cancelSelectedTicketLeg(JTable table, int ownerCustomerId, boolean enforceClassRule) {
        int row = table.getSelectedRow();
        if (row < 0) { message("Select a reservation first."); return; }
        String seatClass = value(table, row, "class");
        if (enforceClassRule && !"business".equalsIgnoreCase(seatClass) && !"first".equalsIgnoreCase(seatClass)) {
            message("Only business or first class reservations can be cancelled.");
            return;
        }
        String ticketNumber = value(table, row, "ticket_number");
        String airline = value(table, row, "airline_id");
        String flight = value(table, row, "flight_number");
        LocalDate flightDate;
        try { flightDate = LocalDate.parse(value(table, row, "departure_date")); }
        catch (Exception ex) { showError(ex); return; }

        try {
            String sql = ownerCustomerId > 0
                ? "DELETE FROM ticket WHERE ticket_number = ? AND customer_id = ?"
                : "DELETE FROM ticket WHERE ticket_number = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, Integer.parseInt(ticketNumber));
                if (ownerCustomerId > 0) ps.setInt(2, ownerCustomerId);
                int changed = ps.executeUpdate();
                if (changed == 0) {
                    message("No reservation was cancelled (it may belong to another customer).");
                    return;
                }
            }
            String alertedTo = sendWaitlistAlertIfAny(airline, flight, flightDate);
            if (alertedTo != null) {
                message("Reservation cancelled. Alert sent to waitlisted customer: " + alertedTo);
            } else {
                message("Reservation cancelled.");
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private String sendWaitlistAlertIfAny(String airline, String flight, LocalDate flightDate) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT w.customer_id, c.first_name, c.last_name, c.email " +
                "FROM waiting_list w JOIN customer c ON w.customer_id = c.customer_id " +
                "WHERE w.airline_id = ? AND w.flight_number = ? AND w.flight_date = ? AND w.alert_sent = FALSE " +
                "ORDER BY w.request_datetime LIMIT 1")) {
            ps.setString(1, airline);
            ps.setString(2, flight);
            ps.setDate(3, Date.valueOf(flightDate));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int waitedCustomer = rs.getInt("customer_id");
                    try (PreparedStatement upd = con.prepareStatement(
                            "UPDATE waiting_list SET alert_sent = TRUE WHERE customer_id = ? AND airline_id = ? AND flight_number = ? AND flight_date = ?")) {
                        upd.setInt(1, waitedCustomer);
                        upd.setString(2, airline);
                        upd.setString(3, flight);
                        upd.setDate(4, Date.valueOf(flightDate));
                        upd.executeUpdate();
                    }
                    return rs.getString("first_name") + " " + rs.getString("last_name") + " <" + rs.getString("email") + ">";
                }
            }
        }
        return null;
    }

    private void addWaitlist(int customerId, String airline, String flight, LocalDate date) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT IGNORE INTO waiting_list (customer_id, airline_id, flight_number, flight_date) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, customerId);
            ps.setString(2, airline);
            ps.setString(3, flight);
            ps.setDate(4, Date.valueOf(date));
            ps.executeUpdate();
        }
    }

    private void removeSelectedWaitlist(JTable table, int customerId) {
        int row = table.getSelectedRow();
        if (row < 0) { message("Select a waitlist entry first."); return; }
        try (PreparedStatement ps = con.prepareStatement(
                "DELETE FROM waiting_list WHERE customer_id = ? AND airline_id = ? AND flight_number = ? AND flight_date = ?")) {
            ps.setInt(1, customerId);
            ps.setString(2, value(table, row, "airline_id"));
            ps.setString(3, value(table, row, "flight_number"));
            ps.setDate(4, Date.valueOf(value(table, row, "flight_date")));
            ps.executeUpdate();
            message("Waitlist entry removed.");
            loadCustomerWaitlist(table, customerId);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    // ==================== Customer Q&A ====================

    private JPanel customerQuestionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JTable table = table();
        JTextArea question = new JTextArea(4, 40);
        question.setLineWrap(true);
        question.setWrapStyleWord(true);
        JTextField keyword = field("");
        JButton post = new JButton("Post Question");
        JButton search = new JButton("Search/Browse Q&A");
        JPanel top = new JPanel(new BorderLayout(8, 8));
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchRow.add(label("Keyword"));
        searchRow.add(keyword);
        searchRow.add(search);
        top.add(searchRow, BorderLayout.NORTH);
        top.add(new JScrollPane(question), BorderLayout.CENTER);
        top.add(post, BorderLayout.SOUTH);

        post.addActionListener(e -> {
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO question (customer_id, question_text) VALUES (?, ?)")) {
                ps.setInt(1, currentCustomerId);
                ps.setString(2, question.getText().trim());
                ps.executeUpdate();
                question.setText("");
                loadQuestions(table, keyword.getText());
                message("Question posted.");
            } catch (SQLException ex) {
                showError(ex);
            }
        });
        search.addActionListener(e -> loadQuestions(table, keyword.getText()));

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        loadQuestions(table, "");
        return panel;
    }

    private void loadQuestions(JTable table, String keyword) {
        String like = "%" + keyword.trim() + "%";
        setModel(table,
            "SELECT q.question_id, c.email AS customer_email, q.question_text, q.answer_text, q.asked_datetime, q.answered_datetime " +
            "FROM question q JOIN customer c ON q.customer_id = c.customer_id " +
            "WHERE q.question_text LIKE ? OR IFNULL(q.answer_text, '') LIKE ? ORDER BY q.asked_datetime DESC",
            like, like);
    }

    // ==================== Flights / Airports / Aircraft / Customers / Employees ====================

    private JPanel flightsPanel(boolean adminView) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JTable table = table();
        JPanel form = new JPanel(new GridLayout(0, 4, 8, 8));

        JTextField airline = field("AA");
        JTextField number = field("999");
        JTextField aircraft = field("1");
        JTextField from = field("EWR");
        JTextField to = field("LAX");
        JTextField depart = field("08:00:00");
        JTextField arrive = field("11:00:00");
        JTextField days = field("Daily");
        JTextField economy = field("199.00");
        JTextField business = field("399.00");
        JTextField first = field("699.00");
        JComboBox<String> domestic = new JComboBox<>(new String[] {"true", "false"});

        addField(form, "Airline", airline);
        addField(form, "Flight #", number);
        addField(form, "Aircraft ID", aircraft);
        addField(form, "From", from);
        addField(form, "To", to);
        addField(form, "Depart Time", depart);
        addField(form, "Arrive Time", arrive);
        addField(form, "Days", days);
        addField(form, "Economy Fare", economy);
        addField(form, "Business Fare", business);
        addField(form, "First Fare", first);
        form.add(label("Domestic"));
        form.add(domestic);

        JButton add = new JButton("Add Flight");
        JButton update = new JButton("Update Selected Flight");
        JButton delete = new JButton("Delete Selected Flight");
        JButton refresh = new JButton("Refresh");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(refresh);
        actions.add(add);
        actions.add(update);
        actions.add(delete);

        if (adminView) {
            add.setEnabled(false);
            update.setEnabled(false);
            delete.setEnabled(false);
        }

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && !e.getValueIsAdjusting()) {
                airline.setText(value(table, row, "airline_id"));
                number.setText(value(table, row, "flight_number"));
                aircraft.setText(value(table, row, "aircraft_id"));
                from.setText(value(table, row, "departure_airport"));
                to.setText(value(table, row, "arrival_airport"));
                depart.setText(value(table, row, "departure_time"));
                arrive.setText(value(table, row, "arrival_time"));
                days.setText(value(table, row, "operating_days"));
                economy.setText(value(table, row, "economy_fare"));
                business.setText(value(table, row, "business_fare"));
                first.setText(value(table, row, "first_fare"));
                domestic.setSelectedItem(value(table, row, "is_domestic"));
            }
        });

        add.addActionListener(e -> saveFlight(false, airline, number, aircraft, from, to, depart, arrive, days, economy, business, first, domestic, table));
        update.addActionListener(e -> saveFlight(true, airline, number, aircraft, from, to, depart, arrive, days, economy, business, first, domestic, table));
        delete.addActionListener(e -> deleteSelectedFlight(table));
        refresh.addActionListener(e -> loadFlights(table));

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        loadFlights(table);
        return panel;
    }

    private JPanel customersPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JTable table = table();
        JPanel form = new JPanel(new GridLayout(0, 4, 8, 8));

        JTextField id = field(""); id.setEditable(false);
        JTextField firstName = field("");
        JTextField lastName = field("");
        JTextField email = field("");
        JTextField password = field("");
        JTextField phone = field("");
        JTextField street = field("");
        JTextField city = field("");
        JTextField state = field("");
        JTextField zip = field("");

        addField(form, "ID", id);
        addField(form, "First Name", firstName);
        addField(form, "Last Name", lastName);
        addField(form, "Email", email);
        addField(form, "Password", password);
        addField(form, "Phone", phone);
        addField(form, "Street", street);
        addField(form, "City", city);
        addField(form, "State", state);
        addField(form, "Zip", zip);

        JButton add = new JButton("Add Customer");
        JButton update = new JButton("Update Selected Customer");
        JButton delete = new JButton("Delete Selected Customer");
        JButton refresh = new JButton("Refresh");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(refresh);
        actions.add(add);
        actions.add(update);
        actions.add(delete);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && !e.getValueIsAdjusting()) {
                id.setText(value(table, row, "customer_id"));
                firstName.setText(value(table, row, "first_name"));
                lastName.setText(value(table, row, "last_name"));
                email.setText(value(table, row, "email"));
                password.setText(value(table, row, "password"));
                phone.setText(value(table, row, "phone"));
                street.setText(value(table, row, "street"));
                city.setText(value(table, row, "city"));
                state.setText(value(table, row, "state"));
                zip.setText(value(table, row, "zip_code"));
            }
        });

        add.addActionListener(e -> saveCustomer(false, id, firstName, lastName, email, password, phone, street, city, state, zip, table));
        update.addActionListener(e -> saveCustomer(true, id, firstName, lastName, email, password, phone, street, city, state, zip, table));
        delete.addActionListener(e -> deleteSelectedCustomer(table));
        refresh.addActionListener(e -> loadCustomers(table));

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        loadCustomers(table);
        return panel;
    }

    private JPanel employeesPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JTable table = table();
        JPanel form = new JPanel(new GridLayout(0, 4, 8, 8));

        JTextField id = field(""); id.setEditable(false);
        JTextField firstName = field("");
        JTextField lastName = field("");
        JTextField email = field("");
        JTextField password = field("");
        JTextField phone = field("");
        JComboBox<String> role = new JComboBox<>(new String[] {"admin", "customer_rep"});

        addField(form, "ID", id);
        addField(form, "First Name", firstName);
        addField(form, "Last Name", lastName);
        addField(form, "Email", email);
        addField(form, "Password", password);
        addField(form, "Phone", phone);
        form.add(label("Role"));
        form.add(role);

        JButton add = new JButton("Add Employee");
        JButton update = new JButton("Update Selected Employee");
        JButton delete = new JButton("Delete Selected Employee");
        JButton refresh = new JButton("Refresh");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(refresh);
        actions.add(add);
        actions.add(update);
        actions.add(delete);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && !e.getValueIsAdjusting()) {
                id.setText(value(table, row, "employee_id"));
                firstName.setText(value(table, row, "first_name"));
                lastName.setText(value(table, row, "last_name"));
                email.setText(value(table, row, "email"));
                password.setText(value(table, row, "password"));
                phone.setText(value(table, row, "phone"));
                role.setSelectedItem(value(table, row, "role"));
            }
        });

        add.addActionListener(e -> saveEmployee(false, id, firstName, lastName, email, password, phone, role, table));
        update.addActionListener(e -> saveEmployee(true, id, firstName, lastName, email, password, phone, role, table));
        delete.addActionListener(e -> deleteSelectedEmployee(table));
        refresh.addActionListener(e -> loadEmployees(table));

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        loadEmployees(table);
        return panel;
    }

    // ==================== Rep: reservations / waiting lists / flights by airport / Q&A ====================

    private JPanel representativeReservationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JTable table = table();
        JPanel top = new JPanel(new GridLayout(0, 4, 8, 8));
        JTextField customerName = field("");
        JTextField customerEmail = field("");
        JTextField airline = field("");
        JTextField flightNumber = field("");
        JTextField newSeatClass = field("");
        JTextField newDate = field("");
        JTextField newMeal = field("");

        addField(top, "Customer Name (any part)", customerName);
        addField(top, "Customer Email", customerEmail);
        addField(top, "Airline (e.g. AA)", airline);
        addField(top, "Flight #", flightNumber);
        addField(top, "Edit: New Class", newSeatClass);
        addField(top, "Edit: New Date (YYYY-MM-DD)", newDate);
        addField(top, "Edit: New Meal", newMeal);

        JButton find = new JButton("Find Reservations");
        JButton edit = new JButton("Save Edits to Selected");
        JButton cancel = new JButton("Cancel Selected (any class)");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(find); actions.add(edit); actions.add(cancel);

        find.addActionListener(e -> repFindReservations(table, customerName.getText(), customerEmail.getText(), airline.getText(), flightNumber.getText()));
        cancel.addActionListener(e -> cancelSelectedTicketLeg(table, -1, false));
        edit.addActionListener(e -> editSelectedReservation(table, newSeatClass.getText(), newDate.getText(), newMeal.getText()));

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void repFindReservations(JTable table, String customerName, String customerEmail, String airline, String flightNumber) {
        StringBuilder sql = new StringBuilder(
            "SELECT c.first_name, c.last_name, c.email, t.ticket_number, t.ticket_type, t.total_fare, " +
            "tf.leg_number, tf.airline_id, tf.flight_number, tf.departure_date, tf.seat_number, tf.class, tf.special_meal " +
            "FROM customer c JOIN ticket t ON c.customer_id = t.customer_id " +
            "JOIN ticket_flight tf ON t.ticket_number = tf.ticket_number WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        if (!customerName.trim().isEmpty()) {
            sql.append(" AND (c.first_name LIKE ? OR c.last_name LIKE ? OR CONCAT(c.first_name,' ',c.last_name) LIKE ?)");
            String like = "%" + customerName.trim() + "%";
            params.add(like); params.add(like); params.add(like);
        }
        if (!customerEmail.trim().isEmpty()) {
            sql.append(" AND c.email = ?");
            params.add(customerEmail.trim());
        }
        if (!airline.trim().isEmpty()) {
            sql.append(" AND tf.airline_id = ?");
            params.add(airline.trim().toUpperCase());
        }
        if (!flightNumber.trim().isEmpty()) {
            sql.append(" AND tf.flight_number = ?");
            params.add(flightNumber.trim());
        }
        sql.append(" ORDER BY tf.departure_date, t.ticket_number, tf.leg_number");
        setModel(table, sql.toString(), params.toArray());
    }

    private void editSelectedReservation(JTable table, String newClass, String newDate, String newMeal) {
        int row = table.getSelectedRow();
        if (row < 0) { message("Select a reservation row first."); return; }
        try {
            int ticketNumber = Integer.parseInt(value(table, row, "ticket_number"));
            int legNumber = Integer.parseInt(value(table, row, "leg_number"));
            StringBuilder sql = new StringBuilder("UPDATE ticket_flight SET ");
            List<Object> params = new ArrayList<>();
            boolean first = true;
            if (!newClass.trim().isEmpty()) {
                sql.append("class = ?"); params.add(newClass.trim().toLowerCase()); first = false;
            }
            if (!newDate.trim().isEmpty()) {
                if (!first) sql.append(", ");
                sql.append("departure_date = ?"); params.add(Date.valueOf(LocalDate.parse(newDate.trim()))); first = false;
            }
            if (!newMeal.trim().isEmpty()) {
                if (!first) sql.append(", ");
                sql.append("special_meal = ?"); params.add(newMeal.trim()); first = false;
            }
            if (first) { message("Enter at least one new value to update."); return; }
            sql.append(" WHERE ticket_number = ? AND leg_number = ?");
            params.add(ticketNumber); params.add(legNumber);

            try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
                ps.executeUpdate();
            }
            message("Reservation updated.");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private JPanel repWaitingListsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JTable table = table();
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField airline = field("");
        JTextField flightNumber = field("");
        JTextField flightDate = field("");
        top.add(label("Airline")); top.add(airline);
        top.add(label("Flight #")); top.add(flightNumber);
        top.add(label("Flight Date")); top.add(flightDate);
        JButton refresh = new JButton("Filter");
        JButton showAll = new JButton("Show All");
        top.add(refresh); top.add(showAll);
        refresh.addActionListener(e -> repLoadWaitingList(table, airline.getText(), flightNumber.getText(), flightDate.getText()));
        showAll.addActionListener(e -> repLoadWaitingList(table, "", "", ""));
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        repLoadWaitingList(table, "", "", "");
        return panel;
    }

    private void repLoadWaitingList(JTable table, String airline, String flightNumber, String flightDate) {
        StringBuilder sql = new StringBuilder(
            "SELECT c.customer_id, c.first_name, c.last_name, c.email, w.airline_id, w.flight_number, w.flight_date, w.request_datetime, w.alert_sent " +
            "FROM waiting_list w JOIN customer c ON w.customer_id = c.customer_id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();
        if (!airline.trim().isEmpty()) { sql.append(" AND w.airline_id = ?"); params.add(airline.trim().toUpperCase()); }
        if (!flightNumber.trim().isEmpty()) { sql.append(" AND w.flight_number = ?"); params.add(flightNumber.trim()); }
        if (!flightDate.trim().isEmpty()) { sql.append(" AND w.flight_date = ?"); params.add(Date.valueOf(LocalDate.parse(flightDate.trim()))); }
        sql.append(" ORDER BY w.airline_id, w.flight_number, w.request_datetime");
        setModel(table, sql.toString(), params.toArray());
    }

    private JPanel flightsByAirportPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JTable table = table();
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField airport = field("EWR");
        JButton run = new JButton("Show Flights for Airport");
        top.add(label("Airport Code")); top.add(airport); top.add(run);
        run.addActionListener(e -> setModel(table,
            "SELECT 'departing' AS direction, f.airline_id, f.flight_number, f.departure_airport, f.arrival_airport, f.departure_time, f.arrival_time, f.operating_days " +
            "FROM flight f WHERE f.departure_airport = ? " +
            "UNION ALL " +
            "SELECT 'arriving' AS direction, f.airline_id, f.flight_number, f.departure_airport, f.arrival_airport, f.departure_time, f.arrival_time, f.operating_days " +
            "FROM flight f WHERE f.arrival_airport = ? ORDER BY direction, airline_id, flight_number",
            airport.getText().trim().toUpperCase(), airport.getText().trim().toUpperCase()));
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel airportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JTable table = table();
        JPanel form = new JPanel(new GridLayout(0, 4, 8, 8));
        JTextField code = field("BOS");
        JTextField name = field("Boston Logan International Airport");
        JTextField city = field("Boston");
        JTextField country = field("USA");
        addField(form, "Airport Code", code);
        addField(form, "Name", name);
        addField(form, "City", city);
        addField(form, "Country", country);
        JButton add = new JButton("Add Airport");
        JButton update = new JButton("Update Selected Airport");
        JButton delete = new JButton("Delete Selected Airport");
        JButton refresh = new JButton("Refresh");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(refresh); actions.add(add); actions.add(update); actions.add(delete);
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && !e.getValueIsAdjusting()) {
                code.setText(value(table, row, "airport_code"));
                name.setText(value(table, row, "airport_name"));
                city.setText(value(table, row, "city"));
                country.setText(value(table, row, "country"));
            }
        });
        add.addActionListener(e -> saveAirport(false, code, name, city, country, table));
        update.addActionListener(e -> saveAirport(true, code, name, city, country, table));
        delete.addActionListener(e -> deleteByKey("airport", "airport_code", code.getText().trim().toUpperCase(), table, "SELECT * FROM airport ORDER BY airport_code"));
        refresh.addActionListener(e -> setModel(table, "SELECT * FROM airport ORDER BY airport_code"));
        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        setModel(table, "SELECT * FROM airport ORDER BY airport_code");
        return panel;
    }

    private JPanel aircraftPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JTable table = table();
        JPanel form = new JPanel(new GridLayout(0, 4, 8, 8));
        JTextField id = field(""); id.setEditable(false);
        JTextField airline = field("AA");
        JTextField model = field("Airbus A320");
        JTextField economy = field("120");
        JTextField business = field("24");
        JTextField first = field("8");
        addField(form, "Aircraft ID", id);
        addField(form, "Airline", airline);
        addField(form, "Model", model);
        addField(form, "Economy Seats", economy);
        addField(form, "Business Seats", business);
        addField(form, "First Seats", first);
        JButton add = new JButton("Add Aircraft");
        JButton update = new JButton("Update Selected Aircraft");
        JButton delete = new JButton("Delete Selected Aircraft");
        JButton refresh = new JButton("Refresh");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(refresh); actions.add(add); actions.add(update); actions.add(delete);
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && !e.getValueIsAdjusting()) {
                id.setText(value(table, row, "aircraft_id"));
                airline.setText(value(table, row, "airline_id"));
                model.setText(value(table, row, "model"));
                economy.setText(value(table, row, "num_economy_seats"));
                business.setText(value(table, row, "num_business_seats"));
                first.setText(value(table, row, "num_first_seats"));
            }
        });
        add.addActionListener(e -> saveAircraft(false, id, airline, model, economy, business, first, table));
        update.addActionListener(e -> saveAircraft(true, id, airline, model, economy, business, first, table));
        delete.addActionListener(e -> deleteByKey("aircraft", "aircraft_id", id.getText().trim(), table, "SELECT * FROM aircraft ORDER BY aircraft_id"));
        refresh.addActionListener(e -> setModel(table, "SELECT * FROM aircraft ORDER BY aircraft_id"));
        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        setModel(table, "SELECT * FROM aircraft ORDER BY aircraft_id");
        return panel;
    }

    private JPanel representativeQuestionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JTable table = table();
        JTextArea answer = new JTextArea(4, 40);
        answer.setLineWrap(true);
        answer.setWrapStyleWord(true);
        JButton refresh = new JButton("Refresh Questions");
        JButton reply = new JButton("Reply to Selected Question");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(refresh);
        actions.add(reply);
        refresh.addActionListener(e -> loadQuestions(table, ""));
        reply.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { message("Select a question first."); return; }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE question SET employee_id = ?, answer_text = ?, answered_datetime = NOW() WHERE question_id = ?")) {
                ps.setInt(1, currentEmployeeId);
                ps.setString(2, answer.getText().trim());
                ps.setInt(3, Integer.parseInt(value(table, row, "question_id")));
                ps.executeUpdate();
                answer.setText("");
                loadQuestions(table, "");
                message("Answer saved.");
            } catch (Exception ex) {
                showError(ex);
            }
        });
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(new JScrollPane(answer), BorderLayout.NORTH);
        panel.add(actions, BorderLayout.SOUTH);
        loadQuestions(table, "");
        return panel;
    }

    // ==================== Admin reports ====================

    private JPanel reportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JTable table = table();
        JPanel top = new JPanel(new GridLayout(0, 4, 8, 8));

        JTextField month = field(YearMonth.now().toString());
        JTextField filterFlight = field("");
        JTextField filterCustomer = field("");
        JTextField filterAirline = field("");
        addField(top, "Sales Month (YYYY-MM)", month);
        addField(top, "Reservation: Flight #", filterFlight);
        addField(top, "Reservation: Customer Name", filterCustomer);
        addField(top, "Revenue: Airline (e.g. AA)", filterAirline);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton sales = new JButton("Sales for Month");
        JButton revenueFlight = new JButton("Revenue by Flight");
        JButton revenueAirline = new JButton("Revenue by Airline");
        JButton revenueCustomer = new JButton("Revenue by Customer");
        JButton bestCustomers = new JButton("Best Customer (most revenue)");
        JButton activeFlights = new JButton("Most Active Flights");
        JButton reservations = new JButton("Reservations (filter by flight # or customer name)");
        buttons.add(sales);
        buttons.add(revenueFlight);
        buttons.add(revenueAirline);
        buttons.add(revenueCustomer);
        buttons.add(bestCustomers);
        buttons.add(activeFlights);
        buttons.add(reservations);

        sales.addActionListener(e -> setModel(table,
            "SELECT DATE_FORMAT(purchase_datetime, '%Y-%m') AS month, COUNT(*) AS tickets_sold, " +
            "SUM(total_fare + booking_fee) AS revenue FROM ticket " +
            "WHERE DATE_FORMAT(purchase_datetime, '%Y-%m') = ? GROUP BY month",
            month.getText().trim()));

        revenueFlight.addActionListener(e -> setModel(table,
            "SELECT tf.airline_id, tf.flight_number, COUNT(*) AS seats_sold, " +
            "SUM(t.total_fare + t.booking_fee) AS revenue FROM ticket t " +
            "JOIN ticket_flight tf ON t.ticket_number = tf.ticket_number " +
            "GROUP BY tf.airline_id, tf.flight_number ORDER BY revenue DESC"));

        revenueAirline.addActionListener(e -> {
            String code = filterAirline.getText().trim().toUpperCase();
            if (code.isEmpty()) {
                setModel(table,
                    "SELECT a.airline_id, a.name, COUNT(*) AS legs_sold, " +
                    "SUM(t.total_fare + t.booking_fee) AS revenue FROM ticket t " +
                    "JOIN ticket_flight tf ON t.ticket_number = tf.ticket_number " +
                    "JOIN airline a ON tf.airline_id = a.airline_id " +
                    "GROUP BY a.airline_id, a.name ORDER BY revenue DESC");
            } else {
                setModel(table,
                    "SELECT a.airline_id, a.name, COUNT(*) AS legs_sold, " +
                    "SUM(t.total_fare + t.booking_fee) AS revenue FROM ticket t " +
                    "JOIN ticket_flight tf ON t.ticket_number = tf.ticket_number " +
                    "JOIN airline a ON tf.airline_id = a.airline_id WHERE a.airline_id = ? " +
                    "GROUP BY a.airline_id, a.name", code);
            }
        });

        revenueCustomer.addActionListener(e -> setModel(table,
            "SELECT c.customer_id, c.first_name, c.last_name, c.email, " +
            "SUM(t.total_fare + t.booking_fee) AS revenue FROM customer c " +
            "JOIN ticket t ON c.customer_id = t.customer_id " +
            "GROUP BY c.customer_id, c.first_name, c.last_name, c.email ORDER BY revenue DESC"));

        bestCustomers.addActionListener(e -> setModel(table,
            "SELECT c.customer_id, c.first_name, c.last_name, c.email, " +
            "COUNT(t.ticket_number) AS tickets_bought, " +
            "SUM(t.total_fare + t.booking_fee) AS total_spent FROM customer c " +
            "JOIN ticket t ON c.customer_id = t.customer_id " +
            "GROUP BY c.customer_id, c.first_name, c.last_name, c.email " +
            "ORDER BY total_spent DESC LIMIT 1"));

        activeFlights.addActionListener(e -> setModel(table,
            "SELECT tf.airline_id, tf.flight_number, COUNT(*) AS reservations FROM ticket_flight tf " +
            "GROUP BY tf.airline_id, tf.flight_number ORDER BY reservations DESC"));

        reservations.addActionListener(e -> {
            StringBuilder sql = new StringBuilder(
                "SELECT tf.airline_id, tf.flight_number, c.first_name, c.last_name, c.email, " +
                "t.ticket_number, tf.departure_date, tf.seat_number, tf.class FROM ticket t " +
                "JOIN customer c ON t.customer_id = c.customer_id " +
                "JOIN ticket_flight tf ON t.ticket_number = tf.ticket_number WHERE 1=1"
            );
            List<Object> params = new ArrayList<>();
            if (!filterFlight.getText().trim().isEmpty()) {
                sql.append(" AND tf.flight_number = ?");
                params.add(filterFlight.getText().trim());
            }
            if (!filterCustomer.getText().trim().isEmpty()) {
                sql.append(" AND (c.first_name LIKE ? OR c.last_name LIKE ? OR CONCAT(c.first_name,' ',c.last_name) LIKE ?)");
                String like = "%" + filterCustomer.getText().trim() + "%";
                params.add(like); params.add(like); params.add(like);
            }
            sql.append(" ORDER BY tf.airline_id, tf.flight_number, c.last_name");
            setModel(table, sql.toString(), params.toArray());
        });

        panel.add(top, BorderLayout.NORTH);
        panel.add(buttons, BorderLayout.CENTER);
        panel.add(new JScrollPane(table), BorderLayout.SOUTH);
        sales.doClick();
        return panel;
    }

    // ==================== Loaders & savers ====================

    private void loadFlights(JTable table) {
        setModel(table, "SELECT airline_id, flight_number, aircraft_id, departure_airport, arrival_airport, departure_time, arrival_time, is_domestic, operating_days, economy_fare, business_fare, first_fare FROM flight ORDER BY airline_id, flight_number");
    }

    private void loadCustomers(JTable table) {
        setModel(table, "SELECT customer_id, first_name, last_name, email, password, phone, street, city, state, zip_code FROM customer ORDER BY customer_id");
    }

    private void loadEmployees(JTable table) {
        setModel(table, "SELECT employee_id, first_name, last_name, email, password, phone, role FROM employee ORDER BY employee_id");
    }

    private void saveFlight(boolean update, JTextField airline, JTextField number, JTextField aircraft, JTextField from, JTextField to,
            JTextField depart, JTextField arrive, JTextField days, JTextField economy, JTextField business, JTextField first,
            JComboBox<String> domestic, JTable table) {
        String sql = update
            ? "UPDATE flight SET aircraft_id=?, departure_airport=?, arrival_airport=?, departure_time=?, arrival_time=?, is_domestic=?, operating_days=?, economy_fare=?, business_fare=?, first_fare=? WHERE airline_id=? AND flight_number=?"
            : "INSERT INTO flight (aircraft_id, departure_airport, arrival_airport, departure_time, arrival_time, is_domestic, operating_days, economy_fare, business_fare, first_fare, airline_id, flight_number) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(aircraft.getText().trim()));
            ps.setString(2, from.getText().trim().toUpperCase());
            ps.setString(3, to.getText().trim().toUpperCase());
            ps.setTime(4, Time.valueOf(depart.getText().trim()));
            ps.setTime(5, Time.valueOf(arrive.getText().trim()));
            ps.setBoolean(6, Boolean.parseBoolean((String) domestic.getSelectedItem()));
            ps.setString(7, days.getText().trim());
            ps.setBigDecimal(8, new BigDecimal(economy.getText().trim()));
            ps.setBigDecimal(9, new BigDecimal(business.getText().trim()));
            ps.setBigDecimal(10, new BigDecimal(first.getText().trim()));
            ps.setString(11, airline.getText().trim().toUpperCase());
            ps.setString(12, number.getText().trim());
            ps.executeUpdate();
            message(update ? "Flight updated." : "Flight added.");
            loadFlights(table);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void deleteSelectedFlight(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { message("Select a flight first."); return; }
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM flight WHERE airline_id = ? AND flight_number = ?")) {
            ps.setString(1, value(table, row, "airline_id"));
            ps.setString(2, value(table, row, "flight_number"));
            ps.executeUpdate();
            message("Flight deleted (if it had no reservations).");
            loadFlights(table);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void saveCustomer(boolean update, JTextField id, JTextField firstName, JTextField lastName, JTextField email, JTextField password,
            JTextField phone, JTextField street, JTextField city, JTextField state, JTextField zip, JTable table) {
        String sql = update
            ? "UPDATE customer SET first_name=?, last_name=?, email=?, password=?, phone=?, street=?, city=?, state=?, zip_code=? WHERE customer_id=?"
            : "INSERT INTO customer (first_name, last_name, email, password, phone, street, city, state, zip_code) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, firstName.getText().trim());
            ps.setString(2, lastName.getText().trim());
            ps.setString(3, email.getText().trim());
            ps.setString(4, password.getText().trim());
            ps.setString(5, phone.getText().trim());
            ps.setString(6, street.getText().trim());
            ps.setString(7, city.getText().trim());
            ps.setString(8, state.getText().trim());
            ps.setString(9, zip.getText().trim());
            if (update) ps.setInt(10, Integer.parseInt(id.getText()));
            ps.executeUpdate();
            message(update ? "Customer updated." : "Customer added.");
            loadCustomers(table);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void deleteSelectedCustomer(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { message("Select a customer first."); return; }
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM customer WHERE customer_id = ?")) {
            ps.setInt(1, Integer.parseInt(value(table, row, "customer_id")));
            ps.executeUpdate();
            message("Customer deleted.");
            loadCustomers(table);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void saveEmployee(boolean update, JTextField id, JTextField firstName, JTextField lastName, JTextField email,
            JTextField password, JTextField phone, JComboBox<String> role, JTable table) {
        String sql = update
            ? "UPDATE employee SET first_name=?, last_name=?, email=?, password=?, phone=?, role=? WHERE employee_id=?"
            : "INSERT INTO employee (first_name, last_name, email, password, phone, role) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, firstName.getText().trim());
            ps.setString(2, lastName.getText().trim());
            ps.setString(3, email.getText().trim());
            ps.setString(4, password.getText().trim());
            ps.setString(5, phone.getText().trim());
            ps.setString(6, (String) role.getSelectedItem());
            if (update) ps.setInt(7, Integer.parseInt(id.getText()));
            ps.executeUpdate();
            message(update ? "Employee updated." : "Employee added.");
            loadEmployees(table);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void deleteSelectedEmployee(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { message("Select an employee first."); return; }
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM employee WHERE employee_id = ?")) {
            ps.setInt(1, Integer.parseInt(value(table, row, "employee_id")));
            ps.executeUpdate();
            message("Employee deleted.");
            loadEmployees(table);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void saveAirport(boolean update, JTextField code, JTextField name, JTextField city, JTextField country, JTable table) {
        String sql = update
            ? "UPDATE airport SET airport_name=?, city=?, country=? WHERE airport_code=?"
            : "INSERT INTO airport (airport_name, city, country, airport_code) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name.getText().trim());
            ps.setString(2, city.getText().trim());
            ps.setString(3, country.getText().trim());
            ps.setString(4, code.getText().trim().toUpperCase());
            ps.executeUpdate();
            message(update ? "Airport updated." : "Airport added.");
            setModel(table, "SELECT * FROM airport ORDER BY airport_code");
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void saveAircraft(boolean update, JTextField id, JTextField airline, JTextField model, JTextField economy,
            JTextField business, JTextField first, JTable table) {
        String sql = update
            ? "UPDATE aircraft SET airline_id=?, model=?, num_economy_seats=?, num_business_seats=?, num_first_seats=? WHERE aircraft_id=?"
            : "INSERT INTO aircraft (airline_id, model, num_economy_seats, num_business_seats, num_first_seats) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, airline.getText().trim().toUpperCase());
            ps.setString(2, model.getText().trim());
            ps.setInt(3, Integer.parseInt(economy.getText().trim()));
            ps.setInt(4, Integer.parseInt(business.getText().trim()));
            ps.setInt(5, Integer.parseInt(first.getText().trim()));
            if (update) ps.setInt(6, Integer.parseInt(id.getText().trim()));
            ps.executeUpdate();
            message(update ? "Aircraft updated." : "Aircraft added.");
            setModel(table, "SELECT * FROM aircraft ORDER BY aircraft_id");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void deleteByKey(String tableName, String keyColumn, String keyValue, JTable table, String refreshSql) {
        if (keyValue == null || keyValue.trim().isEmpty()) { message("Select a row first."); return; }
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM " + tableName + " WHERE " + keyColumn + " = ?")) {
            ps.setString(1, keyValue);
            ps.executeUpdate();
            message("Row deleted (if it was not referenced by another record).");
            setModel(table, refreshSql);
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void showCreateCustomerDialog() {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        JTextField firstName = field("");
        JTextField lastName = field("");
        JTextField email = field("");
        JTextField password = field("");
        form.add(label("First Name")); form.add(firstName);
        form.add(label("Last Name")); form.add(lastName);
        form.add(label("Email")); form.add(email);
        form.add(label("Password")); form.add(password);
        int result = JOptionPane.showConfirmDialog(this, form, "Create Customer Account", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO customer (first_name, last_name, email, password) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, firstName.getText().trim());
                ps.setString(2, lastName.getText().trim());
                ps.setString(3, email.getText().trim());
                ps.setString(4, password.getText().trim());
                ps.executeUpdate();
                message("Account created. You can now log in as a customer.");
            } catch (SQLException ex) {
                showError(ex);
            }
        }
    }

    // ==================== Helpers ====================

    private DefaultTableModel queryModel(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                Vector<String> columns = new Vector<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) columns.add(meta.getColumnLabel(i));
                Vector<Vector<Object>> rows = new Vector<>();
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    for (int i = 1; i <= meta.getColumnCount(); i++) row.add(rs.getObject(i));
                    rows.add(row);
                }
                return new DefaultTableModel(rows, columns) {
                    @Override public boolean isCellEditable(int row, int column) { return false; }
                };
            }
        }
    }

    private void setModel(JTable table, String sql, Object... params) {
        try {
            table.setModel(queryModel(sql, params));
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private JTable table() {
        JTable table = new JTable();
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        return table;
    }

    private JTextField field() { return field(""); }

    private JTextField field(String text) {
        JTextField field = new JTextField(text);
        field.setFont(mainFont);
        return field;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(mainFont);
        return label;
    }

    private void addField(JPanel panel, String labelText, JComboBox<String> combo) {
        panel.add(label(labelText));
        panel.add(combo);
    }

    private void addField(JPanel panel, String labelText, JTextField field) {
        panel.add(label(labelText));
        panel.add(field);
    }

    private String value(JTable table, int row, String columnName) {
        int modelRow = table.convertRowIndexToModel(row);
        int viewColumn = table.getColumnModel().getColumnIndex(columnName);
        int modelColumn = table.convertColumnIndexToModel(viewColumn);
        Object v = table.getModel().getValueAt(modelRow, modelColumn);
        return v == null ? "" : v.toString();
    }

    private void message(String text) {
        JOptionPane.showMessageDialog(this, text);
    }

    private void showError(Exception ex) {
        JTextArea text = new JTextArea(ex.getMessage());
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        JOptionPane.showMessageDialog(this, new JScrollPane(text), "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    private void rollback() {
        try { con.rollback(); } catch (SQLException ignored) {}
    }

    private void resetAutoCommit() {
        try { con.setAutoCommit(true); } catch (SQLException ignored) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                new ProjectFrame(con).setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                    "Unable to connect to MySQL. Import project.sql, install MySQL Connector/J, and update DB_USER/DB_PASSWORD if needed.\n\n" + ex.getMessage(),
                    "Startup Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
