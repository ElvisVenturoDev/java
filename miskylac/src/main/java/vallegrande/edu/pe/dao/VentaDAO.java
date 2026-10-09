package vallegrande.edu.pe.dao;

import vallegrande.edu.pe.config.ConexionBD;
import vallegrande.edu.pe.model.DetalleVenta;
import vallegrande.edu.pe.model.Venta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentaDAO {

    public List<Venta> listarVentas() {
        List<Venta> lista = new ArrayList<>();
        String sql = "SELECT identificador, fecha, identificador_cliente FROM venta";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Venta(
                        rs.getInt("identificador"),
                        rs.getDate("fecha").toLocalDate(),
                        rs.getInt("identificador_cliente")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<DetalleVenta> listarDetalles() {
        List<DetalleVenta> lista = new ArrayList<>();
        String sql = "SELECT identificador, identificador_venta, identificador_producto, "
                + "cantidad, precio_unitario FROM detalle_venta";

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new DetalleVenta(
                        rs.getInt("identificador"),
                        rs.getInt("identificador_venta"),
                        rs.getInt("identificador_producto"),
                        rs.getInt("cantidad"),
                        rs.getDouble("precio_unitario")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}