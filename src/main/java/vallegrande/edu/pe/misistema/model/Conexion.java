package vallegrande.edu.pe.misistema.model;

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
 * Conexión a MySQL en el contenedor Docker (127.0.0.1:3308, base Miskylac, usuario santa_clara).
 * Funciona sin archivos extra. Opcional: variables de entorno o db.properties
 * (DB_HOST, DB_PORT, DB_USER, DB_PASSWORD, DB_NAME) para cambiar algún valor.
 */
public final class Conexion {
    private static final Map<String, String> CONFIG = new HashMap<>();

    private static final List<String> BUSCADO = new ArrayList<>();

    static {
        // Busca en la carpeta de ejecución y hasta 3 carpetas más arriba (según cómo el IDE lance la app).
        Path dir = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        for (int i = 0; i < 4 && dir != null; i++, dir = dir.getParent()) {
            BUSCADO.add(dir.toString());
            for (String archivo : List.of("db.properties")) cargarArchivo(dir.resolve(archivo));
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
                // Lo que se leyó primero (carpeta más cercana) tiene prioridad.
                CONFIG.putIfAbsent(l.substring(0, i).strip(), l.substring(i + 1).strip());
            }
        } catch (IOException ignored) {
            // Se usan los valores por defecto.
        }
    }

    private static String valor(String clave, String defecto) {
        String env = System.getenv(clave);
        if (env != null && !env.isBlank()) return env;
        String v = CONFIG.get(clave);
        return (v != null && !v.isBlank()) ? v : defecto;
    }

    public static Connection conectar() throws SQLException {
        // Valores por defecto = los del comando docker run entregado. Se pueden cambiar con
        // variables de entorno o con un db.properties opcional.
        String clave = valor("DB_PASSWORD", "SantaClara2026");
        String url = "jdbc:mysql://" + valor("DB_HOST", "127.0.0.1") + ":" + valor("DB_PORT", "3308")
                + "/" + valor("DB_NAME", "Miskylac")
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=utf8";
        return DriverManager.getConnection(url, valor("DB_USER", "santa_clara"), clave);
    }
}
