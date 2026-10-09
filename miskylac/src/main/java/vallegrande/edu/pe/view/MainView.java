package vallegrande.edu.pe.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import vallegrande.edu.pe.controller.DashboardController;
import vallegrande.edu.pe.controller.ProductoController;

import java.util.ArrayList;
import java.util.List;

public class MainView {

    private final BorderPane root = new BorderPane();
    private final ScrollPane scroll = new ScrollPane();
    private final List<Button> navButtons = new ArrayList<>();

    // Controladores compartidos: lo que cambies en una pantalla se ve en las demás
    private final DashboardController dashboard = new DashboardController();
    private final ProductoController productoCtrl = new ProductoController();

    private final InicioView inicioView = new InicioView(dashboard, productoCtrl);
    private final ProductosView productosView = new ProductosView(productoCtrl);
    private final ClientesView clientesView = new ClientesView(dashboard);
    private final PerfilView perfilView = new PerfilView();

    public MainView() {
        root.getStyleClass().add("root-pane");
        root.setLeft(crearSidebar());

        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");
        root.setCenter(scroll);

        mostrar("Panel");
    }

    public BorderPane getRoot() {
        return root;
    }

    private VBox crearSidebar() {
        VBox box = new VBox(4);
        box.getStyleClass().add("sidebar");
        box.setPrefWidth(220);
        box.setMinWidth(220);

        Label logo = new Label("🥛 MISKYLAC");
        logo.getStyleClass().add("logo");
        Label sub = new Label("PURO ORIGEN");
        sub.getStyleClass().add("logo-sub");
        Region espacio = new Region();
        espacio.setPrefHeight(20);
        box.getChildren().addAll(logo, sub, espacio);

        String[][] items = {{"📊", "Panel"}, {"📦", "Inventario"}, {"👥", "Clientes"}};
        for (String[] it : items) {
            box.getChildren().add(crearNav(it[0], it[1]));
        }

        Label cuenta = new Label("CUENTA");
        cuenta.getStyleClass().add("section-label");
        box.getChildren().addAll(cuenta, crearNav("👤", "Perfil"), crearNav("🚪", "Cerrar sesión"));

        Region relleno = new Region();
        VBox.setVgrow(relleno, Priority.ALWAYS);

        Label promoT = new Label("Uso interno");
        promoT.getStyleClass().add("promo-title");
        Label promoD = new Label("Gestión de inventario, ventas y clientes de MiskyLac.");
        promoD.setWrapText(true);
        promoD.getStyleClass().add("promo-text");
        VBox promo = new VBox(6, promoT, promoD);
        promo.getStyleClass().add("promo");
        promo.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(relleno, promo);
        return box;
    }

    private Button crearNav(String icono, String nombre) {
        Button b = new Button(icono + "   " + nombre);
        b.setMaxWidth(Double.MAX_VALUE);
        b.getStyleClass().add("nav-btn");
        b.setUserData(nombre);
        b.setOnAction(e -> mostrar(nombre));
        navButtons.add(b);
        return b;
    }

    private void mostrar(String nombre) {
        for (Button b : navButtons) {
            b.getStyleClass().remove("active");
            if (b.getUserData().equals(nombre)) {
                b.getStyleClass().add("active");
            }
        }

        Node pagina;
        switch (nombre) {
            case "Panel" -> pagina = inicioView.crear(this::mostrar);
            case "Inventario" -> pagina = productosView.crear();
            case "Clientes" -> pagina = clientesView.crear();
            case "Perfil" -> pagina = perfilView.crear();
            case "Cerrar sesión" -> {
                javafx.application.Platform.exit();
                return;
            }
            default -> pagina = new Label(nombre);
        }

        VBox contenido = new VBox(pagina);
        contenido.setPadding(new Insets(26));

        if (!nombre.equals("Panel")) {
            Animaciones.aparecer(contenido, 0);
        }

        scroll.setContent(contenido);
    }
}