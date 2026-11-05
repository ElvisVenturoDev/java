package vallegrande.edu.pe.misistema.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;

public class MainView {
    private final BorderPane view;
    private final StackPane contenido = new StackPane();
    private final Map<Section, Button> botones = new EnumMap<>(Section.class);
    private final Map<Section, Page> paginas = new EnumMap<>(Section.class);

    public MainView() {
        view = new BorderPane();
        view.getStyleClass().add("root-pane");

        paginas.put(Section.INICIO, new InicioView(this::mostrar));
        paginas.put(Section.PRODUCTOS, new ProductosView());
        paginas.put(Section.CLIENTES, new ClientesView());
        paginas.put(Section.CONTACTOS, new ContactosView());
        paginas.put(Section.CUENTA, new CuentaView());
        paginas.put(Section.LOGIN, new LoginView());

        VBox sidebar = new VBox(20);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(250);
        sidebar.setPadding(new Insets(30, 20, 20, 20));

        HBox logoBox = Ui.logo();
        logoBox.setPadding(new Insets(0, 0, 30, 0));

        VBox menuBox = new VBox(10);
        menuBox.getChildren().addAll(
                crearBoton("Inicio", Section.INICIO),
                crearBoton("Productos", Section.PRODUCTOS),
                crearBoton("Clientes", Section.CLIENTES),
                crearBoton("Contactos", Section.CONTACTOS),
                crearBoton("Cuenta", Section.CUENTA),
                crearBoton("Login", Section.LOGIN));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(logoBox, menuBox, spacer, crearReloj());
        view.setLeft(sidebar);
        view.setCenter(contenido);
        mostrar(Section.INICIO);
    }

    /** Cambia de apartado: marca el botón activo, muestra la página y recarga sus datos. */
    private void mostrar(Section seccion) {
        botones.forEach((s, b) -> {
            b.getStyleClass().remove("menu-button-active");
            if (s == seccion) b.getStyleClass().add("menu-button-active");
        });
        Page pagina = paginas.get(seccion);
        contenido.getChildren().setAll(pagina.getView());
        pagina.cargar();
    }

    private Button crearBoton(String texto, Section seccion) {
        Button btn = new Button(texto);
        btn.getStyleClass().add("menu-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(e -> mostrar(seccion));
        botones.put(seccion, btn);
        return btn;
    }

    private VBox crearReloj() {
        VBox clockBox = new VBox(5);
        clockBox.getStyleClass().add("clock-box");
        clockBox.setAlignment(Pos.CENTER);
        Label lblTime = new Label();
        lblTime.getStyleClass().add("clock-time");
        Label lblDate = new Label();
        lblDate.getStyleClass().add("clock-date");
        clockBox.getChildren().addAll(lblTime, lblDate);

        DateTimeFormatter horaFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter fechaFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Runnable actualizar = () -> {
            LocalDateTime ahora = LocalDateTime.now();
            lblTime.setText(ahora.format(horaFmt));
            lblDate.setText(ahora.format(fechaFmt));
        };
        actualizar.run();
        Timeline reloj = new Timeline(new KeyFrame(Duration.seconds(1), e -> actualizar.run()));
        reloj.setCycleCount(Timeline.INDEFINITE);
        reloj.play();
        return clockBox;
    }

    public BorderPane getView() { return view; }
}