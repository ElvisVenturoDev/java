package vallegrande.edu.pe.misistema.controller;

import vallegrande.edu.pe.misistema.model.Mensaje;
import vallegrande.edu.pe.misistema.model.MensajeDAO;

import java.sql.SQLException;
import java.util.List;

public class MensajeController {
    private final MensajeDAO dao = new MensajeDAO();

    public List<Mensaje> listar() throws SQLException { return dao.listar(); }

    /** id = 0 registra un mensaje nuevo; cualquier otro id lo actualiza. */
    public void guardar(int id, String nombre, String celular, String correo, String asunto, String mensaje)
            throws SQLException {
        nombre = limpio(nombre); celular = limpio(celular); correo = limpio(correo);
        asunto = limpio(asunto); mensaje = limpio(mensaje);
        if (nombre.isEmpty()) throw new IllegalArgumentException("El nombre es obligatorio.");
        if (celular.isEmpty()) throw new IllegalArgumentException("El celular es obligatorio.");
        if (!correo.isEmpty() && !correo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new IllegalArgumentException("El correo no tiene un formato válido.");
        if (mensaje.isEmpty()) throw new IllegalArgumentException("El mensaje es obligatorio.");

        Mensaje m = new Mensaje(id, "", nombre, celular, correo, asunto, mensaje);
        if (id == 0) dao.insertar(m); else dao.actualizar(m);
    }

    public void eliminar(int id) throws SQLException { dao.eliminar(id); }

    private static String limpio(String s) { return s == null ? "" : s.strip(); }
}
