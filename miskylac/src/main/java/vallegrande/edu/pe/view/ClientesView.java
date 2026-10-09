package vallegrande.edu.pe.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import vallegrande.edu.pe.controller.DashboardController;
import vallegrande.edu.pe.model.Cliente;

import java.time.format.DateTimeFormatter;

public class ClientesView {

    private final DashboardController dashboard;
    private final VBox lista = new VBox(10);
    private final TextField buscar = new TextField();
    private final Label contador = new Label();
    private String tipoFiltro = "";

    public ClientesView(DashboardController dashboard) {
        this.dashboard = dashboard;
        buscar.setPromptText("🔍  Buscar por nombre o teléfono...");
        buscar.getStyleClass().add("search");
        buscar.setPrefWidth(300);
        buscar.textProperty().addListener((o, a, b) -> refrescar());
        contador.getStyleClass().add("muted");
    }

    public VBox crear() {
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox header = new HBox(Componentes.encabezado("Clientes 👥", "Quién compra tus lácteos"), espacio, buscar);
        header.setAlignment(Pos.CENTER_LEFT);

        HBox chips = new HBox(10);
        for (String c : new String[]{"Todos", "Minorista", "Mayorista"}) {
            Button chip = new Button(c);
            chip.getStyleClass().add("chip");
            chip.setOnAction(e -> {
                tipoFiltro = c.equals("Todos") ? "" : c;
                refrescar();
            });
            chips.getChildren().add(chip);
        }
        Region e2 = new Region();
        HBox.setHgrow(e2, Priority.ALWAYS);
        chips.getChildren().addAll(e2, contador);
        chips.setAlignment(Pos.CENTER_LEFT);

        VBox izquierda = new VBox(12, chips, lista);
        HBox.setHgrow(izquierda, Priority.ALWAYS);

        HBox cuerpo = new HBox(20, izquierda, formulario());
        refrescar();

        VBox pagina = new VBox(18, header, cuerpo);
        return pagina;
    }

    private VBox formulario() {
        Label t = new Label("➕ Nuevo cliente");
        t.getStyleClass().add("panel-title");

        TextField nombre = new TextField();
        nombre.setPromptText("Nombre completo");
        nombre.getStyleClass().add("field");
        TextField telefono = new TextField();
        telefono.setPromptText("Teléfono");
        telefono.getStyleClass().add("field");
        TextField direccion = new TextField();
        direccion.setPromptText("Dirección");
        direccion.getStyleClass().add("field");

        ComboBox<String> tipo = new ComboBox<>();
        tipo.getItems().addAll("Minorista", "Mayorista");
        tipo.setValue("Minorista");
        tipo.setMaxWidth(Double.MAX_VALUE);

        Label aviso = new Label();
        aviso.setWrapText(true);

        Button agregar = new Button("Agregar cliente");
        agregar.getStyleClass().add("primary-btn");
        Animaciones.hover(agregar, 1.05);
        agregar.setOnAction(e -> {
            if (nombre.getText().isBlank() || telefono.getText().isBlank() || direccion.getText().isBlank()) {
                aviso.setText("⚠ Completa todos los campos");
                aviso.setStyle("-fx-text-fill: #D9534F;");
                return;
            }
            dashboard.agregarCliente(nombre.getText().trim(), telefono.getText().trim(),
                    direccion.getText().trim(), tipo.getValue());
            aviso.setText("✅ Cliente agregado");
            aviso.setStyle("-fx-text-fill: #3E9B5F;");
            nombre.clear();
            telefono.clear();
            direccion.clear();
            refrescar();
        });

        VBox form = new VBox(10, t, nombre, telefono, direccion, tipo, agregar, aviso);
        form.getStyleClass().add("card");
        form.setPrefWidth(280);
        form.setMinWidth(280);
        form.setMaxHeight(Region.USE_PREF_SIZE);
        return form;
    }

    private void refrescar() {
        lista.getChildren().clear();
        String q = buscar.getText() == null ? "" : buscar.getText().toLowerCase();
        int i = 0;
        for (Cliente c : dashboard.clientes()) {
            boolean okTipo = tipoFiltro.isEmpty() || c.tipoCliente().equals(tipoFiltro);
            boolean okTexto = c.nombre().toLowerCase().contains(q) || c.telefono().contains(q);
            if (okTipo && okTexto) {
                lista.getChildren().add(fila(c, Math.min(i * 30, 300)));
                i++;
            }
        }
        if (i == 0) {
            Label vacio = new Label("No encontramos clientes 😕");
            vacio.getStyleClass().add("muted");
            lista.getChildren().add(vacio);
        }
        contador.setText(i + " clientes");
    }

    private HBox fila(Cliente c, int retraso) {
        Label avatar = new Label(c.nombre().substring(0, 1).toUpperCase());
        avatar.getStyleClass().add("avatar");

        Label nombre = new Label(c.nombre());
        nombre.getStyleClass().add("product-name");
        Label datos = new Label("📞 " + c.telefono() + "     📍 " + c.direccion());
        datos.getStyleClass().add("muted");
        Label desde = new Label("Cliente desde " + c.fechaRegistro().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        desde.getStyleClass().add("tiny");

        Label badge = new Label(c.tipoCliente());
        badge.getStyleClass().add(c.tipoCliente().equals("Mayorista") ? "badge-may" : "badge-min");

        Label total = new Label(String.format("S/ %,.2f", dashboard.totalDeCliente(c.id())));
        total.getStyleClass().add("product-price");
        Label pedidos = new Label(dashboard.pedidosDeCliente(c.id()) + " pedidos");
        pedidos.getStyleClass().add("tiny");
        VBox montos = new VBox(2, total, pedidos);
        montos.setAlignment(Pos.CENTER_RIGHT);
        montos.setMinWidth(90);

        Region esp = new Region();
        HBox.setHgrow(esp, Priority.ALWAYS);
        HBox fila = new HBox(14, avatar, new VBox(2, nombre, datos, desde), esp, badge, montos);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("card-small");
        Animaciones.elevar(fila);
        Animaciones.aparecer(fila, retraso);
        return fila;
    }
}