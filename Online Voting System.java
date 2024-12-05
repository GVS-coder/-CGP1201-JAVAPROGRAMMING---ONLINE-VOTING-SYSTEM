import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.HashSet;

public class OnlineVotingSystem extends Frame implements ActionListener {
    private CardLayout cardLayout;

    private Panel loginPanel, registerPanel, votingPanel, electionTypePanel;
    private TextField voterIdField, usernameField, passwordField;
    private TextArea resultsArea;
    private Label statusLabel;
    private Choice candidateDropdown, electionTypeDropdown;

    // Data structures to hold user data and vote counts
    private HashMap<String, String> userDatabase; // Maps username to password
    private HashMap<String, String> voterIds; // Maps username to voter ID
    private HashMap<String, HashMap<String, Integer>> electionVoteCounts; // Election type -> (Candidate -> Vote Count)
    private HashMap<String, HashSet<String>> electionVoters; // Election type -> Set of voter IDs who have voted

    private String currentVoterId;

    public OnlineVotingSystem() {
        // Initialize user data storage
        userDatabase = new HashMap<>();
        voterIds = new HashMap<>();
        electionVoteCounts = new HashMap<>();
        electionVoters = new HashMap<>();

        // Initialize vote counts and voter records for each election type
        for (String electionType : new String[]{"Governmental", "Organizational", "Educational"}) {
            HashMap<String, Integer> voteCount = new HashMap<>();
            voteCount.put("Candidate A", 0);
            voteCount.put("Candidate B", 0);
            voteCount.put("Candidate C", 0);
            electionVoteCounts.put(electionType, voteCount);
            electionVoters.put(electionType, new HashSet<>());
        }

        // Set up the frame and layout
        setSize(400, 400);
        setTitle("Online Voting System");
        cardLayout = new CardLayout();
        setLayout(cardLayout);

        // Initialize panels
        createLoginPanel();
        createRegisterPanel();
        createElectionTypePanel();
        createVotingPanel();

        // Add panels to the card layout
        add(loginPanel, "Login");
        add(registerPanel, "Register");
        add(electionTypePanel, "ElectionType");
        add(votingPanel, "Voting");

        // Show the login panel by default
        showLoginPanel();

        // Close operation
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });
    }

    private void createLoginPanel() {
        loginPanel = new Panel(new GridLayout(6, 2));
        loginPanel.add(new Label("Voter ID:"));
        voterIdField = new TextField();
        loginPanel.add(voterIdField);
        loginPanel.add(new Label("Username:"));
        usernameField = new TextField();
        loginPanel.add(usernameField);
        loginPanel.add(new Label("Password:"));
        passwordField = new TextField();
        passwordField.setEchoChar('*');
        loginPanel.add(passwordField);

        Button loginButton = new Button("Login");
        loginButton.addActionListener(this);
        loginPanel.add(loginButton);

        Button registerButton = new Button("Go to Register");
        registerButton.addActionListener(e -> showRegisterPanel());
        loginPanel.add(registerButton);

        statusLabel = new Label("");
        loginPanel.add(statusLabel);
    }

    private void createRegisterPanel() {
        registerPanel = new Panel(new GridLayout(5, 2));
        TextField regVoterIdField = new TextField();
        TextField regUsernameField = new TextField();
        TextField regPasswordField = new TextField();
        regPasswordField.setEchoChar('*');

        registerPanel.add(new Label("Voter ID:"));
        registerPanel.add(regVoterIdField);
        registerPanel.add(new Label("Username:"));
        registerPanel.add(regUsernameField);
        registerPanel.add(new Label("Password:"));
        registerPanel.add(regPasswordField);

        Button submitRegisterButton = new Button("Register");
        submitRegisterButton.addActionListener(e -> {
            registerUser(regVoterIdField.getText(), regUsernameField.getText(), regPasswordField.getText());
        });
        registerPanel.add(submitRegisterButton);

        Button goToLoginButton = new Button("Already Registered? Go to Login");
        goToLoginButton.addActionListener(e -> showLoginPanel());
        registerPanel.add(goToLoginButton);
    }

    private void createElectionTypePanel() {
        electionTypePanel = new Panel(new GridLayout(3, 1));
        electionTypeDropdown = new Choice();
        electionTypeDropdown.add("Governmental");
        electionTypeDropdown.add("Organizational");
        electionTypeDropdown.add("Educational");
        electionTypePanel.add(new Label("Select the type of election:"));
        electionTypePanel.add(electionTypeDropdown);

        Button proceedButton = new Button("Proceed to Voting");
        proceedButton.addActionListener(e -> showVotingPanel());
        electionTypePanel.add(proceedButton);
    }

    private void createVotingPanel() {
        votingPanel = new Panel(new BorderLayout());
        votingPanel.add(new Label("Vote for Your Candidate"), BorderLayout.NORTH);

        // Panel for candidate selection
        Panel voteInputPanel = new Panel(new FlowLayout());
        candidateDropdown = new Choice();
        candidateDropdown.add("Candidate A");
        candidateDropdown.add("Candidate B");
        candidateDropdown.add("Candidate C");
        voteInputPanel.add(new Label("Select Candidate:"));
        voteInputPanel.add(candidateDropdown);

        Button voteButton = new Button("Vote");
        voteButton.addActionListener(e -> submitVote(candidateDropdown.getSelectedItem()));
        voteInputPanel.add(voteButton);

        votingPanel.add(voteInputPanel, BorderLayout.CENTER);

        // Results display area
        resultsArea = new TextArea(10, 30);
        resultsArea.setEditable(false);
        votingPanel.add(resultsArea, BorderLayout.SOUTH);
    }

    private void showLoginPanel() {
        cardLayout.show(this, "Login");
        clearFields();
        statusLabel.setText("");
    }

    private void showRegisterPanel() {
        cardLayout.show(this, "Register");
    }

    private void showElectionTypePanel() {
        cardLayout.show(this, "ElectionType");
    }

    private void showVotingPanel() {
        cardLayout.show(this, "Voting");
        updateResults();
    }

    private void clearFields() {
        voterIdField.setText("");
        usernameField.setText("");
        passwordField.setText("");
    }

    private void registerUser(String voterId, String username, String password) {
        if (voterId.isEmpty() || username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("All fields are required!");
            return;
        }
        if (userDatabase.containsKey(username)) {
            statusLabel.setText("Username already exists!");
            return;
        }
        userDatabase.put(username, password);
        voterIds.put(username, voterId);
        statusLabel.setText("Registration successful!");
    }

    private void submitVote(String candidate) {
        String electionType = electionTypeDropdown.getSelectedItem();

        // Check if the voter has already voted in this election
        if (electionVoters.get(electionType).contains(currentVoterId)) {
            resultsArea.setText("You have already voted in this election!");
            return;
        }

        // Record the vote
        HashMap<String, Integer> voteCount = electionVoteCounts.get(electionType);
        voteCount.put(candidate, voteCount.get(candidate) + 1);

        // Mark the voter as having voted
        electionVoters.get(electionType).add(currentVoterId);

        resultsArea.setText("Vote cast successfully!\n");
        updateResults();
    }

    private void updateResults() {
        String electionType = electionTypeDropdown.getSelectedItem();
        HashMap<String, Integer> voteCount = electionVoteCounts.get(electionType);
        resultsArea.append("Current Results for " + electionType + " Election:\n");
        voteCount.forEach((c, count) -> resultsArea.append(c + ": " + count + "\n"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String voterId = voterIdField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (!voterIds.containsKey(username) || !userDatabase.get(username).equals(password) || !voterIds.get(username).equals(voterId)) {
            statusLabel.setText("Invalid credentials!");
            return;
        }
        currentVoterId = voterId; // Save current voter ID
        statusLabel.setText("Login successful!");
        showElectionTypePanel();
    }

    public static void main(String[] args) {
        new OnlineVotingSystem().setVisible(true);
    }
}
