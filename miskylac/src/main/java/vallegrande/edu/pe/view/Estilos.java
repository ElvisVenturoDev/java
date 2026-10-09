package vallegrande.edu.pe.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class Estilos {

    public static final String TEXTO = "#1b2a41";
    public static final String GRIS = "#6b7280";
    public static final String AZUL = "#1f5a8c";
    public static final String VERDE = "#2e7d32";
    public static final String NARANJA = "#ef6c00";
    public static final String ROJO = "#c62828";
    public static final String MORADO = "#6a1b9a";

    // ===== Imágenes =====
    public static final String URL_VACA = "https://media.lmneuquen.com/p/06fb8cb7cef39959e2576132007f237e/adjuntos/353/imagenes/007/746/0007746894/730x0/smart/b9ae8yhepng.png";
    private static final String URL_LECHE = "https://www.recetasnestle.com.co/sites/default/files/2024-04/jarra-vaso-tipo-leche_0.jpg";
    private static final String URL_YOGURT = "https://t3.ftcdn.net/jpg/00/41/61/32/360_F_41613202_iULNcZRPUBqPKQGxOsK8B5J34AQnGZ3o.jpg";
    private static final String URL_QUESO = "https://www.cocinadelirante.com/sites/default/files/images/2023/09/como-hacer-queso-con-1-litro-de-leche.jpg";

    private static final String[] PALETA = {AZUL, VERDE, NARANJA, MORADO, ROJO, "#00838f"};
    private static final Map<String, Image> CACHE = new HashMap<>();

    private Estilos() {}

    // ===== Textos =====
    public static Label titulo(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size:26px;-fx-font-weight:bold;-fx-text-fill:" + TEXTO + ";");
        return l;
    }

    public static Label subtitulo(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:" + TEXTO + ";");
        return l;
    }

    public static Label negrita(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-weight:bold;-fx-text-fill:" + TEXTO + ";");
        return l;
    }

    public static Label texto(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-text-fill:" + TEXTO + ";");
        return l;
    }

    public static Label gris(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-text-fill:" + GRIS + ";");
        return l;
    }

    // ===== Botones =====
    public static Button botonPrimario(String t) {
        Button b = new Button(t);
        b.setStyle("-fx-background-color:" + AZUL + ";-fx-text-fill:white;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-padding:8 16 8 16;-fx-cursor:hand;");
        return b;
    }

    public static Button botonSecundario(String t) {
        Button b = new Button(t);
        b.setStyle("-fx-background-color:white;-fx-text-fill:" + AZUL + ";-fx-font-weight:bold;"
                + "-fx-border-color:" + AZUL + ";-fx-border-radius:8;-fx-background-radius:8;"
                + "-fx-padding:7 14 7 14;-fx-cursor:hand;");
        return b;
    }

    // ===== Cajas y tarjetas =====
    public static VBox caja(Node contenido) {
        VBox c = new VBox(contenido);
        c.setPadding(new Insets(14));
        c.setStyle("-fx-background-color:white;-fx-background-radius:14;-fx-border-color:#eceff3;-fx-border-radius:14;");
        HBox.setHgrow(c, Priority.ALWAYS);
        return c;
    }

    public static VBox tarjeta(String texto, String valor, String color, String detalle) {
        Label v = new Label(valor);
        v.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:" + color + ";");
        Label t = new Label(texto);
        t.setWrapText(true);
        t.setStyle("-fx-font-weight:bold;-fx-text-fill:" + TEXTO + ";");
        Label d = new Label(detalle);
        d.setWrapText(true);
        d.setStyle("-fx-font-size:11px;-fx-text-fill:" + GRIS + ";");
        VBox c = new VBox(3, v, t, d);
        c.setPadding(new Insets(14, 16, 14, 16));
        c.setMinWidth(120);
        c.setPrefWidth(10);
        c.setMaxWidth(Double.MAX_VALUE);
        c.setStyle("-fx-background-color:white;-fx-background-radius:14;-fx-border-color:#eceff3;-fx-border-radius:14;");
        HBox.setHgrow(c, Priority.ALWAYS);
        return c;
    }

    public static HBox filaTarjetas(Node... tarjetas) {
        return new HBox(14, tarjetas);
    }

    public static Label aviso(String texto, String fondo, String color) {
        Label l = new Label(texto);
        l.setWrapText(true);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setStyle("-fx-background-color:" + fondo + ";-fx-text-fill:" + color + ";-fx-font-weight:bold;"
                + "-fx-padding:10 14 10 14;-fx-background-radius:10;");
        return l;
    }

    // ===== Estado del stock =====
    public static String colorEstado(String estado) {
        return switch (estado) {
            case "Agotado" -> ROJO;
            case "Stock bajo" -> NARANJA;
            default -> VERDE;
        };
    }

    public static String fondoEstado(String estado) {
        return switch (estado) {
            case "Agotado" -> "#fde8e8";
            case "Stock bajo" -> "#fff0dc";
            default -> "#e3f4e6";
        };
    }

    public static Label chip(String estado) {
        Label l = new Label(estado);
        l.setStyle("-fx-background-color:" + fondoEstado(estado) + ";-fx-text-fill:" + colorEstado(estado)
                + ";-fx-font-weight:bold;-fx-font-size:11px;-fx-padding:3 10 3 10;-fx-background-radius:20;");
        return l;
    }

    // ===== Imágenes =====
    private static Image cargar(String url) {
        return CACHE.computeIfAbsent(url, u -> new Image(u, 0, 0, true, true, true));
    }

    public static ImageView imagen(String url, double alto) {
        ImageView iv = new ImageView(cargar(url));
        iv.setFitHeight(alto);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        return iv;
    }

    private static String urlPorCategoria(String categoria) {
        return switch (categoria.toLowerCase()) {
            case "leche" -> URL_LECHE;
            case "yogurt" -> URL_YOGURT;
            case "queso" -> URL_QUESO;
            default -> null; // agrega aquí más URLs (mantequilla, manjar...)
        };
    }

    private static String emojiPorCategoria(String categoria) {
        return switch (categoria.toLowerCase()) {
            case "leche" -> "🥛";
            case "queso" -> "🧀";
            case "yogurt" -> "🍶";
            case "mantequilla" -> "🧈";
            case "manjar" -> "🍯";
            default -> "📦";
        };
    }

    // Foto del producto (si no carga o no hay URL, queda el emoji)
    public static StackPane foto(String categoria, double w, double h) {
        Label emoji = new Label(emojiPorCategoria(categoria));
        emoji.setStyle("-fx-font-size:" + (int) (Math.min(w, h) * 0.55) + "px;");
        StackPane p = new StackPane(emoji);
        p.setMinSize(w, h);
        p.setPrefSize(w, h);
        p.setMaxSize(w, h);
        p.setStyle("-fx-background-color:#eef2f7;-fx-background-radius:10;");

        Rectangle clip = new Rectangle(w, h);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        p.setClip(clip);

        String url = urlPorCategoria(categoria);
        if (url != null) {
            Image img = cargar(url);
            ImageView iv = new ImageView(img);
            iv.setFitWidth(w);
            iv.setFitHeight(h);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            Runnable ajustar = () -> {
                if (img.getProgress() >= 1.0 && !img.isError()) emoji.setVisible(false);
            };
            ajustar.run();
            img.progressProperty().addListener((o, a, b) -> ajustar.run());
            p.getChildren().add(iv);
        }
        return p;
    }

    // ===== Tablas =====
    public static Label encabezadoCol(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-font-weight:bold;-fx-text-fill:" + TEXTO + ";");
        return l;
    }

    public static <T> TableColumn<T, String> col(String titulo, Function<T, String> f) {
        TableColumn<T, String> c = new TableColumn<>();
        c.setGraphic(encabezadoCol(titulo));
        c.setCellValueFactory(x -> new SimpleStringProperty(f.apply(x.getValue())));
        c.setCellFactory(k -> new TableCell<T, String>() {
            @Override
            protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty ? null : s);
                setStyle("-fx-text-fill:" + TEXTO + ";");
            }
        });
        return c;
    }

    // ===== Gráficos =====
    public static BarChart<String, Number> barras(List<String> etiquetas, List<? extends Number> valores,
                                                  List<String> colores) {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        x.setStyle("-fx-tick-label-fill:" + TEXTO + ";");
        y.setStyle("-fx-tick-label-fill:" + TEXTO + ";");
        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setPrefHeight(280);
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        for (int i = 0; i < etiquetas.size(); i++) {
            XYChart.Data<String, Number> d = new XYChart.Data<String, Number>(etiquetas.get(i), valores.get(i));
            String color = colores.get(i);
            d.nodeProperty().addListener((o, a, n) -> {
                if (n != null) n.setStyle("-fx-bar-fill:" + color + ";");
            });
            serie.getData().add(d);
        }
        chart.getData().add(serie);
        return chart;
    }

    // Gráfico circular con leyenda propia (para que siempre se lea)
    public static Node pastel(Map<String, Double> datos) {
        if (datos.isEmpty()) return gris("Sin datos todavía");
        PieChart pie = new PieChart();
        pie.setLabelsVisible(false);
        pie.setLegendVisible(false);
        pie.setAnimated(false);
        pie.setStartAngle(90);
        pie.setMinSize(230, 230);
        pie.setPrefSize(230, 230);
        VBox leyenda = new VBox(8);
        leyenda.setAlignment(Pos.CENTER_LEFT);
        int i = 0;
        for (Map.Entry<String, Double> e : datos.entrySet()) {
            String color = PALETA[i % PALETA.length];
            PieChart.Data d = new PieChart.Data(e.getKey(), e.getValue());
            d.nodeProperty().addListener((o, a, n) -> {
                if (n != null) n.setStyle("-fx-pie-color:" + color + ";");
            });
            pie.getData().add(d);
            HBox fila = new HBox(8, new Circle(6, Color.web(color)), texto(e.getKey()));
            fila.setAlignment(Pos.CENTER_LEFT);
            leyenda.getChildren().add(fila);
            i++;
        }
        HBox r = new HBox(16, pie, leyenda);
        r.setAlignment(Pos.CENTER_LEFT);
        return r;
    }
}