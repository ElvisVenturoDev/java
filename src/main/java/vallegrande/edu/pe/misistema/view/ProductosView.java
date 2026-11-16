package vallegrande.edu.pe.misistema.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import vallegrande.edu.pe.misistema.controller.Asincrono;
import vallegrande.edu.pe.misistema.controller.ProductoController;
import vallegrande.edu.pe.misistema.model.Producto;

import java.util.Locale;

public class ProductosView implements Page {
    private final ProductoController controller = new ProductoController();
    private final VBox raiz;
    private final ObservableList<Producto> datos = FXCollections.observableArrayList();
    private final FilteredList<Producto> filtrados = new FilteredList<>(datos, p -> true);
    private final TableView<Producto> tabla = new TableView<>();
    private final TextField txtBuscar = new TextField();
    private final Label lblRegistros = new Label("0 registros");
    private final Label lblEstado = new Label();

    public ProductosView() {
        lblRegistros.getStyleClass().add("lbl-registros");
        lblEstado.getStyleClass().add("lbl-registros");

        txtBuscar.setPromptText("Buscar por nombre, categoría o ID");
        txtBuscar.textProperty().addListener((o, a, nuevo) -> {
            String q = nuevo == null ? "" : nuevo.trim().toLowerCase(Locale.ROOT);
            filtrados.setPredicate(p -> q.isEmpty()
                    || p.getNombre().toLowerCase(Locale.ROOT).contains(q)
                    || textoSeguro(p.getCategoria()).toLowerCase(Locale.ROOT).contains(q)
                    || String.valueOf(p.getId()).equals(q));
            actualizarContador();
        });

        FlowPane barra = Ui.barra(txtBuscar,
                Ui.boton("Nuevo", "btn-actualizar", () -> abrirFormulario(null)),
                Ui.boton("Editar", "btn-limpiar", this::editar),
                Ui.boton("Eliminar", "btn-eliminar", this::eliminar),
                Ui.boton("Limpiar", "btn-limpiar", () -> { txtBuscar.clear(); tabla.getSelectionModel().clearSelection(); }));

        tabla.setPlaceholder(new Label("Aún no hay productos registrados"));
        TableColumn<Producto, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);
        TableColumn<Producto, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(260);
        TableColumn<Producto, String> colCat = new TableColumn<>("Categoría");
        colCat.setCellValueFactory(new PropertyValueFactory<>("categoriaTexto"));
        colCat.setPrefWidth(130);
        TableColumn<Producto, String> colDetalle = new TableColumn<>("Detalle");
        colDetalle.setCellValueFactory(new PropertyValueFactory<>("detalle"));
        colDetalle.setPrefWidth(120);
        TableColumn<Producto, Double> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colPrecio.setPrefWidth(100);
        colPrecio.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double v, boolean vacio) {
                super.updateItem(v, vacio);
                setText(vacio || v == null ? null : String.format(Locale.US, "S/ %.2f", v));
            }
        });
        TableColumn<Producto, Integer> colStock = new TableColumn<>("Stock");
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStock.setPrefWidth(90);
        tabla.getColumns().addAll(colId, colNombre, colCat, colDetalle, colPrecio, colStock);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setRowFactory(tv -> {
            TableRow<Producto> fila = new TableRow<>();
            fila.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !fila.isEmpty()) abrirFormulario(fila.getItem()); });
            return fila;
        });

        SortedList<Producto> ordenados = new SortedList<>(filtrados);
        ordenados.comparatorProperty().bind(tabla.comparatorProperty());
        tabla.setItems(ordenados);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        VBox card = Ui.tarjeta();
        card.getChildren().addAll(
                Ui.encabezado("Productos", "Catálogo guardado en la base de datos. Doble clic en una fila para editarla.",
                        lblRegistros, Ui.boton("Actualizar", "btn-actualizar", this::cargar)),
                barra, tabla, lblEstado);
        raiz = Ui.pagina(card);
    }

    private static String textoSeguro(String s) { return s == null ? "" : s; }

    private void actualizarContador() {
        int n = filtrados.size();
        lblRegistros.setText(n + (n == 1 ? " registro" : " registros"));
    }

    private void editar() {
        Producto p = tabla.getSelectionModel().getSelectedItem();
        if (p == null) { Ui.aviso("Editar producto", "Selecciona un producto de la tabla primero."); return; }
        abrirFormulario(p);
    }

    /** p == null → crear; p != null → editar. */
    private void abrirFormulario(Producto p) {
        TextField nombre = Ui.campo(p == null ? "" : p.getNombre());
        ComboBox<String> categoria = new ComboBox<>(FXCollections.observableArrayList(
                "leche", "yogurt", "queso", "mantequilla", "crema"));
        categoria.setEditable(true);
        categoria.setValue(p == null ? "leche" : textoSeguro(p.getCategoria()));
        TextField detalle = Ui.campo(p == null ? "" : p.getDetalle());
        detalle.setPromptText("Ej. 1 litro, 200 gramos");
        nombre.setPromptText("Ej. Leche Entera Fresca");
        TextArea descripcion = new TextArea(p == null ? "" : textoSeguro(p.getDescripcion()));
        descripcion.setPrefRowCount(3);
        descripcion.setWrapText(true);
        TextField imagen = Ui.campo(p == null ? "" : p.getUrlImagen());
        imagen.setPromptText("https://... (opcional)");
        descripcion.setPromptText("Texto corto sobre el producto (opcional)");
        TextField precio = Ui.campo(p == null ? "" : String.format(Locale.US, "%.2f", p.getPrecio()));
        precio.setPromptText("Ej. 6.50");
        TextField stock = Ui.campo(p == null ? "0" : String.valueOf(p.getStock()));
        stock.setPromptText("Unidades disponibles. Ej. 20");
        TextField stockMin = Ui.campo(p == null ? "5" : String.valueOf(p.getMinimoStock()));
        stockMin.setPromptText("Avisar cuando queden pocas. Ej. 5");

        GridPane g = Ui.grid();
        Ui.fila(g, 0, "Nombre *", nombre);
        Ui.fila(g, 1, "Categoría *", categoria);
        Ui.fila(g, 2, "Detalle (opcional)", detalle);
        Ui.fila(g, 3, "Descripción (opcional)", descripcion);
        Ui.fila(g, 4, "Imagen (opcional)", imagen);
        Ui.fila(g, 5, "Precio (S/) *", precio);
        Ui.fila(g, 6, "Stock *", stock);
        Ui.fila(g, 7, "Stock mínimo *", stockMin);

        int id = p == null ? 0 : p.getId();
        Ui.formulario(p == null ? "Nuevo producto" : "Editar producto", g,
                () -> { controller.guardar(id, nombre.getText(), categoria.getEditor().getText(), detalle.getText(),
                        descripcion.getText(), imagen.getText(), precio.getText(), stock.getText(), stockMin.getText());
                    return true; },
                () -> { Ui.ok(lblEstado, id == 0 ? "Producto creado." : "Producto actualizado."); cargar(); });
    }

    private void eliminar() {
        Producto p = tabla.getSelectionModel().getSelectedItem();
        if (p == null) { Ui.aviso("Eliminar producto", "Selecciona un producto de la tabla primero."); return; }
        if (!Ui.confirmar("Eliminar producto", "¿Eliminar \"" + p.getNombre() + "\"?")) return;
        Asincrono.ejecutar(() -> controller.eliminar(p.getId()), borrado -> {
            cargar();
            Ui.ok(lblEstado, borrado ? "Producto eliminado."
                    : "El producto tiene ventas registradas: se desactivó y ya no aparece en la web.");
        }, e -> Ui.error(lblEstado, e));
    }

    @Override public Node getView() { return raiz; }

    @Override public void cargar() {
        Asincrono.ejecutar(controller::listar, lista -> {
            datos.setAll(lista);
            actualizarContador();
        }, e -> Ui.error(lblEstado, e));
    }
}
