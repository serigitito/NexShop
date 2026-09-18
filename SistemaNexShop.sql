CREATE DATABASE IF NOT EXISTS SistemaNexShop;
USE SistemaNexShop;

CREATE TABLE IF NOT EXISTS Usuario (
    NoDocumento BIGINT PRIMARY KEY,
    Nombre VARCHAR(150) NOT NULL,
    Usuario VARCHAR(100) NOT NULL,
    Contrasena VARCHAR(255) NOT NULL,
    correo VARCHAR(150) NOT NULL,
    telefono VARCHAR(50),
    direccion VARCHAR(255),
    rol VARCHAR(30) NOT NULL DEFAULT 'usuario',
    estado VARCHAR(30) DEFAULT 'activo'
);

CREATE TABLE IF NOT EXISTS Cliente (
    idCliente BIGINT PRIMARY KEY AUTO_INCREMENT,
    NoDocumento BIGINT NOT NULL,
    FOREIGN KEY (NoDocumento) REFERENCES Usuario(NoDocumento)
);

CREATE TABLE IF NOT EXISTS Vendedor (
    idVendedor BIGINT PRIMARY KEY AUTO_INCREMENT,
    NoDocumento BIGINT NOT NULL,
    FOREIGN KEY (NoDocumento) REFERENCES Usuario(NoDocumento)
);

CREATE TABLE IF NOT EXISTS Producto (
    idProducto BIGINT PRIMARY KEY AUTO_INCREMENT,
    Nombre VARCHAR(150) NOT NULL,
    precio DOUBLE NOT NULL,
    categoria VARCHAR(100) NOT NULL,
    imagen VARCHAR(255),
    idVendedor BIGINT NOT NULL,
    FOREIGN KEY (idVendedor) REFERENCES Vendedor(idVendedor)
);

CREATE TABLE IF NOT EXISTS Venta (
    idVenta BIGINT PRIMARY KEY AUTO_INCREMENT,
    fecha DATE,
    metodoPago VARCHAR(50),
    subtotal DOUBLE DEFAULT 0,
    iva DOUBLE DEFAULT 0,
    costoFinal DOUBLE DEFAULT 0,
    idCliente BIGINT NOT NULL,
    idVendedor BIGINT NOT NULL,
    direccionEnvio VARCHAR(255),
    estado VARCHAR(30),
    estadoEnvio VARCHAR(30) DEFAULT 'Preparando',
    FOREIGN KEY (idCliente) REFERENCES Cliente(idCliente),
    FOREIGN KEY (idVendedor) REFERENCES Vendedor(idVendedor)
);

CREATE TABLE IF NOT EXISTS DetalleVenta (
    idDetalle BIGINT PRIMARY KEY AUTO_INCREMENT,
    idVenta BIGINT NOT NULL,
    idProducto BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precioUnitario DOUBLE NOT NULL,
    subtotal DOUBLE NOT NULL,
    FOREIGN KEY (idVenta) REFERENCES Venta(idVenta),
    FOREIGN KEY (idProducto) REFERENCES Producto(idProducto)
);

CREATE TABLE IF NOT EXISTS Pago (
    idPago BIGINT PRIMARY KEY AUTO_INCREMENT,
    iva DOUBLE,
    estado VARCHAR(30),
    metodoPago VARCHAR(50),
    historial VARCHAR(255),
    idVenta BIGINT NOT NULL,
    FOREIGN KEY (idVenta) REFERENCES Venta(idVenta)
);


-- Si ya tenias usuarios de las versiones anteriores, todos los que no sean admin
-- pasan a ser usuarios normales.
UPDATE Usuario SET rol = 'usuario' WHERE rol IS NULL OR rol NOT IN ('admin');
