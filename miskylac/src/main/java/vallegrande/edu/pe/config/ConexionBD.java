package vallegrande.edu.pe.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String URL =
            "jdbc:mysql://localhost:3307/miskylac?useSSL=false&serverTimezone=America/Lima&allowPublicKeyRetrieval=true";
    private static final String USER = "miskylac_user";
    private static final String PASSWORD = "miskylac123";

    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        try (Connection con = getConexion()) {
            System.out.println("Conectado: " + !con.isClosed());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}