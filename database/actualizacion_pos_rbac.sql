-- =====================================================
-- ACTUALIZACIÓN DE BASE DE DATOS PARA SISTEMA POS
-- Soluciona los 4 problemas críticos identificados
-- =====================================================

-- 1. TABLA DE AUDITORÍA (RBAC)
CREATE TABLE IF NOT EXISTS audit_log (
    id SERIAL PRIMARY KEY,
    usuario_id INTEGER NOT NULL,
    accion VARCHAR(50) NOT NULL,
    detalle TEXT,
    ip_address VARCHAR(45),
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Índices para optimizar consultas de auditoría
CREATE INDEX IF NOT EXISTS idx_audit_usuario ON audit_log(usuario_id);
CREATE INDEX IF NOT EXISTS idx_audit_fecha ON audit_log(fecha_hora);
CREATE INDEX IF NOT EXISTS idx_audit_accion ON audit_log(accion);

-- 2. TABLA DE MÉTODOS DE PAGO
CREATE TABLE IF NOT EXISTS metodos_pago (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT,
    requiere_validacion BOOLEAN DEFAULT FALSE,
    es_digital BOOLEAN DEFAULT FALSE,
    activo BOOLEAN DEFAULT TRUE
);

-- Insertar métodos de pago predefinidos
INSERT INTO metodos_pago (id, nombre, descripcion, requiere_validacion, es_digital) VALUES
(1, 'Efectivo', 'Pago en efectivo', FALSE, FALSE),
(2, 'Tarjeta Débito', 'Pago con tarjeta de débito', TRUE, FALSE),
(3, 'Tarjeta Crédito', 'Pago con tarjeta de crédito', TRUE, FALSE),
(4, 'Yape', 'Pago digital Yape', TRUE, TRUE),
(5, 'Plin', 'Pago digital Plin', TRUE, TRUE),
(6, 'Transferencia', 'Transferencia bancaria', TRUE, TRUE),
(7, 'Mixto', 'Combinación de métodos de pago', FALSE, FALSE)
ON CONFLICT (id) DO NOTHING;

-- 3. ACTUALIZAR TABLA DE VENTAS (Integridad de datos)
-- Agregar columnas obligatorias si no existen
DO $$ 
BEGIN
    -- Fecha/hora exacta
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'ventas' AND column_name = 'fecha_hora_exacta') THEN
        ALTER TABLE ventas ADD COLUMN fecha_hora_exacta TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
    -- Método de pago
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'ventas' AND column_name = 'metodo_pago_id') THEN
        ALTER TABLE ventas ADD COLUMN metodo_pago_id INTEGER;
        ALTER TABLE ventas ADD CONSTRAINT fk_ventas_metodo_pago 
            FOREIGN KEY (metodo_pago_id) REFERENCES metodos_pago(id);
    END IF;
    
    -- Referencia de pago
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'ventas' AND column_name = 'referencia_pago') THEN
        ALTER TABLE ventas ADD COLUMN referencia_pago VARCHAR(100);
    END IF;
    
    -- Subtotal e impuestos
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'ventas' AND column_name = 'subtotal') THEN
        ALTER TABLE ventas ADD COLUMN subtotal DECIMAL(10,2);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'ventas' AND column_name = 'impuestos') THEN
        ALTER TABLE ventas ADD COLUMN impuestos DECIMAL(10,2);
    END IF;
    
    -- Estado de la venta
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'ventas' AND column_name = 'estado') THEN
        ALTER TABLE ventas ADD COLUMN estado VARCHAR(20) DEFAULT 'COMPLETADA';
    END IF;
END $$;

-- 4. TABLA DE LOTES DE PRODUCTOS (Gestión de inventario FIFO)
CREATE TABLE IF NOT EXISTS lotes_productos (
    id SERIAL PRIMARY KEY,
    producto_id INTEGER NOT NULL,
    numero_lote VARCHAR(50) NOT NULL,
    fecha_vencimiento DATE,
    fecha_ingreso DATE DEFAULT CURRENT_DATE,
    stock_inicial INTEGER NOT NULL,
    stock_disponible INTEGER NOT NULL,
    proveedor VARCHAR(100),
    costo_unitario DECIMAL(10,2),
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (producto_id) REFERENCES productos(id),
    UNIQUE(producto_id, numero_lote)
);

-- Índices para optimizar FIFO
CREATE INDEX IF NOT EXISTS idx_lotes_producto ON lotes_productos(producto_id);
CREATE INDEX IF NOT EXISTS idx_lotes_fifo ON lotes_productos(fecha_ingreso, fecha_vencimiento);
CREATE INDEX IF NOT EXISTS idx_lotes_stock ON lotes_productos(stock_disponible) WHERE stock_disponible > 0;

-- 5. TABLA DE MOVIMIENTOS DE INVENTARIO (Auditoría de stock)
CREATE TABLE IF NOT EXISTS movimientos_inventario (
    id SERIAL PRIMARY KEY,
    producto_id INTEGER NOT NULL,
    lote_id INTEGER, -- Puede ser NULL para productos sin lote
    cantidad INTEGER NOT NULL,
    tipo_movimiento VARCHAR(20) NOT NULL, -- ENTRADA, SALIDA, VENTA, AJUSTE
    descripcion TEXT,
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    usuario_id INTEGER,
    FOREIGN KEY (producto_id) REFERENCES productos(id),
    FOREIGN KEY (lote_id) REFERENCES lotes_productos(id),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Índices para movimientos
CREATE INDEX IF NOT EXISTS idx_movimientos_producto ON movimientos_inventario(producto_id);
CREATE INDEX IF NOT EXISTS idx_movimientos_fecha ON movimientos_inventario(fecha_hora);
CREATE INDEX IF NOT EXISTS idx_movimientos_tipo ON movimientos_inventario(tipo_movimiento);

-- 6. ACTUALIZAR TABLA DE USUARIOS (RBAC)
-- Asegurar que la columna rol existe y tiene valores correctos
DO $$
BEGIN
    -- Verificar si la columna rol existe
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'usuarios' AND column_name = 'rol') THEN
        
        -- Actualizar roles existentes a formato estándar
        UPDATE usuarios SET rol = 'ADMINISTRADOR' WHERE UPPER(rol) IN ('ADMIN', 'ADMINISTRADOR');
        UPDATE usuarios SET rol = 'SUPERVISOR' WHERE UPPER(rol) = 'SUPERVISOR';
        UPDATE usuarios SET rol = 'CAJERO' WHERE UPPER(rol) IN ('CAJERA', 'CAJERO') OR rol IS NULL;
        
    ELSE
        -- Agregar columna rol si no existe
        ALTER TABLE usuarios ADD COLUMN rol VARCHAR(20) DEFAULT 'CAJERO';
    END IF;
END $$;

-- 7. TRIGGERS PARA ALERTAS AUTOMÁTICAS (Observer Pattern)

-- Trigger para alertas de stock crítico después de ventas
CREATE OR REPLACE FUNCTION trigger_alerta_stock_critico()
RETURNS TRIGGER AS $$
BEGIN
    -- Si el stock resultante es <= 10, registrar alerta
    IF NEW.stock <= 10 AND OLD.stock > 10 THEN
        INSERT INTO alertas_inventario (producto_id, tipo_alerta, mensaje, fecha_hora)
        VALUES (NEW.id, 'STOCK_CRITICO', 
                CONCAT('Stock crítico: ', NEW.nombre, ' - ', NEW.stock, ' unidades restantes'),
                CURRENT_TIMESTAMP);
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Aplicar trigger a tabla productos
DROP TRIGGER IF EXISTS trg_stock_critico ON productos;
CREATE TRIGGER trg_stock_critico
    AFTER UPDATE OF stock ON productos
    FOR EACH ROW
    EXECUTE FUNCTION trigger_alerta_stock_critico();

-- Trigger para auditoría automática de cambios de precios
CREATE OR REPLACE FUNCTION trigger_auditoria_precios()
RETURNS TRIGGER AS $$
BEGIN
    -- Registrar cambio de precio en auditoría
    IF OLD.precio != NEW.precio THEN
        INSERT INTO audit_log (usuario_id, accion, detalle, fecha_hora)
        VALUES (
            COALESCE(current_setting('app.current_user_id', true)::INTEGER, 1),
            'MODIFICAR_PRECIO',
            CONCAT('Producto: ', NEW.nombre, ', Precio anterior: ', OLD.precio, 
                   ', Precio nuevo: ', NEW.precio),
            CURRENT_TIMESTAMP
        );
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Aplicar trigger de auditoría de precios
DROP TRIGGER IF EXISTS trg_auditoria_precios ON productos;
CREATE TRIGGER trg_auditoria_precios
    AFTER UPDATE OF precio ON productos
    FOR EACH ROW
    EXECUTE FUNCTION trigger_auditoria_precios();

-- 8. VISTAS PARA REPORTES Y CONSULTAS OPTIMIZADAS

-- Vista de stock actual con lotes
CREATE OR REPLACE VIEW v_stock_actual AS
SELECT 
    p.id,
    p.nombre,
    p.precio,
    p.stock as stock_general,
    COALESCE(SUM(l.stock_disponible), 0) as stock_lotes,
    GREATEST(p.stock, COALESCE(SUM(l.stock_disponible), 0)) as stock_total,
    COUNT(l.id) as cantidad_lotes,
    MIN(l.fecha_vencimiento) as proximo_vencimiento
FROM productos p
LEFT JOIN lotes_productos l ON p.id = l.producto_id AND l.stock_disponible > 0
GROUP BY p.id, p.nombre, p.precio, p.stock;

-- Vista de productos con stock crítico
CREATE OR REPLACE VIEW v_productos_stock_critico AS
SELECT 
    p.id,
    p.nombre,
    p.precio,
    p.stock,
    p.categoria,
    CASE 
        WHEN p.stock <= 5 THEN 'CRÍTICO'
        WHEN p.stock <= 10 THEN 'BAJO'
        ELSE 'NORMAL'
    END as nivel_stock
FROM productos p
WHERE p.stock <= 10
ORDER BY p.stock ASC;

-- Vista de auditoría de ventas
CREATE OR REPLACE VIEW v_auditoria_ventas AS
SELECT 
    v.id,
    v.numero_venta,
    v.fecha_hora_exacta,
    u.nombre as cajero,
    mp.nombre as metodo_pago,
    v.total,
    v.estado,
    COUNT(dv.id) as cantidad_items
FROM ventas v
LEFT JOIN usuarios u ON v.cajero_id = u.id
LEFT JOIN metodos_pago mp ON v.metodo_pago_id = mp.id
LEFT JOIN detalle_ventas dv ON v.id = dv.venta_id
GROUP BY v.id, v.numero_venta, v.fecha_hora_exacta, u.nombre, mp.nombre, v.total, v.estado
ORDER BY v.fecha_hora_exacta DESC;

-- 9. DATOS DE EJEMPLO PARA TESTING

-- Insertar lotes de ejemplo
INSERT INTO lotes_productos (producto_id, numero_lote, fecha_vencimiento, stock_inicial, stock_disponible, proveedor, costo_unitario)
SELECT 
    p.id,
    'LOTE_' || p.id || '_001',
    CURRENT_DATE + INTERVAL '30 days',
    50,
    45,
    'Proveedor Test',
    p.precio * 0.7
FROM productos p
WHERE p.id <= 5
ON CONFLICT (producto_id, numero_lote) DO NOTHING;

-- Actualizar método de pago en ventas existentes (si existen)
UPDATE ventas SET metodo_pago_id = 1 WHERE metodo_pago_id IS NULL;

-- 10. ÍNDICES ADICIONALES PARA PERFORMANCE

-- Índice compuesto para consultas de ventas por fecha y cajero
CREATE INDEX IF NOT EXISTS idx_ventas_fecha_cajero ON ventas(fecha_hora_exacta, cajero_id);

-- Índice para búsquedas de productos por nombre
CREATE INDEX IF NOT EXISTS idx_productos_nombre ON productos USING gin(to_tsvector('spanish', nombre));

-- Índice para auditoría por usuario y fecha
CREATE INDEX IF NOT EXISTS idx_audit_usuario_fecha ON audit_log(usuario_id, fecha_hora);

-- =====================================================
-- COMENTARIOS Y DOCUMENTACIÓN
-- =====================================================

COMMENT ON TABLE audit_log IS 'Registro de auditoría para RBAC - rastrea todas las acciones sensibles';
COMMENT ON TABLE metodos_pago IS 'Catálogo de métodos de pago para integridad de facturación';
COMMENT ON TABLE lotes_productos IS 'Gestión de lotes con lógica FIFO para inventario';
COMMENT ON TABLE movimientos_inventario IS 'Auditoría completa de movimientos de stock';

COMMENT ON COLUMN ventas.fecha_hora_exacta IS 'Timestamp exacto de la venta (obligatorio)';
COMMENT ON COLUMN ventas.metodo_pago_id IS 'Método de pago usado (obligatorio)';
COMMENT ON COLUMN ventas.referencia_pago IS 'Referencia del pago (número de tarjeta, código, etc.)';

-- =====================================================
-- VERIFICACIÓN FINAL
-- =====================================================

-- Mostrar resumen de tablas creadas/actualizadas
SELECT 
    'audit_log' as tabla,
    COUNT(*) as registros
FROM audit_log
UNION ALL
SELECT 
    'metodos_pago' as tabla,
    COUNT(*) as registros
FROM metodos_pago
UNION ALL
SELECT 
    'lotes_productos' as tabla,
    COUNT(*) as registros
FROM lotes_productos
UNION ALL
SELECT 
    'movimientos_inventario' as tabla,
    COUNT(*) as registros
FROM movimientos_inventario;

-- Verificar que las columnas obligatorias existen
SELECT 
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns 
WHERE table_name = 'ventas' 
AND column_name IN ('fecha_hora_exacta', 'metodo_pago_id', 'referencia_pago')
ORDER BY column_name;

COMMIT;
