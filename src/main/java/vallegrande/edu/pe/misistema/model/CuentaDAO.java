package vallegrande.edu.pe.misistema.model;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Cuentas de clientes (tablas customers y customer_details, como en la web). */
public class CuentaDAO {

    public List<Cuenta> listar() throws SQLException {
        List<Cuenta> lista = new ArrayList<>();
        try (Connection c = Conexion.conectar();
             PreparedStatement st = c.prepareStatement(
                     "SELECT id, name, phone, email, active FROM customers ORDER BY id");
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                lista.add(new Cuenta(rs.getInt("id"), rs.getString("name"),
                        rs.getString("phone"), rs.getString("email"), rs.getBoolean("active")));
            }
        }
        return lista;
    }

    public void insertar(Cuenta cu) throws SQLException {
        try (Connection c = Conexion.conectar()) {
            c.setAutoCommit(false);
            try {
                int id;
                // Igual que la web: cliente sin contraseña (password_hash vacío) hasta que se registre.
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO customers (name, phone, email, password_hash) VALUES (?, ?, ?, '')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, cu.getNombre());
                    ps.setString(2, vacioANulo(cu.getTelefono()));
                    ps.setString(3, cu.getCorreo());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        id = keys.getInt(1);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO customer_details (customer_id, address, customer_type, registered_on) "
                                + "VALUES (?, 'Sin dirección', 'Minorista', ?) "
                                + "ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP")) {
                    ps.setInt(1, id);
                    ps.setDate(2, Date.valueOf(LocalDate.now()));
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void actualizar(Cuenta cu) throws SQLException {
        try (Connection c = Conexion.conectar();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE customers SET name = ?, phone = ?, email = ? WHERE id = ?")) {
            ps.setString(1, cu.getNombre());
            ps.setString(2, vacioANulo(cu.getTelefono()));
            ps.setString(3, cu.getCorreo());
            ps.setInt(4, cu.getId());
            ps.executeUpdate();
        }
    }

    public void cambiarEstado(int id, boolean activo) throws SQLException {
        try (Connection c = Conexion.conectar();
             PreparedStatement ps = c.prepareStatement("UPDATE customers SET active = ? WHERE id = ?")) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        try (Connection c = Conexion.conectar()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM customer_details WHERE customer_id = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM customers WHERE id = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private static String vacioANulo(String s) { return s == null || s.isBlank() ? null : s; }
}
