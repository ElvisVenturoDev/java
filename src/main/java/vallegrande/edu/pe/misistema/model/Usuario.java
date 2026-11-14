package vallegrande.edu.pe.misistema.model;

/** Usuario del sistema (tabla usuario). */
public class Usuario {
    private final int id;
    private final String nombreUsuario, nombre, rol;
    private final boolean activo;

    public Usuario(int id, String nombreUsuario, String nombre, String rol) {
        this(id, nombreUsuario, nombre, rol, true);
    }

    public Usuario(int id, String nombreUsuario, String nombre, String rol, boolean activo) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.nombre = nombre;
        this.rol = rol;
        this.activo = activo;
    }

    public int getId() { return id; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getNombre() { return nombre; }
    public String getRol() { return rol; }
    public boolean isActivo() { return activo; }

    /** Rol con la primera letra en mayúscula (para mostrar en pantalla). */
    public String getRolVisible() {
        if (rol == null || rol.isBlank()) return "";
        return Character.toUpperCase(rol.charAt(0)) + rol.substring(1);
    }
}
