package vallegrande.edu.pe.view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import vallegrande.edu.pe.controller.DashboardController;
import vallegrande.edu.pe.controller.PoliticaInventario;
import vallegrande.edu.pe.controller.ProductoController;
import vallegrande.edu.pe.model.Cliente;
import vallegrande.edu.pe.model.Producto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;

public class InicioView {

    private final DashboardController dash;
    private final ProductoController productoCtrl;

    public InicioView(DashboardController dash, ProductoController productoCtrl) {
        this.dash = dash;
        this.productoCtrl = productoCtrl;
    }

    private record FilaTop(String producto, int unidades, double ingresos) {}
    private record FilaVenta(String nro, String fecha, String cliente, String total) {}

    public VBox crear(Consumer<String> navegar) {
        List<Producto> productos = productoCtrl.listarTodos();

        // ================= Banner con la vaca =================
        Label titulo = new Label("Panel de control");
        titulo.setStyle("-fx-font-size:28px;-fx-font-weight:bold;-fx-text-fill:white;");
        String fecha = LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy", Locale.forLanguageTag("es-PE")));
        Label sub = new Label("Resumen de MiskyLac · " + fecha);
        sub.setStyle("-fx-text-fill:#dbeafe;");

        Button btnInv = botonBanner("Ver inventario");
        btnInv.setOnAction(e -> navegar.accept("Inventario"));
        Button btnCli = botonBanner("Ver clientes");
        btnCli.setOnAction(e -> navegar.accept("Clientes"));
        Animaciones.hover(btnInv, 1.06);
        Animaciones.hover(btnCli, 1.06);

        VBox textoBanner = new VBox(10, titulo, sub, new HBox(10, btnInv, btnCli));
        textoBanner.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textoBanner, Priority.ALWAYS);

        var vaca = Estilos.imagen(Estilos.URL_VACA, 150);
        Animaciones.flotar(vaca);

        HBox banner = new HBox(20, textoBanner, vaca);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(20, 28, 20, 28));
        banner.setStyle("-fx-background-color:linear-gradient(to right, #1f5a8c, #3a8fd1);-fx-background-radius:18;");
        Animaciones.aparecer(banner, 0);

        // ================= Tarjetas: ventas y clientes =================
        double var = dash.variacionVsAyer();
        String varTxt = String.format("%s%.1f%% vs ayer", var >= 0 ? "▲ " : "▼ ", Math.abs(var));
        DoubleFunction<String> soles = v -> String.format("S/ %.2f", v);
        DoubleFunction<String> entero = v -> String.valueOf((int) Math.round(v));

        HBox ventasCards = new HBox(14,
                stat("Ventas de hoy", dash.ventasDeHoy(), soles, Estilos.AZUL, varTxt, 80),
                stat("Pedidos de hoy", dash.pedidosDeHoy(), entero, "#00838f", "Registrados hoy", 130),
                stat("Ventas acumuladas", dash.totalVentas(), soles, Estilos.TEXTO,
                        dash.pedidosTotales() + " pedidos en total", 180),
                stat("Ticket promedio", dash.ticketPromedio(), soles, Estilos.MORADO, "Por pedido", 230),
                stat("Clientes activos", dash.clientesActivos(), entero, Estilos.VERDE,
                        dash.nuevosClientes() + " nuevos esta semana", 280));

        // ================= Tarjetas: inventario =================
        long agotados = productos.stream().filter(p -> p.stock() == 0).count();
        long bajos = productos.stream()
                .filter(p -> p.stock() > 0 && p.stock() < PoliticaInventario.STOCK_BAJO).count();
        long descuento = productos.stream().filter(PoliticaInventario::aDescuento).count();
        int unidadesTotales = productos.stream().mapToInt(Producto::stock).sum();
        double valorInv = productos.stream().mapToDouble(p -> p.precio() * p.stock()).sum();

        HBox invCards = new HBox(14,
                stat("Productos", productos.size(), entero, Estilos.AZUL, unidadesTotales + " unidades en total", 330),
                stat("Valor del inventario", valorInv, soles, Estilos.TEXTO, "A precio normal", 380),
                stat("Stock bajo", bajos, entero, Estilos.NARANJA, "Hay que reponer", 430),
                stat("Agotados", agotados, entero, Estilos.ROJO, "Sin unidades", 480),
                stat("A descuento", descuento, entero, Estilos.MORADO,
                        "-" + (int) (PoliticaInventario.DESCUENTO * 100) + "% por exceso de stock", 530));

        // ================= Productos de un vistazo (tus tarjetas, con foto) =================
        FlowPane tiles = new FlowPane(16, 16);
        int i = 0;
        for (Producto p : productos) {
            String detalle = p.presentacion() + " · " + p.stock() + " uds\n"
                    + (PoliticaInventario.aDescuento(p)
                    ? String.format("S/ %.2f → S/ %.2f (-%d%%)", p.precio(), PoliticaInventario.precioFinal(p),
                    (int) (PoliticaInventario.DESCUENTO * 100))
                    : String.format("S/ %.2f", p.precio()));
            tiles.getChildren().add(Componentes.tarjetaFoto(
                    Estilos.foto(p.categoria(), 190, 100),
                    PoliticaInventario.estado(p.stock()),
                    p.nombre(), detalle, 220, 600 + i * 70));
            i++;
        }

        // ================= Gráficos =================
        List<String> etV = new ArrayList<>();
        List<Double> valV = new ArrayList<>();
        List<String> colV = new ArrayList<>();
        double[] dias = dash.ventasUltimosDias(7);
        for (int d = 0; d < dias.length; d++) {
            etV.add(LocalDate.now().minusDays(6 - d).format(DateTimeFormatter.ofPattern("dd/MM")));
            valV.add(dias[d]);
            colV.add(Estilos.AZUL);
        }

        Map<Integer, Double> ingresos = dash.ingresosPorProducto();
        Map<String, Double> ingresosCat = new LinkedHashMap<>();
        for (Producto p : productos) {
            double ing = ingresos.getOrDefault(p.id(), 0.0);
            if (ing > 0) ingresosCat.merge(p.categoria(), ing, Double::sum);
        }
        Map<String, Double> ingresosEtiquetado = new LinkedHashMap<>();
        ingresosCat.forEach((c, v) -> ingresosEtiquetado.put(String.format("%s (S/ %.0f)", c, v), v));

        List<String> etS = new ArrayList<>();
        List<Integer> valS = new ArrayList<>();
        List<String> colS = new ArrayList<>();
        for (Producto p : productos) {
            etS.add(p.nombre());
            valS.add(p.stock());
            colS.add(Estilos.colorEstado(PoliticaInventario.estado(p.stock())));
        }

        Map<String, Double> tipos = new LinkedHashMap<>();
        dash.clientesPorTipo().forEach((t, n) -> tipos.put(t + " (" + n + ")", n.doubleValue()));

        VBox g1a = Estilos.caja(new VBox(8, Estilos.subtitulo("Ventas de los últimos 7 días (S/)"),
                Estilos.barras(etV, valV, colV)));
        VBox g1b = Estilos.caja(new VBox(8, Estilos.subtitulo("Ingresos por categoría"),
                Estilos.pastel(ingresosEtiquetado)));
        VBox g2a = Estilos.caja(new VBox(8, Estilos.subtitulo("Stock por producto (unidades)"),
                Estilos.barras(etS, valS, colS)));
        VBox g2b = Estilos.caja(new VBox(8, Estilos.subtitulo("Clientes por tipo"),
                Estilos.pastel(tipos)));
        Animaciones.aparecer(g1a, 900);
        Animaciones.aparecer(g1b, 960);
        Animaciones.aparecer(g2a, 1020);
        Animaciones.aparecer(g2b, 1080);
        HBox graficos1 = new HBox(15, g1a, g1b);
        HBox graficos2 = new HBox(15, g2a, g2b);

        // ================= Tabla: más vendidos =================
        Map<Integer, Integer> unidades = dash.unidadesPorProducto();
        List<FilaTop> top = productos.stream()
                .filter(p -> unidades.containsKey(p.id()))
                .map(p -> new FilaTop(p.nombre(), unidades.get(p.id()), ingresos.getOrDefault(p.id(), 0.0)))
                .sorted((a, b) -> Integer.compare(b.unidades(), a.unidades()))
                .toList();

        TableView<FilaTop> tablaTop = new TableView<>(FXCollections.observableArrayList(top));
        tablaTop.setPrefHeight(250);
        tablaTop.setPlaceholder(Estilos.gris("Aún no hay ventas"));
        TableColumn<FilaTop, String> tNombre = Estilos.col("Producto", FilaTop::producto);
        TableColumn<FilaTop, String> tUnid = Estilos.col("Unidades vendidas", f -> String.valueOf(f.unidades()));
        TableColumn<FilaTop, String> tIng = Estilos.col("Ingresos", f -> String.format("S/ %.2f", f.ingresos()));
        tablaTop.getColumns().addAll(List.of(tNombre, tUnid, tIng));
        tablaTop.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // ================= Tabla: últimas ventas =================
        List<FilaVenta> ult = dash.ultimasVentas(8).stream().map(v -> {
            Cliente c = dash.clientePorId(v.idCliente());
            return new FilaVenta("#" + v.id(),
                    v.fecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    c == null ? "—" : c.nombre(),
                    String.format("S/ %.2f", dash.totalDeVenta(v.id())));
        }).toList();

        TableView<FilaVenta> tablaVentas = new TableView<>(FXCollections.observableArrayList(ult));
        tablaVentas.setPrefHeight(250);
        tablaVentas.setPlaceholder(Estilos.gris("Aún no hay ventas"));
        TableColumn<FilaVenta, String> vNro = Estilos.col("N°", FilaVenta::nro);
        TableColumn<FilaVenta, String> vFecha = Estilos.col("Fecha", FilaVenta::fecha);
        TableColumn<FilaVenta, String> vCli = Estilos.col("Cliente", FilaVenta::cliente);
        TableColumn<FilaVenta, String> vTot = Estilos.col("Total", FilaVenta::total);
        tablaVentas.getColumns().addAll(List.of(vNro, vFecha, vCli, vTot));
        tablaVentas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        VBox t1 = Estilos.caja(new VBox(8, Estilos.subtitulo("Productos más vendidos"), tablaTop));
        VBox t2 = Estilos.caja(new VBox(8, Estilos.subtitulo("Últimas ventas"), tablaVentas));
        Animaciones.aparecer(t1, 1140);
        Animaciones.aparecer(t2, 1200);
        HBox tablas = new HBox(15, t1, t2);

        // ================= Tabla: alertas =================
        List<Producto> alertas = productos.stream()
                .filter(p -> p.stock() < PoliticaInventario.STOCK_BAJO || PoliticaInventario.aDescuento(p))
                .toList();

        TableView<Producto> tablaAlertas = new TableView<>(FXCollections.observableArrayList(alertas));
        tablaAlertas.setPrefHeight(220);
        tablaAlertas.setPlaceholder(Estilos.gris("Todo en orden"));
        TableColumn<Producto, String> aNombre = Estilos.col("Producto", Producto::nombre);
        TableColumn<Producto, String> aStock = Estilos.col("Stock", p -> String.valueOf(p.stock()));
        TableColumn<Producto, String> aAccion = Estilos.col("Acción sugerida", this::accion);
        TableColumn<Producto, String> aNormal = Estilos.col("Precio normal", p -> String.format("S/ %.2f", p.precio()));
        TableColumn<Producto, String> aFinal = Estilos.col("Precio final",
                p -> String.format("S/ %.2f", PoliticaInventario.precioFinal(p)));
        tablaAlertas.getColumns().addAll(List.of(aNombre, aStock, aAccion, aNormal, aFinal));
        tablaAlertas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        VBox alertasBox = Estilos.caja(new VBox(8,
                Estilos.subtitulo("Alertas de inventario y descuentos"), tablaAlertas));
        Animaciones.aparecer(alertasBox, 1260);

        // ================= Armado final =================
        VBox root = new VBox(18,
                banner,
                Estilos.subtitulo("Ventas y clientes"), ventasCards,
                Estilos.subtitulo("Inventario"), invCards,
                Estilos.subtitulo("Productos de un vistazo"), tiles,
                graficos1, graficos2, tablas, alertasBox);
        root.setPadding(new Insets(24));
        return root;
    }

    // Tarjeta de dato: sube con el mouse, aparece escalonada y el número cuenta desde 0
    private VBox stat(String texto, double valor, DoubleFunction<String> formato,
                      String color, String detalle, int retraso) {
        VBox c = Estilos.tarjeta(texto, formato.apply(valor), color, detalle);
        Label numero = (Label) c.getChildren().get(0);
        Animaciones.elevar(c);
        Animaciones.aparecer(c, retraso);
        Animaciones.contador(numero, valor, formato, retraso);
        return c;
    }

    private Button botonBanner(String texto) {
        Button b = new Button(texto);
        b.setStyle("-fx-background-color:white;-fx-text-fill:" + Estilos.AZUL + ";-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-padding:7 16 7 16;-fx-cursor:hand;");
        return b;
    }

    private String accion(Producto p) {
        if (p.stock() == 0) return "Reponer urgente";
        if (p.stock() < PoliticaInventario.STOCK_BAJO) return "Reponer stock";
        return "Aplicar descuento";
    }
}