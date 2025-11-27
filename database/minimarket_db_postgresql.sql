-- =====================================================
-- SCRIPT DE BASE DE DATOS PARA SISTEMA DE MINIMARKET
-- Base de datos: PostgreSQL
-- =====================================================

-- Crear la base de datos (ejecutar como superusuario)
-- CREATE DATABASE minimarket_db;
-- \c minimarket_db;

-- =====================================================
-- 1. TABLA DE USUARIOS
-- =====================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('CAJERA', 'SUPERVISOR', 'ADMINISTRADOR')),
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 2. TABLA DE CATEGORÍAS
-- =====================================================
CREATE TABLE IF NOT EXISTS categorias (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 3. TABLA DE PRODUCTOS
-- =====================================================
CREATE TABLE IF NOT EXISTS productos (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL CHECK (precio >= 0),
    stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    categoria_id BIGINT REFERENCES categorias(id),
    categoria VARCHAR(100), -- Para compatibilidad con el código
    lote VARCHAR(100),
    fecha_vencimiento DATE,
    requiere_lote BOOLEAN DEFAULT FALSE,
    requiere_fecha_vencimiento BOOLEAN DEFAULT FALSE,
    vendible BOOLEAN DEFAULT TRUE,
    motivo_no_vendible VARCHAR(200),
    activo BOOLEAN DEFAULT TRUE,
    fecha_ultima_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 4. TABLA DE VENTAS
-- =====================================================
CREATE TABLE IF NOT EXISTS ventas (
    id BIGSERIAL PRIMARY KEY,
    numero VARCHAR(50) UNIQUE NOT NULL,
    fecha_hora TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cajera_id BIGINT NOT NULL REFERENCES usuarios(id),
    metodo_pago VARCHAR(30) NOT NULL CHECK (metodo_pago IN ('EFECTIVO', 'TARJETA_DEBITO', 'TARJETA_CREDITO', 'YAPE', 'PLIN', 'TRANSFERENCIA')),
    subtotal DECIMAL(10,2) NOT NULL CHECK (subtotal >= 0),
    igv DECIMAL(10,2) NOT NULL CHECK (igv >= 0),
    total DECIMAL(10,2) NOT NULL CHECK (total >= 0),
    observaciones TEXT,
    estado VARCHAR(20) DEFAULT 'ACTIVA' CHECK (estado IN ('ACTIVA', 'ANULADA')),
    fecha_anulacion TIMESTAMP,
    usuario_anulacion VARCHAR(100),
    motivo_anulacion TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 5. TABLA DE DETALLE DE VENTAS
-- =====================================================
CREATE TABLE IF NOT EXISTS detalle_ventas (
    id BIGSERIAL PRIMARY KEY,
    venta_id BIGINT NOT NULL REFERENCES ventas(id) ON DELETE CASCADE,
    producto_id BIGINT NOT NULL REFERENCES productos(id),
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal DECIMAL(10,2) NOT NULL CHECK (subtotal >= 0),
    lote_vendido VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 6. TABLA DE AUDITORÍA DE PRECIOS
-- =====================================================
CREATE TABLE IF NOT EXISTS auditoria_precios (
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL REFERENCES productos(id),
    precio_anterior DECIMAL(10,2) NOT NULL,
    precio_nuevo DECIMAL(10,2) NOT NULL,
    usuario VARCHAR(100) NOT NULL,
    fecha_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accion VARCHAR(20) NOT NULL CHECK (accion IN ('MODIFICACION', 'REVERSION')),
    observaciones TEXT
);

-- =====================================================
-- 7. TABLA DE ALERTAS DE INVENTARIO
-- =====================================================
CREATE TABLE IF NOT EXISTS alertas_inventario (
    id BIGSERIAL PRIMARY KEY,
    tipo_alerta VARCHAR(30) NOT NULL CHECK (tipo_alerta IN ('STOCK_BAJO', 'PROXIMO_VENCER', 'VENCIDO', 'SIN_LOTE')),
    producto VARCHAR(200) NOT NULL,
    detalle TEXT NOT NULL,
    fecha_alerta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'REVISADA', 'RESUELTA')),
    usuario_revision VARCHAR(100),
    fecha_revision TIMESTAMP,
    observaciones_revision TEXT
);

-- =====================================================
-- 8. TABLA DE SOLICITUDES DE APROBACIÓN
-- =====================================================
CREATE TABLE IF NOT EXISTS solicitudes_aprobacion (
    id BIGSERIAL PRIMARY KEY,
    codigo_solicitud VARCHAR(100) UNIQUE NOT NULL,
    tipo_solicitud VARCHAR(50) NOT NULL,
    usuario_solicitante_id BIGINT NOT NULL REFERENCES usuarios(id),
    descripcion TEXT NOT NULL,
    parametros JSONB, -- Para almacenar parámetros adicionales
    prioridad VARCHAR(20) DEFAULT 'NORMAL' CHECK (prioridad IN ('BAJA', 'NORMAL', 'ALTA', 'URGENTE')),
    estado VARCHAR(20) DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA', 'ESCALADA')),
    nivel_requerido VARCHAR(20) NOT NULL,
    usuario_aprobador VARCHAR(100),
    fecha_aprobacion TIMESTAMP,
    observaciones TEXT,
    fecha_solicitud TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 9. TABLA DE CONFIGURACIÓN DEL SISTEMA
-- =====================================================
CREATE TABLE IF NOT EXISTS configuracion_sistema (
    id BIGSERIAL PRIMARY KEY,
    parametro VARCHAR(100) UNIQUE NOT NULL,
    valor TEXT NOT NULL,
    descripcion TEXT,
    tipo_dato VARCHAR(20) DEFAULT 'STRING' CHECK (tipo_dato IN ('STRING', 'INTEGER', 'DECIMAL', 'BOOLEAN', 'DATE')),
    usuario_modificacion VARCHAR(100),
    fecha_modificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 10. TABLA DE LOGS DEL SISTEMA
-- =====================================================
CREATE TABLE IF NOT EXISTS logs_sistema (
    id BIGSERIAL PRIMARY KEY,
    nivel VARCHAR(20) NOT NULL CHECK (nivel IN ('INFO', 'WARNING', 'ERROR', 'DEBUG')),
    mensaje TEXT NOT NULL,
    usuario VARCHAR(100),
    modulo VARCHAR(100),
    ip_address INET,
    detalles JSONB,
    fecha_log TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- ÍNDICES PARA MEJORAR RENDIMIENTO
-- =====================================================

-- Índices para usuarios
CREATE INDEX IF NOT EXISTS idx_usuarios_username ON usuarios(username);
CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);
CREATE INDEX IF NOT EXISTS idx_usuarios_rol ON usuarios(rol);

-- Índices para productos
CREATE INDEX IF NOT EXISTS idx_productos_codigo ON productos(codigo);
CREATE INDEX IF NOT EXISTS idx_productos_nombre ON productos(nombre);
CREATE INDEX IF NOT EXISTS idx_productos_categoria ON productos(categoria_id);
CREATE INDEX IF NOT EXISTS idx_productos_stock_bajo ON productos(stock) WHERE stock <= 10;
CREATE INDEX IF NOT EXISTS idx_productos_vencimiento ON productos(fecha_vencimiento) WHERE fecha_vencimiento IS NOT NULL;

-- Índices para ventas
CREATE INDEX IF NOT EXISTS idx_ventas_numero ON ventas(numero);
CREATE INDEX IF NOT EXISTS idx_ventas_fecha ON ventas(fecha_hora);
CREATE INDEX IF NOT EXISTS idx_ventas_cajera ON ventas(cajera_id);
CREATE INDEX IF NOT EXISTS idx_ventas_estado ON ventas(estado);

-- Índices para detalle de ventas
CREATE INDEX IF NOT EXISTS idx_detalle_venta_id ON detalle_ventas(venta_id);
CREATE INDEX IF NOT EXISTS idx_detalle_producto_id ON detalle_ventas(producto_id);

-- Índices para auditoría
CREATE INDEX IF NOT EXISTS idx_auditoria_producto ON auditoria_precios(producto_id);
CREATE INDEX IF NOT EXISTS idx_auditoria_fecha ON auditoria_precios(fecha_cambio);
CREATE INDEX IF NOT EXISTS idx_auditoria_usuario ON auditoria_precios(usuario);

-- Índices para alertas
CREATE INDEX IF NOT EXISTS idx_alertas_tipo ON alertas_inventario(tipo_alerta);
CREATE INDEX IF NOT EXISTS idx_alertas_estado ON alertas_inventario(estado);
CREATE INDEX IF NOT EXISTS idx_alertas_fecha ON alertas_inventario(fecha_alerta);

-- =====================================================
-- TRIGGERS PARA ACTUALIZACIÓN AUTOMÁTICA
-- =====================================================

-- Función para actualizar timestamp
CREATE OR REPLACE FUNCTION actualizar_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers para updated_at
CREATE TRIGGER trigger_usuarios_updated_at
    BEFORE UPDATE ON usuarios
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp();

CREATE TRIGGER trigger_productos_updated_at
    BEFORE UPDATE ON productos
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_timestamp();

-- Función para validar stock después de venta
CREATE OR REPLACE FUNCTION verificar_stock_bajo()
RETURNS TRIGGER AS $$
BEGIN
    -- Si el stock queda en 10 o menos, insertar alerta
    IF NEW.stock <= 10 AND OLD.stock > 10 THEN
        INSERT INTO alertas_inventario (tipo_alerta, producto, detalle)
        VALUES ('STOCK_BAJO', NEW.nombre, 'Stock actual: ' || NEW.stock || ' unidades');
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para alertas de stock bajo
CREATE TRIGGER trigger_stock_bajo
    AFTER UPDATE OF stock ON productos
    FOR EACH ROW
    EXECUTE FUNCTION verificar_stock_bajo();

-- =====================================================
-- DATOS INICIALES
-- =====================================================

-- Insertar categorías básicas
INSERT INTO categorias (nombre, descripcion) VALUES
('Lácteos', 'Productos lácteos y derivados'),
('Panadería', 'Pan, galletas y productos de panadería'),
('Bebidas', 'Bebidas gaseosas, jugos y agua'),
('Limpieza', 'Productos de limpieza e higiene'),
('Abarrotes', 'Productos secos y enlatados'),
('Carnes', 'Carnes y embutidos'),
('Frutas y Verduras', 'Productos frescos')
ON CONFLICT (nombre) DO NOTHING;

-- Insertar usuarios iniciales
INSERT INTO usuarios (username, password, nombre, apellido, email, rol) VALUES
('admin', 'admin123', 'Carlos', 'Administrador', 'admin@minimarket.com', 'ADMINISTRADOR'),
('supervisor', 'super123', 'Juan', 'López', 'supervisor@minimarket.com', 'SUPERVISOR'),
('cajera1', 'cajera123', 'María', 'García', 'maria@minimarket.com', 'CAJERA'),
('cajera2', 'cajera456', 'Ana', 'López', 'ana@minimarket.com', 'CAJERA')
ON CONFLICT (username) DO NOTHING;

-- Insertar productos de ejemplo
INSERT INTO productos (codigo, nombre, descripcion, precio, stock, categoria, requiere_lote, requiere_fecha_vencimiento) VALUES
('LECHE001', 'Leche Entera Gloria 1L', 'Leche entera pasteurizada', 4.50, 30, 'Lácteos', true, true),
('PAN001', 'Pan Integral Bimbo', 'Pan integral en rebanadas', 3.20, 15, 'Panadería', true, true),
('AGUA001', 'Agua San Luis 625ml', 'Agua mineral sin gas', 1.50, 50, 'Bebidas', false, false),
('ARROZ001', 'Arroz Costeño 1kg', 'Arroz extra superior', 3.80, 8, 'Abarrotes', false, false),
('YOGURT001', 'Yogurt Gloria Fresa', 'Yogurt con sabor a fresa', 2.50, 12, 'Lácteos', true, true),
('GALLETA001', 'Galletas Soda Field', 'Galletas saladas', 2.00, 25, 'Panadería', false, false),
('DETERG001', 'Detergente Ace 1kg', 'Detergente en polvo', 8.90, 20, 'Limpieza', false, false),
('ATUN001', 'Atún A1 en aceite', 'Atún en conserva', 4.20, 35, 'Abarrotes', false, false)
ON CONFLICT (codigo) DO NOTHING;

-- Actualizar algunos productos con lotes y fechas
UPDATE productos SET 
    lote = 'LOTE_2024_001',
    fecha_vencimiento = CURRENT_DATE + INTERVAL '30 days'
WHERE codigo IN ('LECHE001', 'YOGURT001');

UPDATE productos SET 
    lote = 'LOTE_2024_002',
    fecha_vencimiento = CURRENT_DATE + INTERVAL '15 days'
WHERE codigo = 'PAN001';

-- Insertar configuración inicial del sistema
INSERT INTO configuracion_sistema (parametro, valor, descripcion, tipo_dato) VALUES
('STOCK_MINIMO_ALERTA', '10', 'Cantidad mínima de stock para generar alerta', 'INTEGER'),
('DIAS_VENCIMIENTO_ALERTA', '7', 'Días antes del vencimiento para generar alerta', 'INTEGER'),
('IGV_PORCENTAJE', '18', 'Porcentaje de IGV aplicado a las ventas', 'DECIMAL'),
('DESCUENTO_MAXIMO_CAJERA', '5', 'Porcentaje máximo de descuento que puede aplicar una cajera', 'DECIMAL'),
('DESCUENTO_MAXIMO_SUPERVISOR', '25', 'Porcentaje máximo de descuento que puede aplicar un supervisor', 'DECIMAL'),
('PRECIO_MAXIMO_SUPERVISOR', '1000', 'Precio máximo que puede establecer un supervisor', 'DECIMAL'),
('BACKUP_AUTOMATICO', 'true', 'Indica si se realizan backups automáticos', 'BOOLEAN'),
('NOTIFICACIONES_EMAIL', 'true', 'Indica si están habilitadas las notificaciones por email', 'BOOLEAN')
ON CONFLICT (parametro) DO NOTHING;

-- =====================================================
-- VISTAS ÚTILES
-- =====================================================

-- Vista de productos con stock bajo
CREATE OR REPLACE VIEW vista_productos_stock_bajo AS
SELECT 
    p.id,
    p.codigo,
    p.nombre,
    p.stock,
    p.precio,
    c.nombre as categoria_nombre,
    p.fecha_vencimiento
FROM productos p
LEFT JOIN categorias c ON p.categoria_id = c.id
WHERE p.stock <= 10 AND p.activo = true;

-- Vista de productos próximos a vencer
CREATE OR REPLACE VIEW vista_productos_proximo_vencer AS
SELECT 
    p.id,
    p.codigo,
    p.nombre,
    p.stock,
    p.fecha_vencimiento,
    (p.fecha_vencimiento - CURRENT_DATE) as dias_restantes
FROM productos p
WHERE p.fecha_vencimiento IS NOT NULL 
    AND p.fecha_vencimiento BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '7 days'
    AND p.activo = true;

-- Vista de ventas del día
CREATE OR REPLACE VIEW vista_ventas_hoy AS
SELECT 
    v.id,
    v.numero,
    v.fecha_hora,
    u.nombre || ' ' || u.apellido as cajera_nombre,
    v.metodo_pago,
    v.total,
    v.estado
FROM ventas v
JOIN usuarios u ON v.cajera_id = u.id
WHERE DATE(v.fecha_hora) = CURRENT_DATE;

-- Vista de resumen de ventas por cajera
CREATE OR REPLACE VIEW vista_resumen_cajeras AS
SELECT 
    u.id,
    u.nombre || ' ' || u.apellido as cajera_nombre,
    COUNT(v.id) as total_ventas,
    COALESCE(SUM(v.total), 0) as total_vendido,
    COALESCE(AVG(v.total), 0) as promedio_venta
FROM usuarios u
LEFT JOIN ventas v ON u.id = v.cajera_id AND DATE(v.fecha_hora) = CURRENT_DATE AND v.estado = 'ACTIVA'
WHERE u.rol = 'CAJERA' AND u.activo = true
GROUP BY u.id, u.nombre, u.apellido;

-- =====================================================
-- FUNCIONES ÚTILES
-- =====================================================

-- Función para generar número de boleta único
CREATE OR REPLACE FUNCTION generar_numero_boleta()
RETURNS VARCHAR(50) AS $$
DECLARE
    numero VARCHAR(50);
    existe BOOLEAN;
BEGIN
    LOOP
        numero := 'BOL-' || TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || '-' || 
                  LPAD(FLOOR(RANDOM() * 10000)::TEXT, 4, '0');
        
        SELECT EXISTS(SELECT 1 FROM ventas WHERE numero = numero) INTO existe;
        
        IF NOT existe THEN
            EXIT;
        END IF;
    END LOOP;
    
    RETURN numero;
END;
$$ LANGUAGE plpgsql;

-- Función para calcular IGV
CREATE OR REPLACE FUNCTION calcular_igv(subtotal DECIMAL)
RETURNS DECIMAL AS $$
DECLARE
    porcentaje_igv DECIMAL;
BEGIN
    SELECT valor::DECIMAL INTO porcentaje_igv 
    FROM configuracion_sistema 
    WHERE parametro = 'IGV_PORCENTAJE';
    
    IF porcentaje_igv IS NULL THEN
        porcentaje_igv := 18;
    END IF;
    
    RETURN ROUND(subtotal * (porcentaje_igv / 100), 2);
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- PERMISOS (Opcional - ajustar según necesidades)
-- =====================================================

-- Crear rol para la aplicación
-- CREATE ROLE minimarket_app WITH LOGIN PASSWORD 'app_password';
-- GRANT CONNECT ON DATABASE minimarket_db TO minimarket_app;
-- GRANT USAGE ON SCHEMA public TO minimarket_app;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO minimarket_app;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO minimarket_app;

-- =====================================================
-- COMENTARIOS FINALES
-- =====================================================

-- Este script crea una base de datos completa para el sistema de minimarket
-- Incluye todas las tablas necesarias, índices, triggers, vistas y datos iniciales
-- Para conectar desde Java, usar:
-- URL: jdbc:postgresql://localhost:5432/minimarket_db
-- Usuario: postgres (o minimarket_app si se crea el rol específico)
-- Password: tu_password

COMMENT ON DATABASE minimarket_db IS 'Base de datos del Sistema de Ventas del Minimarket';
