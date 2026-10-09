package vallegrande.edu.pe.controller;

import vallegrande.edu.pe.dao.ProductoDAO;
import vallegrande.edu.pe.model.Producto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductoController {

    private final ProductoDAO dao = new ProductoDAO();
    private final List<Producto> productos = new ArrayList<>();

    public ProductoController() {
        recargar();
    }

    // Vuelve a leer MySQL (para ver el stock que bajó por compras en la web)
    public void recargar() {
        productos.clear();
        productos.addAll(dao.listar());
    }

    // ===== Consultas =====
    public List<Producto> listarTodos() {
        return List.copyOf(productos);
    }

    public Optional<Producto> buscarPorId(int id) {
        return productos.stream().filter(p -> p.id() == id).findFirst();
    }

    public List<String> categorias() {
        return productos.stream().map(Producto::categoria).distinct().sorted().toList();
    }

    public List<Producto> listarPorCategoria(String categoria) {
        return productos.stream()
                .filter(p -> p.categoria().equalsIgnoreCase(categoria))
                .toList();
    }

    public int totalProductos() {
        return productos.size();
    }

    public int totalUnidades() {
        return productos.stream().mapToInt(Producto::stock).sum();
    }

    // ===== Cambios (se guardan en MySQL y se vuelve a leer) =====

    // Versión anterior: se mantiene para no romper otro código
    public Producto agregar(String nombre, String categoria, String presentacion, double precio, int stock) {
        return agregar(nombre, categoria, presentacion, precio, stock, null);
    }

    // Versión nueva: con URL de imagen
    public Producto agregar(String nombre, String categoria, String presentacion, double precio, int stock,
                            String imagenUrl) {
        if (!dao.insertar(nombre, categoria, presentacion, precio, stock, imagenUrl)) return null;
        recargar();
        return productos.stream()
                .filter(p -> p.nombre().equals(nombre))
                .reduce((a, b) -> b) // el más reciente con ese nombre
                .orElse(null);
    }

    public void editar(int id, String nombre, String categoria, String presentacion, double precio, int stock) {
        dao.actualizar(id, nombre, categoria, presentacion, precio, stock);
        recargar();
    }

    public void actualizar(Producto m) {
        editar(m.id(), m.nombre(), m.categoria(), m.presentacion(), m.precio(), m.stock());
    }

    public void eliminar(int id) {
        dao.eliminar(id);
        recargar();
    }

    // cantidad positiva = ingreso de producción, negativa = salida
    public void ajustarStock(int id, int cantidad) {
        dao.ajustarStock(id, cantidad);
        recargar();
    }
}