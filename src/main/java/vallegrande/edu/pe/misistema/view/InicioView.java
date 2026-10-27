package vallegrande.edu.pe.misistema.view;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import vallegrande.edu.pe.misistema.controller.Async;
import vallegrande.edu.pe.misistema.controller.InicioController;

import java.util.function.Consumer;

public class InicioView implements Page {
    private final InicioController controller = new InicioController();
    private final VBox raiz;
    private final Label lblEstado = new Label();
    private final Label vProductos = new Label("—");
    private final Label vStockBajo = new Label("—");
    private final Label vClientes = new Label("—");
    private final Label vPedidos = new Label("—");
    private final Label vMensajes = new Label("—");

    public InicioView(Consumer<Section> navegar) {
        FlowPane tarjetas = new FlowPane(20, 20);
        tarjetas.getChildren().addAll(
                stat("Productos activos", vProductos, Section.PRODUCTOS, navegar),
                stat("Con stock bajo", vStockBajo, Section.PRODUCTOS, navegar),
                stat("Clientes", vClientes, Section.CLIENTES, navegar),
                stat("Pedidos", vPedidos, null, navegar),
                stat("Mensajes de contacto", vMensajes, Section.CONTACTOS, navegar));

        lblEstado.getStyleClass().add("lbl-registros");

        VBox card = Ui.tarjeta();
        card.getChildren().addAll(
                Ui.encabezado("Bienvenido a Miskylac",
                        "Resumen general del sistema de gestión. Toca una tarjeta para ir a su apartado."),
                tarjetas, lblEstado);
        raiz = Ui.pagina(card);
    }

    private VBox stat(String titulo, Label valor, Section destino, Consumer<Section> navegar) {
        valor.getStyleClass().add("stat-value");
        Label t = new Label(titulo);
        t.getStyleClass().add("stat-title");
        VBox caja = new VBox(6, valor, t);
        caja.getStyleClass().add("stat-card");
        caja.setPrefWidth(200);
        if (destino != null) {
            caja.getStyleClass().add("stat-card-link");
            caja.setOnMouseClicked(e -> navegar.accept(destino));
        }
        return caja;
    }

    private static String num(int n) { return n < 0 ? "—" : String.valueOf(n); }

    @Override public Node getView() { return raiz; }

    @Override public void cargar() {
        Async.run(controller::resumen, r -> {
            vProductos.setText(num(r.productos()));
            vStockBajo.setText(num(r.stockBajo()));
            vClientes.setText(num(r.clientes()));
            vPedidos.setText(num(r.pedidos()));
            vMensajes.setText(num(r.mensajes()));
            Ui.ok(lblEstado, "");
        }, e -> Ui.error(lblEstado, e));
    }
}
