-- ============================================
-- SCRIPT DE DATOS DE EJEMPLO PARA MINIMARKET
-- ============================================
-- Este script inserta datos de ejemplo para:
-- - Productos con stock crítico (para alertas)
-- - Productos con lotes próximos a vencer
-- - Ventas del día actual (para KPIs)
-- ============================================

-- Conectar a la base de datos
\connect minimarket_db

-- ============================================
-- 1. PRODUCTOS CON STOCK CRÍTICO (< 10 unidades)
-- ============================================

-- Actualizar productos existentes con stock bajo para generar alertas
UPDATE productos 
SET stock = 5 
WHERE nombre LIKE '%Arroz%' OR nombre LIKE '%Costeño%';

UPDATE productos 
SET stock = 8 
WHERE nombre LIKE '%Leche%' OR nombre LIKE '%Gloria%';

UPDATE productos 
SET stock = 3 
WHERE nombre LIKE '%Aceite%' OR nombre LIKE '%Primor%';

UPDATE productos 
SET stock = 7 
WHERE nombre LIKE '%Azúcar%';

UPDATE productos 
SET stock = 4 
WHERE nombre LIKE '%Fideos%';

-- Si no existen, insertar productos con stock crítico
INSERT INTO productos (codigo, nombre, descripcion, precio, stock, categoria, activo, requiere_lote, requiere_fecha_vencimiento)
VALUES 
    ('PROD-001', 'Arroz Costeño 1kg', 'Arroz extra de 1 kilogramo', 3.80, 5, 'Abarrotes', true, false, false),
    ('PROD-002', 'Leche Entera Gloria 1L', 'Leche entera en envase de 1 litro', 5.50, 8, 'Lácteos', true, true, true),
    ('PROD-003', 'Aceite Primor 900ml', 'Aceite vegetal de 900ml', 8.90, 3, 'Abarrotes', true, false, false),
    ('PROD-004', 'Azúcar Rubia Bella Flor 1kg', 'Azúcar rubia de 1 kilogramo', 4.20, 7, 'Abarrotes', true, false, false),
    ('PROD-005', 'Fideos Don Vittorio 400g', 'Fideos tallarín de 400 gramos', 2.50, 4, 'Abarrotes', true, false, false),
    ('PROD-006', 'Atún A1 en aceite 160g', 'Atún en conserva de 160 gramos', 4.20, 9, 'Conservas', true, true, true),
    ('PROD-007', 'Detergente Ace 1kg', 'Detergente en polvo de 1 kilogramo', 8.90, 6, 'Limpieza', true, false, false),
    ('PROD-008', 'Galletas Soda Field 400g', 'Galletas soda de 400 gramos', 2.00, 2, 'Galletas', true, true, true)
ON CONFLICT DO NOTHING;

-- ============================================
-- 2. PRODUCTOS CON LOTES PRÓXIMOS A VENCER
-- ============================================
-- (Productos que vencen en los próximos 7 días)

-- Actualizar productos existentes con fechas próximas a vencer
UPDATE productos 
SET fecha_vencimiento = CURRENT_DATE + INTERVAL '3 days',
    lote = 'LOTE-' || EXTRACT(YEAR FROM CURRENT_DATE) || '-001'
WHERE nombre LIKE '%Yogurt%' OR nombre LIKE '%Gloria%';

UPDATE productos 
SET fecha_vencimiento = CURRENT_DATE + INTERVAL '5 days',
    lote = 'LOTE-' || EXTRACT(YEAR FROM CURRENT_DATE) || '-002'
WHERE nombre LIKE '%Jamón%' OR nombre LIKE '%San Fernando%';

UPDATE productos 
SET fecha_vencimiento = CURRENT_DATE + INTERVAL '6 days',
    lote = 'LOTE-' || EXTRACT(YEAR FROM CURRENT_DATE) || '-003'
WHERE nombre LIKE '%Queso%' OR nombre LIKE '%Laive%';

-- Insertar productos con lotes próximos a vencer
INSERT INTO productos (codigo, nombre, descripcion, precio, stock, categoria, lote, fecha_vencimiento, activo, requiere_lote, requiere_fecha_vencimiento)
VALUES 
    ('PROD-009', 'Yogurt Gloria Fresa 1L', 'Yogurt de fresa de 1 litro', 2.50, 15, 'Lácteos', 'LOTE-2025-001', CURRENT_DATE + INTERVAL '3 days', true, true, true),
    ('PROD-010', 'Jamón San Fernando 200g', 'Jamón de pavo en rebanadas', 6.50, 12, 'Carnes', 'LOTE-2025-002', CURRENT_DATE + INTERVAL '5 days', true, true, true),
    ('PROD-011', 'Queso Laive Fresco 250g', 'Queso fresco de 250 gramos', 7.80, 10, 'Lácteos', 'LOTE-2025-003', CURRENT_DATE + INTERVAL '6 days', true, true, true),
    ('PROD-012', 'Pan Integral Bimbo 680g', 'Pan integral en rebanadas', 3.20, 8, 'Panadería', 'LOTE-2025-004', CURRENT_DATE + INTERVAL '4 days', true, true, true),
    ('PROD-013', 'Mantequilla Gloria 250g', 'Mantequilla de 250 gramos', 4.50, 6, 'Lácteos', 'LOTE-2025-005', CURRENT_DATE + INTERVAL '2 days', true, true, true)
ON CONFLICT DO NOTHING;

-- ============================================
-- 2.1. ASIGNAR FECHAS DE VENCIMIENTO A PRODUCTOS SIN FECHA
-- ============================================
-- Productos no perecederos: fechas lejanas (6-12 meses) - VERDE
-- Productos perecederos que no tienen fecha: fechas intermedias (2-4 meses) - AMARILLO

-- Productos no perecederos (Abarrotes, Limpieza, Galletas): 6-12 meses (VERDE)
UPDATE productos 
SET fecha_vencimiento = CURRENT_DATE + INTERVAL '8 months'
WHERE fecha_vencimiento IS NULL 
  AND (categoria = 'Abarrotes' OR categoria = 'Limpieza' OR categoria = 'Galletas' OR categoria = 'Conservas')
  AND activo = true;

-- Productos perecederos sin fecha: 2-4 meses (AMARILLO)
UPDATE productos 
SET fecha_vencimiento = CURRENT_DATE + INTERVAL '3 months'
WHERE fecha_vencimiento IS NULL 
  AND (categoria = 'Lácteos' OR categoria = 'Carnes' OR categoria = 'Panadería')
  AND activo = true;

-- Productos que aún no tienen fecha: asignar 6 meses por defecto (AMARILLO)
UPDATE productos 
SET fecha_vencimiento = CURRENT_DATE + INTERVAL '6 months'
WHERE fecha_vencimiento IS NULL 
  AND activo = true;

-- ============================================
-- 3. VENTAS DEL DÍA ACTUAL (Para KPIs)
-- ============================================

-- IMPORTANTE: Eliminar ventas del día actual si ya existen (para evitar duplicados)
-- Esto permite ejecutar el script múltiples veces sin errores
DELETE FROM detalle_ventas 
WHERE venta_id IN (
    SELECT id FROM ventas 
    WHERE DATE(fecha_hora) = CURRENT_DATE
);

DELETE FROM ventas 
WHERE DATE(fecha_hora) = CURRENT_DATE;

-- Obtener el ID del usuario cajero (ajustar según tu base de datos)
DO $$
DECLARE
    cajero_id_var BIGINT;
    venta_id_var BIGINT;
    producto_id_var BIGINT;
BEGIN
    -- Obtener ID del cajero (usar el primer usuario activo como cajero)
    SELECT id INTO cajero_id_var 
    FROM usuarios 
    WHERE rol = 'CAJERO' OR rol = 'CAJERA' 
    LIMIT 1;
    
    -- Si no hay cajero, usar el primer usuario
    IF cajero_id_var IS NULL THEN
        SELECT id INTO cajero_id_var FROM usuarios WHERE activo = true LIMIT 1;
    END IF;
    
    -- Si aún no hay usuario, crear uno temporal
    IF cajero_id_var IS NULL THEN
        INSERT INTO usuarios (username, password, nombre, apellido, email, rol, activo)
        VALUES ('cajero_temp', '1234', 'Cajero', 'Temporal', 'cajero@minimarket.com', 'CAJERO', true)
        RETURNING id INTO cajero_id_var;
    END IF;
    
    -- VENTA 1: Venta pequeña con efectivo
    INSERT INTO ventas (numero, fecha_hora, cajera_id, metodo_pago, subtotal, igv, total, estado)
    VALUES ('V-' || TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || '-001', CURRENT_TIMESTAMP, cajero_id_var, 'EFECTIVO', 6.44, 1.16, 7.60, 'ACTIVA')
    RETURNING id INTO venta_id_var;
    
    -- Detalles de la venta 1
    SELECT id INTO producto_id_var FROM productos WHERE codigo = 'PROD-001' AND stock >= 2 LIMIT 1;
    IF producto_id_var IS NOT NULL THEN
        INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, precio_unitario, subtotal)
        VALUES (venta_id_var, producto_id_var, 2, 3.80, 7.60);
        
        -- Actualizar stock solo si hay suficiente
        UPDATE productos SET stock = stock - 2 WHERE id = producto_id_var AND stock >= 2;
    END IF;
    
    -- VENTA 2: Venta mediana con Yape
    INSERT INTO ventas (numero, fecha_hora, cajera_id, metodo_pago, subtotal, igv, total, estado)
    VALUES ('V-' || TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || '-002', CURRENT_TIMESTAMP - INTERVAL '2 hours', cajero_id_var, 'YAPE', 11.02, 1.98, 13.00, 'ACTIVA')
    RETURNING id INTO venta_id_var;
    
    -- Detalles de la venta 2
    SELECT id INTO producto_id_var FROM productos WHERE codigo = 'PROD-002' AND stock >= 2 LIMIT 1;
    IF producto_id_var IS NOT NULL THEN
        INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, precio_unitario, subtotal)
        VALUES (venta_id_var, producto_id_var, 2, 5.50, 11.00);
        UPDATE productos SET stock = stock - 2 WHERE id = producto_id_var AND stock >= 2;
    END IF;
    
    SELECT id INTO producto_id_var FROM productos WHERE codigo = 'PROD-008' AND stock >= 1 LIMIT 1;
    IF producto_id_var IS NOT NULL THEN
        -- Usar solo 1 unidad si el stock es bajo
        INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, precio_unitario, subtotal)
        VALUES (venta_id_var, producto_id_var, 1, 2.00, 2.00);
        UPDATE productos SET stock = stock - 1 WHERE id = producto_id_var AND stock >= 1;
    END IF;
    
    -- VENTA 3: Venta mediana con efectivo
    INSERT INTO ventas (numero, fecha_hora, cajera_id, metodo_pago, subtotal, igv, total, estado)
    VALUES ('V-' || TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || '-003', CURRENT_TIMESTAMP - INTERVAL '4 hours', cajero_id_var, 'EFECTIVO', 18.98, 3.42, 22.40, 'ACTIVA')
    RETURNING id INTO venta_id_var;
    
    -- Detalles de la venta 3
    SELECT id INTO producto_id_var FROM productos WHERE codigo = 'PROD-003' AND stock >= 1 LIMIT 1;
    IF producto_id_var IS NOT NULL THEN
        INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, precio_unitario, subtotal)
        VALUES (venta_id_var, producto_id_var, 1, 8.90, 8.90);
        UPDATE productos SET stock = stock - 1 WHERE id = producto_id_var AND stock >= 1;
    END IF;
    
    SELECT id INTO producto_id_var FROM productos WHERE codigo = 'PROD-004' AND stock >= 2 LIMIT 1;
    IF producto_id_var IS NOT NULL THEN
        INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, precio_unitario, subtotal)
        VALUES (venta_id_var, producto_id_var, 2, 4.20, 8.40);
        UPDATE productos SET stock = stock - 2 WHERE id = producto_id_var AND stock >= 2;
    END IF;
    
    SELECT id INTO producto_id_var FROM productos WHERE codigo = 'PROD-005' AND stock >= 2 LIMIT 1;
    IF producto_id_var IS NOT NULL THEN
        INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, precio_unitario, subtotal)
        VALUES (venta_id_var, producto_id_var, 2, 2.50, 5.00);
        UPDATE productos SET stock = stock - 2 WHERE id = producto_id_var AND stock >= 2;
    END IF;
    
    -- VENTA 4: Venta con tarjeta
    INSERT INTO ventas (numero, fecha_hora, cajera_id, metodo_pago, subtotal, igv, total, estado)
    VALUES ('V-' || TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || '-004', CURRENT_TIMESTAMP - INTERVAL '1 hour', cajero_id_var, 'TARJETA_DEBITO', 14.66, 2.64, 17.30, 'ACTIVA')
    RETURNING id INTO venta_id_var;
    
    -- Detalles de la venta 4
    SELECT id INTO producto_id_var FROM productos WHERE codigo = 'PROD-006' AND stock >= 2 LIMIT 1;
    IF producto_id_var IS NOT NULL THEN
        INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, precio_unitario, subtotal)
        VALUES (venta_id_var, producto_id_var, 2, 4.20, 8.40);
        UPDATE productos SET stock = stock - 2 WHERE id = producto_id_var AND stock >= 2;
    END IF;
    
    SELECT id INTO producto_id_var FROM productos WHERE codigo = 'PROD-007' AND stock >= 1 LIMIT 1;
    IF producto_id_var IS NOT NULL THEN
        INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, precio_unitario, subtotal)
        VALUES (venta_id_var, producto_id_var, 1, 8.90, 8.90);
        UPDATE productos SET stock = stock - 1 WHERE id = producto_id_var AND stock >= 1;
    END IF;
    
    -- VENTA 5: Venta pequeña con Yape (omitida por falta de stock)
    -- Se omite esta venta porque los productos tienen stock muy bajo
    
    -- Detalles de la venta 5 (omitir si no hay stock disponible)
    -- Este producto ya tiene stock bajo, se omite para evitar error
    
    -- VENTA 6: Venta mediana con efectivo
    INSERT INTO ventas (numero, fecha_hora, cajera_id, metodo_pago, subtotal, igv, total, estado)
    VALUES ('V-' || TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || '-006', CURRENT_TIMESTAMP - INTERVAL '3 hours', cajero_id_var, 'EFECTIVO', 7.29, 1.31, 8.60, 'ACTIVA')
    RETURNING id INTO venta_id_var;
    
    -- Detalles de la venta 6
    SELECT id INTO producto_id_var FROM productos WHERE codigo = 'PROD-009' AND stock >= 2 LIMIT 1;
    IF producto_id_var IS NOT NULL THEN
        INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, precio_unitario, subtotal)
        VALUES (venta_id_var, producto_id_var, 2, 2.50, 5.00);
        UPDATE productos SET stock = stock - 2 WHERE id = producto_id_var AND stock >= 2;
    END IF;
    
    SELECT id INTO producto_id_var FROM productos WHERE codigo = 'PROD-012' AND stock >= 1 LIMIT 1;
    IF producto_id_var IS NOT NULL THEN
        INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, precio_unitario, subtotal)
        VALUES (venta_id_var, producto_id_var, 1, 3.20, 3.20);
        UPDATE productos SET stock = stock - 1 WHERE id = producto_id_var AND stock >= 1;
    END IF;
    
    -- VENTA 7: Venta mediana con Yape
    INSERT INTO ventas (numero, fecha_hora, cajera_id, metodo_pago, subtotal, igv, total, estado)
    VALUES ('V-' || TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || '-007', CURRENT_TIMESTAMP - INTERVAL '5 hours', cajero_id_var, 'YAPE', 21.19, 3.81, 25.10, 'ACTIVA')
    RETURNING id INTO venta_id_var;
    
    -- Detalles de la venta 7
    SELECT id INTO producto_id_var FROM productos WHERE codigo = 'PROD-001' AND stock >= 2 LIMIT 1;
    IF producto_id_var IS NOT NULL THEN
        INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, precio_unitario, subtotal)
        VALUES (venta_id_var, producto_id_var, 2, 3.80, 7.60);
        UPDATE productos SET stock = stock - 2 WHERE id = producto_id_var AND stock >= 2;
    END IF;
    
    SELECT id INTO producto_id_var FROM productos WHERE codigo = 'PROD-002' AND stock >= 2 LIMIT 1;
    IF producto_id_var IS NOT NULL THEN
        INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, precio_unitario, subtotal)
        VALUES (venta_id_var, producto_id_var, 2, 5.50, 11.00);
        UPDATE productos SET stock = stock - 2 WHERE id = producto_id_var AND stock >= 2;
    END IF;
    
    SELECT id INTO producto_id_var FROM productos WHERE codigo = 'PROD-010' AND stock >= 1 LIMIT 1;
    IF producto_id_var IS NOT NULL THEN
        INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, precio_unitario, subtotal)
        VALUES (venta_id_var, producto_id_var, 1, 6.50, 6.50);
        UPDATE productos SET stock = stock - 1 WHERE id = producto_id_var AND stock >= 1;
    END IF;
    
END $$;

-- ============================================
-- 4. VERIFICACIÓN DE DATOS INSERTADOS
-- ============================================

-- Mostrar resumen de datos insertados
SELECT 'Productos con stock crítico (< 10):' as tipo, COUNT(*) as cantidad
FROM productos 
WHERE stock < 10 AND activo = true

UNION ALL

SELECT 'Productos próximos a vencer (7 días):' as tipo, COUNT(*) as cantidad
FROM productos 
WHERE fecha_vencimiento IS NOT NULL 
  AND fecha_vencimiento BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '7 days'
  AND activo = true

UNION ALL

SELECT 'Ventas del día actual:' as tipo, COUNT(*) as cantidad
FROM ventas 
WHERE DATE(fecha_hora) = CURRENT_DATE AND estado = 'ACTIVA'

UNION ALL

SELECT 'Total recaudado hoy:' as tipo, COALESCE(SUM(total), 0)::INTEGER as cantidad
FROM ventas 
WHERE DATE(fecha_hora) = CURRENT_DATE AND estado = 'ACTIVA';

-- ============================================
-- FIN DEL SCRIPT
-- ============================================
-- Después de ejecutar este script, el dashboard mostrará:
-- - Alertas de stock crítico (productos con stock < 10)
-- - Alertas de lotes por vencer (productos que vencen en 7 días)
-- - KPIs con datos reales (ventas del día, transacciones, etc.)
-- ============================================

