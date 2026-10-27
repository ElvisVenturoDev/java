package vallegrande.edu.pe.misistema;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import vallegrande.edu.pe.misistema.view.MainView;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        MainView view = new MainView();

        Scene scene = new Scene(view.getView(), 1200, 700);
        scene.getStylesheets().add(getClass().getResource("/vallegrande/edu/pe/misistema/estilos.css").toExternalForm());

        primaryStage.setTitle("Miskylac - Sistema de gestión");
        primaryStage.setMinWidth(980);
        primaryStage.setMinHeight(600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) { launch(args); }
}
