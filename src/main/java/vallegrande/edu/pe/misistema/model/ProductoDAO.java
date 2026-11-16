package vallegrande.edu.pe.misistema.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public List<Producto> listar() throws SQLException {
        String sql = "SELECT p.id, p.name, p.category, p.detail, p.description, p.image_url, "
                + "p.price, COALESCE(i.stock, 0) AS stock, COALESCE(i.min_stock, 5) AS stock_minimo "
                + "FROM products p LEFT JOIN inventory i ON i.product_id = p.id "
                + "WHERE p.active = 1 ORDER BY p.id";
        List<Producto> lista = new ArrayList<>();
        try (Connection c = Conexion.conectar(); PreparedStatement st = c.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                lista.add(new Producto(rs.getInt("id"), rs.getString("name"), rs.getString("category"),
                        rs.getString("detail"), rs.getString("description"), rs.getString("image_url"),
                        rs.getDouble("price"), rs.getInt("stock"), rs.getInt("stock_minimo")));
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
                        "INSERT INTO products (name, category, detail, description, price, image_url) "
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
                        "UPDATE products SET name = ?, category = ?, detail = ?, description = ?, "
                                + "price = ?, image_url = ? WHERE id = ?")) {
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
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM inventory WHERE product_id = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM products WHERE id = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                c.commit();
                return true;
            } catch (SQLIntegrityConstraintViolationException e) {
                c.rollback();
                try (PreparedStatement ps = c.prepareStatement("UPDATE products SET active = 0 WHERE id = ?")) {
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
        ps.setString(6, p.getUrlImagen());
    }

    private void guardarInventario(Connection c, int idProducto, Producto p) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO inventory (product_id, stock, min_stock) VALUES (?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE stock = VALUES(stock), min_stock = VALUES(min_stock)")) {
            ps.setInt(1, idProducto);
            ps.setInt(2, p.getStock());
            ps.setInt(3, p.getMinimoStock());
            ps.executeUpdate();
        }
    }
}
