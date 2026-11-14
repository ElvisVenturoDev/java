package vallegrande.edu.pe.misistema.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import vallegrande.edu.pe.misistema.controller.Async;
import vallegrande.edu.pe.misistema.controller.MensajeController;
import vallegrande.edu.pe.misistema.model.Mensaje;

import java.util.Locale;

public class ContactosView implements Page {
    private final MensajeController controller = new MensajeController();
    private final VBox raiz;
    private final ObservableList<Mensaje> datos = FXCollections.observableArrayList();
    private final FilteredList<Mensaje> filtrados = new FilteredList<>(datos, m -> true);
    private final TableView<Mensaje> tabla = new TableView<>();
    private final TextField txtBuscar = new TextField();
    private final TextArea detalle = new TextArea();
    private final Label lblRegistros = new Label("0 registros");
    private final Label lblEstado = new Label();

    public ContactosView() {
        lblRegistros.getStyleClass().add("lbl-registros");
        lblEstado.getStyleClass().add("lbl-registros");

        txtBuscar.setPromptText("Buscar por nombre, correo, celular o asunto");
        txtBuscar.textProperty().addListener((o, a, nuevo) -> {
            String q = nuevo == null ? "" : nuevo.trim().toLowerCase(Locale.ROOT);
            filtrados.setPredicate(m -> q.isEmpty()
                    || m.getNombre().toLowerCase(Locale.ROOT).contains(q)
                    || m.getCorreo().toLowerCase(Locale.ROOT).contains(q)
                    || m.getCelular().toLowerCase(Locale.ROOT).contains(q)
                    || m.getAsunto().toLowerCase(Locale.ROOT).contains(q));
            actualizarContador();
        });

        FlowPane barra = Ui.barra(txtBuscar,
                Ui.boton("Nuevo", "btn-actualizar", () -> abrirFormulario(null)),
                Ui.boton("Editar", "btn-limpiar", this::editar),
                Ui.boton("Eliminar", "btn-eliminar", this::eliminar),
                Ui.boton("Limpiar", "btn-limpiar", () -> { txtBuscar.clear(); tabla.getSelectionModel().clearSelection(); }));

        tabla.setPlaceholder(new Label("Aún no hay mensajes de contacto"));
        tabla.getColumns().addAll(
                columna("Fecha", "fecha", 130), columna("Nombre", "nombre", 170),
                columna("Celular", "celular", 120), columna("Correo", "correo", 200),
                columna("Asunto", "asunto", 200));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setRowFactory(tv -> {
            TableRow<Mensaje> fila = new TableRow<>();
            fila.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !fila.isEmpty()) abrirFormulario(fila.getItem()); });
            return fila;
        });
        tabla.setItems(filtrados);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        detalle.setEditable(false);
        detalle.setWrapText(true);
        detalle.setPrefRowCount(4);
        detalle.setPromptText("Selecciona un mensaje para leerlo completo");
        detalle.getStyleClass().add("detalle-mensaje");
        tabla.getSelectionModel().selectedItemProperty().addListener((o, a, m) ->
                detalle.setText(m == null ? "" : m.getMensaje()));

        VBox card = Ui.tarjeta();
        card.getChildren().addAll(
                Ui.encabezado("Contactos", "Mensajes del formulario de la web. Doble clic en una fila para editarla.",
                        lblRegistros, Ui.boton("Actualizar", "btn-actualizar", this::cargar)),
                barra, tabla, detalle, lblEstado);
        raiz = Ui.pagina(card);
    }

    private static TableColumn<Mensaje, String> columna(String titulo, String propiedad, double ancho) {
        TableColumn<Mensaje, String> c = new TableColumn<>(titulo);
        c.setCellValueFactory(new PropertyValueFactory<>(propiedad));
        c.setPrefWidth(ancho);
        return c;
    }

    private void actualizarContador() {
        int n = filtrados.size();
        lblRegistros.setText(n + (n == 1 ? " registro" : " registros"));
    }

    private void editar() {
        Mensaje m = tabla.getSelectionModel().getSelectedItem();
        if (m == null) { Ui.aviso("Editar mensaje", "Selecciona un mensaje de la tabla primero."); return; }
        abrirFormulario(m);
    }

    /** m == null → crear; m != null → editar. */
    private void abrirFormulario(Mensaje m) {
        TextField nombre = Ui.campo(m == null ? "" : m.getNombre());
        TextField celular = Ui.campo(m == null ? "" : m.getCelular());
        TextField correo = Ui.campo(m == null ? "" : m.getCorreo());
        TextField asunto = Ui.campo(m == null ? "" : m.getAsunto());
        nombre.setPromptText("Nombre de quien escribe");
        celular.setPromptText("Ej. 987654321");
        correo.setPromptText("Ej. nombre@gmail.com (opcional)");
        asunto.setPromptText("Tema del mensaje (opcional)");
        TextArea mensaje = new TextArea(m == null ? "" : m.getMensaje());
        mensaje.setPromptText("Escribe aquí el mensaje");
        mensaje.setPrefRowCount(5);
        mensaje.setWrapText(true);

        GridPane g = Ui.grid();
        Ui.fila(g, 0, "Nombre *", nombre);
        Ui.fila(g, 1, "Celular *", celular);
        Ui.fila(g, 2, "Correo (opcional)", correo);
        Ui.fila(g, 3, "Asunto (opcional)", asunto);
        Ui.fila(g, 4, "Mensaje *", mensaje);

        int id = m == null ? 0 : m.getId();
        Ui.formulario(m == null ? "Nuevo mensaje" : "Editar mensaje", g,
                () -> { controller.guardar(id, nombre.getText(), celular.getText(), correo.getText(),
                        asunto.getText(), mensaje.getText());
                    return true; },
                () -> { Ui.ok(lblEstado, id == 0 ? "Mensaje registrado." : "Mensaje actualizado."); cargar(); });
    }

    private void eliminar() {
        Mensaje m = tabla.getSelectionModel().getSelectedItem();
        if (m == null) { Ui.aviso("Eliminar mensaje", "Selecciona un mensaje de la tabla primero."); return; }
        if (!Ui.confirmar("Eliminar mensaje", "¿Eliminar el mensaje de " + m.getNombre() + "?")) return;
        Async.run(() -> { controller.eliminar(m.getId()); return true; },
                ok -> { cargar(); Ui.ok(lblEstado, "Mensaje eliminado."); },
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
