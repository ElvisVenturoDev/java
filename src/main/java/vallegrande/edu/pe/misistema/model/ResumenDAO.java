package vallegrande.edu.pe.misistema.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ResumenDAO {
    public Resumen obtener() throws SQLException {
        try (Connection c = Conexion.conectar()) {
            return new Resumen(
                contar(c, "SELECT COUNT(*) FROM products WHERE active = 1"),
                contar(c, "SELECT COUNT(*) FROM products p LEFT JOIN inventory i "
                        + "ON i.product_id = p.id "
                        + "WHERE p.active = 1 AND COALESCE(i.stock, 0) <= COALESCE(i.min_stock, 5)"),
                contar(c, "SELECT COUNT(*) FROM customers"),
                contar(c, "SELECT COUNT(*) FROM sales"),
                contar(c, "SELECT COUNT(*) FROM contact_messages"));
        }
    }

    private int contar(Connection c, String sql) {
        try (PreparedStatement st = c.prepareStatement(sql); ResultSet rs = st.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return -1;
        }
    }
}
