/*
 * HospitalManagementSystem (HMSApp.java)
 * Single-file Java Swing application implementing a simple Hospital Management System.
 * - Uses Serializable objects for simple file-based persistence
 * - Provides GUI forms for Patients, Doctors, Appointments, Inventory and simple Billing
 * - Save/Load data to .dat files in the app working directory
 *
 * How to compile/run:
 * 1) javac HMSApp.java
 * 2) java HMSApp
 *
 * Note: This is a simplified but complete example intended for learning and small demos.
 * In production you'd want a proper DB (SQLite/MySQL), input validation, authentication,
 * multi-threading for I/O, and better error handling.
 */

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class HMSApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}

// -------------------- Data Models --------------------

class Patient implements Serializable {
    private static final long serialVersionUID = 1L;
    int id;
    String name;
    String gender;
    String phone;
    String address;
    Date dob;

    public Patient(int id, String name, String gender, String phone, String address, Date dob) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
        this.dob = dob;
    }
}

class Doctor implements Serializable {
    private static final long serialVersionUID = 1L;
    int id;
    String name;
    String specialization;
    String phone;

    public Doctor(int id, String name, String specialization, String phone) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.phone = phone;
    }
}

class Appointment implements Serializable {
    private static final long serialVersionUID = 1L;
    int id;
    int patientId;
    int doctorId;
    Date dateTime;
    String notes;

    public Appointment(int id, int patientId, int doctorId, Date dateTime, String notes) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.dateTime = dateTime;
        this.notes = notes;
    }
}

class InventoryItem implements Serializable {
    private static final long serialVersionUID = 1L;
    int id;
    String name;
    int quantity;
    double unitPrice;

    public InventoryItem(int id, String name, int quantity, double unitPrice) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
}

// -------------------- Data Store (file-based) --------------------

class DataStore {
    List<Patient> patients = new ArrayList<>();
    List<Doctor> doctors = new ArrayList<>();
    List<Appointment> appointments = new ArrayList<>();
    List<InventoryItem> inventory = new ArrayList<>();

    // Files
    private final File P_FILE = new File("patients.dat");
    private final File D_FILE = new File("doctors.dat");
    private final File A_FILE = new File("appointments.dat");
    private final File I_FILE = new File("inventory.dat");

    public DataStore() {
        loadAll();
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> readList(File f) {
        if (!f.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Object obj = ois.readObject();
            return (List<T>) obj;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void writeList(File f, List<?> list) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
            oos.writeObject(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadAll() {
        patients = readList(P_FILE);
        doctors = readList(D_FILE);
        appointments = readList(A_FILE);
        inventory = readList(I_FILE);
    }

    public void saveAll() {
        writeList(P_FILE, patients);
        writeList(D_FILE, doctors);
        writeList(A_FILE, appointments);
        writeList(I_FILE, inventory);
    }

    // Utility ID generators
    public int nextPatientId() { return nextIdFrom(patients.size(), patients.stream().map(p -> p.id).max(Integer::compare).orElse(0)); }
    public int nextDoctorId() { return nextIdFrom(doctors.size(), doctors.stream().map(d -> d.id).max(Integer::compare).orElse(0)); }
    public int nextAppointmentId() { return nextIdFrom(appointments.size(), appointments.stream().map(a -> a.id).max(Integer::compare).orElse(0)); }
    public int nextInventoryId() { return nextIdFrom(inventory.size(), inventory.stream().map(i -> i.id).max(Integer::compare).orElse(0)); }

    private int nextIdFrom(int size, int maxExisting) {
        return Math.max(size, maxExisting) + 1;
    }
}

// -------------------- MainFrame (GUI) --------------------

class MainFrame extends JFrame {
    DataStore store = new DataStore();

    PatientTableModel patientModel;
    DoctorTableModel doctorModel;
    AppointmentTableModel appointmentModel;
    InventoryTableModel inventoryModel;

    public MainFrame() {
        setTitle("Hospital Management System (HMS)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();

        // Models
        patientModel = new PatientTableModel(store.patients);
        doctorModel = new DoctorTableModel(store.doctors);
        appointmentModel = new AppointmentTableModel(store.appointments, store);
        inventoryModel = new InventoryTableModel(store.inventory);

        // Panels
        tabs.addTab("Patients", new PatientsPanel(this));
        tabs.addTab("Doctors", new DoctorsPanel(this));
        tabs.addTab("Appointments", new AppointmentsPanel(this));
        tabs.addTab("Inventory", new InventoryPanel(this));
        tabs.addTab("Billing", new BillingPanel(this));

        add(tabs, BorderLayout.CENTER);

        // Save on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                store.saveAll();
            }
        });
    }
}

// -------------------- Table Models --------------------

class PatientTableModel extends AbstractTableModel {
    private final String[] cols = {"ID", "Name", "Gender", "Phone", "Address", "DOB"};
    List<Patient> data;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    PatientTableModel(List<Patient> data) { this.data = data; }

    public int getRowCount() { return data.size(); }
    public int getColumnCount() { return cols.length; }
    public String getColumnName(int col) { return cols[col]; }

    public Object getValueAt(int r, int c) {
        Patient p = data.get(r);
        switch (c) {
            case 0: return p.id;
            case 1: return p.name;
            case 2: return p.gender;
            case 3: return p.phone;
            case 4: return p.address;
            case 5: return p.dob == null ? "" : sdf.format(p.dob);
        }
        return null;
    }
}

class DoctorTableModel extends AbstractTableModel {
    private final String[] cols = {"ID", "Name", "Specialization", "Phone"};
    List<Doctor> data;
    DoctorTableModel(List<Doctor> data) { this.data = data; }
    public int getRowCount() { return data.size(); }
    public int getColumnCount() { return cols.length; }
    public String getColumnName(int col) { return cols[col]; }
    public Object getValueAt(int r, int c) {
        Doctor d = data.get(r);
        switch(c) {
            case 0: return d.id;
            case 1: return d.name;
            case 2: return d.specialization;
            case 3: return d.phone;
        }
        return null;
    }
}

class AppointmentTableModel extends AbstractTableModel {
    private final String[] cols = {"ID", "Patient", "Doctor", "DateTime", "Notes"};
    List<Appointment> data;
    DataStore store;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    AppointmentTableModel(List<Appointment> data, DataStore store) { this.data = data; this.store = store; }
    public int getRowCount() { return data.size(); }
    public int getColumnCount() { return cols.length; }
    public String getColumnName(int col) { return cols[col]; }
    public Object getValueAt(int r, int c) {
        Appointment a = data.get(r);
        switch(c) {
            case 0: return a.id;
            case 1: return findPatientName(a.patientId);
            case 2: return findDoctorName(a.doctorId);
            case 3: return a.dateTime == null ? "" : sdf.format(a.dateTime);
            case 4: return a.notes;
        }
        return null;
    }

    private String findPatientName(int pid) {
        return store.patients.stream().filter(p -> p.id == pid).map(p -> p.name).findFirst().orElse("-deleted-");
    }
    private String findDoctorName(int did) {
        return store.doctors.stream().filter(d -> d.id == did).map(d -> d.name).findFirst().orElse("-deleted-");
    }
}

class InventoryTableModel extends AbstractTableModel {
    private final String[] cols = {"ID", "Name", "Quantity", "Unit Price"};
    List<InventoryItem> data;
    InventoryTableModel(List<InventoryItem> data) { this.data = data; }
    public int getRowCount() { return data.size(); }
    public int getColumnCount() { return cols.length; }
    public String getColumnName(int col) { return cols[col]; }
    public Object getValueAt(int r, int c) {
        InventoryItem it = data.get(r);
        switch(c) {
            case 0: return it.id;
            case 1: return it.name;
            case 2: return it.quantity;
            case 3: return it.unitPrice;
        }
        return null;
    }
}

// -------------------- Panels (forms) --------------------

class PatientsPanel extends JPanel {
    MainFrame parent;
    JTable table;
    public PatientsPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());

        table = new JTable(parent.patientModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel controls = new JPanel();
        JButton addBtn = new JButton("Add Patient");
        JButton editBtn = new JButton("Edit Selected");
        JButton delBtn = new JButton("Delete Selected");
        JButton saveBtn = new JButton("Save Data");

        controls.add(addBtn); controls.add(editBtn); controls.add(delBtn); controls.add(saveBtn);
        add(controls, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> openForm(null));
        editBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) openForm(parent.store.patients.get(r)); else showMsg("Select a row first.");
        });
        delBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) {
                int confirm = JOptionPane.showConfirmDialog(this, "Delete selected patient?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    parent.store.patients.remove(r);
                    parent.patientModel.fireTableDataChanged();
                }
            } else showMsg("Select a row first.");
        });
        saveBtn.addActionListener(e -> { parent.store.saveAll(); showMsg("Data saved."); });
    }

    private void openForm(Patient existing) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Patient Form", true);
        d.setSize(450, 400); d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(6,6,6,6); c.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameF = new JTextField();
        JTextField genderF = new JTextField();
        JTextField phoneF = new JTextField();
        JTextField addressF = new JTextField();
        JTextField dobF = new JTextField(); // yyyy-MM-dd

        if (existing != null) {
            nameF.setText(existing.name);
            genderF.setText(existing.gender);
            phoneF.setText(existing.phone);
            addressF.setText(existing.address);
            dobF.setText(existing.dob == null ? "" : new SimpleDateFormat("yyyy-MM-dd").format(existing.dob));
        }

        int row = 0;
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Name:"), c);
        c.gridx = 1; p.add(nameF, c);
        row++;
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Gender:"), c);
        c.gridx = 1; p.add(genderF, c);
        row++;
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Phone:"), c);
        c.gridx = 1; p.add(phoneF, c);
        row++;
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Address:"), c);
        c.gridx = 1; p.add(addressF, c);
        row++;
        c.gridx = 0; c.gridy = row; p.add(new JLabel("DOB (yyyy-MM-dd):"), c);
        c.gridx = 1; p.add(dobF, c);
        row++;

        JButton submit = new JButton(existing == null ? "Add" : "Update");
        c.gridx = 0; c.gridy = row; c.gridwidth = 2; p.add(submit, c);

        submit.addActionListener(ev -> {
            try {
                String name = nameF.getText().trim();
                String gender = genderF.getText().trim();
                String phone = phoneF.getText().trim();
                String address = addressF.getText().trim();
                String dobStr = dobF.getText().trim();
                Date dob = null;
                if (!dobStr.isEmpty()) dob = new SimpleDateFormat("yyyy-MM-dd").parse(dobStr);
                if (name.isEmpty()) { showMsg("Name required."); return; }
                if (existing == null) {
                    int id = parent.store.nextPatientId();
                    Patient pObj = new Patient(id, name, gender, phone, address, dob);
                    parent.store.patients.add(pObj);
                } else {
                    existing.name = name; existing.gender = gender; existing.phone = phone; existing.address = address; existing.dob = dob;
                }
                parent.patientModel.fireTableDataChanged();
                d.dispose();
            } catch (Exception ex) { ex.printStackTrace(); showMsg("Error: " + ex.getMessage()); }
        });

        d.add(p); d.setVisible(true);
    }

    private void showMsg(String s) { JOptionPane.showMessageDialog(this, s); }
}

class DoctorsPanel extends JPanel {
    MainFrame parent;
    JTable table;
    public DoctorsPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());

        table = new JTable(parent.doctorModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel controls = new JPanel();
        JButton addBtn = new JButton("Add Doctor");
        JButton editBtn = new JButton("Edit Selected");
        JButton delBtn = new JButton("Delete Selected");
        JButton saveBtn = new JButton("Save Data");

        controls.add(addBtn); controls.add(editBtn); controls.add(delBtn); controls.add(saveBtn);
        add(controls, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> openForm(null));
        editBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) openForm(parent.store.doctors.get(r)); else showMsg("Select a row first.");
        });
        delBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) {
                int confirm = JOptionPane.showConfirmDialog(this, "Delete selected doctor?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    parent.store.doctors.remove(r);
                    parent.doctorModel.fireTableDataChanged();
                }
            } else showMsg("Select a row first.");
        });
        saveBtn.addActionListener(e -> { parent.store.saveAll(); showMsg("Data saved."); });
    }

    private void openForm(Doctor existing) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Doctor Form", true);
        d.setSize(400, 300); d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridLayout(5,2,8,8));

        JTextField nameF = new JTextField();
        JTextField specF = new JTextField();
        JTextField phoneF = new JTextField();

        if (existing != null) { nameF.setText(existing.name); specF.setText(existing.specialization); phoneF.setText(existing.phone); }

        p.add(new JLabel("Name:")); p.add(nameF);
        p.add(new JLabel("Specialization:")); p.add(specF);
        p.add(new JLabel("Phone:")); p.add(phoneF);

        JButton submit = new JButton(existing == null ? "Add" : "Update");
        p.add(submit);
        submit.addActionListener(ev -> {
            String name = nameF.getText().trim();
            String spec = specF.getText().trim();
            String phone = phoneF.getText().trim();
            if (name.isEmpty()) { showMsg("Name required."); return; }
            if (existing == null) {
                int id = parent.store.nextDoctorId();
                parent.store.doctors.add(new Doctor(id, name, spec, phone));
            } else {
                existing.name = name; existing.specialization = spec; existing.phone = phone;
            }
            parent.doctorModel.fireTableDataChanged();
            d.dispose();
        });

        d.add(p); d.setVisible(true);
    }
    private void showMsg(String s) { JOptionPane.showMessageDialog(this, s); }
}

class AppointmentsPanel extends JPanel {
    MainFrame parent;
    JTable table;
    public AppointmentsPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());

        table = new JTable(parent.appointmentModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel controls = new JPanel();
        JButton addBtn = new JButton("Add Appointment");
        JButton editBtn = new JButton("Edit Selected");
        JButton delBtn = new JButton("Delete Selected");
        JButton saveBtn = new JButton("Save Data");

        controls.add(addBtn); controls.add(editBtn); controls.add(delBtn); controls.add(saveBtn);
        add(controls, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> openForm(null));
        editBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) openForm(parent.store.appointments.get(r)); else showMsg("Select a row first.");
        });
        delBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) {
                int confirm = JOptionPane.showConfirmDialog(this, "Delete selected appointment?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    parent.store.appointments.remove(r);
                    parent.appointmentModel.fireTableDataChanged();
                }
            } else showMsg("Select a row first.");
        });
        saveBtn.addActionListener(e -> { parent.store.saveAll(); showMsg("Data saved."); });
    }

    private void openForm(Appointment existing) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Appointment Form", true);
        d.setSize(500, 350); d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(6,6,6,6); c.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> patientCb = new JComboBox<>();
        for (Patient pt : parent.store.patients) patientCb.addItem(pt.id + ": " + pt.name);
        JComboBox<String> doctorCb = new JComboBox<>();
        for (Doctor dt : parent.store.doctors) doctorCb.addItem(dt.id + ": " + dt.name + " (" + dt.specialization + ")");

        JTextField datetimeF = new JTextField(); // yyyy-MM-dd HH:mm
        JTextArea notesA = new JTextArea(4, 20);

        if (existing != null) {
            // try select appropriate indices
            selectComboById(patientCb, existing.patientId);
            selectComboById(doctorCb, existing.doctorId);
            datetimeF.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(existing.dateTime));
            notesA.setText(existing.notes);
        }

        int row = 0;
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Patient:"), c);
        c.gridx = 1; p.add(patientCb, c); row++;
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Doctor:"), c);
        c.gridx = 1; p.add(doctorCb, c); row++;
        c.gridx = 0; c.gridy = row; p.add(new JLabel("DateTime (yyyy-MM-dd HH:mm):"), c);
        c.gridx = 1; p.add(datetimeF, c); row++;
        c.gridx = 0; c.gridy = row; p.add(new JLabel("Notes:"), c);
        c.gridx = 1; p.add(new JScrollPane(notesA), c); row++;

        JButton submit = new JButton(existing == null ? "Add" : "Update");
        c.gridx = 0; c.gridy = row; c.gridwidth = 2; p.add(submit, c);

        submit.addActionListener(ev -> {
            try {
                if (patientCb.getItemCount() == 0 || doctorCb.getItemCount() == 0) { showMsg("Add patients and doctors first."); return; }
                int pid = extractIdFromCombo((String)patientCb.getSelectedItem());
                int did = extractIdFromCombo((String)doctorCb.getSelectedItem());
                String dtStr = datetimeF.getText().trim();
                Date dt = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(dtStr);
                String notes = notesA.getText().trim();
                if (existing == null) {
                    int id = parent.store.nextAppointmentId();
                    parent.store.appointments.add(new Appointment(id, pid, did, dt, notes));
                } else {
                    existing.patientId = pid; existing.doctorId = did; existing.dateTime = dt; existing.notes = notes;
                }
                parent.appointmentModel.fireTableDataChanged();
                d.dispose();
            } catch (Exception ex) { ex.printStackTrace(); showMsg("Error: " + ex.getMessage()); }
        });

        d.add(p); d.setVisible(true);
    }

    private void selectComboById(JComboBox<String> cb, int id) {
        for (int i=0;i<cb.getItemCount();i++) {
            String s = cb.getItemAt(i);
            if (s.startsWith(id + ":")) { cb.setSelectedIndex(i); return; }
        }
    }
    private int extractIdFromCombo(String s) { return Integer.parseInt(s.split(":")[0]); }
    private void showMsg(String s) { JOptionPane.showMessageDialog(this, s); }
}

class InventoryPanel extends JPanel {
    MainFrame parent;
    JTable table;
    public InventoryPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());

        table = new JTable(parent.inventoryModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel controls = new JPanel();
        JButton addBtn = new JButton("Add Item");
        JButton editBtn = new JButton("Edit Selected");
        JButton delBtn = new JButton("Delete Selected");
        JButton restockBtn = new JButton("Restock Selected");
        JButton saveBtn = new JButton("Save Data");

        controls.add(addBtn); controls.add(editBtn); controls.add(delBtn); controls.add(restockBtn); controls.add(saveBtn);
        add(controls, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> openForm(null));
        editBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) openForm(parent.store.inventory.get(r)); else showMsg("Select a row first.");
        });
        delBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) {
                int confirm = JOptionPane.showConfirmDialog(this, "Delete selected item?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    parent.store.inventory.remove(r);
                    parent.inventoryModel.fireTableDataChanged();
                }
            } else showMsg("Select a row first.");
        });
        restockBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) {
                String qty = JOptionPane.showInputDialog(this, "Enter additional quantity:");
                try { int add = Integer.parseInt(qty); parent.store.inventory.get(r).quantity += add; parent.inventoryModel.fireTableDataChanged(); }
                catch (Exception ex) { showMsg("Invalid number."); }
            } else showMsg("Select a row first.");
        });
        saveBtn.addActionListener(e -> { parent.store.saveAll(); showMsg("Data saved."); });
    }

    private void openForm(InventoryItem existing) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Inventory Item", true);
        d.setSize(400, 300); d.setLocationRelativeTo(this);
        JPanel p = new JPanel(new GridLayout(5,2,8,8));

        JTextField nameF = new JTextField();
        JTextField qtyF = new JTextField();
        JTextField priceF = new JTextField();

        if (existing != null) { nameF.setText(existing.name); qtyF.setText(String.valueOf(existing.quantity)); priceF.setText(String.valueOf(existing.unitPrice)); }

        p.add(new JLabel("Name:")); p.add(nameF);
        p.add(new JLabel("Quantity:")); p.add(qtyF);
        p.add(new JLabel("Unit Price:")); p.add(priceF);

        JButton submit = new JButton(existing == null ? "Add" : "Update");
        p.add(submit);
        submit.addActionListener(ev -> {
            try {
                String name = nameF.getText().trim();
                int qty = Integer.parseInt(qtyF.getText().trim());
                double price = Double.parseDouble(priceF.getText().trim());
                if (name.isEmpty()) { showMsg("Name required."); return; }
                if (existing == null) {
                    int id = parent.store.nextInventoryId();
                    parent.store.inventory.add(new InventoryItem(id, name, qty, price));
                } else {
                    existing.name = name; existing.quantity = qty; existing.unitPrice = price;
                }
                parent.inventoryModel.fireTableDataChanged();
                d.dispose();
            } catch (Exception ex) { ex.printStackTrace(); showMsg("Error: " + ex.getMessage()); }
        });

        d.add(p); d.setVisible(true);
    }
    private void showMsg(String s) { JOptionPane.showMessageDialog(this, s); }
}

class BillingPanel extends JPanel {
    MainFrame parent;
    public BillingPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(4,2,8,8));
        JComboBox<String> patientCb = new JComboBox<>();
        for (Patient p : parent.store.patients) patientCb.addItem(p.id + ": " + p.name);
        top.add(new JLabel("Patient:")); top.add(patientCb);

        JTextField serviceChargeF = new JTextField("0");
        JTextField medicineChargeF = new JTextField("0");
        top.add(new JLabel("Service Charge:")); top.add(serviceChargeF);
        top.add(new JLabel("Medicine Charge:")); top.add(medicineChargeF);

        add(top, BorderLayout.NORTH);

        JTextArea billArea = new JTextArea(); billArea.setEditable(false);
        add(new JScrollPane(billArea), BorderLayout.CENTER);

        JButton genBtn = new JButton("Generate Bill");
        JButton saveBtn = new JButton("Save Bill to File");
        JPanel bottom = new JPanel(); bottom.add(genBtn); bottom.add(saveBtn);
        add(bottom, BorderLayout.SOUTH);

        genBtn.addActionListener(e -> {
            if (patientCb.getItemCount() == 0) { JOptionPane.showMessageDialog(this, "No patients available."); return; }
            int pid = Integer.parseInt(((String)patientCb.getSelectedItem()).split(":")[0]);
            Patient p = parent.store.patients.stream().filter(x -> x.id == pid).findFirst().orElse(null);
            double service = parseDoubleOrZero(serviceChargeF.getText());
            double med = parseDoubleOrZero(medicineChargeF.getText());
            double tax = (service + med) * 0.18; // example GST 18%
            double total = service + med + tax;
            StringBuilder sb = new StringBuilder();
            sb.append("--- Hospital Bill ---\n");
            sb.append("Patient: " + (p==null?"-" : p.name) + "\n");
            sb.append("Service: " + service + "\n");
            sb.append("Medicine: " + med + "\n");
            sb.append("Tax (18%): " + String.format("%.2f", tax) + "\n");
            sb.append("TOTAL: " + String.format("%.2f", total) + "\n");
            billArea.setText(sb.toString());
        });

        saveBtn.addActionListener(e -> {
            try {
                JFileChooser fc = new JFileChooser();
                if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    try (PrintWriter pw = new PrintWriter(f)) { pw.write(((JTextArea)((JScrollPane)getComponent(1)).getViewport().getView()).getText()); }
                    JOptionPane.showMessageDialog(this, "Saved to " + f.getAbsolutePath());
                }
            } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage()); }
        });
    }

    private double parseDoubleOrZero(String s) { try { return Double.parseDouble(s.trim()); } catch (Exception e) { return 0.0; } }
}

// End of file
