package vallegrande.edu.pe.misistema.model;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Clientes con su ficha comercial (cliente + detalle_cliente) y cantidad de pedidos. */
public class ClienteDAO {

    public List<Cliente> listar() throws SQLException {
        String sql = "SELECT c.identificador, c.nombre, c.telefono, c.correo, "
                + "COALESCE(d.direccion, '') AS direccion, COALESCE(d.tipo_cliente, 'Minorista') AS tipo, "
                + "d.fecha_registro, "
                + "(SELECT COUNT(*) FROM venta v WHERE v.identificador_cliente = c.identificador) AS pedidos "
                + "FROM cliente c LEFT JOIN detalle_cliente d ON d.identificador_cliente = c.identificador "
                + "ORDER BY c.identificador";
        List<Cliente> lista = new ArrayList<>();
        try (Connection c = Conexion.conectar(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Date f = rs.getDate("fecha_registro");
                lista.add(new Cliente(rs.getInt("identificador"), rs.getString("nombre"), rs.getString("telefono"),
                        rs.getString("correo"), rs.getString("direccion"), rs.getString("tipo"),
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
                        "INSERT INTO cliente (nombre, telefono, correo, password_hash) VALUES (?, ?, ?, '')",
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
                        "UPDATE cliente SET nombre = ?, telefono = ?, correo = ? WHERE identificador = ?")) {
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

    /** Crea o actualiza la ficha; la fecha de registro solo se fija al crearla. */
    private void guardarDetalle(Connection c, int id, Cliente cl, Date hoy) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO detalle_cliente (identificador_cliente, direccion, tipo_cliente, fecha_registro) "
                        + "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE direccion = VALUES(direccion), "
                        + "tipo_cliente = VALUES(tipo_cliente)")) {
            ps.setInt(1, id);
            ps.setString(2, cl.getDireccion());
            ps.setString(3, cl.getTipo());
            ps.setDate(4, hoy);
            ps.executeUpdate();
        }
    }

    private static String vacioANulo(String s) { return s == null || s.isBlank() ? null : s; }
}
