import java.sql.*;

public class DB {
    private static final String URL = "jdbc:sqlite:inventory.db?busy_timeout=5000";

    static {
        try {
            Class.forName("org.sqlite.JDBC"); // explicit driver registration
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void init() {
        System.out.println("Working directory: " + System.getProperty("user.dir"));

        try (Connection c = get(); Statement s = c.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON;");
            s.execute("CREATE TABLE IF NOT EXISTS users (username TEXT PRIMARY KEY, password TEXT)");
            s.execute("CREATE TABLE IF NOT EXISTS products (id TEXT PRIMARY KEY, name TEXT, qty INT)");

            // Insert default users
            s.execute("INSERT OR IGNORE INTO users VALUES ('admin','123')");
            s.execute("INSERT OR IGNORE INTO users VALUES ('user','pass')");

            // Debug: print all users in the database
            try (ResultSet rs = s.executeQuery("SELECT username, password FROM users")) {
                System.out.println("Users in DB:");
                while (rs.next()) {
                    System.out.println(" - " + rs.getString("username") + " | " + rs.getString("password"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
