-- =====================================================
-- VERIFICAR Y CREAR USUARIOS DE PRUEBA
-- =====================================================

-- 1. Verificar si existen usuarios
SELECT 'Usuarios existentes:' as info;
SELECT username, nombre_completo, rol, activo, password_hash FROM usuarios;

-- 2. Si no hay usuarios, crearlos
INSERT INTO usuarios (username, password_hash, nombre_completo, email, rol, activo, fecha_creacion) 
VALUES 
('admin', 'admin123', 'Administrador Sistema', 'admin@minimarket.com', 'ADMINISTRADOR', true, CURRENT_TIMESTAMP),
('supervisor', 'super123', 'Supervisor Tienda', 'supervisor@minimarket.com', 'SUPERVISOR', true, CURRENT_TIMESTAMP),
('cajero', 'cajero123', 'Cajero Principal', 'cajero@minimarket.com', 'CAJERO', true, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    nombre_completo = EXCLUDED.nombre_completo,
    email = EXCLUDED.email,
    rol = EXCLUDED.rol,
    activo = EXCLUDED.activo;

-- 3. Verificar que se crearon correctamente
SELECT 'Usuarios después de inserción:' as info;
SELECT username, nombre_completo, rol, activo, password_hash FROM usuarios WHERE username IN ('admin', 'supervisor', 'cajero');

-- 4. Verificar estructura de la tabla
SELECT 'Estructura de tabla usuarios:' as info;
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'usuarios' 
ORDER BY ordinal_position;
