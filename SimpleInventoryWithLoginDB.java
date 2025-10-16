import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class SimpleInventoryWithLoginDB extends JFrame {
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"ID","Name","Qty"}, 0);
    private final JTable table = new JTable(model);
    private final JTextField tId = new JTextField(8), tName = new JTextField(12), tQty = new JTextField(5);

    private final JButton addBtn = new JButton("Add");
    private final JButton updBtn = new JButton("Update");
    private final JButton delBtn = new JButton("Delete");

    public SimpleInventoryWithLoginDB() {
        super("Inventory (with DB)");
        setSize(600, 420);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(6, 6));

        JPanel form = new JPanel();
        form.add(new JLabel("ID:")); form.add(tId);
        form.add(new JLabel("Name:")); form.add(tName);
        form.add(new JLabel("Qty:")); form.add(tQty);
        form.add(addBtn); form.add(updBtn); form.add(delBtn);

        add(form, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadData();

        addBtn.addActionListener(e -> addProduct());
        updBtn.addActionListener(e -> updateProduct());
        delBtn.addActionListener(e -> deleteProduct());
    }

    private void loadData() {
        model.setRowCount(0);
        new SwingWorker<Void, Object[]>() {
            protected Void doInBackground() {
                try (Connection c = DB.get();
                     Statement s = c.createStatement();
                     ResultSet rs = s.executeQuery("SELECT * FROM products")) {
                    while (rs.next()) {
                        publish(new Object[]{rs.getString("id"), rs.getString("name"), rs.getInt("qty")});
                    }
                } catch (Exception e) { e.printStackTrace(); }
                return null;
            }
            protected void process(java.util.List<Object[]> rows) {
                for (Object[] r : rows) model.addRow(r);
            }
        }.execute();
    }

    private void addProduct() {
        String id = tId.getText().trim();
        String name = tName.getText().trim();
        String qtyStr = tQty.getText().trim();

        if (id.isEmpty() || name.isEmpty() || qtyStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill all fields");
            return;
        }
        int qty;
        try { qty = Integer.parseInt(qtyStr); }
        catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Qty must be a number"); return; }

        new SwingWorker<Void, Void>() {
            protected Void doInBackground() {
                try (Connection c = DB.get();
                     PreparedStatement ps = c.prepareStatement("INSERT INTO products VALUES (?, ?, ?)")) {
                    ps.setString(1, id);
                    ps.setString(2, name);
                    ps.setInt(3, qty);
                    ps.executeUpdate();
                } catch (Exception ex) { SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(SimpleInventoryWithLoginDB.this, "Error: " + ex.getMessage())); }
                return null;
            }
            protected void done() { loadData(); clear(); }
        }.execute();
    }

    private void updateProduct() {
        String id = tId.getText().trim();
        String qtyStr = tQty.getText().trim();

        if (id.isEmpty() || qtyStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter ID and Qty");
            return;
        }
        int qty;
        try { qty = Integer.parseInt(qtyStr); }
        catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Qty must be number"); return; }

        new SwingWorker<Void, Void>() {
            protected Void doInBackground() {
                try (Connection c = DB.get();
                     PreparedStatement ps = c.prepareStatement("UPDATE products SET qty=? WHERE id=?")) {
                    ps.setInt(1, qty);
                    ps.setString(2, id);
                    ps.executeUpdate();
                } catch (Exception ex) { SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(SimpleInventoryWithLoginDB.this, "Error: " + ex.getMessage())); }
                return null;
            }
            protected void done() { loadData(); }
        }.execute();
    }

    private void deleteProduct() {
        String id = tId.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter ID to delete");
            return;
        }

        new SwingWorker<Void, Void>() {
            protected Void doInBackground() {
                try (Connection c = DB.get();
                     PreparedStatement ps = c.prepareStatement("DELETE FROM products WHERE id=?")) {
                    ps.setString(1, id);
                    ps.executeUpdate();
                } catch (Exception ex) { SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(SimpleInventoryWithLoginDB.this, "Error: " + ex.getMessage())); }
                return null;
            }
            protected void done() { loadData(); clear(); }
        }.execute();
    }

    private void clear() {
        tId.setText(""); tName.setText(""); tQty.setText("");
    }

    public static void main(String[] args) {
        DB.init();
        SwingUtilities.invokeLater(() -> {
            Login login = new Login(null);
            login.setVisible(true);
            if (login.isOk())
                new SimpleInventoryWithLoginDB().setVisible(true);
            else
                System.exit(0);
        });
    }
}
