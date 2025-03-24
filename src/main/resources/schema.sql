-- Crear tabla de usuarios si no existe
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(150) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE
);

-- Crear tabla de estados de tarea si no existe
CREATE TABLE IF NOT EXISTS estados_tarea (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

-- Crear tabla de tareas si no existe
CREATE TABLE IF NOT EXISTS tareas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(100) NOT NULL,
    descripcion VARCHAR(1000),
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_vencimiento DATE,
    estado_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    FOREIGN KEY (estado_id) REFERENCES estados_tarea(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Insertar estados solo si no existen
MERGE INTO estados_tarea (nombre)
KEY(nombre)
VALUES ('PENDIENTE');

MERGE INTO estados_tarea (nombre)
KEY(nombre)
VALUES ('EN_PROGRESO');

MERGE INTO estados_tarea (nombre)
KEY(nombre)
VALUES ('BLOQUEADA');

MERGE INTO estados_tarea (nombre)
KEY(nombre)
VALUES ('COMPLETADA');

MERGE INTO estados_tarea (nombre)
KEY(nombre)
VALUES ('CANCELADA');

-- Insertar usuarios de prueba si no existen
MERGE INTO usuarios (username, password, nombre, email)
KEY(username)
VALUES ('admin', '$2a$10$A/xmsJKpkvzaBkSFdAr/QOfa.OH3SqY/nIehMvoOhGPYBnU3cyRtq', 'Administrador', 'admin@spa.com');

MERGE INTO usuarios (username, password, nombre, email)
KEY(username)
VALUES ('usuario1', '$2a$10$A/xmsJKpkvzaBkSFdAr/QOfa.OH3SqY/nIehMvoOhGPYBnU3cyRtq', 'Juan Pérez', 'juan.perez@spa.com');

MERGE INTO usuarios (username, password, nombre, email)
KEY(username)
VALUES ('usuario2', '$2a$10$A/xmsJKpkvzaBkSFdAr/QOfa.OH3SqY/nIehMvoOhGPYBnU3cyRtq', 'María Rodríguez', 'maria.rodriguez@spa.com');