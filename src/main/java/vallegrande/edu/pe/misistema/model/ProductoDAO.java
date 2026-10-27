package vallegrande.edu.pe.misistema.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public List<Producto> listar() throws SQLException {
        String sql = "SELECT p.identificador, p.nombre, p.categoria, p.detalle, p.descripcion, p.imagen_url, "
                + "p.precio, COALESCE(i.stock, 0) AS stock, COALESCE(i.stock_minimo, 5) AS stock_minimo "
                + "FROM producto p LEFT JOIN inventario i ON i.identificador_producto = p.identificador "
                + "WHERE p.activo = 1 ORDER BY p.identificador";
        List<Producto> lista = new ArrayList<>();
        try (Connection c = Conexion.conectar(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Producto(rs.getInt("identificador"), rs.getString("nombre"), rs.getString("categoria"),
                        rs.getString("detalle"), rs.getString("descripcion"), rs.getString("imagen_url"),
                        rs.getDouble("precio"), rs.getInt("stock"), rs.getInt("stock_minimo")));
            }
        }
        return lista;
    }

    public void insertar(Producto p) throws SQLException {
        try (Connection c = Conexion.conectar()) {
            c.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO producto (nombre, categoria, detalle, descripcion, precio, imagen_url) "
                                + "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    llenar(ps, p);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        id = keys.getInt(1);
                    }
                }
                guardarInventario(c, id, p);
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void actualizar(Producto p) throws SQLException {
        try (Connection c = Conexion.conectar()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE producto SET nombre = ?, categoria = ?, detalle = ?, descripcion = ?, "
                                + "precio = ?, imagen_url = ? WHERE identificador = ?")) {
                    llenar(ps, p);
                    ps.setInt(7, p.getId());
                    ps.executeUpdate();
                }
                guardarInventario(c, p.getId(), p);
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /**
     * Intenta borrar el producto. Si ya tiene ventas (clave foránea) hace baja lógica
     * (activo = 0), igual que la web, para no perder el historial.
     * @return true si se borró definitivamente; false si solo se desactivó.
     */
    public boolean eliminar(int id) throws SQLException {
        try (Connection c = Conexion.conectar()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM inventario WHERE identificador_producto = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM producto WHERE identificador = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                c.commit();
                return true;
            } catch (SQLIntegrityConstraintViolationException e) {
                c.rollback();
                try (PreparedStatement ps = c.prepareStatement("UPDATE producto SET activo = 0 WHERE identificador = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                c.commit();
                return false;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private void llenar(PreparedStatement ps, Producto p) throws SQLException {
        ps.setString(1, p.getNombre());
        ps.setString(2, p.getCategoria());
        ps.setString(3, p.getDetalle());
        ps.setString(4, p.getDescripcion());
        ps.setDouble(5, p.getPrecio());
        ps.setString(6, p.getImagenUrl());
    }

    private void guardarInventario(Connection c, int idProducto, Producto p) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO inventario (identificador_producto, stock, stock_minimo) VALUES (?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE stock = VALUES(stock), stock_minimo = VALUES(stock_minimo)")) {
            ps.setInt(1, idProducto);
            ps.setInt(2, p.getStock());
            ps.setInt(3, p.getStockMinimo());
            ps.executeUpdate();
        }
    }
}
