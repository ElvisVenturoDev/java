# Miskylac · Sistema de gestión (JavaFX)

Aplicación de escritorio para la gestión de productos, inventario, clientes,
cuentas y mensajes de contacto. Está desarrollada en Java 17 con JavaFX y
Maven, y se conecta a una base de datos MySQL/MariaDB mediante JDBC.

## Requisitos

- **JDK 17** (o superior, compatible con Java 17).
- **Maven 3.8+**.
- **MySQL 8** o **MariaDB** accesible desde el equipo donde se ejecuta la app.

Verifica las versiones instaladas:

```bash
java -version
mvn -version
```

## 1. Preparar la base de datos

El esquema versionado se encuentra en
[`src/main/resources/esquema.sql`](src/main/resources/esquema.sql) (hay una
copia en la raíz como `esquema.sql`). Crea la base `Miskylac`, las tablas y los
datos iniciales ejecutando:

```bash
mysql -u <usuario> -p < src/main/resources/esquema.sql
```

El script usa `CREATE DATABASE IF NOT EXISTS` y `CREATE TABLE IF NOT EXISTS`,
por lo que puede ejecutarse varias veces sin perder los datos existentes.

## 2. Configurar la conexión

La aplicación **no contiene credenciales en el código**. Los datos de conexión
se leen, por orden de prioridad, de:

1. Variables de entorno: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`.
2. Un archivo `db.properties` local (en la raíz del proyecto o junto al ejecutable).

Copia el archivo de ejemplo y completa tus valores:

```bash
copy db.properties.example db.properties   # Windows
cp db.properties.example db.properties     # Linux/macOS
```

Contenido esperado de `db.properties`:

```properties
DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=Miskylac
DB_USER=<tu_usuario>
DB_PASSWORD=<tu_contraseña>
```

`db.properties` y `*.pem` están excluidos en `.gitignore`; **no** los subas al
repositorio. Si falta alguna de las claves obligatorias, la aplicación muestra
un mensaje indicando cuál debe configurarse.

## 3. Ejecutar la aplicación

Desde la raíz del proyecto, con el plugin de JavaFX configurado en el `pom.xml`:

```bash
mvn clean javafx:run
```

La clase principal configurada es `vallegrande.edu.pe.misistema.Main`.

Para compilar y generar el JAR con sus dependencias:

```bash
mvn clean package
```

El resultado queda en `target/` (JAR y carpeta `target/lib` con las
dependencias). Para ejecutarlo:

```bash
java -jar target/vallegrande.edu.pe-1.0-SNAPSHOT.jar
```

## Estructura del proyecto

```
src/main/java/vallegrande/edu/pe/misistema/
├── Main.java              # Punto de entrada JavaFX
├── controller/            # Validaciones y lógica de aplicación
├── model/                 # Entidades, DAO y conexión a la base de datos
└── view/                  # Vistas y componentes JavaFX

src/main/resources/
├── esquema.sql            # Script de creación de la base de datos
└── vallegrande/edu/pe/misistema/estilos.css
```

## Notas

- Las contraseñas de usuario se almacenan con hash SHA-256.
- El archivo `db.properties` es local y no debe versionarse.
- Los directorios de IDE (`.idea/`, `*.iml`) y las salidas de compilación
  (`target/`, `dist/`) están excluidos del control de versiones.
