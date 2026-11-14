package vallegrande.edu.pe.misistema.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import vallegrande.edu.pe.misistema.controller.Async;
import vallegrande.edu.pe.misistema.controller.ClienteController;
import vallegrande.edu.pe.misistema.model.Cliente;

import java.util.Locale;

/** Ficha comercial de los clientes: contacto, dirección, tipo y pedidos. */
public class ClientesView implements Page {
    private final ClienteController controller = new ClienteController();
    private final VBox raiz;
    private final ObservableList<Cliente> datos = FXCollections.observableArrayList();
    private final FilteredList<Cliente> filtrados = new FilteredList<>(datos, c -> true);
    private final TableView<Cliente> tabla = new TableView<>();
    private final TextField txtBuscar = new TextField();
    private final Label lblRegistros = new Label("0 registros");
    private final Label lblEstado = new Label();

    public ClientesView() {
        lblRegistros.getStyleClass().add("lbl-registros");
        lblEstado.getStyleClass().add("lbl-registros");

        txtBuscar.setPromptText("Buscar por nombre, correo, teléfono o dirección");
        txtBuscar.textProperty().addListener((o, a, nuevo) -> {
            String q = nuevo == null ? "" : nuevo.trim().toLowerCase(Locale.ROOT);
            filtrados.setPredicate(c -> q.isEmpty()
                    || nz(c.getNombre()).toLowerCase(Locale.ROOT).contains(q)
                    || nz(c.getCorreo()).toLowerCase(Locale.ROOT).contains(q)
                    || nz(c.getTelefono()).toLowerCase(Locale.ROOT).contains(q)
                    || nz(c.getDireccion()).toLowerCase(Locale.ROOT).contains(q));
            actualizarContador();
        });

        FlowPane barra = Ui.barra(txtBuscar,
                Ui.boton("Nuevo", "btn-actualizar", () -> abrirFormulario(null)),
                Ui.boton("Editar", "btn-limpiar", this::editar),
                Ui.boton("Eliminar", "btn-eliminar", this::eliminar),
                Ui.boton("Limpiar", "btn-limpiar", () -> { txtBuscar.clear(); tabla.getSelectionModel().clearSelection(); }));

        tabla.setPlaceholder(new Label("Aún no hay clientes registrados"));
        TableColumn<Cliente, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);
        tabla.getColumns().add(colId);
        tabla.getColumns().add(texto("Nombre", "nombre", 200));
        tabla.getColumns().add(texto("Teléfono", "telefono", 110));
        tabla.getColumns().add(texto("Correo", "correo", 210));
        tabla.getColumns().add(texto("Dirección", "direccion", 220));
        tabla.getColumns().add(texto("Tipo", "tipo", 100));
        tabla.getColumns().add(texto("Registro", "fechaRegistro", 100));
        TableColumn<Cliente, Integer> colPedidos = new TableColumn<>("Pedidos");
        colPedidos.setCellValueFactory(new PropertyValueFactory<>("pedidos"));
        colPedidos.setPrefWidth(80);
        tabla.getColumns().add(colPedidos);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setRowFactory(tv -> {
            TableRow<Cliente> fila = new TableRow<>();
            fila.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !fila.isEmpty()) abrirFormulario(fila.getItem()); });
            return fila;
        });
        tabla.setItems(filtrados);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        VBox card = Ui.tarjeta();
        card.getChildren().addAll(
                Ui.encabezado("Clientes", "Datos comerciales de tus clientes. Doble clic en una fila para editarla.",
                        lblRegistros, Ui.boton("Actualizar", "btn-actualizar", this::cargar)),
                barra, tabla, lblEstado);
        raiz = Ui.pagina(card);
    }

    private static String nz(String s) { return s == null ? "" : s; }

    private static TableColumn<Cliente, String> texto(String titulo, String propiedad, double ancho) {
        TableColumn<Cliente, String> c = new TableColumn<>(titulo);
        c.setCellValueFactory(new PropertyValueFactory<>(propiedad));
        c.setPrefWidth(ancho);
        return c;
    }

    private void actualizarContador() {
        int n = filtrados.size();
        lblRegistros.setText(n + (n == 1 ? " registro" : " registros"));
    }

    private Cliente seleccionado(String titulo) {
        Cliente c = tabla.getSelectionModel().getSelectedItem();
        if (c == null) Ui.aviso(titulo, "Selecciona un cliente de la tabla primero.");
        return c;
    }

    private void editar() {
        Cliente c = seleccionado("Editar cliente");
        if (c != null) abrirFormulario(c);
    }

    /** c == null → crear; c != null → editar. */
    private void abrirFormulario(Cliente c) {
        TextField nombre = Ui.campo(c == null ? "" : c.getNombre());
        TextField telefono = Ui.campo(c == null ? "" : c.getTelefono());
        TextField correo = Ui.campo(c == null ? "" : c.getCorreo());
        TextField direccion = Ui.campo(c == null ? "" : c.getDireccion());
        ComboBox<String> tipo = new ComboBox<>(FXCollections.observableArrayList("Minorista", "Mayorista", "Distribuidor"));
        tipo.setEditable(true);
        tipo.setValue(c == null ? "Minorista" : nz(c.getTipo()));
        nombre.setPromptText("Nombre completo del cliente");
        telefono.setPromptText("Ej. 987654321 (opcional)");
        correo.setPromptText("Ej. nombre@gmail.com");
        direccion.setPromptText("Ej. Av. Lima 123, Lima (opcional)");

        GridPane g = Ui.grid();
        Ui.fila(g, 0, "Nombre *", nombre);
        Ui.fila(g, 1, "Teléfono (opcional)", telefono);
        Ui.fila(g, 2, "Correo *", correo);
        Ui.fila(g, 3, "Dirección", direccion);
        Ui.fila(g, 4, "Tipo de cliente", tipo);

        int id = c == null ? 0 : c.getId();
        Ui.formulario(c == null ? "Nuevo cliente" : "Editar cliente", g,
                () -> { controller.guardar(id, nombre.getText(), telefono.getText(), correo.getText(),
                        direccion.getText(), tipo.getEditor().getText()); return true; },
                () -> { Ui.ok(lblEstado, id == 0 ? "Cliente creado." : "Cliente actualizado."); cargar(); });
    }

    private void eliminar() {
        Cliente c = seleccionado("Eliminar cliente");
        if (c == null) return;
        if (!Ui.confirmar("Eliminar cliente", "¿Eliminar a " + nz(c.getNombre()) + "?")) return;
        Async.run(() -> { controller.eliminar(c.getId()); return true; },
                ok -> { cargar(); Ui.ok(lblEstado, "Cliente eliminado."); },
                e -> Ui.error(lblEstado, e));
    }

    @Override public Node getView() { return raiz; }

    @Override public void cargar() {
        Async.run(controller::listar, lista -> {
            datos.setAll(lista);
            actualizarContador();
        }, e -> Ui.error(lblEstado, e));
    }
}
