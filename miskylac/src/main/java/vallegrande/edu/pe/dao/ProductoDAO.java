package vallegrande.edu.pe.dao;

import vallegrande.edu.pe.config.ConexionBD;
import vallegrande.edu.pe.model.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("SqlResolve")
public class ProductoDAO {

    public List<Producto> listar() {
        List<Producto> lista = new ArrayList<>();
        String sql = """
                SELECT p.identificador, p.nombre, p.categoria, p.presentacion, p.precio,
                       COALESCE(i.stock, 0) AS stock
                FROM producto p
                LEFT JOIN inventario i ON i.identificador_producto = p.identificador
                ORDER BY p.identificador
                """;

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String pres = rs.getString("presentacion");
                lista.add(new Producto(
                        rs.getInt("identificador"),
                        rs.getString("nombre"),
                        capitalizar(rs.getString("categoria")),
                        pres == null ? "" : pres,
                        rs.getDouble("precio"),
                        rs.getInt("stock"),
                        "", ""));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Versión anterior: se mantiene para no romper el código que ya la llama
    public boolean insertar(String nombre, String categoria, String presentacion, double precio, int stock) {
        return insertar(nombre, categoria, presentacion, precio, stock, null);
    }

    // Versión nueva: con URL de imagen (puede ser null o vacía)
    public boolean insertar(String nombre, String categoria, String presentacion, double precio, int stock,
                            String imagenUrl) {
        String sqlProd = "INSERT INTO producto (nombre, categoria, presentacion, precio, imagen_url) "
                + "VALUES (?, ?, ?, ?, ?)";
        String sqlInv = "INSERT INTO inventario (identificador_producto, stock) VALUES (?, ?)";

        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sqlProd, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nombre);
                ps.setString(2, categoria.toLowerCase());
                ps.setString(3, presentacion);
                ps.setDouble(4, precio);
                if (imagenUrl == null || imagenUrl.isBlank()) {
                    ps.setNull(5, Types.VARCHAR);
                } else {
                    ps.setString(5, imagenUrl.trim());
                }
                ps.executeUpdate();

                int id;
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    id = keys.getInt(1);
                }
                try (PreparedStatement ps2 = con.prepareStatement(sqlInv)) {
                    ps2.setInt(1, id);
                    ps2.setInt(2, stock);
                    ps2.executeUpdate();
                }
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizar(int id, String nombre, String categoria, String presentacion, double precio, int stock) {
        String sqlProd = "UPDATE producto SET nombre = ?, categoria = ?, presentacion = ?, precio = ? "
                + "WHERE identificador = ?";
        String sqlStock = "UPDATE inventario SET stock = ? WHERE identificador_producto = ?";
        String sqlNuevo = "INSERT INTO inventario (identificador_producto, stock) VALUES (?, ?)";

        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(sqlProd);
                 PreparedStatement ps2 = con.prepareStatement(sqlStock)) {
                ps.setString(1, nombre);
                ps.setString(2, categoria.toLowerCase());
                ps.setString(3, presentacion);
                ps.setDouble(4, precio);
                ps.setInt(5, id);
                ps.executeUpdate();

                ps2.setInt(1, stock);
                ps2.setInt(2, id);
                if (ps2.executeUpdate() == 0) { // el producto no tenía fila de inventario
                    try (PreparedStatement ps3 = con.prepareStatement(sqlNuevo)) {
                        ps3.setInt(1, id);
                        ps3.setInt(2, stock);
                        ps3.executeUpdate();
                    }
                }
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Falla si el producto ya tiene ventas (para no romper el historial)
    public boolean eliminar(int id) {
        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement("DELETE FROM inventario WHERE identificador_producto = ?");
                 PreparedStatement ps2 = con.prepareStatement("DELETE FROM producto WHERE identificador = ?")) {
                ps1.setInt(1, id);
                ps1.executeUpdate();
                ps2.setInt(1, id);
                ps2.executeUpdate();
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("No se pudo eliminar (¿tiene ventas?): " + e.getMessage());
            return false;
        }
    }

    // cantidad positiva = ingreso, negativa = salida (nunca baja de 0)
    public void ajustarStock(int id, int cantidad) {
        String sql = "UPDATE inventario SET stock = GREATEST(0, stock + ?) WHERE identificador_producto = ?";
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String capitalizar(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}