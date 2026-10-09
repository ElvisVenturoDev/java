package vallegrande.edu.pe.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class PerfilView {

    public VBox crear() {
        Label titulo = new Label("Mi Perfil");
        titulo.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre completo");

        TextField txtCorreo = new TextField();
        txtCorreo.setPromptText("Correo electrónico");

        TextField txtTelefono = new TextField();
        txtTelefono.setPromptText("Teléfono");

        Button btnGuardar = new Button("Guardar cambios");
        btnGuardar.setOnAction(e ->
                System.out.println("Perfil guardado: " + txtNombre.getText())
        );

        VBox root = new VBox(12, titulo, txtNombre, txtCorreo, txtTelefono, btnGuardar);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_LEFT);
        root.setMaxWidth(400);

        return root;
    }
}