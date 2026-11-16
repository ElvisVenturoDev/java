package vallegrande.edu.pe.misistema.model;

/** Ficha comercial de un cliente (tablas customers + customer_details). */
public class Cliente {
    private final int id;
    private final String nombre, telefono, correo, direccion, tipo, fechaRegistro;
    private final int pedidos;

    public Cliente(int id, String nombre, String telefono, String correo, String direccion,
                   String tipo, String fechaRegistro, int pedidos) {
        this.id = id; this.nombre = nombre; this.telefono = telefono; this.correo = correo;
        this.direccion = direccion; this.tipo = tipo; this.fechaRegistro = fechaRegistro; this.pedidos = pedidos;
    }
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    public String getDireccion() { return direccion; }
    public String getTipo() { return tipo; }
    public String getFechaRegistro() { return fechaRegistro; }
    public int getPedidos() { return pedidos; }
}
