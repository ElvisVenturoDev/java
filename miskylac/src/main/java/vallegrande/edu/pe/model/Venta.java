package vallegrande.edu.pe.model;

import java.time.LocalDate;

public class Venta {

    private final int id;
    private final LocalDate fecha;
    private final int idCliente;

    public Venta(int id, LocalDate fecha, int idCliente) {
        this.id = id;
        this.fecha = fecha;
        this.idCliente = idCliente;
    }

    public int id() { return id; }
    public LocalDate fecha() { return fecha; }
    public int idCliente() { return idCliente; }
}