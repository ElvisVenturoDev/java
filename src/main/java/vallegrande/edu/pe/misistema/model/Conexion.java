package vallegrande.edu.pe.misistema.model;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Conexión a MySQL (RDS privado) mediante túnel SSH a través de la EC2.
 *
 * Los valores se leen de variables de entorno (SSH_HOST, DB_PASSWORD, ...)
 * o de config.properties (ssh.host, db.password, ...); ver config.properties.example.
 * config.properties y la llave .pem están excluidos del control de versiones.
 */
public final class Conexion {
    private static final Map<String, String> CONFIG = new HashMap<>();
    private static final List<String> BUSCADO = new ArrayList<>();
    private static Session sesion;

    static {
        Path dir = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        for (int i = 0; i < 4 && dir != null; i++, dir = dir.getParent()) {
            BUSCADO.add(dir.toString());
            cargarArchivo(dir.resolve("config.properties"));
        }
    }

    private Conexion() {}

    private static void cargarArchivo(Path ruta) {
        if (!Files.isRegularFile(ruta)) return;
        try {
            for (String linea : Files.readAllLines(ruta, StandardCharsets.UTF_8)) {
                String l = linea.strip();
                if (l.isEmpty() || l.startsWith("#") || !l.contains("=")) continue;
                int i = l.indexOf('=');
                CONFIG.putIfAbsent(l.substring(0, i).strip(), l.substring(i + 1).strip());
            }
        } catch (IOException ignored) {
        }
    }

    /** Lee una clave de la variable de entorno (SSH_HOST) o de config.properties (ssh.host). */
    private static String valor(String clave) {
        String env = System.getenv(clave.toUpperCase().replace('.', '_'));
        if (env != null && !env.isBlank()) return env;
        String v = CONFIG.get(clave);
        if (v != null && !v.isBlank()) return v;
        throw new IllegalStateException("Falta la configuración " + clave
                + ". Defínala en config.properties (copie config.properties.example). "
                + "Carpetas revisadas: " + String.join(", ", BUSCADO));
    }

    /** Abre el túnel SSH una sola vez; si ya está abierto no hace nada. */
    private static synchronized void abrirTunel() throws SQLException {
        if (sesion != null && sesion.isConnected()) return;
        try {
            int puertoLocal = Integer.parseInt(valor("local.port"));
            JSch jsch = new JSch();
            jsch.addIdentity(valor("ssh.key"));
            sesion = jsch.getSession(valor("ssh.user"), valor("ssh.host"),
                    Integer.parseInt(valor("ssh.port")));
            sesion.setConfig("StrictHostKeyChecking", "no");
            sesion.connect(15000);
            sesion.setPortForwardingL(puertoLocal, valor("rds.endpoint"), 3306);
        } catch (JSchException e) {
            cerrar();
            throw new SQLException("No se pudo abrir el túnel SSH: " + e.getMessage(), e);
        }
    }

    public static Connection conectar() throws SQLException {
        abrirTunel();

        String url = "jdbc:mysql://127.0.0.1:" + valor("local.port") + "/" + valor("db.name")
                + "?sslMode=PREFERRED&allowPublicKeyRetrieval=true"
                + "&serverTimezone=UTC&characterEncoding=utf8"
                + "&connectTimeout=10000&socketTimeout=30000";

        return DriverManager.getConnection(url, valor("db.user"), valor("db.password"));
    }

    /** Cierra el túnel SSH. Llamar al salir de la app. */
    public static synchronized void cerrar() {
        if (sesion != null && sesion.isConnected()) sesion.disconnect();
        sesion = null;
    }
}