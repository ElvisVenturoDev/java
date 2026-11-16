package vallegrande.edu.pe.misistema;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import vallegrande.edu.pe.misistema.model.Conexion;
import vallegrande.edu.pe.misistema.view.MainView;

public class Main extends Application {
    @Override
    public void stop() {
        Conexion.cerrar();
    }
    @Override
    public void start(Stage stage) {
        MainView view = new MainView();
        Scene scene = new Scene(view.getView(), 1200, 700);
        scene.getStylesheets().add(getClass()
                .getResource("/vallegrande/edu/pe/misistema/estilos.css").toExternalForm());

        stage.setTitle("Miskylac - Sistema de gestión");
        stage.setMinWidth(640);
        stage.setMinHeight(480);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) { launch(args); }
}
