package vallegrande.edu.pe.view;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import vallegrande.edu.pe.controller.PoliticaInventario;
import vallegrande.edu.pe.controller.ProductoController;
import vallegrande.edu.pe.model.Producto;

import java.util.List;
import java.util.function.DoubleFunction;
import java.util.stream.Collectors;

public class ProductosView {

    private static final String CHIP_ON = "-fx-background-color:" + Estilos.AZUL + ";-fx-text-fill:white;"
            + "-fx-font-weight:bold;-fx-background-radius:20;-fx-padding:6 16 6 16;-fx-cursor:hand;";
    private static final String CHIP_OFF = "-fx-background-color:white;-fx-text-fill:" + Estilos.TEXTO + ";"
            + "-fx-border-color:#d5dbe3;-fx-border-radius:20;-fx-background-radius:20;-fx-padding:6 16 6 16;-fx-cursor:hand;";

    private final ProductoController ctrl;

    private ObservableList<Producto> datos;
    private FilteredList<Producto> filtrados;
    private TableView<Producto> tabla;
    private HBox tarjetas;
    private VBox avisos;

    private Label lblFormTitulo;
    private Label lblError;
    private TextField txtNombre;
    private TextField txtPresentacion;
    private TextField txtPrecio;
    private TextField txtStock;
    private TextField txtImagenUrl;
    private ComboBox<String> cmbCategoria;
    private Button btnGuardar;

    private Producto editando;
    private String filtroEstado;
    private String busqueda;

    public ProductosView(ProductoController ctrl) {
        this.ctrl = ctrl;
    }

    public VBox crear() {
        editando = null;
        filtroEstado = "Todos";
        busqueda = "";
        datos = FXCollections.observableArrayList(ctrl.listarTodos());
        filtrados = new FilteredList<>(datos, p -> true);

        VBox encabezado = Componentes.encabezado("Inventario de productos 📦",
                "Controla qué hay en stock, qué se está acabando y registra productos nuevos");

        tarjetas = new HBox(14);
        avisos = new VBox(8);

        // ---------- Filtros por estado ----------
        ToggleGroup grupo = new ToggleGroup();
        HBox filtros = new HBox(8);
        String[][] opciones = {{"Todos", "Todos"}, {"En stock", "En stock"},
                {"Stock bajo", "Stock bajo"}, {"Agotados", "Agotado"}};
        ToggleButton primero = null;
        for (String[] op : opciones) {
            ToggleButton b = new ToggleButton(op[0]);
            b.setUserData(op[1]);
            b.setToggleGroup(grupo);
            b.setStyle(CHIP_OFF);
            b.selectedProperty().addListener((o, a, sel) -> b.setStyle(sel ? CHIP_ON : CHIP_OFF));
            Animaciones.hover(b, 1.05);
            filtros.getChildren().add(b);
            if (primero == null) primero = b;
        }
        grupo.selectedToggleProperty().addListener((o, viejo, nuevo) -> {
            if (nuevo == null) {
                if (viejo != null) viejo.setSelected(true);
                return;
            }
            filtroEstado = (String) nuevo.getUserData();
            aplicarFiltro();
        });
        primero.setSelected(true);

        // ---------- Buscador y botones ----------
        TextField buscar = new TextField();
        buscar.setPromptText("Buscar producto o categoría...");
        buscar.setPrefWidth(230);
        buscar.textProperty().addListener((o, a, nuevo) -> {
            busqueda = nuevo == null ? "" : nuevo;
            aplicarFiltro();
        });

        Button btnEditar = Estilos.botonSecundario("Editar");
        btnEditar.setOnAction(e -> {
            Producto sel = tabla.getSelectionModel().getSelectedItem();
            if (sel == null) {
                mensaje("Primero selecciona un producto de la tabla.");
                return;
            }
            cargarEnFormulario(sel);
        });

        Button btnStock = Estilos.botonSecundario("Ajustar stock");
        btnStock.setOnAction(e -> ajustarStock());

        Button btnEliminar = Estilos.botonSecundario("Eliminar");
        btnEliminar.setOnAction(e -> eliminar());

        Animaciones.hover(btnEditar, 1.06);
        Animaciones.hover(btnStock, 1.06);
        Animaciones.hover(btnEliminar, 1.06);

        HBox barra = new HBox(10, buscar, btnEditar, btnStock, btnEliminar);

        // ---------- Tabla ----------
        tabla = new TableView<>(filtrados);
        tabla.setPrefHeight(440);
        tabla.setPlaceholder(Estilos.gris("No hay productos con ese filtro"));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Producto, Producto> colFoto = new TableColumn<>();
        colFoto.setGraphic(Estilos.encabezadoCol("Foto"));
        colFoto.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        colFoto.setCellFactory(c -> new TableCell<Producto, Producto>() {
            @Override
            protected void updateItem(Producto p, boolean empty) {
                super.updateItem(p, empty);
                setText(null);
                setGraphic(empty || p == null ? null : Estilos.foto(p.categoria(), 48, 48));
            }
        });
        colFoto.setMinWidth(70);
        colFoto.setMaxWidth(70);
        colFoto.setSortable(false);

        TableColumn<Producto, String> colNombre =
                Estilos.col("Producto", p -> p.nombre() + " (" + p.presentacion() + ")");
        TableColumn<Producto, String> colCategoria = Estilos.col("Categoría", Producto::categoria);
        TableColumn<Producto, String> colPrecio =
                Estilos.col("Precio", p -> String.format("S/ %.2f", p.precio()));
        TableColumn<Producto, String> colDesc = Estilos.col("Desc.", p ->
                PoliticaInventario.aDescuento(p) ? "-" + (int) (PoliticaInventario.DESCUENTO * 100) + "%" : "—");
        TableColumn<Producto, String> colStock = Estilos.col("Stock", p -> String.valueOf(p.stock()));

        TableColumn<Producto, String> colEstado = new TableColumn<>();
        colEstado.setGraphic(Estilos.encabezadoCol("Estado"));
        colEstado.setCellValueFactory(c ->
                new SimpleStringProperty(PoliticaInventario.estado(c.getValue().stock())));
        colEstado.setCellFactory(c -> new TableCell<Producto, String>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                setText(null);
                setGraphic(empty || estado == null ? null : Estilos.chip(estado));
            }
        });

        tabla.getColumns().addAll(List.of(colFoto, colNombre, colCategoria, colPrecio, colDesc, colStock, colEstado));

        VBox izquierda = new VBox(12, filtros, barra, tabla);
        HBox.setHgrow(izquierda, Priority.ALWAYS);

        HBox cuerpo = new HBox(18, izquierda, crearFormulario());

        VBox root = new VBox(16, encabezado, tarjetas, avisos, cuerpo);
        root.setPadding(new Insets(24));

        refrescarResumen();
        return root;
    }

    // =============== Formulario ===============
    private VBox crearFormulario() {
        lblFormTitulo = Estilos.subtitulo("Nuevo producto");

        txtNombre = campo("Ej. Leche Entera Fresca");
        txtPresentacion = campo("Ej. 1 Litro, 500 g");
        txtPrecio = campo("0.00");
        txtStock = campo("0");
        txtImagenUrl = campo("https://... (opcional)");

        cmbCategoria = new ComboBox<>(FXCollections.observableArrayList(ctrl.categorias()));
        cmbCategoria.setEditable(true);
        cmbCategoria.getEditor().setPromptText("Elige o escribe una categoría");
        cmbCategoria.setMaxWidth(Double.MAX_VALUE);

        lblError = new Label();
        lblError.setWrapText(true);
        lblError.setStyle("-fx-text-fill:" + Estilos.ROJO + ";-fx-font-weight:bold;");

        btnGuardar = Estilos.botonPrimario("Agregar producto");
        btnGuardar.setOnAction(e -> guardar());
        Button btnLimpiar = Estilos.botonSecundario("Limpiar");
        btnLimpiar.setOnAction(e -> limpiarFormulario());
        Animaciones.hover(btnGuardar, 1.06);
        Animaciones.hover(btnLimpiar, 1.06);

        VBox f = new VBox(6,
                lblFormTitulo,
                Estilos.negrita("Nombre"), txtNombre,
                Estilos.negrita("Categoría"), cmbCategoria,
                Estilos.negrita("Presentación"), txtPresentacion,
                Estilos.negrita("Precio (S/)"), txtPrecio,
                Estilos.negrita("Stock inicial"), txtStock,
                Estilos.negrita("URL de la imagen"), txtImagenUrl,
                lblError,
                new HBox(8, btnGuardar, btnLimpiar));
        f.setPadding(new Insets(18));
        f.setPrefWidth(290);
        f.setMinWidth(290);
        f.setMaxWidth(290);
        f.setStyle("-fx-background-color:white;-fx-background-radius:14;-fx-border-color:#eceff3;-fx-border-radius:14;");
        return f;
    }

    private TextField campo(String prompt) {
        TextField t = new TextField();
        t.setPromptText(prompt);
        t.setStyle("-fx-text-fill:" + Estilos.TEXTO + ";");
        return t;
    }

    private void guardar() {
        String nombre = txtNombre.getText().trim();
        String categoria = cmbCategoria.getEditor().getText().trim();
        String presentacion = txtPresentacion.getText().trim();
        String imagenUrl = txtImagenUrl.getText().trim();

        if (nombre.isEmpty() || categoria.isEmpty() || presentacion.isEmpty()) {
            lblError.setText("Completa nombre, categoría y presentación.");
            return;
        }
        if (!imagenUrl.isEmpty() && !imagenUrl.startsWith("http://") && !imagenUrl.startsWith("https://")) {
            lblError.setText("La URL de la imagen debe empezar con http:// o https://");
            return;
        }
        double precio;
        int stock;
        try {
            precio = Double.parseDouble(txtPrecio.getText().trim().replace(',', '.'));
            stock = Integer.parseInt(txtStock.getText().trim());
        } catch (NumberFormatException ex) {
            lblError.setText("El precio y el stock deben ser números (el stock, entero).");
            return;
        }
        if (precio <= 0 || stock < 0) {
            lblError.setText("El precio debe ser mayor a 0 y el stock no puede ser negativo.");
            return;
        }

        if (editando == null) {
            ctrl.agregar(nombre, categoria, presentacion, precio, stock, imagenUrl);
        } else {
            ctrl.editar(editando.id(), nombre, categoria, presentacion, precio, stock);
        }
        recargar();
        limpiarFormulario();
    }

    private void cargarEnFormulario(Producto p) {
        editando = p;
        txtNombre.setText(p.nombre());
        cmbCategoria.getEditor().setText(p.categoria());
        txtPresentacion.setText(p.presentacion());
        txtPrecio.setText(String.valueOf(p.precio()));
        txtStock.setText(String.valueOf(p.stock()));
        txtImagenUrl.clear();
        lblError.setText("");
        lblFormTitulo.setText("Editar producto");
        btnGuardar.setText("Guardar cambios");
    }

    private void limpiarFormulario() {
        editando = null;
        txtNombre.clear();
        cmbCategoria.getEditor().clear();
        cmbCategoria.setValue(null);
        txtPresentacion.clear();
        txtPrecio.clear();
        txtStock.clear();
        txtImagenUrl.clear();
        lblError.setText("");
        lblFormTitulo.setText("Nuevo producto");
        btnGuardar.setText("Agregar producto");
    }

    // =============== Acciones ===============
    private void eliminar() {
        Producto sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mensaje("Primero selecciona un producto de la tabla.");
            return;
        }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar \"" + sel.nombre() + "\" del inventario?", ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        a.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
            ctrl.eliminar(sel.id());
            if (editando != null && editando.id() == sel.id()) limpiarFormulario();
            recargar();
        });
    }

    private void ajustarStock() {
        Producto sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mensaje("Primero selecciona un producto de la tabla.");
            return;
        }
        TextInputDialog d = new TextInputDialog("10");
        d.setTitle("Ajustar stock");
        d.setHeaderText(sel.nombre() + " (stock actual: " + sel.stock() + ")");
        d.setContentText("Cantidad (+ ingreso, - salida):");
        d.showAndWait().ifPresent(txt -> {
            try {
                ctrl.ajustarStock(sel.id(), Integer.parseInt(txt.trim()));
                recargar();
            } catch (NumberFormatException ex) {
                mensaje("Escribe un número entero, por ejemplo 20 o -5.");
            }
        });
    }

    private void mensaje(String texto) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, texto);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void recargar() {
        datos.setAll(ctrl.listarTodos());
        cmbCategoria.getItems().setAll(ctrl.categorias());
        refrescarResumen();
    }

    private void aplicarFiltro() {
        String q = busqueda.toLowerCase().trim();
        filtrados.setPredicate(p -> {
            boolean okTexto = q.isEmpty()
                    || p.nombre().toLowerCase().contains(q)
                    || p.categoria().toLowerCase().contains(q);
            boolean okEstado = filtroEstado.equals("Todos")
                    || PoliticaInventario.estado(p.stock()).equals(filtroEstado);
            return okTexto && okEstado;
        });
    }

    // =============== Resumen y avisos ===============
    private VBox stat(String texto, double valor, DoubleFunction<String> formato, String color, String detalle) {
        VBox c = Estilos.tarjeta(texto, formato.apply(valor), color, detalle);
        Animaciones.elevar(c);
        Animaciones.contador((Label) c.getChildren().get(0), valor, formato, 0);
        return c;
    }

    private void refrescarResumen() {
        List<Producto> agotados = datos.stream().filter(p -> p.stock() == 0).toList();
        List<Producto> bajos = datos.stream()
                .filter(p -> p.stock() > 0 && p.stock() < PoliticaInventario.STOCK_BAJO).toList();
        long enStock = datos.size() - agotados.size() - bajos.size();
        int unidades = datos.stream().mapToInt(Producto::stock).sum();
        double valor = datos.stream().mapToDouble(p -> p.precio() * p.stock()).sum();

        DoubleFunction<String> entero = v -> String.valueOf((int) Math.round(v));
        DoubleFunction<String> soles = v -> String.format("S/ %.2f", v);

        tarjetas.getChildren().setAll(
                stat("Productos", datos.size(), entero, Estilos.AZUL, unidades + " unidades en total"),
                stat("En stock", enStock, entero, Estilos.VERDE, "Con stock suficiente"),
                stat("Stock bajo", bajos.size(), entero, Estilos.NARANJA, "Hay que reponer"),
                stat("Agotados", agotados.size(), entero, Estilos.ROJO, "Sin unidades"),
                stat("Valor del inventario", valor, soles, Estilos.TEXTO, "A precio normal"));

        avisos.getChildren().clear();
        if (agotados.isEmpty()) {
            avisos.getChildren().add(Estilos.aviso("✔ No hay productos agotados", "#e3f4e6", Estilos.VERDE));
        } else {
            avisos.getChildren().add(Estilos.aviso("⛔ Se acabaron: " + nombres(agotados), "#fde8e8", Estilos.ROJO));
        }
        if (!bajos.isEmpty()) {
            avisos.getChildren().add(Estilos.aviso("⚠ Por acabarse (menos de "
                    + PoliticaInventario.STOCK_BAJO + " unidades): " + nombres(bajos), "#fff0dc", Estilos.NARANJA));
        }
    }

    private String nombres(List<Producto> lista) {
        return lista.stream().map(Producto::nombre).collect(Collectors.joining(", "));
    }
}