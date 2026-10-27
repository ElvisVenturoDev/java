package vallegrande.edu.pe.misistema.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import vallegrande.edu.pe.misistema.controller.Async;
import vallegrande.edu.pe.misistema.controller.CuentaController;
import vallegrande.edu.pe.misistema.model.Cuenta;

import java.util.Locale;

/** Cuentas de clientes registrados en la web (tabla cliente). */
public class CuentaView implements Page {
    private final CuentaController controller = new CuentaController();
    private final VBox raiz;
    private final ObservableList<Cuenta> datos = FXCollections.observableArrayList();
    private final FilteredList<Cuenta> filtrados = new FilteredList<>(datos, c -> true);
    private final TableView<Cuenta> tabla = new TableView<>();
    private final TextField txtBuscar = new TextField();
    private final Label lblRegistros = new Label("0 registros");
    private final Label lblEstado = new Label();

    public CuentaView() {
        lblRegistros.getStyleClass().add("lbl-registros");
        lblEstado.getStyleClass().add("lbl-registros");

        txtBuscar.setPromptText("Buscar por nombre, correo o teléfono");
        txtBuscar.textProperty().addListener((o, a, nuevo) -> {
            String q = nuevo == null ? "" : nuevo.trim().toLowerCase(Locale.ROOT);
            filtrados.setPredicate(c -> q.isEmpty()
                    || nz(c.getNombre()).toLowerCase(Locale.ROOT).contains(q)
                    || nz(c.getCorreo()).toLowerCase(Locale.ROOT).contains(q)
                    || nz(c.getTelefono()).toLowerCase(Locale.ROOT).contains(q));
            actualizarContador();
        });

        HBox barra = Ui.barra(txtBuscar,
                Ui.boton("Nuevo", "btn-actualizar", () -> abrirFormulario(null)),
                Ui.boton("Editar", "btn-limpiar", this::editar),
                Ui.boton("Activar / Desactivar", "btn-limpiar", this::cambiarEstado),
                Ui.boton("Eliminar", "btn-eliminar", this::eliminar),
                Ui.boton("Limpiar", "btn-limpiar", () -> { txtBuscar.clear(); tabla.getSelectionModel().clearSelection(); }));

        tabla.setPlaceholder(new Label("Aún no hay cuentas registradas"));
        TableColumn<Cuenta, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);
        tabla.getColumns().add(colId);
        tabla.getColumns().add(texto("Nombre", "nombre", 240));
        tabla.getColumns().add(texto("Teléfono", "telefono", 140));
        tabla.getColumns().add(texto("Correo", "correo", 260));
        tabla.getColumns().add(texto("Estado", "estado", 110));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setRowFactory(tv -> {
            TableRow<Cuenta> fila = new TableRow<>();
            fila.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !fila.isEmpty()) abrirFormulario(fila.getItem()); });
            return fila;
        });
        tabla.setItems(filtrados);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        VBox card = Ui.tarjeta();
        card.getChildren().addAll(
                Ui.encabezado("Cuenta", "Clientes registrados en la web. Doble clic en una fila para editarla.",
                        lblRegistros, Ui.boton("Actualizar", "btn-actualizar", this::cargar)),
                barra, tabla, lblEstado);
        raiz = Ui.pagina(card);
    }

    private static String nz(String s) { return s == null ? "" : s; }

    private static TableColumn<Cuenta, String> texto(String titulo, String propiedad, double ancho) {
        TableColumn<Cuenta, String> c = new TableColumn<>(titulo);
        c.setCellValueFactory(new PropertyValueFactory<>(propiedad));
        c.setPrefWidth(ancho);
        return c;
    }

    private void actualizarContador() {
        int n = filtrados.size();
        lblRegistros.setText(n + (n == 1 ? " registro" : " registros"));
    }

    private Cuenta seleccionada(String titulo) {
        Cuenta c = tabla.getSelectionModel().getSelectedItem();
        if (c == null) Ui.aviso(titulo, "Selecciona una cuenta de la tabla primero.");
        return c;
    }

    private void editar() {
        Cuenta c = seleccionada("Editar cuenta");
        if (c != null) abrirFormulario(c);
    }

    /** c == null → crear; c != null → editar. */
    private void abrirFormulario(Cuenta c) {
        TextField nombre = Ui.campo(c == null ? "" : c.getNombre());
        TextField telefono = Ui.campo(c == null ? "" : c.getTelefono());
        TextField correo = Ui.campo(c == null ? "" : c.getCorreo());
        nombre.setPromptText("Nombre completo del cliente");
        telefono.setPromptText("Ej. 987654321 (opcional)");
        correo.setPromptText("Ej. nombre@gmail.com");

        GridPane g = Ui.grid();
        Ui.fila(g, 0, "Nombre *", nombre);
        Ui.fila(g, 1, "Teléfono (opcional)", telefono);
        Ui.fila(g, 2, "Correo *", correo);

        int id = c == null ? 0 : c.getId();
        Ui.formulario(c == null ? "Nueva cuenta" : "Editar cuenta", g,
                () -> { controller.guardar(id, nombre.getText(), telefono.getText(), correo.getText()); return true; },
                () -> { Ui.ok(lblEstado, id == 0 ? "Cuenta creada." : "Cuenta actualizada."); cargar(); });
    }

    private void cambiarEstado() {
        Cuenta c = seleccionada("Cuenta");
        if (c == null) return;
        boolean nuevo = !c.isActivo();
        if (!Ui.confirmar("Cuenta", "¿Seguro que quieres " + (nuevo ? "activar" : "desactivar")
                + " la cuenta de " + nz(c.getNombre()) + "?")) return;
        Async.run(() -> { controller.cambiarEstado(c.getId(), nuevo); return true; },
                ok -> { cargar(); Ui.ok(lblEstado, nuevo ? "Cuenta activada." : "Cuenta desactivada."); },
                e -> Ui.error(lblEstado, e));
    }

    private void eliminar() {
        Cuenta c = seleccionada("Eliminar cuenta");
        if (c == null) return;
        if (!Ui.confirmar("Eliminar cuenta", "¿Eliminar la cuenta de " + nz(c.getNombre()) + "?")) return;
        Async.run(() -> { controller.eliminar(c.getId()); return true; },
                ok -> { cargar(); Ui.ok(lblEstado, "Cuenta eliminada."); },
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
