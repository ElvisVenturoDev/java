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

        double p = decimal(precio, "El precio");
        int s = entero(stock, "El stock");
        int sm = entero(minimoStock, "El stock mínimo");

        Producto prod = new Producto(id, nombre, categoria, detalle.isEmpty() ? null : detalle,
                vacioANulo(descripcion), vacioANulo(urlImagen), p, s, sm);
        if (id == 0) dao.insertar(prod); else dao.actualizar(prod);
    }

    /** @return true si se borró definitivamente; false si tenía ventas y solo se desactivó. */
    public boolean eliminar(int id) throws SQLException { return dao.eliminar(id); }

    private static String limpio(String s) { return s == null ? "" : s.strip(); }
    private static String vacioANulo(String s) { return limpio(s).isEmpty() ? null : limpio(s); }

    private static double decimal(String texto, String campo) {
        try {
            double v = Double.parseDouble(limpio(texto).replace(',', '.'));
            if (v < 0) throw new IllegalArgumentException(campo + " no puede ser negativo.");
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(campo + " debe ser un número (ej. 6.50).");
        }
    }

    private static int entero(String texto, String campo) {
        try {
            int v = Integer.parseInt(limpio(texto));
            if (v < 0) throw new IllegalArgumentException(campo + " no puede ser negativo.");
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(campo + " debe ser un número entero.");
        }
    }
}
