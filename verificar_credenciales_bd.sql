-- =====================================================
-- VERIFICAR CREDENCIALES EN BASE DE DATOS
-- =====================================================

-- 1. Ver todos los usuarios existentes
SELECT 'USUARIOS EXISTENTES:' as info;
SELECT 
    id,
    username,
    nombre_completo,
    email,
    rol,
    activo,
    password_hash,
    fecha_creacion
FROM usuarios 
ORDER BY id;

-- 2. Verificar específicamente el usuario 'cajero'
SELECT 'DATOS DEL USUARIO CAJERO:' as info;
SELECT 
    id,
    username,
    nombre_completo,
    email,
    rol,
    activo,
    password_hash,
    LENGTH(password_hash) as password_length,
    fecha_creacion
FROM usuarios 
WHERE username = 'cajero';

-- 3. Verificar si el password es texto plano o hash
SELECT 'ANÁLISIS DE PASSWORDS:' as info;
SELECT 
    username,
    password_hash,
    LENGTH(password_hash) as length,
    CASE 
        WHEN password_hash = 'cajero123' THEN 'TEXTO PLANO'
        WHEN LENGTH(password_hash) = 32 THEN 'POSIBLE MD5'
        WHEN password_hash LIKE '$%' THEN 'HASH BCRYPT/SCRYPT'
        ELSE 'FORMATO DESCONOCIDO'
    END as tipo_password
FROM usuarios
WHERE username IN ('admin', 'supervisor', 'cajero');

-- 4. Probar login directo (simulación)
SELECT 'SIMULACIÓN DE LOGIN:' as info;
SELECT 
    username,
    CASE 
        WHEN username = 'cajero' AND password_hash = 'cajero123' THEN 'LOGIN EXITOSO'
        WHEN username = 'cajero' AND password_hash != 'cajero123' THEN 'PASSWORD INCORRECTO'
        ELSE 'USUARIO NO ENCONTRADO'
    END as resultado_login
FROM usuarios 
WHERE username = 'cajero' AND activo = true;
