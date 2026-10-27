package vallegrande.edu.pe.misistema.model;

public class Cuenta {
    private final int id;
    private final String nombre, telefono, correo;
    private final boolean activo;

    public Cuenta(int id, String nombre, String telefono, String correo, boolean activo) {
        this.id = id; this.nombre = nombre; this.telefono = telefono; this.correo = correo; this.activo = activo;
    }
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    public boolean isActivo() { return activo; }
    public String getEstado() { return activo ? "Activa" : "Inactiva"; }
}
