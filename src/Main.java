import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;

// Seat class
class Seat {
    String seatNumber;
    String category;
    boolean isBooked;
    String customerName;

    public Seat(String seatNumber, String category) {
        this.seatNumber = seatNumber;
        this.category = category;
        this.isBooked = false;
        this.customerName = "";
    }

    public void book(String customerName) {
        this.isBooked = true;
        this.customerName = customerName;
    }

    public void cancel() {
        this.isBooked = false;
        this.customerName = "";
    }
}

// AVL Tree Node
class CategoryNode {
    String category;
    int price;
    int availableSeats;
    int height;
    CategoryNode left, right;

    public CategoryNode(String category, int price, int availableSeats) {
        this.category = category;
        this.price = price;
        this.availableSeats = availableSeats;
        this.height = 1;
    }
}

// Ticket System
class TicketSystem {
    private CategoryNode root;
    private final Map<String, Seat> seatMap = new HashMap<>();
    private final Stack<String> bookingStack = new Stack<>();
    private final Stack<String> cancellationStack = new Stack<>();
    private final Map<String, Long> expiryMap = new HashMap<>();
    private static final long EXPIRY_TIME = 5 * 60 * 1000;

    private int height(CategoryNode n) {
        return n == null ? 0 : n.height;
    }

    private int getBalance(CategoryNode n) {
        return n == null ? 0 : height(n.left) - height(n.right);
    }

    private CategoryNode rightRotate(CategoryNode y) {
        CategoryNode x = y.left;
        CategoryNode T2 = x.right;
        x.right = y;
        y.left = T2;
        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;
        return x;
    }

    private CategoryNode leftRotate(CategoryNode x) {
        CategoryNode y = x.right;
        CategoryNode T2 = y.left;
        y.left = x;
        x.right = T2;
        x.height = Math.max(height(x.left), height(x.right)) + 1;
        y.height = Math.max(height(y.left), height(y.right)) + 1;
        return y;
    }

    private CategoryNode insertCategory(CategoryNode node, String category, int price, int availableSeats) {
        if (node == null) return new CategoryNode(category, price, availableSeats);
        if (category.compareTo(node.category) < 0)
            node.left = insertCategory(node.left, category, price, availableSeats);
        else if (category.compareTo(node.category) > 0)
            node.right = insertCategory(node.right, category, price, availableSeats);
        else return node;

        node.height = 1 + Math.max(height(node.left), height(node.right));
        int balance = getBalance(node);

        if (balance > 1 && category.compareTo(node.left.category) < 0)
            return rightRotate(node);
        if (balance < -1 && category.compareTo(node.right.category) > 0)
            return leftRotate(node);
        if (balance > 1 && category.compareTo(node.left.category) > 0) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }
        if (balance < -1 && category.compareTo(node.right.category) < 0) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }
        return node;
    }

    public void addCategory(String category, int price, int availableSeats) {
        root = insertCategory(root, category, price, availableSeats);
    }

    private CategoryNode findCategory(String category) {
        CategoryNode current = root;
        while (current != null) {
            if (category.equals(current.category)) return current;
            if (category.compareTo(current.category) < 0)
                current = current.left;
            else
                current = current.right;
        }
        return null;
    }

    public void addSeat(String seatNumber, String category) {
        seatMap.put(seatNumber, new Seat(seatNumber, category));
        CategoryNode catNode = findCategory(category);
        if (catNode != null) {
            catNode.availableSeats++;
        }
    }

    public String bookSeat(String seatNumber, String customerName) {
        Seat seat = seatMap.get(seatNumber);
        if (seat == null) return "Seat does not exist.";
        if (seat.isBooked) return "Seat already booked.";

        CategoryNode catNode = findCategory(seat.category);
        if (catNode == null || catNode.availableSeats <= 0) return "Category unavailable.";

        seat.book(customerName);
        catNode.availableSeats--;
        bookingStack.push(seatNumber);
        expiryMap.put(seatNumber, System.currentTimeMillis());
        return "Seat " + seatNumber + " booked for " + customerName + ".";
    }

    public String cancelSeat(String seatNumber) {
        Seat seat = seatMap.get(seatNumber);
        if (seat == null || !seat.isBooked) return "Seat not booked.";

        CategoryNode catNode = findCategory(seat.category);
        seat.cancel();
        if (catNode != null) catNode.availableSeats++;
        cancellationStack.push(seatNumber);
        expiryMap.remove(seatNumber);
        return "Booking cancelled for seat " + seatNumber + ".";
    }

    public void checkExpiry() {
        long now = System.currentTimeMillis();
        List<String> expiredSeats = new ArrayList<>();
        for (Map.Entry<String, Long> entry : expiryMap.entrySet()) {
            if (now - entry.getValue() >= EXPIRY_TIME) {
                expiredSeats.add(entry.getKey());
            }
        }
        for (String seat : expiredSeats) {
            cancelSeat(seat);
        }
    }

    public String seatInfo(String seatNumber) {
        Seat seat = seatMap.get(seatNumber);
        if (seat == null) return "Seat not found.";
        return "Seat: " + seat.seatNumber + ", Category: " + seat.category + ", Booked: " + seat.isBooked + ", Customer: " + seat.customerName;
    }

    public void loadDataset(String csvPath) {
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 5) continue;
                String seatNumber = parts[0];
                String category = parts[1];
                int price = Integer.parseInt(parts[2]);
                String customerName = parts[3];
                boolean isBooked = Boolean.parseBoolean(parts[4]);

                if (findCategory(category) == null) {
                    addCategory(category, price, 0);
                }

                addSeat(seatNumber, category);
                if (isBooked) {
                    bookSeat(seatNumber, customerName);
                }
            }
            System.out.println("Dataset loaded successfully!");
        } catch (IOException e) {
            System.out.println("Error loading dataset: " + e.getMessage());
        }
    }
    public void saveToFile(String fileName) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            List<Seat> sortedSeats = new ArrayList<>(seatMap.values());

            // Sort by category prefix (V, P, E) and then by seat number (101, 102, ...)
            sortedSeats.sort(Comparator
                    .comparing((Seat seat) -> getPrefix(seat.seatNumber)) // V, P, E
                    .thenComparingInt(seat -> getSeatNumber(seat.seatNumber))); // 101–450

            for (Seat seat : sortedSeats) {
                CategoryNode catNode = findCategory(seat.category);
                int price = (catNode != null) ? catNode.price : 0;
                String line = seat.seatNumber + "," + seat.category + "," + price + "," + seat.customerName + "," + seat.isBooked;
                bw.write(line);
                bw.newLine();
            }

            System.out.println("Data saved successfully to " + fileName);
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }


    // Extract the prefix (e.g., 'V' from V101)
    private String getPrefix(String seatNumber) {
        return seatNumber.replaceAll("[0-9]", ""); // Removes digits, keeps letters
    }

    // Extract the numeric part (e.g., 101 from V101)
    private int getSeatNumber(String seatNumber) {
        try {
            return Integer.parseInt(seatNumber.replaceAll("\\D+", ""));
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE; // Send invalid ones to the end
        }
    }
}

// GUI class
class MainGUI {
    private final TicketSystem system = new TicketSystem();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI().createAndShowGUI());
    }

    public void createAndShowGUI() {
        JFrame frame = new JFrame("Ticket Reservation System");
        frame.setSize(700, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField seatField = new JTextField(15);
        JTextField nameField = new JTextField(15);
        JTextArea output = new JTextArea(15, 40);
        output.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(output);

        JButton bookBtn = new JButton("Book Seat");
        JButton cancelBtn = new JButton("Cancel Seat");
        JButton infoBtn = new JButton("Seat Info");
        JButton expiryBtn = new JButton("Check Expiry");
        JButton saveBtn = new JButton("Save to File");

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Seat No:"), gbc);
        gbc.gridx = 1;
        panel.add(seatField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(bookBtn, gbc);
        gbc.gridx = 1;
        panel.add(cancelBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(infoBtn, gbc);
        gbc.gridx = 1;
        panel.add(expiryBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(scrollPane, gbc);

        frame.add(panel);
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(saveBtn, gbc);



        // Load dataset (adjust path if needed)
        system.loadDataset("ticket_dataset.csv");

        bookBtn.addActionListener(e -> {
            String seat = seatField.getText().trim();
            String name = nameField.getText().trim();
            output.append(system.bookSeat(seat, name) + "\n");
        });

        cancelBtn.addActionListener(e -> {
            String seat = seatField.getText().trim();
            output.append(system.cancelSeat(seat) + "\n");
        });

        infoBtn.addActionListener(e -> {
            String seat = seatField.getText().trim();
            output.append(system.seatInfo(seat) + "\n");
        });

        expiryBtn.addActionListener(e -> {
            system.checkExpiry();
            output.append("Expired bookings checked and removed.\n");
        });
        saveBtn.addActionListener(e -> {
            system.saveToFile("ticket_data_out.csv");
            output.append("Data saved to ticket_data_out.csv\n");
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}