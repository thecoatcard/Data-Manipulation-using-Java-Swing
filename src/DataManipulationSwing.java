import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.Vector;


class DataNode {
    String[] data;
    DataNode next;


    public DataNode(int columns) {
        data = new String[columns];
        next = null;
    }
}


class DataList {
    DataNode head;
    String[] headers;


    public DataList(String[] headers) {
        this.headers = headers;
        head = null;
    }


    public void insert(String[] rowData) {
        DataNode newNode = new DataNode(rowData.length);
        newNode.data = rowData;


        if (head == null) {
            head = newNode;
        } else {
            DataNode last = head;
            while (last.next != null) {
                last = last.next;
            }
            last.next = newNode;
        }
    }


    public DefaultTableModel getTableModel() {
        Vector<Vector<String>> dataVector = new Vector<>();
        DataNode current = head;
        while (current != null) {
            Vector<String> row = new Vector<>();
            for (String datum : current.data) {
                row.add(datum);
            }
            dataVector.add(row);
            current = current.next;
        }


        Vector<String> columnIdentifiers = new Vector<>();
        for (String header : headers) {
            columnIdentifiers.add(header);
        }


        return new DefaultTableModel(dataVector, columnIdentifiers);
    }


    public void sortByColumn(int columnIndex, boolean ascending) {
        head = mergeSort(head, columnIndex, ascending);
    }


    private DataNode mergeSort(DataNode node, int columnIndex, boolean ascending) {
        if (node == null || node.next == null) {
            return node;
        }


        DataNode middle = getMiddle(node);
        DataNode nextOfMiddle = middle.next;


        middle.next = null;


        DataNode left = mergeSort(node, columnIndex, ascending);
        DataNode right = mergeSort(nextOfMiddle, columnIndex, ascending);


        return sortedMerge(left, right, columnIndex, ascending);
    }


    private DataNode sortedMerge(DataNode left, DataNode right, int columnIndex, boolean ascending) {
        DataNode result = null;


        if (left == null) {
            return right;
        }


        if (right == null) {
            return left;
        }


        String leftData = left.data[columnIndex];
        String rightData = right.data[columnIndex];


        if (ascending ? leftData.compareTo(rightData) <= 0 : leftData.compareTo(rightData) >= 0) {
            result = left;
            result.next = sortedMerge(left.next, right, columnIndex, ascending);
        } else {
            result = right;
            result.next = sortedMerge(left, right.next, columnIndex, ascending);
        }


        return result;
    }


    private DataNode getMiddle(DataNode node) {
        if (node == null) {
            return node;
        }


        DataNode slow = node, fast = node.next;


        while (fast != null) {
            fast = fast.next;
            if (fast != null) {
                slow = slow.next;
                fast = fast.next;
            }
        }
        return slow;
    }


    public DefaultTableModel searchByColumn(int columnIndex, String query, boolean saveToFile, String fileName) {
        Vector<Vector<String>> searchResults = new Vector<>();
        boolean found = false;
        for (DataNode current = head; current != null; current = current.next) {
            for (String data : current.data) {
                if (data.toLowerCase().contains(query.toLowerCase())) {
                    Vector<String> row = new Vector<>();
                    for (String datum : current.data) {
                        row.add(datum);
                    }
                    searchResults.add(row);
                    found = true;
                    break; // Only add the row once if any column matches
                }
            }
        }


        if (!found) {
            JOptionPane.showMessageDialog(null, "No matching data found for query: " + query);
        } else if (saveToFile) {
            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write(convertToCSV(searchResults));
                JOptionPane.showMessageDialog(null, "Search results saved to " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        Vector<String> columnIdentifiers = new Vector<>();
        for (String header : headers) {
            columnIdentifiers.add(header);
        }


        return new DefaultTableModel(searchResults, columnIdentifiers);
    }


    private String convertToCSV(Vector<Vector<String>> data) {
        StringBuilder output = new StringBuilder();
        for (Vector<String> row : data) {
            for (String datum : row) {
                output.append(datum).append(",");
            }
            output.append("\n");
        }
        return output.toString();
    }


    public void updateEntry(int regNoIndex, int columnIndex, String regNo, String newValue) {
        for (DataNode current = head; current != null; current = current.next) {
            if (current.data.length > regNoIndex && current.data[regNoIndex].equals(regNo)) {
                if (columnIndex < current.data.length) {
                    current.data[columnIndex] = newValue;
                    JOptionPane.showMessageDialog(null, "Entry updated.");
                    return;
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid column index.");
                    return;
                }
            }
        }
        JOptionPane.showMessageDialog(null, "Data not found.");
    }


    public void deleteEntry(int regNoIndex, String regNo) {
        DataNode current = head;
        DataNode previous = null;
        while (current != null) {
            if (current.data.length > regNoIndex && current.data[regNoIndex].equals(regNo)) {
                if (previous == null) {
                    head = current.next;
                } else {
                    previous.next = current.next;
                }
                JOptionPane.showMessageDialog(null, "Entry deleted.");
                return;
            }
            previous = current;
            current = current.next;
        }
        JOptionPane.showMessageDialog(null, "Data not found.");
    }


    public void saveToFile(String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(String.join(",", headers) + "\n");
            for (DataNode current = head; current != null; current = current.next) {
                writer.write(String.join(",", current.data) + "\n");
            }
            JOptionPane.showMessageDialog(null, "Data saved to " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


public class DataManipulationSwing {
    private String[] headers;
    private DataList dataList;
    private JFrame mainFrame;
    private JTable dataTable;


    public DataManipulationSwing(String[] headers, DataList dataList) {
        this.headers = headers;
        this.dataList = dataList;
        initializeUI();
    }


    private void initializeUI() {
        mainFrame = new JFrame("Data Manipulation");
        mainFrame.setSize(800, 600);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);


        dataTable = new JTable(dataList.getTableModel());
        JScrollPane scrollPane = new JScrollPane(dataTable);
        mainFrame.add(scrollPane, BorderLayout.CENTER);


        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 3, 10, 10));


        JButton addButton = new JButton("Add");
        addButton.setBackground(new Color(70, 130, 180));
        addButton.setForeground(Color.WHITE);
        addButton.setToolTipText("Add new entry");
        addButton.addActionListener(e -> showAddDialog());


        JButton updateButton = new JButton("Update");
        updateButton.setBackground(new Color(34, 139, 34));
        updateButton.setForeground(Color.WHITE);
        updateButton.setToolTipText("Update existing entry");
        updateButton.addActionListener(e -> showUpdateDialog());


        JButton deleteButton = new JButton("Delete");
        deleteButton.setBackground(new Color(220, 20, 60));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setToolTipText("Delete an entry");
        deleteButton.addActionListener(e -> showDeleteDialog());


        JButton sortButton = new JButton("Sort");
        sortButton.setBackground(new Color(255, 140, 0));
        sortButton.setForeground(Color.WHITE);
        sortButton.setToolTipText("Sort data by column");
        sortButton.addActionListener(e -> showSortDialog());


        JButton searchButton = new JButton("Search");
        searchButton.setBackground(new Color(75, 0, 130));
        searchButton.setForeground(Color.WHITE);
        searchButton.setToolTipText("Search data by column");
        searchButton.addActionListener(e -> showSearchDialog());


        JButton saveButton = new JButton("Save");
        saveButton.setBackground(new Color(128, 0, 128));
        saveButton.setForeground(Color.WHITE);
        saveButton.setToolTipText("Save data to file");
        saveButton.addActionListener(e -> dataList.saveToFile("output.csv"));


        JButton loadButton = new JButton("Load CSV");
        loadButton.setBackground(new Color(0, 128, 128));
        loadButton.setForeground(Color.WHITE);
        loadButton.setToolTipText("Load CSV file");
        loadButton.addActionListener(e -> loadCSVFile());


        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(sortButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);


        mainFrame.add(buttonPanel, BorderLayout.SOUTH);
        mainFrame.setVisible(true);
    }


    private void loadCSVFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(mainFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            loadCSVData(selectedFile);
        }
    }


    private void loadCSVData(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            headers = line.split(",");
            dataList = new DataList(headers);


            while ((line = br.readLine()) != null) {
                String[] rowData = line.split(",");
                dataList.insert(rowData);
            }


            dataTable.setModel(dataList.getTableModel());
            mainFrame.setTitle("Data Manipulation - " + file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showAddDialog() {
        JTextField[] fields = new JTextField[headers.length];
        JPanel panel = new JPanel(new GridLayout(headers.length, 2));


        for (int i = 0; i < headers.length; i++) {
            panel.add(new JLabel(headers[i] + ":"));
            fields[i] = new JTextField();
            panel.add(fields[i]);
        }


        int result = JOptionPane.showConfirmDialog(null, panel, "Add Entry", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String[] rowData = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                rowData[i] = fields[i].getText();
            }
            dataList.insert(rowData);
            dataTable.setModel(dataList.getTableModel());
        }
    }


    private void showUpdateDialog() {
        JTextField regNoField = new JTextField();
        JComboBox<String> columnComboBox = new JComboBox<>(headers);
        JTextField newValueField = new JTextField();


        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Reg No:"));
        panel.add(regNoField);
        panel.add(new JLabel("Column Name:"));
        panel.add(columnComboBox);
        panel.add(new JLabel("New Value:"));
        panel.add(newValueField);


        int result = JOptionPane.showConfirmDialog(null, panel, "Update Entry", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int regNoIndex = 0; // Assuming Reg No is the first column
            int columnIndex = columnComboBox.getSelectedIndex();
            String regNo = regNoField.getText();
            String newValue = newValueField.getText();


            dataList.updateEntry(regNoIndex, columnIndex, regNo, newValue);
            dataTable.setModel(dataList.getTableModel());
        }
    }


    private void showDeleteDialog() {
        JTextField regNoField = new JTextField();
        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.add(new JLabel("Reg No:"));
        panel.add(regNoField);


        int result = JOptionPane.showConfirmDialog(null, panel, "Delete Entry", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int regNoIndex = 0; // Assuming Reg No is the first column
            String regNo = regNoField.getText();
            dataList.deleteEntry(regNoIndex, regNo);
            dataTable.setModel(dataList.getTableModel());
        }
    }


    private void showSortDialog() {
        JComboBox<String> columnComboBox = new JComboBox<>(headers);
        JCheckBox ascendingCheckBox = new JCheckBox("Ascending", true);
        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Column Name:"));
        panel.add(columnComboBox);
        panel.add(ascendingCheckBox);


        int result = JOptionPane.showConfirmDialog(null, panel, "Sort Data", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int columnIndex = columnComboBox.getSelectedIndex();
            boolean ascending = ascendingCheckBox.isSelected();
            dataList.sortByColumn(columnIndex, ascending);
            dataTable.setModel(dataList.getTableModel());
        }
    }


    private void showSearchDialog() {
        JComboBox<String> columnComboBox = new JComboBox<>(headers);
        JTextField queryField = new JTextField();
        JCheckBox saveToFileCheckBox = new JCheckBox("Save to File", false);


        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Column Name:"));
        panel.add(columnComboBox);
        panel.add(new JLabel("Query:"));
        panel.add(queryField);
        panel.add(saveToFileCheckBox);


        int result = JOptionPane.showConfirmDialog(null, panel, "Search Data", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int columnIndex = columnComboBox.getSelectedIndex();
            String query = queryField.getText();
            boolean saveToFile = saveToFileCheckBox.isSelected();
            String fileName = "search_results.csv";


            dataTable.setModel(dataList.searchByColumn(columnIndex, query, saveToFile, fileName));
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String[] initialHeaders = {"Reg No", "Name", "Course"};
            DataList dataList = new DataList(initialHeaders);
            new DataManipulationSwing(initialHeaders, dataList);
        });
    }
}

