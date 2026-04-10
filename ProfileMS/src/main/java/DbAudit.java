
import java.sql.*;

public class DbAudit {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/profiledb";
        String user = "root";
        String pass = "Lynxvi123.";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            Statement stmt = conn.createStatement();
            
            // Count Patients
            ResultSet rsP = stmt.executeQuery("SELECT COUNT(*) FROM patient");
            rsP.next();
            long countP = rsP.getLong(1);
            
            // Count Doctors
            ResultSet rsD = stmt.executeQuery("SELECT COUNT(*) FROM doctor");
            rsD.next();
            long countD = rsD.getLong(1);
            
            System.out.println("REAL_DB_PATIENTS=" + countP);
            System.out.println("REAL_DB_DOCTORS=" + countD);
            
        } catch (Exception e) {
            System.err.println("DB_ERROR: " + e.getMessage());
        }
    }
}
