package vallegrande.edu.pe.misistema.controller;

import vallegrande.edu.pe.misistema.model.Cliente;
import vallegrande.edu.pe.misistema.model.ClienteDAO;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

public class ClienteController {
    private final ClienteDAO dao = new ClienteDAO();

    public List<Cliente> listar() throws SQLException { return dao.listar(); }

    /** id = 0 crea un cliente nuevo; cualquier otro id actualiza ese cliente. */
    public void guardar(int id, String nombre, String telefono, String correo, String direccion, String tipo)
            throws SQLException {
        nombre = limpio(nombre); telefono = limpio(telefono); correo = limpio(correo).toLowerCase();
        direccion = limpio(direccion); tipo = limpio(tipo);
        if (nombre.isEmpty()) throw new IllegalArgumentException("El nombre es obligatorio.");
        if (!correo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new IllegalArgumentException("Ingresa un correo válido.");
        if (direccion.isEmpty()) direccion = "Sin dirección";
        if (direccion.length() > 255) throw new IllegalArgumentException("La dirección admite máximo 255 caracteres.");
        if (tipo.isEmpty()) tipo = "Minorista";

        Cliente cl = new Cliente(id, nombre, telefono, correo, direccion, tipo, "", 0);
        try {
            if (id == 0) dao.insertar(cl); else dao.actualizar(cl);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalStateException("Ya existe un cliente con ese correo.");
        }
    }

    public void eliminar(int id) throws SQLException {
        try {
            dao.eliminar(id);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalStateException("Este cliente tiene pedidos registrados y no se puede eliminar.");
        }
    }

    private static String limpio(String s) { return s == null ? "" : s.strip(); }
}
