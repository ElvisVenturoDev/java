package vallegrande.edu.pe.misistema.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import vallegrande.edu.pe.misistema.controller.Asincrono;
import vallegrande.edu.pe.misistema.controller.UsuarioController;
import vallegrande.edu.pe.misistema.model.Sesion;
import vallegrande.edu.pe.misistema.model.Usuario;

/**
 * Inicio de sesión de los usuarios del sistema.
 * Incluye el alta de cuentas nuevas y el cambio de contraseña del usuario activo.
 */
public class LoginView implements Page {
    private final UsuarioController controller = new UsuarioController();
    private final StackPane contenedor = new StackPane();
    private final VBox raiz;

    public LoginView() {
        contenedor.setAlignment(Pos.CENTER);
        raiz = Ui.pagina(contenedor);
    }

    // ---------------------------------------------------------------- login

    private void mostrarLogin(String usuarioPrefill, String aviso) {
        TextField usuario = Ui.campo(usuarioPrefill);
        usuario.setPromptText("Tu nombre de usuario");

        PasswordField clave = new PasswordField();
        clave.getStyleClass().add("search-field");
        clave.setPromptText("Tu contraseña");

        Label mensaje = new Label();
        mensaje.setWrapText(true);
        mensaje.setMinHeight(Region.USE_PREF_SIZE);
        boolean hayAviso = aviso != null && !aviso.isEmpty();
        puesto(mensaje, aviso, hayAviso);

        Button entrar = Ui.boton("Iniciar sesión", "btn-actualizar", () -> {});
        entrar.setOnAction(e -> ingresar(usuario, clave, mensaje, entrar));
        Button crear = Ui.boton("Crear cuenta", "btn-limpiar", this::mostrarRegistro);
        entrar.setMaxWidth(Double.MAX_VALUE);
        crear.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(entrar, Priority.ALWAYS);
        HBox.setHgrow(crear, Priority.ALWAYS);
        HBox acciones = new HBox(10, entrar, crear);

        usuario.setOnAction(e -> clave.requestFocus());
        clave.setOnAction(e -> entrar.fire());

        VBox card = tarjeta();
        card.getChildren().addAll(
                titulo("Iniciar sesión"),
                subtitulo("Accede con tu cuenta para gestionar Miskylac."),
                grupo("Usuario *", usuario),
                grupo("Contraseña *", clave),
                mensaje,
                acciones,
                subtitulo("¿Aún no tienes cuenta? Usa el botón «Crear cuenta»."));
        contenedor.getChildren().setAll(card);
    }

    private void ingresar(TextField usuario, PasswordField clave, Label mensaje, Button entrar) {
        puesto(mensaje, "", false);
        if (entrar != null) entrar.setDisable(true);

        Asincrono.ejecutar(() -> controller.iniciarSesion(usuario.getText(), clave.getText()),
                user -> {
                    if (entrar != null) entrar.setDisable(false);
                    if (user == null) {
                        puesto(mensaje, "Usuario o contraseña incorrectos.", false);
                        return;
                    }
                    Sesion.iniciar(user);
                    clave.clear();
                    mostrarSesion();
                },
                e -> {
                    if (entrar != null) entrar.setDisable(false);
                    puesto(mensaje, Ui.mensaje(e), false);
                });
    }

    // ------------------------------------------------------------- registro

    private void mostrarRegistro() {
        TextField nombre = Ui.campo("");
        nombre.setPromptText("Nombre y apellidos");

        TextField usuario = Ui.campo("");
        usuario.setPromptText("Entre 3 y 30 caracteres");

        PasswordField clave = new PasswordField();
        clave.getStyleClass().add("search-field");
        clave.setPromptText("Mínimo 6 caracteres");

        PasswordField confirmar = new PasswordField();
        confirmar.getStyleClass().add("search-field");
        confirmar.setPromptText("Repite la contraseña");

        Label mensaje = new Label();
        mensaje.setWrapText(true);
        mensaje.setMinHeight(Region.USE_PREF_SIZE);

        Button registrar = Ui.boton("Registrar cuenta", "btn-actualizar", () -> {});
        registrar.setOnAction(e -> registrar(nombre, usuario, clave, confirmar, mensaje, registrar));
        Button volver = Ui.boton("Volver al inicio de sesión", "btn-limpiar",
                () -> mostrarLogin("", ""));
        registrar.setMaxWidth(Double.MAX_VALUE);
        volver.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(registrar, Priority.ALWAYS);
        HBox.setHgrow(volver, Priority.ALWAYS);
        HBox acciones = new HBox(10, registrar, volver);

        clave.setOnAction(e -> confirmar.requestFocus());
        confirmar.setOnAction(e -> registrar.fire());

        VBox card = tarjeta();
        card.getChildren().addAll(
                titulo("Crear cuenta"),
                subtitulo("Completa los datos para crear tu cuenta de acceso."),
                grupo("Nombre *", nombre),
                grupo("Usuario *", usuario),
                grupo("Contraseña *", clave),
                grupo("Confirmar contraseña *", confirmar),
                mensaje,
                acciones);
        contenedor.getChildren().setAll(card);
    }

    private void registrar(TextField nombre, TextField usuario, PasswordField clave,
                           PasswordField confirmar, Label mensaje, Button registrar) {
        puesto(mensaje, "", false);
        if (registrar != null) registrar.setDisable(true);

        Asincrono.ejecutar(() -> {
                    controller.registrar(nombre.getText(), usuario.getText(),
                            clave.getText(), confirmar.getText());
                    return true;
                },
                ok -> {
                    if (registrar != null) registrar.setDisable(false);
                    mostrarLogin(usuario.getText().strip(),
                            "Cuenta creada correctamente. Ya puedes iniciar sesión.");
                },
                e -> {
                    if (registrar != null) registrar.setDisable(false);
                    puesto(mensaje, Ui.mensaje(e), false);
                });
    }

    // ---------------------------------------------------------------- sesión

    private void mostrarSesion() {
        Usuario u = Sesion.getActual();
        if (u == null) {
            mostrarLogin("", "");
            return;
        }

        Label nombre = new Label(u.getNombre());
        nombre.getStyleClass().add("login-session");

        Label detalle = new Label("Usuario: @" + u.getNombreUsuario() + "   ·   Rol: " + u.getRolVisible());
        detalle.getStyleClass().add("lbl-registros");

        Button cambiar = Ui.boton("Cambiar contraseña", "btn-actualizar", this::abrirCambioPassword);
        Button salir = Ui.boton("Cerrar sesión", "btn-eliminar", () -> {
            Sesion.cerrar();
            mostrarLogin("", "Sesión cerrada correctamente.");
        });
        cambiar.setMaxWidth(Double.MAX_VALUE);
        salir.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cambiar, Priority.ALWAYS);
        HBox.setHgrow(salir, Priority.ALWAYS);
        HBox acciones = new HBox(10, cambiar, salir);

        VBox card = tarjeta();
        card.getChildren().addAll(
                titulo("Sesión iniciada"),
                subtitulo("Bienvenido de nuevo al sistema de gestión Miskylac."),
                nombre,
                detalle,
                acciones);
        contenedor.getChildren().setAll(card);
    }

    /** Funcionalidad nueva: cambio de contraseña del usuario autenticado. */
    private void abrirCambioPassword() {
        Usuario u = Sesion.getActual();
        if (u == null) {
            mostrarLogin("", "");
            return;
        }

        PasswordField actual = new PasswordField();
        actual.getStyleClass().add("search-field");
        actual.setPromptText("Tu contraseña actual");

        PasswordField nueva = new PasswordField();
        nueva.getStyleClass().add("search-field");
        nueva.setPromptText("Mínimo 6 caracteres");

        PasswordField confirmar = new PasswordField();
        confirmar.getStyleClass().add("search-field");
        confirmar.setPromptText("Repite la nueva contraseña");

        GridPane g = Ui.grid();
        Ui.fila(g, 0, "Contraseña actual *", actual);
        Ui.fila(g, 1, "Contraseña nueva *", nueva);
        Ui.fila(g, 2, "Confirmar nueva *", confirmar);

        Ui.formulario("Cambiar contraseña", g,
                () -> {
                    controller.cambiarClave(u.getId(), actual.getText(),
                            nueva.getText(), confirmar.getText());
                    return true;
                },
                () -> Ui.aviso("Cambiar contraseña",
                        "Tu contraseña se actualizó correctamente."));
    }

    // ---------------------------------------------------------------- utilidades

    private static VBox tarjeta() {
        VBox card = new VBox(14);
        card.getStyleClass().addAll("content-card", "login-card");
        card.setPadding(new Insets(35));
        card.setMaxWidth(460);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        return card;
    }

    private static Label titulo(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("page-title");
        return l;
    }

    private static Label subtitulo(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("page-subtitle");
        l.setWrapText(true);
        return l;
    }

    private static VBox grupo(String etiqueta, Node campo) {
        Label l = new Label(etiqueta);
        l.getStyleClass().add("form-label");
        if (campo instanceof Region r) r.setMaxWidth(Double.MAX_VALUE);
        return new VBox(6, l, campo);
    }

    /** Muestra un mensaje en verde (informativo) o en rojo (error). */
    private static void puesto(Label label, String texto, boolean informativo) {
        label.getStyleClass().removeAll("lbl-error", "lbl-ok");
        label.getStyleClass().add(informativo ? "lbl-ok" : "lbl-error");
        label.setText(texto == null ? "" : texto);
    }

    @Override public Node getView() { return raiz; }

    @Override public void cargar() {
        if (Sesion.activa()) mostrarSesion();
        else mostrarLogin("", "");
    }
}
