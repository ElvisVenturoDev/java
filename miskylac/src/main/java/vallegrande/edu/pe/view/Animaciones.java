package vallegrande.edu.pe.view;

import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import java.util.function.DoubleFunction;

public class Animaciones {

    // Aparece subiendo y con fade
    public static void aparecer(Node n, int retrasoMs) {
        n.setOpacity(0);
        n.setTranslateY(20);
        FadeTransition f = new FadeTransition(Duration.millis(450), n);
        f.setToValue(1);
        TranslateTransition t = new TranslateTransition(Duration.millis(450), n);
        t.setToY(0);
        t.setInterpolator(Interpolator.EASE_OUT);
        ParallelTransition p = new ParallelTransition(f, t);
        p.setDelay(Duration.millis(retrasoMs));
        p.play();
    }

    public static void fundir(Node n) {
        n.setOpacity(0);
        FadeTransition f = new FadeTransition(Duration.millis(500), n);
        f.setToValue(1);
        f.play();
    }

    // Se agranda un poquito al pasar el mouse (para botones)
    public static void hover(Node n, double escala) {
        ScaleTransition in = new ScaleTransition(Duration.millis(150), n);
        in.setToX(escala);
        in.setToY(escala);
        ScaleTransition out = new ScaleTransition(Duration.millis(150), n);
        out.setToX(1);
        out.setToY(1);
        n.setOnMouseEntered(e -> {
            out.stop();
            in.playFromStart();
        });
        n.setOnMouseExited(e -> {
            in.stop();
            out.playFromStart();
        });
    }

    // Se eleva al pasar el mouse (para tarjetas)
    public static void elevar(Node n) {
        TranslateTransition sube = new TranslateTransition(Duration.millis(180), n);
        sube.setToY(-6);
        TranslateTransition baja = new TranslateTransition(Duration.millis(180), n);
        baja.setToY(0);
        n.setOnMouseEntered(e -> {
            baja.stop();
            sube.playFromStart();
        });
        n.setOnMouseExited(e -> {
            sube.stop();
            baja.playFromStart();
        });
    }

    // Latido al hacer clic
    public static void pulso(Node n) {
        ScaleTransition s = new ScaleTransition(Duration.millis(120), n);
        s.setToX(1.35);
        s.setToY(1.35);
        s.setCycleCount(2);
        s.setAutoReverse(true);
        s.play();
    }

    // Sube y baja suavemente sin parar
    public static void flotar(Node n) {
        TranslateTransition t = new TranslateTransition(Duration.seconds(2), n);
        t.setByY(-8);
        t.setCycleCount(Animation.INDEFINITE);
        t.setAutoReverse(true);
        t.setInterpolator(Interpolator.EASE_BOTH);
        t.play();
    }

    // El número cuenta desde 0 hasta su valor
    public static void contador(Label label, double destino, DoubleFunction<String> formato, int retrasoMs) {
        DoubleProperty valor = new SimpleDoubleProperty(0);
        valor.addListener((o, a, b) -> label.setText(formato.apply(b.doubleValue())));
        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(valor, 0)),
                new KeyFrame(Duration.millis(1100), new KeyValue(valor, destino, Interpolator.EASE_OUT)));
        tl.setDelay(Duration.millis(retrasoMs));
        tl.play();
    }

    // La barra crece desde abajo
    public static void crecer(Region barra, double alto, int retrasoMs) {
        barra.setPrefHeight(0);
        Timeline tl = new Timeline(
                new KeyFrame(Duration.millis(700), new KeyValue(barra.prefHeightProperty(), alto, Interpolator.EASE_OUT)));
        tl.setDelay(Duration.millis(retrasoMs));
        tl.play();
    }
}