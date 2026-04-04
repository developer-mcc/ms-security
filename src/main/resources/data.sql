-- =============================================
-- Data inicial para ms-security
-- Ejecutado por Spring Boot (spring.sql.init.mode=always)
-- despues de que Hibernate crea las tablas (ddl-auto=create-drop)
-- =============================================
SET search_path TO sec;

-- Roles base
INSERT INTO roles (id, codigo, nombre, descripcion, usr_creacion, fec_creacion, estado) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'ADMIN',         'Administrador',        'Acceso total al sistema',                  'SYSTEM', CURRENT_TIMESTAMP, 'A'),
    ('a0000000-0000-0000-0000-000000000002', 'SUPERVISOR',    'Supervisor',            'Supervision de sucursal',                  'SYSTEM', CURRENT_TIMESTAMP, 'A'),
    ('a0000000-0000-0000-0000-000000000003', 'CAJERO',        'Cajero',                'Operaciones de caja y ventas',             'SYSTEM', CURRENT_TIMESTAMP, 'A'),
    ('a0000000-0000-0000-0000-000000000004', 'FARMACEUTICO',  'Farmaceutico',          'Despacho de medicamentos controlados',     'SYSTEM', CURRENT_TIMESTAMP, 'A');

-- Permisos por modulo
INSERT INTO permisos (id, codigo, nombre, modulo, usr_creacion, fec_creacion, estado) VALUES
    ('b0000000-0000-0000-0000-000000000001', 'AUTH_CAMBIAR_PASSWORD',     'Cambiar password propio',      'AUTH',         'SYSTEM', CURRENT_TIMESTAMP, 'A'),
    ('b0000000-0000-0000-0000-000000000002', 'AUTH_RESET_PASSWORD',       'Resetear password de otros',   'AUTH',         'SYSTEM', CURRENT_TIMESTAMP, 'A'),
    ('b0000000-0000-0000-0000-000000000010', 'USUARIOS_LISTAR',          'Listar usuarios',              'ADMIN',        'SYSTEM', CURRENT_TIMESTAMP, 'A'),
    ('b0000000-0000-0000-0000-000000000011', 'USUARIOS_CREAR',           'Crear usuarios',               'ADMIN',        'SYSTEM', CURRENT_TIMESTAMP, 'A'),
    ('b0000000-0000-0000-0000-000000000012', 'USUARIOS_EDITAR',          'Editar usuarios',              'ADMIN',        'SYSTEM', CURRENT_TIMESTAMP, 'A'),
    ('b0000000-0000-0000-0000-000000000013', 'USUARIOS_DESACTIVAR',      'Desactivar usuarios',          'ADMIN',        'SYSTEM', CURRENT_TIMESTAMP, 'A'),
    ('b0000000-0000-0000-0000-000000000014', 'SESIONES_REVOCAR',         'Revocar sesiones de otros',    'ADMIN',        'SYSTEM', CURRENT_TIMESTAMP, 'A'),
    ('b0000000-0000-0000-0000-000000000015', 'ROLES_GESTIONAR',          'Asignar/quitar roles',         'ADMIN',        'SYSTEM', CURRENT_TIMESTAMP, 'A'),
    ('b0000000-0000-0000-0000-000000000016', 'AUDIT_CONSULTAR',          'Consultar audit log',          'ADMIN',        'SYSTEM', CURRENT_TIMESTAMP, 'A'),
    ('b0000000-0000-0000-0000-000000000020', 'VENTAS_CREAR',             'Crear ventas',                 'VENTAS',       'SYSTEM', CURRENT_TIMESTAMP, 'A'),
    ('b0000000-0000-0000-0000-000000000021', 'VENTAS_ANULAR',            'Anular ventas',                'VENTAS',       'SYSTEM', CURRENT_TIMESTAMP, 'A'),
    ('b0000000-0000-0000-0000-000000000022', 'VENTAS_CONSULTAR',         'Consultar ventas',             'VENTAS',       'SYSTEM', CURRENT_TIMESTAMP, 'A'),
    ('b0000000-0000-0000-0000-000000000030', 'INVENTARIO_CONSULTAR',     'Consultar inventario',         'INVENTARIO',   'SYSTEM', CURRENT_TIMESTAMP, 'A'),
    ('b0000000-0000-0000-0000-000000000031', 'INVENTARIO_AJUSTAR',       'Ajustar stock',                'INVENTARIO',   'SYSTEM', CURRENT_TIMESTAMP, 'A');

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
INSERT INTO usuarios (id, username, password_hash, email, usr_creacion, fec_creacion, estado) VALUES
    ('c0000000-0000-0000-0000-000000000001', 'admin', '$2a$10$.6cqSF865yhD77Y4hFYTMe2bFeCZ9CwpEwSAZCVrIRIpdIPKM2zkq', 'admin@mcco.pe', 'SYSTEM', CURRENT_TIMESTAMP, 'A');

-- Asignar rol ADMIN al usuario admin
INSERT INTO usuario_roles (usuario_id, rol_id) VALUES
    ('c0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001');
