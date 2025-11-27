-- =====================================================
-- CONSULTAS DE PRUEBA PARA EL DASHBOARD
-- Ejecuta estas consultas en pgAdmin para verificar que funcionan
-- =====================================================

-- 1. VENTAS DEL DÍA (debe funcionar)
SELECT COALESCE(SUM(total), 0) as total_ventas 
FROM ventas 
WHERE DATE(fecha_hora) = CURRENT_DATE AND estado = 'ACTIVA';

-- 2. TRANSACCIONES DEL DÍA (debe funcionar)
SELECT COUNT(*) as total_transacciones 
FROM ventas 
WHERE DATE(fecha_hora) = CURRENT_DATE AND estado = 'ACTIVA';

-- 3. PRODUCTOS VENDIDOS DEL DÍA (consulta simple con JOIN)
SELECT COALESCE(SUM(dv.cantidad), 0) as productos_vendidos 
FROM detalle_ventas dv, ventas v 
WHERE dv.venta_id = v.id 
AND DATE(v.fecha_hora) = CURRENT_DATE 
AND v.estado = 'ACTIVA';

-- 4. MÉTODOS DE PAGO DEL DÍA (debe funcionar)
SELECT metodo_pago, COUNT(*) as cantidad 
FROM ventas 
WHERE DATE(fecha_hora) = CURRENT_DATE AND estado = 'ACTIVA' 
GROUP BY metodo_pago;

-- 5. ALERTAS DE STOCK (debe funcionar)
SELECT nombre, stock 
FROM productos 
WHERE stock < 10 AND activo = true 
ORDER BY stock ASC 
LIMIT 5;

-- 6. LOTES POR VENCER (consulta simplificada)
SELECT nombre, fecha_vencimiento 
FROM productos 
WHERE fecha_vencimiento IS NOT NULL 
AND fecha_vencimiento >= CURRENT_DATE 
AND fecha_vencimiento <= CURRENT_DATE + 7 
AND activo = true 
ORDER BY fecha_vencimiento ASC 
LIMIT 5;

-- =====================================================
-- CONSULTA COMBINADA PARA VERIFICAR TODOS LOS DATOS
-- =====================================================
SELECT 
    'Ventas del día' as tipo,
    COALESCE(SUM(total), 0)::text as valor
FROM ventas 
WHERE DATE(fecha_hora) = CURRENT_DATE AND estado = 'ACTIVA'

UNION ALL

SELECT 
    'Transacciones del día' as tipo,
    COUNT(*)::text as valor
FROM ventas 
WHERE DATE(fecha_hora) = CURRENT_DATE AND estado = 'ACTIVA'

UNION ALL

SELECT 
    'Productos vendidos' as tipo,
    COALESCE(SUM(dv.cantidad), 0)::text as valor
FROM detalle_ventas dv, ventas v 
WHERE dv.venta_id = v.id 
AND DATE(v.fecha_hora) = CURRENT_DATE 
AND v.estado = 'ACTIVA';
