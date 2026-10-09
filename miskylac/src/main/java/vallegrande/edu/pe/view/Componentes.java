package vallegrande.edu.pe.view;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class Componentes {

    public static VBox encabezado(String titulo, String sub) {
        Label t = new Label(titulo);
        t.getStyleClass().add("page-title");
        Label s = new Label(sub);
        s.getStyleClass().add("muted");
        return new VBox(2, t, s);
    }

    // Tarjeta con área de color arriba, etiqueta opcional, título y texto
    public static VBox tarjeta(String emoji, String estiloTile, String etiqueta,
                               String titulo, String texto, double ancho, int retraso) {
        Label e = new Label(emoji);
        e.getStyleClass().add("emoji");
        e.setStyle("-fx-font-size: 40px;");
        StackPane tile = new StackPane(e);
        tile.getStyleClass().addAll("tile", estiloTile);
        tile.setPrefHeight(90);
        tile.setMinHeight(90);

        return tarjetaFoto(tile, etiqueta, titulo, texto, ancho, retraso);
    }

    // Misma tarjeta, pero con cualquier nodo arriba (por ejemplo una foto)
    public static VBox tarjetaFoto(Node arriba, String etiqueta,
                                   String titulo, String texto, double ancho, int retraso) {
        VBox contenido = new VBox(6);
        if (etiqueta != null) {
            Label tag = new Label(etiqueta);
            tag.getStyleClass().add("badge-min");
            contenido.getChildren().add(tag);
        }
        Label t = new Label(titulo);
        t.getStyleClass().add("product-name");
        t.setWrapText(true);
        Label d = new Label(texto);
        d.getStyleClass().add("muted");
        d.setWrapText(true);
        contenido.getChildren().addAll(t, d);

        VBox card = new VBox(12, arriba, contenido);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(ancho);
        card.setMinWidth(ancho);
        card.setMaxWidth(ancho);
        Animaciones.elevar(card);
        Animaciones.aparecer(card, retraso);
        return card;
    }
}