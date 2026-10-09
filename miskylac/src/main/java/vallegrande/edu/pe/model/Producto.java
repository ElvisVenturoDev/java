package vallegrande.edu.pe.model;

public class Producto {

    private final int id;
    private final String nombre;
    private final String categoria;
    private final String presentacion; // solo para la interfaz, no está en la tabla
    private final double precio;
    private final int stock;           // viene de la tabla inventario
    private final String emoji;
    private final String imagenUrl;

    public Producto(int id, String nombre, String categoria, String presentacion,
                    double precio, int stock, String emoji, String imagenUrl) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.presentacion = presentacion;
        this.precio = precio;
        this.stock = stock;
        this.emoji = emoji;
        this.imagenUrl = imagenUrl;
    }

    public int id() { return id; }
    public String nombre() { return nombre; }
    public String categoria() { return categoria; }
    public String presentacion() { return presentacion; }
    public double precio() { return precio; }
    public int stock() { return stock; }
    public String emoji() { return emoji; }
    public String imagenUrl() { return imagenUrl; }
}