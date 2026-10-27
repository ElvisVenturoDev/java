package vallegrande.edu.pe.misistema.model;

public class Mensaje {
    private final int id;
    private final String fecha, nombre, celular, correo, asunto, mensaje;

    public Mensaje(int id, String fecha, String nombre, String celular, String correo, String asunto, String mensaje) {
        this.id = id; this.fecha = fecha; this.nombre = nombre; this.celular = celular;
        this.correo = correo; this.asunto = asunto; this.mensaje = mensaje;
    }
    public int getId() { return id; }
    public String getFecha() { return fecha; }
    public String getNombre() { return nombre; }
    public String getCelular() { return celular; }
    public String getCorreo() { return correo; }
    public String getAsunto() { return asunto; }
    public String getMensaje() { return mensaje; }
}
