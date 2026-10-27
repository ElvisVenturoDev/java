package vallegrande.edu.pe.misistema.model;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Cuentas de clientes (tablas cliente y detalle_cliente, como en la web). */
public class CuentaDAO {

    public List<Cuenta> listar() throws SQLException {
        List<Cuenta> lista = new ArrayList<>();
        try (Connection c = Conexion.conectar(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT identificador, nombre, telefono, correo, activo FROM cliente ORDER BY identificador")) {
            while (rs.next()) {
                lista.add(new Cuenta(rs.getInt("identificador"), rs.getString("nombre"),
                        rs.getString("telefono"), rs.getString("correo"), rs.getBoolean("activo")));
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
                        "INSERT INTO cliente (nombre, telefono, correo, password_hash) VALUES (?, ?, ?, '')",
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
                        "INSERT INTO detalle_cliente (identificador_cliente, direccion, tipo_cliente, fecha_registro) "
                                + "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE actualizado_en = CURRENT_TIMESTAMP")) {
                    ps.setInt(1, id);
                    ps.setString(2, "Sin dirección");
                    ps.setString(3, "Minorista");
                    ps.setDate(4, Date.valueOf(LocalDate.now()));
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
                     "UPDATE cliente SET nombre = ?, telefono = ?, correo = ? WHERE identificador = ?")) {
            ps.setString(1, cu.getNombre());
            ps.setString(2, vacioANulo(cu.getTelefono()));
            ps.setString(3, cu.getCorreo());
            ps.setInt(4, cu.getId());
            ps.executeUpdate();
        }
    }

    public void cambiarEstado(int id, boolean activo) throws SQLException {
        try (Connection c = Conexion.conectar();
             PreparedStatement ps = c.prepareStatement("UPDATE cliente SET activo = ? WHERE identificador = ?")) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        try (Connection c = Conexion.conectar()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM detalle_cliente WHERE identificador_cliente = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM cliente WHERE identificador = ?")) {
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
