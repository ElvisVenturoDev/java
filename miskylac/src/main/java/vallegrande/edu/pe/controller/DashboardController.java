package vallegrande.edu.pe.controller;

import vallegrande.edu.pe.dao.ClienteDAO;
import vallegrande.edu.pe.dao.VentaDAO;
import vallegrande.edu.pe.model.Cliente;
import vallegrande.edu.pe.model.DetalleVenta;
import vallegrande.edu.pe.model.Venta;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardController {

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final VentaDAO ventaDAO = new VentaDAO();

    // Copia en memoria de lo que hay en MySQL
    private final List<Cliente> clientes = new ArrayList<>();
    private final List<Venta> ventas = new ArrayList<>();
    private final List<DetalleVenta> detalles = new ArrayList<>();

    // idVenta -> fecha (evita buscar en toda la lista por cada detalle)
    private final Map<Integer, LocalDate> fechaPorVenta = new HashMap<>();

    public DashboardController() {
        cargarClientes();
        cargarVentas();
    }

    // ===================== Carga desde MySQL =====================
    private void cargarClientes() {
        clientes.addAll(clienteDAO.listar());

        // Si la base está vacía, se guardan los clientes de ejemplo en MySQL
        if (clientes.isEmpty()) {
            LocalDate hoy = LocalDate.now();
            String[] nombres = {"María Quispe", "Carlos Rojas", "Lucía Flores", "Pedro Ramos",
                    "Ana Torres", "Jorge Salas", "Rosa Vega", "Luis Cárdenas"};
            String[] direcciones = {"Av. Arequipa 450, Lima", "Jr. Cusco 123, Lima", "Calle Los Pinos 88, Surco",
                    "Av. Brasil 900, Breña", "Jr. Puno 321, Lima", "Av. Colonial 1500, Callao",
                    "Calle Lima 77, Miraflores", "Av. Universitaria 2100, Los Olivos"};
            for (int i = 0; i < nombres.length; i++) {
                clienteDAO.insertar(nombres[i], "99900000" + i, direcciones[i],
                        i % 2 == 0 ? "Minorista" : "Mayorista", hoy.minusDays(i * 2L));
            }
            clientes.addAll(clienteDAO.listar());
        }
    }

    // Ventas reales (las que llegan desde la web y las que haya en MySQL)
    private void cargarVentas() {
        ventas.clear();
        detalles.clear();
        fechaPorVenta.clear();

        ventas.addAll(ventaDAO.listarVentas());
        detalles.addAll(ventaDAO.listarDetalles());
        for (Venta v : ventas) {
            fechaPorVenta.put(v.id(), v.fecha());
        }
    }

    // Vuelve a leer MySQL (para ver lo que llegó desde la web sin reiniciar)
    public void recargar() {
        clientes.clear();
        clientes.addAll(clienteDAO.listar());
        cargarVentas();
    }

    // ===================== Clientes =====================
    public List<Cliente> clientes() {
        return clientes;
    }

    public Cliente clientePorId(int id) {
        return clientes.stream().filter(c -> c.id() == id).findFirst().orElse(null);
    }

    public void agregarCliente(String nombre, String telefono, String direccion, String tipo) {
        Cliente nuevo = clienteDAO.insertar(nombre, telefono, direccion, tipo); // guarda en MySQL
        if (nuevo != null) {
            clientes.add(0, nuevo); // y lo muestra en pantalla
        }
    }

    public int pedidosDeCliente(int idCliente) {
        return (int) ventas.stream().filter(v -> v.idCliente() == idCliente).count();
    }

    public double totalDeCliente(int idCliente) {
        return ventas.stream()
                .filter(v -> v.idCliente() == idCliente)
                .mapToDouble(v -> totalDeVenta(v.id()))
                .sum();
    }

    public int clientesActivos() {
        return clientes.size();
    }

    public int nuevosClientes() {
        LocalDate limite = LocalDate.now().minusDays(7);
        return (int) clientes.stream().filter(c -> c.fechaRegistro().isAfter(limite)).count();
    }

    // tipo de cliente -> cantidad
    public Map<String, Long> clientesPorTipo() {
        return clientes.stream().collect(
                Collectors.groupingBy(Cliente::tipoCliente, LinkedHashMap::new, Collectors.counting()));
    }

    // ===================== Ventas =====================
    public double totalDelDia(LocalDate fecha) {
        return detalles.stream()
                .filter(d -> fecha.equals(fechaPorVenta.get(d.idVenta())))
                .mapToDouble(DetalleVenta::subtotal)
                .sum();
    }

    public double ventasDeHoy() {
        return totalDelDia(LocalDate.now());
    }

    public double variacionVsAyer() {
        double ayer = totalDelDia(LocalDate.now().minusDays(1));
        if (ayer == 0) return 0;
        return (ventasDeHoy() - ayer) / ayer * 100;
    }

    public int pedidosTotales() {
        return ventas.size();
    }

    public int pedidosDeHoy() {
        LocalDate hoy = LocalDate.now();
        return (int) ventas.stream().filter(v -> v.fecha().equals(hoy)).count();
    }

    public double totalVentas() {
        return detalles.stream().mapToDouble(DetalleVenta::subtotal).sum();
    }

    public double ticketPromedio() {
        return ventas.isEmpty() ? 0 : totalVentas() / ventas.size();
    }

    public double totalDeVenta(int idVenta) {
        return detalles.stream()
                .filter(d -> d.idVenta() == idVenta)
                .mapToDouble(DetalleVenta::subtotal)
                .sum();
    }

    public List<Venta> ultimasVentas(int n) {
        return ventas.stream()
                .sorted(Comparator.comparing(Venta::fecha).reversed()
                        .thenComparing(Comparator.comparingInt(Venta::id).reversed()))
                .limit(n)
                .toList();
    }

    // Del más antiguo al de hoy
    public double[] ventasUltimosDias(int n) {
        double[] datos = new double[n];
        LocalDate hoy = LocalDate.now();
        for (int i = 0; i < n; i++) {
            datos[i] = totalDelDia(hoy.minusDays(n - 1 - i));
        }
        return datos;
    }

    // ===================== Por producto =====================
    // idProducto -> unidades vendidas
    public Map<Integer, Integer> unidadesPorProducto() {
        return detalles.stream().collect(
                Collectors.groupingBy(DetalleVenta::idProducto, Collectors.summingInt(DetalleVenta::cantidad)));
    }

    // idProducto -> dinero generado
    public Map<Integer, Double> ingresosPorProducto() {
        return detalles.stream().collect(
                Collectors.groupingBy(DetalleVenta::idProducto, Collectors.summingDouble(DetalleVenta::subtotal)));
    }
}