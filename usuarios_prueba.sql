-- =====================================================
-- USUARIOS DE PRUEBA PARA EL SISTEMA DE LOGIN
-- Ejecutar en pgAdmin para crear usuarios con diferentes roles
-- =====================================================

-- Limpiar usuarios existentes (opcional)
-- DELETE FROM usuarios WHERE username IN ('admin', 'supervisor', 'cajero');

-- Insertar usuarios de prueba
INSERT INTO usuarios (username, password_hash, nombre_completo, email, rol, activo, fecha_creacion) VALUES
-- ADMINISTRADOR (puede ver todo)
('admin', 'admin123', 'Juan Pérez', 'admin@minimarket.com', 'ADMINISTRADOR', true, CURRENT_TIMESTAMP),

-- SUPERVISOR (puede ver inventario, historial, reportes, alertas - NO usuarios)
('supervisor', 'super123', 'María García', 'supervisor@minimarket.com', 'SUPERVISOR', true, CURRENT_TIMESTAMP),

-- TRABAJADOR/CAJERO (solo puede usar POS - dashboard simplificado)
('cajero', 'cajero123', 'Carlos López', 'cajero@minimarket.com', 'CAJERO', true, CURRENT_TIMESTAMP)

ON CONFLICT (username) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    nombre_completo = EXCLUDED.nombre_completo,
    email = EXCLUDED.email,
    rol = EXCLUDED.rol,
    activo = EXCLUDED.activo;

-- Verificar que se insertaron correctamente
SELECT username, nombre_completo, rol, activo FROM usuarios WHERE username IN ('admin', 'supervisor', 'cajero');

-- =====================================================
-- CREDENCIALES PARA PROBAR:
-- =====================================================
-- Usuario: admin     | Contraseña: admin123     | Rol: ADMINISTRADOR
-- Usuario: supervisor | Contraseña: super123     | Rol: SUPERVISOR  
-- Usuario: cajero     | Contraseña: cajero123    | Rol: CAJERO
-- =====================================================
