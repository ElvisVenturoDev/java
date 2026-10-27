package vallegrande.edu.pe.misistema.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ResumenDAO {
    public Resumen obtener() throws SQLException {
        try (Connection c = Conexion.conectar()) {
            return new Resumen(
                contar(c, "SELECT COUNT(*) FROM producto WHERE activo = 1"),
                contar(c, "SELECT COUNT(*) FROM producto p LEFT JOIN inventario i "
                        + "ON i.identificador_producto = p.identificador "
                        + "WHERE p.activo = 1 AND COALESCE(i.stock, 0) <= COALESCE(i.stock_minimo, 5)"),
                contar(c, "SELECT COUNT(*) FROM cliente"),
                contar(c, "SELECT COUNT(*) FROM venta"),
                contar(c, "SELECT COUNT(*) FROM mensaje_contacto"));
        }
    }

    private int contar(Connection c, String sql) {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return -1;
        }
    }
}
