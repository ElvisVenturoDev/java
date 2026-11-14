package vallegrande.edu.pe.misistema.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Acceso a la tabla usuario. Las contraseñas se guardan como SHA-256. */
public class UsuarioDAO {

    /** Devuelve el usuario si las credenciales son correctas y está activo; si no, null. */
    public Usuario autenticar(String nombreUsuario, String clave) throws SQLException {
        String sql = "SELECT id, username, nombre, rol, activo FROM usuario "
                + "WHERE username = ? AND password = ? AND activo = 1";
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            ps.setString(2, hash(clave));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(rs.getInt("id"), rs.getString("username"),
                            rs.getString("nombre"), rs.getString("rol"), rs.getBoolean("activo"));
                }
            }
        }
        return null;
    }

    /** Indica si el nombre de usuario ya está registrado. */
    public boolean existe(String nombreUsuario) throws SQLException {
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(
                     "SELECT 1 FROM usuario WHERE username = ?")) {
            ps.setString(1, nombreUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void registrar(String nombre, String nombreUsuario, String clave) throws SQLException {
        String sql = "INSERT INTO usuario (username, password, nombre, rol, activo) "
                + "VALUES (?, ?, ?, 'usuario', 1)";
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            ps.setString(2, hash(clave));
            ps.setString(3, nombre);
            ps.executeUpdate();
        }
    }

    /** Comprueba la contraseña actual de un usuario. */
    public boolean claveCorrecta(int id, String clave) throws SQLException {
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(
                     "SELECT 1 FROM usuario WHERE id = ? AND password = ?")) {
            ps.setInt(1, id);
            ps.setString(2, hash(clave));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void cambiarClave(int id, String clave) throws SQLException {
        try (Connection cn = Conexion.conectar();
             PreparedStatement ps = cn.prepareStatement(
                     "UPDATE usuario SET password = ? WHERE id = ?")) {
            ps.setString(1, hash(clave));
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /** SHA-256 en hexadecimal, igual que el usuario inicial del esquema. */
    public static String hash(String texto) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                    .digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cifrar la contraseña.", e);
        }
    }
}
