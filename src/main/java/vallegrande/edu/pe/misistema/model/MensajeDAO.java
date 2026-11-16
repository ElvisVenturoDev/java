package vallegrande.edu.pe.misistema.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MensajeDAO {
    private static final String TABLA = "contact_messages";
    /** El teléfono del contacto se guarda en la columna `mobile` del esquema. */
    private static final String COLUMNA_CELULAR = "mobile";

    /** Detecta la clave primaria real de la tabla para no depender de su nombre. */
    private String clavePrimaria(Connection c) throws SQLException {
        try (ResultSet rs = c.getMetaData().getPrimaryKeys(c.getCatalog(), null, TABLA)) {
            if (rs.next()) return rs.getString("COLUMN_NAME");
        }
        return "id";
    }

    public List<Mensaje> listar() throws SQLException {
        List<Mensaje> lista = new ArrayList<>();
        try (Connection c = Conexion.conectar()) {
            String pk = clavePrimaria(c);
            try (PreparedStatement st = c.prepareStatement(
                    "SELECT * FROM " + TABLA + " ORDER BY `" + pk + "` DESC");
                 ResultSet rs = st.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int colFecha = 0;
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    int t = md.getColumnType(i);
                    if (t == Types.TIMESTAMP || t == Types.TIMESTAMP_WITH_TIMEZONE || t == Types.DATE) { colFecha = i; break; }
                }
                while (rs.next()) {
                    String fecha = colFecha == 0 ? "" : String.valueOf(rs.getObject(colFecha));
                    if (fecha.length() > 16) fecha = fecha.substring(0, 16);
                    lista.add(new Mensaje(rs.getInt(pk), fecha, texto(rs, "name"), texto(rs, COLUMNA_CELULAR),
                            texto(rs, "email"), texto(rs, "subject"), texto(rs, "message")));
                }
            }
        }
        return lista;
    }

    public void insertar(Mensaje m) throws SQLException {
        try (Connection c = Conexion.conectar();
             PreparedStatement ps = c.prepareStatement("INSERT INTO " + TABLA
                     + " (name, " + COLUMNA_CELULAR + ", email, subject, message) VALUES (?, ?, ?, ?, ?)")) {
            llenar(ps, m);
            ps.executeUpdate();
        }
    }

    public void actualizar(Mensaje m) throws SQLException {
        try (Connection c = Conexion.conectar()) {
            String pk = clavePrimaria(c);
            try (PreparedStatement ps = c.prepareStatement("UPDATE " + TABLA
                    + " SET name = ?, " + COLUMNA_CELULAR + " = ?, email = ?, subject = ?, message = ? WHERE `" + pk + "` = ?")) {
                llenar(ps, m);
                ps.setInt(6, m.getId());
                ps.executeUpdate();
            }
        }
    }

    public void eliminar(int id) throws SQLException {
        try (Connection c = Conexion.conectar()) {
            String pk = clavePrimaria(c);
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM " + TABLA + " WHERE `" + pk + "` = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        }
    }

    private void llenar(PreparedStatement ps, Mensaje m) throws SQLException {
        ps.setString(1, m.getNombre());
        ps.setString(2, m.getCelular());
        ps.setString(3, vacioANulo(m.getCorreo()));
        ps.setString(4, vacioANulo(m.getAsunto()));
        ps.setString(5, m.getMensaje());
    }

    private static String vacioANulo(String s) { return s == null || s.isBlank() ? null : s; }

    private static String texto(ResultSet rs, String columna) {
        try {
            String v = rs.getString(columna);
            return v == null ? "" : v;
        } catch (SQLException e) {
            return "";
        }
    }
}
