package vallegrande.edu.pe.misistema.controller;

import vallegrande.edu.pe.misistema.model.Producto;
import vallegrande.edu.pe.misistema.model.ProductoDAO;

import java.sql.SQLException;
import java.util.List;

public class ProductoController {
    private final ProductoDAO dao = new ProductoDAO();

    public List<Producto> listar() throws SQLException { return dao.listar(); }

    /** id = 0 crea un producto nuevo; cualquier otro id actualiza ese producto. */
    public void guardar(int id, String nombre, String categoria, String detalle, String descripcion,
                        String urlImagen, String precio, String stock, String minimoStock) throws SQLException {
        nombre = limpio(nombre);
        categoria = limpio(categoria).toLowerCase();
        detalle = limpio(detalle);
        if (nombre.isEmpty()) throw new IllegalArgumentException("El nombre es obligatorio.");
        if (categoria.isEmpty()) throw new IllegalArgumentException("La categoría es obligatoria.");
        if (categoria.length() > 50) throw new IllegalArgumentException("La categoría admite máximo 50 caracteres.");
        if (detalle.length() > 80) throw new IllegalArgumentException("El detalle admite máximo 80 caracteres.");

        double precioNumerico = decimal(precio, "El precio");
        int stockNumerico = entero(stock, "El stock");
        int stockMinimoNumerico = entero(minimoStock, "El stock mínimo");

        Producto producto = new Producto(id, nombre, categoria, detalle.isEmpty() ? null : detalle,
                vacioANulo(descripcion), vacioANulo(urlImagen),
                precioNumerico, stockNumerico, stockMinimoNumerico);
        if (id == 0) dao.insertar(producto); else dao.actualizar(producto);
    }

    /** @return true si se borró definitivamente; false si tenía ventas y solo se desactivó. */
    public boolean eliminar(int id) throws SQLException { return dao.eliminar(id); }

    private static String limpio(String texto) { return texto == null ? "" : texto.strip(); }
    private static String vacioANulo(String texto) { return limpio(texto).isEmpty() ? null : limpio(texto); }

    private static double decimal(String texto, String campo) {
        try {
            double valor = Double.parseDouble(limpio(texto).replace(',', '.'));
            if (valor < 0) throw new IllegalArgumentException(campo + " no puede ser negativo.");
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(campo + " debe ser un número (ej. 6.50).");
        }
    }

    private static int entero(String texto, String campo) {
        try {
            int valor = Integer.parseInt(limpio(texto));
            if (valor < 0) throw new IllegalArgumentException(campo + " no puede ser negativo.");
            return valor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(campo + " debe ser un número entero.");
        }
    }
}
