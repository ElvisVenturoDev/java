package vallegrande.edu.pe.misistema.model;

import java.util.Locale;

public class Producto {
    private final int id;
    private final String nombre, categoria, detalle, descripcion, urlImagen;
    private final double precio;
    private final int stock, minimoStock;

    public Producto(int id, String nombre, String categoria, String detalle, String descripcion,
                    String urlImagen, double precio, int stock, int minimoStock) {
        this.id = id; this.nombre = nombre; this.categoria = categoria; this.detalle = detalle;
        this.descripcion = descripcion; this.urlImagen = urlImagen; this.precio = precio;
        this.stock = stock; this.minimoStock = minimoStock;
    }
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public String getCategoriaTexto() {
        if (categoria == null || categoria.isBlank()) return "";
        return categoria.substring(0, 1).toUpperCase(Locale.ROOT) + categoria.substring(1);
    }
    public String getDetalle() { return detalle; }
    public String getDescripcion() { return descripcion; }
    public String getUrlImagen() { return urlImagen; }
    public double getPrecio() { return precio; }
    public int getStock() { return stock; }
    public int getMinimoStock() { return minimoStock; }
}
