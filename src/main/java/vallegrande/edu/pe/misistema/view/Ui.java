package vallegrande.edu.pe.misistema.view;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import vallegrande.edu.pe.misistema.controller.Async;

import java.util.Optional;
import java.util.concurrent.Callable;

/** Piezas de interfaz compartidas por todos los apartados. */
final class Ui {
    private static final String CSS = Ui.class.getResource("/vallegrande/edu/pe/misistema/estilos.css").toExternalForm();

    private Ui() {}

    /** Logo de Miskylac: sello verde (círculo con esquina inferior derecha casi recta) + estrella blanca. */
    static HBox logo() {
        Polygon estrella = new Polygon(0, -10, 3, -3, 10, 0, 3, 3, 0, 10, -3, 3, -10, 0, -3, -3);
        estrella.setFill(Color.WHITE);

        StackPane sello = new StackPane(new Group(estrella));
        sello.getStyleClass().add("logo-mark");
        sello.setMinSize(42, 42);
        sello.setPrefSize(42, 42);
        sello.setMaxSize(42, 42);

        Label nombre = new Label("Miskylac");
        nombre.getStyleClass().add("logo-text");
        Label lema = new Label("PURO ORIGEN");
        lema.getStyleClass().add("sublogo-text");
        VBox textos = new VBox(2, nombre, lema);
        textos.setAlignment(Pos.CENTER_LEFT);

        HBox caja = new HBox(12, sello, textos);
        caja.setAlignment(Pos.CENTER_LEFT);
        return caja;
    }

    /** Fila de encabezado: título + subtítulo a la izquierda y controles a la derecha. */
    static HBox encabezado(String titulo, String subtitulo, Node... derecha) {
        Label t = new Label(titulo);
        t.getStyleClass().add("page-title");
        Label s = new Label(subtitulo);
        s.getStyleClass().add("page-subtitle");
        VBox titulos = new VBox(5, t, s);

        Region relleno = new Region();
        HBox.setHgrow(relleno, Priority.ALWAYS);

        HBox fila = new HBox(10, titulos, relleno);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getChildren().addAll(derecha);
        return fila;
    }

    static VBox tarjeta() {
        VBox card = new VBox(20);
        card.getStyleClass().add("content-card");
        card.setPadding(new Insets(30));
        return card;
    }

    static VBox pagina(Node contenido) {
        VBox caja = new VBox(contenido);
        caja.getStyleClass().add("main-content");
        caja.setPadding(new Insets(30));
        VBox.setVgrow(contenido, Priority.ALWAYS);
        return caja;
    }

    static Button boton(String texto, String estilo, Runnable accion) {
        Button b = new Button(texto);
        b.getStyleClass().add(estilo);
        b.setOnAction(e -> accion.run());
        return b;
    }

    /** Barra con buscador y botones de acción; se envuelve si falta ancho. */
    static FlowPane barra(TextField buscar, Node... botones) {
        buscar.getStyleClass().add("search-field");
        buscar.setPrefWidth(280);
        buscar.setMinWidth(180);
        buscar.setMaxWidth(320);
        FlowPane fila = new FlowPane(12, 12, buscar);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setPrefWrapLength(900);
        fila.getChildren().addAll(botones);
        return fila;
    }

    // ---------- formularios (crear / editar) ----------
    static GridPane grid() {
        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(10);
        g.setPrefWidth(520);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(165);
        c1.setPrefWidth(165);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        g.getColumnConstraints().addAll(c1, c2);
        return g;
    }

    static void fila(GridPane g, int fila, String etiqueta, Node campo) {
        Label l = new Label(etiqueta);
        l.getStyleClass().add("form-label");
        l.setMinWidth(Region.USE_PREF_SIZE);
        l.setWrapText(false);
        GridPane.setValignment(l, javafx.geometry.VPos.TOP);
        g.add(l, 0, fila);
        g.add(campo, 1, fila);
        if (campo instanceof Region r) r.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(campo, Priority.ALWAYS);
    }

    static TextField campo(String valor) {
        TextField t = new TextField(valor == null ? "" : valor);
        t.getStyleClass().add("search-field");
        return t;
    }

    /**
     * Muestra un diálogo con Guardar / Cancelar. Si "guardar" falla (validación o base de datos),
     * el mensaje aparece dentro del diálogo y este se mantiene abierto.
     */
    static void formulario(String titulo, Node contenido, Callable<Object> guardar, Runnable despues) {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle(titulo);
        d.setHeaderText(null);
        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);
        d.getDialogPane().getStylesheets().add(CSS);

        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("page-title");
        Label error = new Label();
        error.getStyleClass().add("lbl-error");
        error.setWrapText(true);
        error.setMaxWidth(520);
        error.setMinHeight(Region.USE_PREF_SIZE);
        Label nota = new Label("Los campos con * son obligatorios.");
        nota.getStyleClass().add("lbl-registros");
        VBox cuerpo = new VBox(16, lblTitulo, nota, contenido, error);
        cuerpo.setPadding(new Insets(10));
        d.getDialogPane().setContent(cuerpo);

        Button ok = (Button) d.getDialogPane().lookupButton(btnGuardar);
        ok.getStyleClass().add("btn-actualizar");
        d.getDialogPane().lookupButton(ButtonType.CANCEL).getStyleClass().add("btn-limpiar");
        ok.addEventFilter(ActionEvent.ACTION, ev -> {
            ev.consume();
            ok.setDisable(true);
            error.setText("");
            Async.run(guardar, r -> { d.close(); despues.run(); },
                    e -> { ok.setDisable(false); error.setText(mensaje(e)); });
        });
        d.showAndWait();
    }

    // ---------- mensajes ----------
    static String mensaje(Throwable e) {
        String m = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) return m;
        return "Error de base de datos: " + m;
    }

    static void error(Label estado, Throwable e) {
        if (!estado.getStyleClass().contains("lbl-error")) estado.getStyleClass().add("lbl-error");
        estado.setText(mensaje(e));
    }

    static void ok(Label estado, String texto) {
        estado.getStyleClass().remove("lbl-error");
        estado.setText(texto);
    }

    static boolean confirmar(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.OK, ButtonType.CANCEL);
        a.setTitle(titulo);
        a.setHeaderText(null);
        Optional<ButtonType> r = a.showAndWait();
        return r.isPresent() && r.get() == ButtonType.OK;
    }

    static void aviso(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
