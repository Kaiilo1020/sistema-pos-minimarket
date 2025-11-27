-- Script de Actualización Personalizado para el Sistema POS del Minimarket
-- Adaptado para la estructura de BD existente del usuario

-- ===========================================================
-- VERIFICAR Y ADAPTAR ESTRUCTURA EXISTENTE
-- ===========================================================

-- 1. Verificar si existe la tabla auditoria_log, si no, usar auditoria_precios
DO $$
BEGIN
    -- Si no existe auditoria_log, crearla basada en la estructura existente
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name='auditoria_log') THEN
        CREATE TABLE auditoria_log (
            id SERIAL PRIMARY KEY,
            usuario VARCHAR(50) NOT NULL,
            tipo_evento VARCHAR(50) NOT NULL,
            detalles TEXT,
            fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            ip_origen VARCHAR(45) DEFAULT '127.0.0.1'
        );
        
        -- Insertar algunos datos de ejemplo
        INSERT INTO auditoria_log (usuario, tipo_evento, detalles) VALUES
        ('admin', 'SISTEMA_INICIADO', 'Sistema POS iniciado correctamente'),
        ('admin', 'CONFIGURACION_ACTUALIZADA', 'Configuración de base de datos actualizada');
        
        RAISE NOTICE 'Tabla auditoria_log creada exitosamente';
    ELSE
        RAISE NOTICE 'Tabla auditoria_log ya existe';
    END IF;
END
$$;

-- 2. Verificar y crear tabla lotes_producto si no existe
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name='lotes_producto') THEN
        CREATE TABLE lotes_producto (
            id SERIAL PRIMARY KEY,
            producto_id BIGINT NOT NULL,
            codigo_lote VARCHAR(50) NOT NULL,
            fecha_entrada DATE DEFAULT CURRENT_DATE,
            fecha_vencimiento DATE,
            cantidad INT NOT NULL CHECK (cantidad >= 0),
            CONSTRAINT fk_producto_lote FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE
        );
        
        -- Índice para optimizar búsquedas FIFO
        CREATE INDEX idx_lotes_producto_vencimiento ON lotes_producto (producto_id, fecha_vencimiento, fecha_entrada);
        
        -- Insertar lotes de ejemplo para productos existentes
        INSERT INTO lotes_producto (producto_id, codigo_lote, fecha_entrada, fecha_vencimiento, cantidad) 
        SELECT 
            p.id,
            'LOTE-' || p.id || '-001',
            CURRENT_DATE - INTERVAL '10 days',
            CURRENT_DATE + INTERVAL '30 days',
            LEAST(p.stock, 10)
        FROM productos p 
        WHERE p.stock > 0 
        LIMIT 5;
        
        RAISE NOTICE 'Tabla lotes_producto creada exitosamente';
    ELSE
        RAISE NOTICE 'Tabla lotes_producto ya existe';
    END IF;
END
$$;

-- 3. Verificar y crear tabla metodos_pago si no existe
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name='metodos_pago') THEN
        CREATE TABLE metodos_pago (
            id SERIAL PRIMARY KEY,
            descripcion VARCHAR(50) NOT NULL UNIQUE
        );
        
        -- Insertar métodos de pago
        INSERT INTO metodos_pago (descripcion) VALUES
        ('Efectivo'),
        ('Tarjeta de Crédito'),
        ('Tarjeta de Débito'),
        ('Yape'),
        ('Plin'),
        ('Transferencia Bancaria'),
        ('Otro');
        
        RAISE NOTICE 'Tabla metodos_pago creada exitosamente';
    ELSE
        RAISE NOTICE 'Tabla metodos_pago ya existe';
    END IF;
END
$$;

-- 4. Actualizar tabla ventas con nuevas columnas si no existen
DO $$
BEGIN
    -- Agregar fecha_hora_exacta si no existe
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='ventas' AND column_name='fecha_hora_exacta') THEN
        ALTER TABLE ventas ADD COLUMN fecha_hora_exacta TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
        
        -- Actualizar registros existentes con fecha_hora_exacta basada en fecha existente
        UPDATE ventas SET fecha_hora_exacta = COALESCE(fecha, CURRENT_DATE) + INTERVAL '12 hours' WHERE fecha_hora_exacta IS NULL;
        
        -- Hacer la columna NOT NULL después de actualizar
        ALTER TABLE ventas ALTER COLUMN fecha_hora_exacta SET NOT NULL;
        
        RAISE NOTICE 'Columna fecha_hora_exacta agregada a tabla ventas';
    END IF;
    
    -- Agregar id_metodo_pago si no existe
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='ventas' AND column_name='id_metodo_pago') THEN
        ALTER TABLE ventas ADD COLUMN id_metodo_pago INT;
        
        -- Actualizar registros existentes con método de pago por defecto (Efectivo = 1)
        UPDATE ventas SET id_metodo_pago = 1 WHERE id_metodo_pago IS NULL;
        
        -- Agregar FK constraint si existe la tabla metodos_pago
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name='metodos_pago') THEN
            ALTER TABLE ventas ADD CONSTRAINT fk_metodo_pago FOREIGN KEY (id_metodo_pago) REFERENCES metodos_pago(id);
        END IF;
        
        RAISE NOTICE 'Columna id_metodo_pago agregada a tabla ventas';
    END IF;
    
    -- Agregar referencia_pago si no existe
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='ventas' AND column_name='referencia_pago') THEN
        ALTER TABLE ventas ADD COLUMN referencia_pago VARCHAR(100);
        RAISE NOTICE 'Columna referencia_pago agregada a tabla ventas';
    END IF;
END
$$;

-- 5. Actualizar tabla productos para soporte de lotes si no existe la columna
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='productos' AND column_name='requiere_lote') THEN
        ALTER TABLE productos ADD COLUMN requiere_lote BOOLEAN NOT NULL DEFAULT FALSE;
        
        -- Marcar algunos productos como que requieren lote (ejemplo)
        UPDATE productos SET requiere_lote = TRUE 
        WHERE LOWER(nombre) LIKE '%leche%' 
           OR LOWER(nombre) LIKE '%yogurt%' 
           OR LOWER(nombre) LIKE '%queso%'
           OR LOWER(nombre) LIKE '%carne%';
        
        RAISE NOTICE 'Columna requiere_lote agregada a tabla productos';
    END IF;
END
$$;

-- 6. Actualizar tabla usuarios para incluir roles si no existe
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='usuarios' AND column_name='rol') THEN
        ALTER TABLE usuarios ADD COLUMN rol VARCHAR(20) NOT NULL DEFAULT 'CAJERO';
        
        -- Actualizar roles existentes basado en nombres de usuario
        UPDATE usuarios SET rol = 'ADMINISTRADOR' WHERE LOWER(username) LIKE '%admin%';
        UPDATE usuarios SET rol = 'SUPERVISOR' WHERE LOWER(username) LIKE '%supervisor%' OR LOWER(username) LIKE '%super%';
        UPDATE usuarios SET rol = 'CAJERO' WHERE rol = 'CAJERO'; -- Mantener cajeros como están
        
        RAISE NOTICE 'Columna rol agregada a tabla usuarios';
    END IF;
END
$$;

-- ===========================================================
-- VERIFICACIONES FINALES
-- ===========================================================

-- Mostrar resumen de tablas
SELECT 
    'RESUMEN DE TABLAS ACTUALIZADAS' as mensaje,
    COUNT(*) as total_tablas
FROM information_schema.tables 
WHERE table_schema = 'public';

-- Verificar tablas críticas para POS
SELECT 
    table_name,
    CASE 
        WHEN table_name IN ('auditoria_log', 'lotes_producto', 'metodos_pago') THEN '✅ NUEVA'
        WHEN table_name IN ('productos', 'ventas', 'usuarios') THEN '🔄 ACTUALIZADA'
        ELSE '📋 EXISTENTE'
    END as estado
FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;

-- Verificar columnas críticas en ventas
SELECT 
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns 
WHERE table_name = 'ventas' 
  AND column_name IN ('fecha_hora_exacta', 'id_metodo_pago', 'referencia_pago')
ORDER BY column_name;

-- Mostrar métodos de pago disponibles
SELECT * FROM metodos_pago ORDER BY id;

-- Mostrar algunos lotes de ejemplo
SELECT 
    lp.codigo_lote,
    p.nombre as producto,
    lp.fecha_vencimiento,
    lp.cantidad
FROM lotes_producto lp
JOIN productos p ON lp.producto_id = p.id
LIMIT 5;

-- Mensaje final de confirmación
DO $$
BEGIN
    RAISE NOTICE '🎉 Actualización completada exitosamente. El sistema POS está listo para usar.';
END
$$;
