package vallegrande.edu.pe.misistema.controller;

import vallegrande.edu.pe.misistema.model.Usuario;
import vallegrande.edu.pe.misistema.model.UsuarioDAO;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

/** Validaciones del inicio de sesión, el registro de cuentas y el cambio de contraseña. */
public class UsuarioController {
    private final UsuarioDAO dao = new UsuarioDAO();

    /** Devuelve el usuario autenticado o null si las credenciales no son válidas. */
    public Usuario iniciarSesion(String nombreUsuario, String clave) throws SQLException {
        nombreUsuario = limpio(nombreUsuario);
        if (nombreUsuario.isEmpty() || clave == null || clave.isEmpty())
            throw new IllegalArgumentException("Ingresa tu usuario y tu contraseña.");
        return dao.autenticar(nombreUsuario, clave);
    }

    public void registrar(String nombre, String nombreUsuario, String clave, String confirmar) throws SQLException {
        nombre = limpio(nombre);
        nombreUsuario = limpio(nombreUsuario);

        if (nombre.isEmpty())
            throw new IllegalArgumentException("El nombre es obligatorio.");
        if (!nombreUsuario.matches("^[A-Za-z0-9._-]{3,30}$"))
            throw new IllegalArgumentException("El usuario debe tener entre 3 y 30 caracteres (letras, números, punto, guion o guion bajo).");
        if (clave == null || clave.length() < 6)
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        if (!clave.equals(confirmar))
            throw new IllegalArgumentException("Las contraseñas no coinciden.");

        try {
            dao.registrar(nombre, nombreUsuario, clave);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalStateException("Ese nombre de usuario ya está en uso.");
        }
    }

    public void cambiarClave(int id, String actual, String nueva, String confirmar) throws SQLException {
        if (actual == null || actual.isEmpty())
            throw new IllegalArgumentException("Ingresa tu contraseña actual.");
        if (nueva == null || nueva.length() < 6)
            throw new IllegalArgumentException("La contraseña nueva debe tener al menos 6 caracteres.");
        if (!nueva.equals(confirmar))
            throw new IllegalArgumentException("Las contraseñas nuevas no coinciden.");
        if (!dao.claveCorrecta(id, actual))
            throw new IllegalArgumentException("La contraseña actual no es correcta.");

        dao.cambiarClave(id, nueva);
    }

    private static String limpio(String s) { return s == null ? "" : s.strip(); }
}
