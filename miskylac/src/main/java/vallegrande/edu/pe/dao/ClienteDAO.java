package vallegrande.edu.pe.dao;

import vallegrande.edu.pe.config.ConexionBD;
import vallegrande.edu.pe.model.Cliente;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    public List<Cliente> listar() {
        List<Cliente> lista = new ArrayList<>();
        String sql = """
                SELECT c.identificador, c.nombre, c.telefono,
                       d.direccion, d.tipo_cliente, d.fecha_registro
                FROM cliente c
                JOIN detalle_cliente d ON d.identificador_cliente = c.identificador
                ORDER BY c.identificador DESC
                """;

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Cliente(
                        rs.getInt("identificador"),
                        rs.getString("nombre"),
                        rs.getString("telefono"),
                        rs.getString("direccion"),
                        rs.getString("tipo_cliente"),
                        rs.getDate("fecha_registro").toLocalDate()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Cliente insertar(String nombre, String telefono, String direccion, String tipo) {
        return insertar(nombre, telefono, direccion, tipo, LocalDate.now());
    }

    public Cliente insertar(String nombre, String telefono, String direccion, String tipo, LocalDate fecha) {
        String sqlCliente = "INSERT INTO cliente (nombre, telefono) VALUES (?, ?)";
        String sqlDetalle = "INSERT INTO detalle_cliente "
                + "(identificador_cliente, direccion, tipo_cliente, fecha_registro) VALUES (?, ?, ?, ?)";

        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement(sqlCliente, Statement.RETURN_GENERATED_KEYS)) {
                ps1.setString(1, nombre);
                ps1.setString(2, telefono);
                ps1.executeUpdate();

                int id;
                try (ResultSet keys = ps1.getGeneratedKeys()) {
                    keys.next();
                    id = keys.getInt(1);
                }

                try (PreparedStatement ps2 = con.prepareStatement(sqlDetalle)) {
                    ps2.setInt(1, id);
                    ps2.setString(2, direccion);
                    ps2.setString(3, tipo);
                    ps2.setDate(4, Date.valueOf(fecha));
                    ps2.executeUpdate();
                }
                con.commit();
                return new Cliente(id, nombre, telefono, direccion, tipo, fecha);
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}