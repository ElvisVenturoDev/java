package vallegrande.edu.pe.model;

public class DetalleVenta {

    private final int id;
    private final int idVenta;
    private final int idProducto;
    private final int cantidad;
    private final double precioUnitario;

    public DetalleVenta(int id, int idVenta, int idProducto, int cantidad, double precioUnitario) {
        this.id = id;
        this.idVenta = idVenta;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public int id() { return id; }
    public int idVenta() { return idVenta; }
    public int idProducto() { return idProducto; }
    public int cantidad() { return cantidad; }
    public double precioUnitario() { return precioUnitario; }
    public double subtotal() { return cantidad * precioUnitario; }
}