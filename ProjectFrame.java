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
import java.time.LocalDate;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setContentPane(root);
        showLogin();
    }

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
            tabs.addTab("Search and Book", searchAndBookPanel());
            tabs.addTab("My Reservations", customerReservationsPanel(currentCustomerId));
            tabs.addTab("Waiting List", waitingListPanel(currentCustomerId));
            tabs.addTab("Questions", customerQuestionsPanel());
        } else if ("customer_rep".equals(currentEmployeeRole)) {
            tabs.addTab("Flights", flightsPanel(false));
            tabs.addTab("Airports", airportsPanel());
            tabs.addTab("Aircraft", aircraftPanel());
            tabs.addTab("Customers", customersPanel());
            tabs.addTab("Reservations", representativeReservationsPanel());
            tabs.addTab("Waiting Lists", allWaitingListsPanel());
            tabs.addTab("Questions", representativeQuestionsPanel());
        } else if ("admin".equals(currentEmployeeRole)) {
            tabs.addTab("Employees", employeesPanel());
            tabs.addTab("Sales Reports", reportsPanel());
            tabs.addTab("Flights", flightsPanel(true));
            tabs.addTab("Customers", customersPanel());
        }
        page.add(tabs, BorderLayout.CENTER);

        root.add(page, "app");
        cards.show(root, "app");
        revalidate();
        repaint();
    }

    private String roleSuffix() {
        if (currentCustomerId > 0) {
            return " (customer)";
        }
        if ("admin".equals(currentEmployeeRole)) {
            return " (admin)";
        }
        return " (customer representative)";
    }

    private JPanel searchAndBookPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));

        JTextField from = field("EWR");
        JTextField to = field("LAX");
        JTextField date = field(LocalDate.now().plusWeeks(2).toString());
        JComboBox<String> seatClass = new JComboBox<>(new String[] {"economy", "business", "first"});
        JTextField meal = field("");
        JTable table = table();

        form.add(label("Departure Airport"));
        form.add(from);
        form.add(label("Arrival Airport"));
        form.add(to);
        form.add(label("Travel Date (YYYY-MM-DD)"));
        form.add(date);
        form.add(label("Class"));
        form.add(seatClass);
        form.add(label("Special Meal"));
        form.add(meal);

        JButton search = new JButton("Search Flights");
        JButton book = new JButton("Book Selected Flight");
        JButton waitlist = new JButton("Join Waitlist");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(search);
        actions.add(waitlist);
        actions.add(book);

        search.addActionListener(e -> loadFlightSearch(table, from.getText(), to.getText()));
        book.addActionListener(e -> bookSelectedFlight(table, currentCustomerId, date.getText(), (String) seatClass.getSelectedItem(), meal.getText()));
        waitlist.addActionListener(e -> addSelectedWaitlist(table, currentCustomerId, date.getText()));

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        loadFlightSearch(table, from.getText(), to.getText());
        return panel;
    }

    private JPanel customerReservationsPanel(int customerId) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JTable table = table();
        JButton refresh = new JButton("Refresh");
        JButton cancel = new JButton("Cancel Selected Ticket");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(refresh);
        actions.add(cancel);
        refresh.addActionListener(e -> loadCustomerReservations(table, customerId));
        cancel.addActionListener(e -> cancelSelectedTicket(table, customerId));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        loadCustomerReservations(table, customerId);
        return panel;
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

        JTextField id = field("");
        id.setEditable(false);
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

        JTextField id = field("");
        id.setEditable(false);
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

    private JPanel representativeReservationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JTable table = table();
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField customerEmail = field("customer@example.com");
        top.add(label("Customer Email"));
        top.add(customerEmail);
        JButton find = new JButton("Find Customer Reservations");
        JButton cancel = new JButton("Cancel Selected Ticket");
        top.add(find);
        top.add(cancel);
        find.addActionListener(e -> loadReservationsByEmail(table, customerEmail.getText()));
        cancel.addActionListener(e -> cancelSelectedTicket(table, -1));
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel allWaitingListsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JTable table = table();
        JButton refresh = new JButton("Refresh Waiting Lists");
        refresh.addActionListener(e -> loadAllWaitingLists(table));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(refresh, BorderLayout.SOUTH);
        loadAllWaitingLists(table);
        return panel;
    }

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
            if (row < 0) {
                message("Select a question first.");
                return;
            }
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
        actions.add(refresh);
        actions.add(add);
        actions.add(update);
        actions.add(delete);
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
        JTextField id = field("");
        id.setEditable(false);
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
        actions.add(refresh);
        actions.add(add);
        actions.add(update);
        actions.add(delete);
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

    private JPanel reportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JTable table = table();
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton sales = new JButton("Sales by Month");
        JButton revenueFlights = new JButton("Revenue by Flight");
        JButton revenueCustomers = new JButton("Revenue by Customer");
        JButton bestCustomers = new JButton("Best Customers");
        JButton activeFlights = new JButton("Most Active Flights");
        JButton waitlists = new JButton("Waiting Lists");
        JButton reservations = new JButton("Reservations by Flight/Customer");
        buttons.add(sales);
        buttons.add(revenueFlights);
        buttons.add(revenueCustomers);
        buttons.add(bestCustomers);
        buttons.add(activeFlights);
        buttons.add(waitlists);
        buttons.add(reservations);

        sales.addActionListener(e -> setModel(table, "SELECT DATE_FORMAT(purchase_datetime, '%Y-%m') AS month, COUNT(*) AS tickets_sold, SUM(total_fare + booking_fee) AS revenue FROM ticket GROUP BY month ORDER BY month"));
        revenueFlights.addActionListener(e -> setModel(table, "SELECT tf.airline_id, tf.flight_number, COUNT(*) AS seats_sold, SUM(t.total_fare + t.booking_fee) AS revenue FROM ticket t JOIN ticket_flight tf ON t.ticket_number = tf.ticket_number GROUP BY tf.airline_id, tf.flight_number ORDER BY revenue DESC"));
        revenueCustomers.addActionListener(e -> setModel(table, "SELECT c.customer_id, c.first_name, c.last_name, c.email, SUM(t.total_fare + t.booking_fee) AS revenue FROM customer c JOIN ticket t ON c.customer_id = t.customer_id GROUP BY c.customer_id, c.first_name, c.last_name, c.email ORDER BY revenue DESC"));
        bestCustomers.addActionListener(e -> setModel(table, "SELECT c.customer_id, c.first_name, c.last_name, c.email, COUNT(t.ticket_number) AS tickets_bought, SUM(t.total_fare + t.booking_fee) AS total_spent FROM customer c JOIN ticket t ON c.customer_id = t.customer_id GROUP BY c.customer_id, c.first_name, c.last_name, c.email ORDER BY total_spent DESC"));
        activeFlights.addActionListener(e -> setModel(table, "SELECT tf.airline_id, tf.flight_number, COUNT(*) AS reservations FROM ticket_flight tf GROUP BY tf.airline_id, tf.flight_number ORDER BY reservations DESC"));
        waitlists.addActionListener(e -> loadAllWaitingLists(table));
        reservations.addActionListener(e -> setModel(table, "SELECT tf.airline_id, tf.flight_number, c.first_name, c.last_name, c.email, t.ticket_number, tf.departure_date, tf.seat_number, tf.class FROM ticket t JOIN customer c ON t.customer_id = c.customer_id JOIN ticket_flight tf ON t.ticket_number = tf.ticket_number ORDER BY tf.airline_id, tf.flight_number, c.last_name"));

        panel.add(buttons, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        sales.doClick();
        return panel;
    }

    private void loadFlightSearch(JTable table, String from, String to) {
        setModel(table,
            "SELECT f.airline_id, a.name AS airline, f.flight_number, f.departure_airport, f.arrival_airport, " +
            "f.departure_time, f.arrival_time, f.operating_days, f.economy_fare, f.business_fare, f.first_fare " +
            "FROM flight f JOIN airline a ON f.airline_id = a.airline_id " +
            "WHERE f.departure_airport = ? AND f.arrival_airport = ? ORDER BY f.departure_time",
            from.trim().toUpperCase(), to.trim().toUpperCase());
    }

    private void loadFlights(JTable table) {
        setModel(table, "SELECT airline_id, flight_number, aircraft_id, departure_airport, arrival_airport, departure_time, arrival_time, is_domestic, operating_days, economy_fare, business_fare, first_fare FROM flight ORDER BY airline_id, flight_number");
    }

    private void loadCustomers(JTable table) {
        setModel(table, "SELECT customer_id, first_name, last_name, email, password, phone, street, city, state, zip_code FROM customer ORDER BY customer_id");
    }

    private void loadEmployees(JTable table) {
        setModel(table, "SELECT employee_id, first_name, last_name, email, password, phone, role FROM employee ORDER BY employee_id");
    }

    private void loadCustomerReservations(JTable table, int customerId) {
        setModel(table,
            "SELECT t.ticket_number, t.ticket_type, t.total_fare, t.booking_fee, t.purchase_datetime, " +
            "tf.leg_number, tf.airline_id, tf.flight_number, tf.departure_date, tf.seat_number, tf.class, tf.special_meal " +
            "FROM ticket t JOIN ticket_flight tf ON t.ticket_number = tf.ticket_number " +
            "WHERE t.customer_id = ? ORDER BY t.ticket_number, tf.leg_number",
            customerId);
    }

    private void loadReservationsByEmail(JTable table, String email) {
        setModel(table,
            "SELECT c.email, t.ticket_number, t.ticket_type, t.total_fare, t.booking_fee, tf.leg_number, " +
            "tf.airline_id, tf.flight_number, tf.departure_date, tf.seat_number, tf.class " +
            "FROM customer c JOIN ticket t ON c.customer_id = t.customer_id " +
            "JOIN ticket_flight tf ON t.ticket_number = tf.ticket_number WHERE c.email = ? " +
            "ORDER BY t.ticket_number, tf.leg_number",
            email.trim());
    }

    private void loadCustomerWaitlist(JTable table, int customerId) {
        setModel(table,
            "SELECT w.airline_id, w.flight_number, w.flight_date, w.request_datetime, f.departure_airport, f.arrival_airport " +
            "FROM waiting_list w JOIN flight f ON w.airline_id = f.airline_id AND w.flight_number = f.flight_number " +
            "WHERE w.customer_id = ? ORDER BY w.request_datetime",
            customerId);
    }

    private void loadAllWaitingLists(JTable table) {
        setModel(table,
            "SELECT c.customer_id, c.first_name, c.last_name, c.email, w.airline_id, w.flight_number, w.flight_date, w.request_datetime " +
            "FROM waiting_list w JOIN customer c ON w.customer_id = c.customer_id ORDER BY w.airline_id, w.flight_number, w.request_datetime");
    }

    private void loadQuestions(JTable table, String keyword) {
        String like = "%" + keyword.trim() + "%";
        setModel(table,
            "SELECT q.question_id, c.email AS customer_email, q.question_text, q.answer_text, q.asked_datetime, q.answered_datetime " +
            "FROM question q JOIN customer c ON q.customer_id = c.customer_id " +
            "WHERE q.question_text LIKE ? OR IFNULL(q.answer_text, '') LIKE ? ORDER BY q.asked_datetime DESC",
            like, like);
    }

    private void bookSelectedFlight(JTable table, int customerId, String dateText, String seatClass, String meal) {
        int row = table.getSelectedRow();
        if (row < 0) {
            message("Select a flight first.");
            return;
        }
        try {
            LocalDate departureDate = LocalDate.parse(dateText.trim());
            String airline = value(table, row, "airline_id");
            String flight = value(table, row, "flight_number");
            int available = availableSeats(airline, flight, departureDate, seatClass);
            if (available <= 0) {
                addWaitlist(customerId, airline, flight, departureDate);
                message("Flight is full. Customer added to the waiting list.");
                return;
            }

            BigDecimal fare = fareFor(table, row, seatClass);
            con.setAutoCommit(false);
            int ticketNumber;
            try (PreparedStatement ticket = con.prepareStatement(
                    "INSERT INTO ticket (customer_id, ticket_type, total_fare, booking_fee) VALUES (?, 'one-way', ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ticket.setInt(1, customerId);
                ticket.setBigDecimal(2, fare);
                ticket.setBigDecimal(3, DEFAULT_BOOKING_FEE);
                ticket.executeUpdate();
                try (ResultSet keys = ticket.getGeneratedKeys()) {
                    keys.next();
                    ticketNumber = keys.getInt(1);
                }
            }

            try (PreparedStatement leg = con.prepareStatement(
                    "INSERT INTO ticket_flight (ticket_number, leg_number, airline_id, flight_number, departure_date, seat_number, class, special_meal) VALUES (?, 1, ?, ?, ?, ?, ?, ?)")) {
                leg.setInt(1, ticketNumber);
                leg.setString(2, airline);
                leg.setString(3, flight);
                leg.setDate(4, Date.valueOf(departureDate));
                leg.setString(5, nextSeatNumber(airline, flight, departureDate, seatClass));
                leg.setString(6, seatClass);
                leg.setString(7, meal == null || meal.trim().isEmpty() ? null : meal.trim());
                leg.executeUpdate();
            }
            con.commit();
            message("Reservation created. Ticket number: " + ticketNumber);
        } catch (Exception ex) {
            rollback();
            showError(ex);
        } finally {
            resetAutoCommit();
        }
    }

    private void addSelectedWaitlist(JTable table, int customerId, String dateText) {
        int row = table.getSelectedRow();
        if (row < 0) {
            message("Select a flight first.");
            return;
        }
        try {
            addWaitlist(customerId, value(table, row, "airline_id"), value(table, row, "flight_number"), LocalDate.parse(dateText.trim()));
            message("Added to waiting list.");
        } catch (Exception ex) {
            showError(ex);
        }
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

    private int availableSeats(String airline, String flight, LocalDate date, String seatClass) throws SQLException {
        String capacityColumn = "num_" + seatClass + "_seats";
        String sql =
            "SELECT ac." + capacityColumn + " - COUNT(tf.ticket_number) AS available " +
            "FROM flight f JOIN aircraft ac ON f.aircraft_id = ac.aircraft_id " +
            "LEFT JOIN ticket_flight tf ON f.airline_id = tf.airline_id AND f.flight_number = tf.flight_number " +
            "AND tf.departure_date = ? AND tf.class = ? " +
            "WHERE f.airline_id = ? AND f.flight_number = ? GROUP BY ac." + capacityColumn;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ps.setString(2, seatClass);
            ps.setString(3, airline);
            ps.setString(4, flight);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("available") : 0;
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

    private BigDecimal fareFor(JTable table, int row, String seatClass) {
        String column = seatClass + "_fare";
        return new BigDecimal(value(table, row, column));
    }

    private void cancelSelectedTicket(JTable table, int ownerCustomerId) {
        int row = table.getSelectedRow();
        if (row < 0) {
            message("Select a ticket first.");
            return;
        }
        String ticketNumber = value(table, row, "ticket_number");
        String sql = ownerCustomerId > 0 ? "DELETE FROM ticket WHERE ticket_number = ? AND customer_id = ?" : "DELETE FROM ticket WHERE ticket_number = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(ticketNumber));
            if (ownerCustomerId > 0) {
                ps.setInt(2, ownerCustomerId);
            }
            int changed = ps.executeUpdate();
            message(changed > 0 ? "Ticket cancelled." : "No ticket was cancelled.");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void removeSelectedWaitlist(JTable table, int customerId) {
        int row = table.getSelectedRow();
        if (row < 0) {
            message("Select a waitlist entry first.");
            return;
        }
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
        if (row < 0) {
            message("Select a flight first.");
            return;
        }
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM flight WHERE airline_id = ? AND flight_number = ?")) {
            ps.setString(1, value(table, row, "airline_id"));
            ps.setString(2, value(table, row, "flight_number"));
            ps.executeUpdate();
            message("Flight deleted if it had no reservations.");
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
            if (update) {
                ps.setInt(10, Integer.parseInt(id.getText()));
            }
            ps.executeUpdate();
            message(update ? "Customer updated." : "Customer added.");
            loadCustomers(table);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void deleteSelectedCustomer(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            message("Select a customer first.");
            return;
        }
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
            if (update) {
                ps.setInt(7, Integer.parseInt(id.getText()));
            }
            ps.executeUpdate();
            message(update ? "Employee updated." : "Employee added.");
            loadEmployees(table);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void deleteSelectedEmployee(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            message("Select an employee first.");
            return;
        }
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
            if (update) {
                ps.setInt(6, Integer.parseInt(id.getText().trim()));
            }
            ps.executeUpdate();
            message(update ? "Aircraft updated." : "Aircraft added.");
            setModel(table, "SELECT * FROM aircraft ORDER BY aircraft_id");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void deleteByKey(String tableName, String keyColumn, String keyValue, JTable table, String refreshSql) {
        if (keyValue == null || keyValue.trim().isEmpty()) {
            message("Select a row first.");
            return;
        }
        try (PreparedStatement ps = con.prepareStatement("DELETE FROM " + tableName + " WHERE " + keyColumn + " = ?")) {
            ps.setString(1, keyValue);
            ps.executeUpdate();
            message("Row deleted if it was not referenced by another record.");
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
        form.add(label("First Name"));
        form.add(firstName);
        form.add(label("Last Name"));
        form.add(lastName);
        form.add(label("Email"));
        form.add(email);
        form.add(label("Password"));
        form.add(password);
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

    private DefaultTableModel queryModel(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                Vector<String> columns = new Vector<>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    columns.add(meta.getColumnLabel(i));
                }
                Vector<Vector<Object>> rows = new Vector<>();
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        row.add(rs.getObject(i));
                    }
                    rows.add(row);
                }
                return new DefaultTableModel(rows, columns) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
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

    private JTextField field() {
        return field("");
    }

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

    private void addField(JPanel panel, String labelText, JTextField field) {
        panel.add(label(labelText));
        panel.add(field);
    }

    private String value(JTable table, int row, String columnName) {
        int modelRow = table.convertRowIndexToModel(row);
        int modelColumn = table.getColumnModel().getColumnIndex(columnName);
        Object value = table.getModel().getValueAt(modelRow, table.convertColumnIndexToModel(modelColumn));
        return value == null ? "" : value.toString();
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
        try {
            con.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void resetAutoCommit() {
        try {
            con.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
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