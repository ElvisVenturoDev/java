-- =====================================================================
--  Miskylac · Esquema de base de datos (MySQL 8)
--  Base: Miskylac   Sesion: santa_clara
--
--  Estas son las tablas que usa la web (Flask/SQLAlchemy) y que también
--  consume la aplicación de escritorio. Se pueden ejecutar varias veces
--  sin perder datos (CREATE ... IF NOT EXISTS).
-- =====================================================================
CREATE DATABASE IF NOT EXISTS Miskylac
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE Miskylac;

-- ---------------------------------------------------------------------
-- CLIENTES (cuenta web + ficha comercial)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customers (
    id            INT          NOT NULL AUTO_INCREMENT,
    name          VARCHAR(80)  NOT NULL,
    phone         VARCHAR(20)  NULL,
    email         VARCHAR(120) NULL,
    password_hash VARCHAR(255) NULL,
    active        TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS customer_details (
    customer_id   INT          NOT NULL,
    address       VARCHAR(180) NOT NULL,
    district      VARCHAR(60)  NULL,
    customer_type VARCHAR(30)  NOT NULL DEFAULT 'Minorista',
    registered_on DATE         NOT NULL,
    updated_at    DATETIME     NULL,
    PRIMARY KEY (customer_id),
    CONSTRAINT customer_details_ibfk_1 FOREIGN KEY (customer_id)
        REFERENCES customers (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- PRODUCTOS E INVENTARIO
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id          INT           NOT NULL AUTO_INCREMENT,
    name        VARCHAR(80)   NOT NULL,
    category    VARCHAR(50)   NOT NULL DEFAULT 'otros',
    detail      VARCHAR(80)   NULL,
    description TEXT          NULL,
    price       DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    image_url   VARCHAR(255)  NULL,
    active      TINYINT(1)    NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY name (name),
    CONSTRAINT chk_products_price CHECK (price >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS inventory (
    product_id INT NOT NULL,
    stock      INT NOT NULL DEFAULT 0,
    min_stock  INT NOT NULL DEFAULT 5,
    PRIMARY KEY (product_id),
    CONSTRAINT inventory_ibfk_1 FOREIGN KEY (product_id)
        REFERENCES products (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_inventory_stock CHECK (stock >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- VENTAS (pedidos de la web)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sales (
    id               INT           NOT NULL AUTO_INCREMENT,
    date_of_sale     DATE          NOT NULL,
    customer_id      INT           NOT NULL,
    delivery_type    VARCHAR(30)   NOT NULL,
    delivery_address VARCHAR(180)  NOT NULL,
    district         VARCHAR(60)   NULL,
    comments         VARCHAR(300)  NULL,
    status           VARCHAR(20)   NOT NULL DEFAULT 'pending',
    channel          VARCHAR(20)   NOT NULL DEFAULT 'web',
    PRIMARY KEY (id),
    KEY customer_id (customer_id),
    CONSTRAINT sales_ibfk_1 FOREIGN KEY (customer_id)
        REFERENCES customers (id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sale_details (
    id         INT           NOT NULL AUTO_INCREMENT,
    sale_id    INT           NOT NULL,
    product_id INT           NOT NULL,
    quantity   INT           NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id),
    KEY sale_id (sale_id),
    KEY product_id (product_id),
    CONSTRAINT sale_details_ibfk_1 FOREIGN KEY (sale_id)
        REFERENCES sales (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT sale_details_ibfk_2 FOREIGN KEY (product_id)
        REFERENCES products (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_sale_details_quantity CHECK (quantity > 0),
    CONSTRAINT chk_sale_details_price CHECK (unit_price >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- MENSAJES DE CONTACTO DE LA WEB
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS contact_messages (
    id         INT          NOT NULL AUTO_INCREMENT,
    name       VARCHAR(80)  NOT NULL,
    mobile     VARCHAR(20)  NOT NULL,
    email      VARCHAR(120) NULL,
    subject    VARCHAR(80)  NULL,
    message    TEXT         NOT NULL,
    ip         VARCHAR(45)  NULL,
    user_agent VARCHAR(255) NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_contact_messages_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------------------
-- USUARIOS DEL SISTEMA (login de escritorio)
-- Contraseña guardada como SHA-256.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
    id       INT UNSIGNED NOT NULL AUTO_INCREMENT,
    username VARCHAR(60)  NOT NULL,
    password CHAR(64)     NOT NULL,
    nombre   VARCHAR(120) NOT NULL,
    rol      VARCHAR(30)  NOT NULL DEFAULT 'admin',
    activo   TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuario_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sesion inicial: admin / admin123 (cámbialo en producción)
INSERT INTO usuario (username, password, nombre, rol)
SELECT 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
       'Administrador', 'admin'
WHERE NOT EXISTS (SELECT 1 FROM usuario WHERE username = 'admin');
