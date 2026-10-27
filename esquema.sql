-- =====================================================================
--  Miskylac · Esquema de base de datos (MySQL 8)
--  Base: Miskylac   Usuario: santa_clara
--  Se puede ejecutar varias veces sin perder datos (CREATE ... IF NOT EXISTS).
-- =====================================================================
CREATE DATABASE IF NOT EXISTS Miskylac
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE Miskylac;

-- ---------------------------------------------------------------------
-- PRODUCTOS E INVENTARIO
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS producto (
    identificador  INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(120)  NOT NULL,
    categoria      VARCHAR(50)   NOT NULL,
    detalle        VARCHAR(80)   NULL,
    descripcion    TEXT          NULL,
    imagen_url     VARCHAR(500)  NULL,
    precio         DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    activo         TINYINT(1)    NOT NULL DEFAULT 1,
    creado_en      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (identificador),
    KEY idx_producto_categoria (categoria),
    CONSTRAINT chk_producto_precio CHECK (precio >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS inventario (
    identificador_producto INT UNSIGNED NOT NULL,
    stock                  INT          NOT NULL DEFAULT 0,
    stock_minimo           INT          NOT NULL DEFAULT 5,
    actualizado_en         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (identificador_producto),
    CONSTRAINT fk_inventario_producto FOREIGN KEY (identificador_producto)
        REFERENCES producto (identificador) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_inventario CHECK (stock >= 0 AND stock_minimo >= 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- CLIENTES (cuenta web + ficha comercial)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cliente (
    identificador  INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(120) NOT NULL,
    telefono       VARCHAR(20)  NULL,
    correo         VARCHAR(150) NOT NULL,
    password_hash  VARCHAR(255) NOT NULL DEFAULT '',   -- vacío hasta que el cliente se registre en la web
    activo         TINYINT(1)   NOT NULL DEFAULT 1,
    creado_en      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (identificador),
    UNIQUE KEY uq_cliente_correo (correo)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS detalle_cliente (
    identificador_cliente INT UNSIGNED NOT NULL,
    direccion             VARCHAR(255) NOT NULL DEFAULT 'Sin dirección',
    tipo_cliente          VARCHAR(30)  NOT NULL DEFAULT 'Minorista',   -- Minorista / Mayorista / Distribuidor
    fecha_registro        DATE         NOT NULL,
    actualizado_en        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (identificador_cliente),
    CONSTRAINT fk_detalle_cliente FOREIGN KEY (identificador_cliente)
        REFERENCES cliente (identificador) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- VENTAS (pedidos)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS venta (
    identificador         INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    identificador_cliente INT UNSIGNED  NOT NULL,
    fecha                 DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total                 DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    estado                VARCHAR(20)   NOT NULL DEFAULT 'Pendiente',  -- Pendiente / Pagado / Entregado / Anulado
    PRIMARY KEY (identificador),
    KEY idx_venta_cliente (identificador_cliente),
    KEY idx_venta_fecha (fecha),
    CONSTRAINT fk_venta_cliente FOREIGN KEY (identificador_cliente)
        REFERENCES cliente (identificador) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS detalle_venta (
    identificador          INT UNSIGNED  NOT NULL AUTO_INCREMENT,
    identificador_venta    INT UNSIGNED  NOT NULL,
    identificador_producto INT UNSIGNED  NOT NULL,
    cantidad               INT           NOT NULL,
    precio_unitario        DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (identificador),
    KEY idx_detalle_venta_producto (identificador_producto),
    CONSTRAINT fk_detalle_venta_venta FOREIGN KEY (identificador_venta)
        REFERENCES venta (identificador) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_detalle_venta_producto FOREIGN KEY (identificador_producto)
        REFERENCES producto (identificador) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_detalle_venta CHECK (cantidad > 0 AND precio_unitario >= 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- MENSAJES DE CONTACTO
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS mensaje_contacto (
    identificador INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre        VARCHAR(120) NOT NULL,
    celular       VARCHAR(20)  NOT NULL,
    correo        VARCHAR(150) NULL,
    asunto        VARCHAR(150) NULL,
    mensaje       TEXT         NOT NULL,
    creado_en     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (identificador)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- DATOS DE EJEMPLO (solo si las tablas están vacías)
-- ---------------------------------------------------------------------
INSERT INTO producto (nombre, categoria, detalle, descripcion, precio)
SELECT * FROM (
    SELECT 'Leche Entera Fresca' AS nombre, 'leche' AS categoria, '1 litro' AS detalle,
           'Leche fresca de origen, sin conservantes.' AS descripcion, 4.50 AS precio
    UNION ALL SELECT 'Yogurt Natural', 'yogurt', '1 litro', 'Yogurt natural sin azúcar añadida.', 8.00
    UNION ALL SELECT 'Queso Fresco', 'queso', '500 gramos', 'Queso fresco artesanal.', 14.00
    UNION ALL SELECT 'Mantequilla', 'mantequilla', '200 gramos', 'Mantequilla cremosa de leche fresca.', 9.50
) AS ejemplo
WHERE NOT EXISTS (SELECT 1 FROM producto);

INSERT INTO inventario (identificador_producto, stock, stock_minimo)
SELECT p.identificador, 20, 5 FROM producto p
WHERE NOT EXISTS (SELECT 1 FROM inventario i WHERE i.identificador_producto = p.identificador);

INSERT INTO cliente (nombre, telefono, correo)
SELECT 'Cliente de ejemplo', '987654321', 'cliente@ejemplo.com' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM cliente);

INSERT INTO detalle_cliente (identificador_cliente, direccion, tipo_cliente, fecha_registro)
SELECT c.identificador, 'Av. Principal 123, Lima', 'Minorista', CURRENT_DATE FROM cliente c
WHERE NOT EXISTS (SELECT 1 FROM detalle_cliente d WHERE d.identificador_cliente = c.identificador);
