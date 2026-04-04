-- =============================================
-- Schema de seguridad para ms-security
-- =============================================
CREATE SCHEMA IF NOT EXISTS sec;
SET search_path TO sec;

-- =============================================
-- TABLAS
-- =============================================

-- Usuarios
CREATE TABLE usuarios (
    id                UUID PRIMARY KEY,
    username          VARCHAR(100) NOT NULL UNIQUE,
    password_hash     VARCHAR(255) NOT NULL,
    email             VARCHAR(255),
    celular           VARCHAR(20),
    usr_creacion      VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    fec_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usr_modificacion  VARCHAR(100),
    fec_modificacion  TIMESTAMP,
    estado            VARCHAR(1)   NOT NULL DEFAULT 'A'
);

CREATE INDEX idx_usuarios_estado ON usuarios(estado);

-- Roles del sistema
CREATE TABLE roles (
    id                UUID PRIMARY KEY,
    codigo            VARCHAR(30) NOT NULL UNIQUE,
    nombre            VARCHAR(100) NOT NULL,
    descripcion       VARCHAR(255),
    usr_creacion      VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    fec_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usr_modificacion  VARCHAR(100),
    fec_modificacion  TIMESTAMP,
    estado            VARCHAR(1)   NOT NULL DEFAULT 'A'
);

CREATE INDEX idx_roles_estado ON roles(estado);

-- Permisos granulares
CREATE TABLE permisos (
    id                UUID PRIMARY KEY,
    codigo            VARCHAR(50) NOT NULL UNIQUE,
    nombre            VARCHAR(100) NOT NULL,
    modulo            VARCHAR(30) NOT NULL,
    descripcion       VARCHAR(255),
    usr_creacion      VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    fec_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usr_modificacion  VARCHAR(100),
    fec_modificacion  TIMESTAMP,
    estado            VARCHAR(1)   NOT NULL DEFAULT 'A'
);

CREATE INDEX idx_permisos_modulo ON permisos(modulo);
CREATE INDEX idx_permisos_estado ON permisos(estado);

-- Relacion M:N Usuario <-> Rol
CREATE TABLE usuario_roles (
    usuario_id      UUID NOT NULL REFERENCES usuarios(id),
    rol_id          UUID NOT NULL REFERENCES roles(id),
    asignado_en     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    asignado_por    UUID,
    PRIMARY KEY (usuario_id, rol_id)
);

CREATE INDEX idx_usuario_roles_usuario ON usuario_roles(usuario_id);
CREATE INDEX idx_usuario_roles_rol ON usuario_roles(rol_id);

-- Relacion M:N Rol <-> Permiso
CREATE TABLE rol_permisos (
    rol_id          UUID NOT NULL REFERENCES roles(id),
    permiso_id      UUID NOT NULL REFERENCES permisos(id),
    PRIMARY KEY (rol_id, permiso_id)
);

CREATE INDEX idx_rol_permisos_rol ON rol_permisos(rol_id);

-- Tokens de sesion (access + refresh)
CREATE TABLE tokens_sesion (
    id              UUID PRIMARY KEY,
    jti             VARCHAR(255) NOT NULL UNIQUE,
    user_id         UUID NOT NULL REFERENCES usuarios(id),
    tipo            VARCHAR(20) NOT NULL,
    estado          VARCHAR(30) NOT NULL,
    branch_id       VARCHAR(50),
    emitido_en      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expira_en       TIMESTAMP NOT NULL,
    ultimo_uso      TIMESTAMP,
    revocado_en     TIMESTAMP
);

CREATE INDEX idx_tokens_sesion_user_estado ON tokens_sesion(user_id, estado);
CREATE INDEX idx_tokens_sesion_jti_estado ON tokens_sesion(jti, estado);

-- Intentos de login
CREATE TABLE login_intentos (
    id              UUID PRIMARY KEY,
    username        VARCHAR(100) NOT NULL,
    ip              VARCHAR(45),
    exitoso         BOOLEAN NOT NULL,
    creado_en       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_login_intentos_username ON login_intentos(username, exitoso);

-- Audit log
CREATE TABLE audit_log (
    id              UUID PRIMARY KEY,
    evento          VARCHAR(50) NOT NULL,
    user_id         UUID,
    ip              VARCHAR(45),
    user_agent      VARCHAR(500),
    detalle         TEXT,
    creado_en       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_log_evento ON audit_log(evento, creado_en);

-- Tokens de reset de password
CREATE TABLE password_reset_tokens (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES usuarios(id),
    token           VARCHAR(10) NOT NULL,
    canal           VARCHAR(20) NOT NULL,
    expira_en       TIMESTAMP NOT NULL,
    usado           BOOLEAN NOT NULL DEFAULT false,
    usado_en        TIMESTAMP
);

-- Historial de passwords
CREATE TABLE historial_passwords (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES usuarios(id),
    password_hash   VARCHAR(255) NOT NULL,
    cambiado_en     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_historial_passwords_user ON historial_passwords(user_id, cambiado_en DESC);

-- Cache key-value (reemplaza Redis mientras app.cache.provider=postgres)
CREATE TABLE cache_entries (
    cache_key       VARCHAR(255) PRIMARY KEY,
    cache_value     VARCHAR(500) NOT NULL,
    expira_en       TIMESTAMP NOT NULL
);

CREATE INDEX idx_cache_entries_expira ON cache_entries(expira_en);

-- =============================================
-- DATA INICIAL
-- =============================================

-- Roles base
INSERT INTO roles (id, codigo, nombre, descripcion) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'ADMIN',         'Administrador',        'Acceso total al sistema'),
    ('a0000000-0000-0000-0000-000000000002', 'SUPERVISOR',    'Supervisor',            'Supervision de sucursal'),
    ('a0000000-0000-0000-0000-000000000003', 'CAJERO',        'Cajero',                'Operaciones de caja y ventas'),
    ('a0000000-0000-0000-0000-000000000004', 'FARMACEUTICO',  'Farmaceutico',          'Despacho de medicamentos controlados');

-- Permisos por modulo
INSERT INTO permisos (id, codigo, nombre, modulo) VALUES
    ('b0000000-0000-0000-0000-000000000001', 'AUTH_CAMBIAR_PASSWORD',     'Cambiar password propio',      'AUTH'),
    ('b0000000-0000-0000-0000-000000000002', 'AUTH_RESET_PASSWORD',       'Resetear password de otros',   'AUTH'),
    ('b0000000-0000-0000-0000-000000000010', 'USUARIOS_LISTAR',          'Listar usuarios',              'ADMIN'),
    ('b0000000-0000-0000-0000-000000000011', 'USUARIOS_CREAR',           'Crear usuarios',               'ADMIN'),
    ('b0000000-0000-0000-0000-000000000012', 'USUARIOS_EDITAR',          'Editar usuarios',              'ADMIN'),
    ('b0000000-0000-0000-0000-000000000013', 'USUARIOS_DESACTIVAR',      'Desactivar usuarios',          'ADMIN'),
    ('b0000000-0000-0000-0000-000000000014', 'SESIONES_REVOCAR',         'Revocar sesiones de otros',    'ADMIN'),
    ('b0000000-0000-0000-0000-000000000015', 'ROLES_GESTIONAR',          'Asignar/quitar roles',         'ADMIN'),
    ('b0000000-0000-0000-0000-000000000016', 'AUDIT_CONSULTAR',          'Consultar audit log',          'ADMIN'),
    ('b0000000-0000-0000-0000-000000000020', 'VENTAS_CREAR',             'Crear ventas',                 'VENTAS'),
    ('b0000000-0000-0000-0000-000000000021', 'VENTAS_ANULAR',            'Anular ventas',                'VENTAS'),
    ('b0000000-0000-0000-0000-000000000022', 'VENTAS_CONSULTAR',         'Consultar ventas',             'VENTAS'),
    ('b0000000-0000-0000-0000-000000000030', 'INVENTARIO_CONSULTAR',     'Consultar inventario',         'INVENTARIO'),
    ('b0000000-0000-0000-0000-000000000031', 'INVENTARIO_AJUSTAR',       'Ajustar stock',                'INVENTARIO');

-- Asignar permisos a roles: ADMIN (todos)
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT 'a0000000-0000-0000-0000-000000000001', id FROM permisos;

-- SUPERVISOR
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT 'a0000000-0000-0000-0000-000000000002', id FROM permisos
WHERE codigo IN (
    'AUTH_CAMBIAR_PASSWORD',
    'USUARIOS_LISTAR', 'USUARIOS_CREAR', 'USUARIOS_EDITAR',
    'SESIONES_REVOCAR',
    'VENTAS_CREAR', 'VENTAS_ANULAR', 'VENTAS_CONSULTAR',
    'INVENTARIO_CONSULTAR', 'INVENTARIO_AJUSTAR'
);

-- CAJERO
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT 'a0000000-0000-0000-0000-000000000003', id FROM permisos
WHERE codigo IN (
    'AUTH_CAMBIAR_PASSWORD',
    'VENTAS_CREAR', 'VENTAS_CONSULTAR',
    'INVENTARIO_CONSULTAR'
);

-- FARMACEUTICO
INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT 'a0000000-0000-0000-0000-000000000004', id FROM permisos
WHERE codigo IN (
    'AUTH_CAMBIAR_PASSWORD',
    'VENTAS_CONSULTAR',
    'INVENTARIO_CONSULTAR', 'INVENTARIO_AJUSTAR'
);

-- =============================================
-- Usuario administrador inicial
-- Password: Admin123! (BCrypt hash)
-- =============================================
INSERT INTO usuarios (id, username, password_hash, email) VALUES
    ('c0000000-0000-0000-0000-000000000001', 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@mcco.pe');

-- Asignar rol ADMIN al usuario admin
INSERT INTO usuario_roles (usuario_id, rol_id) VALUES
    ('c0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001');
