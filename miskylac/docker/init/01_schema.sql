USE miskylac;

CREATE TABLE cliente (
                         identificador INT AUTO_INCREMENT PRIMARY KEY,
                         nombre        VARCHAR(100) NOT NULL,
                         telefono      VARCHAR(20)  NOT NULL
);

CREATE TABLE detalle_cliente (
                                 identificador         INT AUTO_INCREMENT PRIMARY KEY,
                                 identificador_cliente INT NOT NULL,
                                 direccion             VARCHAR(150) NOT NULL,
                                 tipo_cliente          VARCHAR(20)  NOT NULL,
                                 fecha_registro        DATE NOT NULL,
                                 CONSTRAINT fk_detalle_cliente_cliente
                                     FOREIGN KEY (identificador_cliente) REFERENCES cliente(identificador)
);

CREATE TABLE producto (
                          identificador INT AUTO_INCREMENT PRIMARY KEY,
                          nombre        VARCHAR(100)  NOT NULL,
                          categoria     VARCHAR(50)   NOT NULL,
                          precio        DECIMAL(10,2) NOT NULL
);

CREATE TABLE inventario (
                            identificador          INT AUTO_INCREMENT PRIMARY KEY,
                            identificador_producto INT NOT NULL,
                            stock                  INT NOT NULL,
                            CONSTRAINT fk_inventario_producto
                                FOREIGN KEY (identificador_producto) REFERENCES producto(identificador)
);

CREATE TABLE venta (
                       identificador         INT AUTO_INCREMENT PRIMARY KEY,
                       fecha                 DATE NOT NULL,
                       identificador_cliente INT  NOT NULL,
                       CONSTRAINT fk_venta_cliente
                           FOREIGN KEY (identificador_cliente) REFERENCES cliente(identificador)
);

CREATE TABLE detalle_venta (
                               identificador          INT AUTO_INCREMENT PRIMARY KEY,
                               identificador_venta    INT NOT NULL,
                               identificador_producto INT NOT NULL,
                               cantidad               INT NOT NULL,
                               precio_unitario        DECIMAL(10,2) NOT NULL,
                               CONSTRAINT fk_detalle_venta_venta
                                   FOREIGN KEY (identificador_venta) REFERENCES venta(identificador),
                               CONSTRAINT fk_detalle_venta_producto
                                   FOREIGN KEY (identificador_producto) REFERENCES producto(identificador)
);

CREATE TABLE detalle_pedido (
                                identificador          INT AUTO_INCREMENT PRIMARY KEY,
                                identificador_producto INT NOT NULL,
                                cantidad               INT NOT NULL,
                                precio_unitario        DECIMAL(10,2) NOT NULL,
                                CONSTRAINT fk_detalle_pedido_producto
                                    FOREIGN KEY (identificador_producto) REFERENCES producto(identificador)
);