import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class Login extends JDialog {
    private boolean ok = false;
    private final JTextField user = new JTextField(10);
    private final JPasswordField pass = new JPasswordField(10);

    public Login(Frame f) {
        super(f, "Login", true);
        setLayout(new GridLayout(3, 2, 5, 5));

        add(new JLabel("User:")); add(user);
        add(new JLabel("Pass:")); add(pass);
        JButton loginBtn = new JButton("Login");
        JButton cancelBtn = new JButton("Cancel");
        add(loginBtn); add(cancelBtn);

        loginBtn.addActionListener(e -> {
            loginBtn.setEnabled(false);

            // Debug: print what user typed
            System.out.println("Attempt login -> user='" + user.getText() + "', pass='" + new String(pass.getPassword()) + "'");

            new SwingWorker<Boolean, Void>() {
                protected Boolean doInBackground() {
                    try (Connection c = DB.get();
                         PreparedStatement ps = c.prepareStatement(
                                 "SELECT 1 FROM users WHERE username=? AND password=?")) {
                        ps.setString(1, user.getText().trim());
                        ps.setString(2, new String(pass.getPassword()).trim());
                        try (ResultSet rs = ps.executeQuery()) {
                            return rs.next();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return false;
                    }
                }

                protected void done() {
                    try {
                        if (get()) {
                            ok = true;
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(Login.this, "Wrong login! Check console for debug info.");
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(Login.this, "Error: " + ex.getMessage());
                    } finally {
                        loginBtn.setEnabled(true);
                    }
                }
            }.execute();
        });

        cancelBtn.addActionListener(e -> dispose());
        pack();
        setLocationRelativeTo(f);
    }

    public boolean isOk() {
        return ok;
    }
}
