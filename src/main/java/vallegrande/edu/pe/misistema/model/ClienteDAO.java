package vallegrande.edu.pe.misistema.model;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Clientes con su ficha comercial (customers + customer_details) y cantidad de pedidos. */
public class ClienteDAO {

    public List<Cliente> listar() throws SQLException {
        String sql = "SELECT c.id, c.name, c.phone, c.email, "
                + "COALESCE(d.address, '') AS address, COALESCE(d.customer_type, 'Minorista') AS customer_type, "
                + "d.registered_on, "
                + "(SELECT COUNT(*) FROM sales v WHERE v.customer_id = c.id) AS pedidos "
                + "FROM customers c LEFT JOIN customer_details d ON d.customer_id = c.id "
                + "ORDER BY c.id";
        List<Cliente> lista = new ArrayList<>();
        try (Connection c = Conexion.conectar(); PreparedStatement st = c.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                Date f = rs.getDate("registered_on");
                lista.add(new Cliente(rs.getInt("id"), rs.getString("name"), rs.getString("phone"),
                        rs.getString("email"), rs.getString("address"), rs.getString("customer_type"),
                        f == null ? "" : f.toString(), rs.getInt("pedidos")));
            }
        }
        return lista;
    }

    public void insertar(Cliente cl) throws SQLException {
        try (Connection c = Conexion.conectar()) {
            c.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO customers (name, phone, email, password_hash) VALUES (?, ?, ?, '')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, cl.getNombre());
                    ps.setString(2, vacioANulo(cl.getTelefono()));
                    ps.setString(3, cl.getCorreo());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        id = keys.getInt(1);
                    }
                }
                guardarDetalle(c, id, cl, Date.valueOf(LocalDate.now()));
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void actualizar(Cliente cl) throws SQLException {
        try (Connection c = Conexion.conectar()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE customers SET name = ?, phone = ?, email = ? WHERE id = ?")) {
                    ps.setString(1, cl.getNombre());
                    ps.setString(2, vacioANulo(cl.getTelefono()));
                    ps.setString(3, cl.getCorreo());
                    ps.setInt(4, cl.getId());
                    ps.executeUpdate();
                }
                guardarDetalle(c, cl.getId(), cl, Date.valueOf(LocalDate.now()));
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
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

    /** Crea o actualiza la ficha; la fecha de registro solo se fija al crearla. */
    private void guardarDetalle(Connection c, int id, Cliente cl, Date hoy) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO customer_details (customer_id, address, customer_type, registered_on) "
                        + "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE address = VALUES(address), "
                        + "customer_type = VALUES(customer_type), updated_at = CURRENT_TIMESTAMP")) {
            ps.setInt(1, id);
            ps.setString(2, cl.getDireccion());
            ps.setString(3, cl.getTipo());
            ps.setDate(4, hoy);
            ps.executeUpdate();
        }
    }

    private static String vacioANulo(String s) { return s == null || s.isBlank() ? null : s; }
}
