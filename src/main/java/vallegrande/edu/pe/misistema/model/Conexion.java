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
 * Conexión a MySQL/MariaDB.
 *
 * No hay credenciales ni hosts escritos en el código: todos los valores deben
 * definirse con variables de entorno o en un archivo db.properties local
 * (DB_HOST, DB_PORT, DB_USER, DB_PASSWORD, DB_NAME); ver db.properties.example.
 * El archivo db.properties está excluido del control de versiones.
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
            // Sin archivo se usan únicamente las variables de entorno.
        }
    }

    /** Lee una clave obligatoria de las variables de entorno o de db.properties. */
    private static String valor(String clave) {
        String env = System.getenv(clave);
        if (env != null && !env.isBlank()) return env;
        String v = CONFIG.get(clave);
        if (v != null && !v.isBlank()) return v;
        throw new IllegalStateException("Falta la configuración " + clave
                + ". Defínala como variable de entorno o en db.properties "
                + "(copie db.properties.example). Carpetas revisadas: "
                + String.join(", ", BUSCADO));
    }

    public static Connection conectar() throws SQLException {
        String host = valor("DB_HOST");
        String puerto = valor("DB_PORT");
        String base = valor("DB_NAME");

        String url = "jdbc:mysql://" + host + ":" + puerto + "/" + base
                + "?sslMode=PREFERRED&allowPublicKeyRetrieval=true"
                + "&serverTimezone=UTC&characterEncoding=utf8"
                + "&connectTimeout=10000&socketTimeout=30000";

        return DriverManager.getConnection(url, valor("DB_USER"), valor("DB_PASSWORD"));
    }

}
