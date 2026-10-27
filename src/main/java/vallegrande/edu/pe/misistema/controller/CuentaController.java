package vallegrande.edu.pe.misistema.controller;

import vallegrande.edu.pe.misistema.model.Cuenta;
import vallegrande.edu.pe.misistema.model.CuentaDAO;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

public class CuentaController {
    private final CuentaDAO dao = new CuentaDAO();

    public List<Cuenta> listar() throws SQLException { return dao.listar(); }

    /** id = 0 crea una cuenta nueva; cualquier otro id actualiza esa cuenta. */
    public void guardar(int id, String nombre, String telefono, String correo) throws SQLException {
        nombre = limpio(nombre); telefono = limpio(telefono); correo = limpio(correo).toLowerCase();
        if (nombre.isEmpty()) throw new IllegalArgumentException("El nombre es obligatorio.");
        if (!correo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new IllegalArgumentException("Ingresa un correo válido.");

        Cuenta c = new Cuenta(id, nombre, telefono, correo, true);
        try {
            if (id == 0) dao.insertar(c); else dao.actualizar(c);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalStateException("Ya existe una cuenta con ese correo.");
        }
    }

    public void cambiarEstado(int id, boolean activo) throws SQLException { dao.cambiarEstado(id, activo); }

    public void eliminar(int id) throws SQLException {
        try {
            dao.eliminar(id);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalStateException("Esta cuenta tiene pedidos registrados. Desactívala en lugar de eliminarla.");
        }
    }

    private static String limpio(String s) { return s == null ? "" : s.strip(); }
}
