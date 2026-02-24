// Java program to compute availability/MTBF for components in series/parallel

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ReliabilityCalculator extends JFrame {
    
    // Variables
    JComboBox<String> configBox;
    JTextArea componentArea;
    JTextArea resultArea;
    ArrayList<Component> componentList;
    
    // Constructor
    public ReliabilityCalculator() {
        setTitle("Reliability Calculator");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        componentList = new ArrayList<>();
        
        // Creating panels
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Configuration:"));
        configBox = new JComboBox<>(new String[]{"Series", "Parallel"});
        topPanel.add(configBox);
        
        JButton addButton = new JButton("Add Component");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addComponent();
            }
        });
        topPanel.add(addButton);
        
        JButton sampleButton = new JButton("Load Sample");
        sampleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadSample();
            }
        });
        topPanel.add(sampleButton);
        
        JButton calcButton = new JButton("Calculate");
        calcButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                calculate();
            }
        });
        topPanel.add(calcButton);
        
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clear();
            }
        });
        topPanel.add(clearButton);
        
        // Component list area
        componentArea = new JTextArea(10, 30);
        componentArea.setEditable(false);
        JScrollPane compScroll = new JScrollPane(componentArea);
        
        // Result section
        resultArea = new JTextArea(10, 30);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane resScroll = new JScrollPane(resultArea);
        
        // Layout for panels
        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Components"));
        leftPanel.add(compScroll);
        
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Results"));
        rightPanel.add(resScroll);
        
        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        
        setVisible(true);
    }
    
    // Adding components
    void addComponent() {
        String name = JOptionPane.showInputDialog("Component Name:");
        if (name == null || name.isEmpty()) return;
        
        String mtbfStr = JOptionPane.showInputDialog("MTBF (hours):");
        if (mtbfStr == null) return;
        
        String availStr = JOptionPane.showInputDialog("Availability (%):");
        if (availStr == null) return;
        
        try {
            double mtbf = Double.parseDouble(mtbfStr);
            double avail = Double.parseDouble(availStr);
            
            Component comp = new Component(name, mtbf, avail);
            componentList.add(comp);
            updateComponentDisplay();
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input!");
        }
    }
    
    // Load sample data
    void loadSample() {
        componentList.clear();
        componentList.add(new Component("Generator", 8300, 98.0));
        componentList.add(new Component("Transformer", 8700, 99.5));
        componentList.add(new Component("Transmission Line", 8600, 99.5));
        componentList.add(new Component("Distribution", 8650, 99.0));
        updateComponentDisplay();
    }
    
    // Update display
    void updateComponentDisplay() {
        componentArea.setText("");
        for (int i = 0; i < componentList.size(); i++) {
            Component c = componentList.get(i);
            componentArea.append((i+1) + ". " + c.name + "\n");
            componentArea.append("   MTBF: " + c.mtbf + " hours\n");
            componentArea.append("   Availability: " + c.availability + "%\n\n");
        }
    }
    
    // Calculating
    void calculate() {
        if (componentList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add components first!");
            return;
        }
        
        try {
            // Build JSON
            String json = "{\"configuration\":\"" + configBox.getSelectedItem().toString().toLowerCase() + "\",";
            json += "\"components\":[";
            
            for (int i = 0; i < componentList.size(); i++) {
                Component c = componentList.get(i);
                json += "{\"name\":\"" + c.name + "\",";
                json += "\"mtbf\":" + c.mtbf + ",";
                json += "\"availability\":" + c.availability + "}";
                if (i < componentList.size() - 1) {
                    json += ",";
                }
            }
            json += "]}";
            
            // Send to backend
            String result = sendToServer(json);
            resultArea.setText(result);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    
    // Send to C++ server
    String sendToServer(String data) throws Exception {
        URL url = new URL("http://localhost:8080/calculate");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        
        // Send data
        OutputStream out = conn.getOutputStream();
        out.write(data.getBytes());
        out.close();
        
        // Get response
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        String response = "";
        while ((line = in.readLine()) != null) {
            response += line + "\n";
        }
        in.close();
        
        return response;
    }
    
    // Clear
    void clear() {
        componentList.clear();
        componentArea.setText("");
        resultArea.setText("");
    }
    
    // Main method
    public static void main(String[] args) {
        new ReliabilityCalculator();
    }
}

// Simple component class
class Component {
    String name;
    double mtbf;
    double availability;
    
    Component(String n, double m, double a) {
        name = n;
        mtbf = m;
        availability = a;
    }
}
