package vallegrande.edu.pe.model;

import java.time.LocalDate;

public class Cliente {

    private final int id;
    private final String nombre;
    private final String telefono;
    private final String direccion;
    private final String tipoCliente;
    private final LocalDate fechaRegistro;

    public Cliente(int id, String nombre, String telefono, String direccion,
                   String tipoCliente, LocalDate fechaRegistro) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
        this.tipoCliente = tipoCliente;
        this.fechaRegistro = fechaRegistro;
    }

    public int id() { return id; }
    public String nombre() { return nombre; }
    public String telefono() { return telefono; }
    public String direccion() { return direccion; }
    public String tipoCliente() { return tipoCliente; }
    public LocalDate fechaRegistro() { return fechaRegistro; }
}