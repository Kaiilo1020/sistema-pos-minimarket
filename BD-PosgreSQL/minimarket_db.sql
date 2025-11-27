--
-- PostgreSQL database dump
--

-- Dumped from database version 17.5
-- Dumped by pg_dump version 17.5

-- Started on 2025-11-27 17:06:32

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 5124 (class 1262 OID 16768)
-- Name: minimarket_db; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE minimarket_db WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Spanish_Peru.utf8';


ALTER DATABASE minimarket_db OWNER TO postgres;

\connect minimarket_db

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 5125 (class 0 OID 0)
-- Dependencies: 5124
-- Name: DATABASE minimarket_db; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON DATABASE minimarket_db IS 'Base de datos del Sistema de Ventas del Minimarket';


--
-- TOC entry 4 (class 2615 OID 2200)
-- Name: public; Type: SCHEMA; Schema: -; Owner: pg_database_owner
--

CREATE SCHEMA public;


ALTER SCHEMA public OWNER TO pg_database_owner;

--
-- TOC entry 5126 (class 0 OID 0)
-- Dependencies: 4
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: pg_database_owner
--

COMMENT ON SCHEMA public IS 'standard public schema';


--
-- TOC entry 245 (class 1255 OID 16966)
-- Name: actualizar_timestamp(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.actualizar_timestamp() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.actualizar_timestamp() OWNER TO postgres;

--
-- TOC entry 248 (class 1255 OID 16991)
-- Name: calcular_igv(numeric); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.calcular_igv(subtotal numeric) RETURNS numeric
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.calcular_igv(subtotal numeric) OWNER TO postgres;

--
-- TOC entry 247 (class 1255 OID 16990)
-- Name: generar_numero_boleta(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.generar_numero_boleta() RETURNS character varying
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.generar_numero_boleta() OWNER TO postgres;

--
-- TOC entry 246 (class 1255 OID 16969)
-- Name: verificar_stock_bajo(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.verificar_stock_bajo() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    -- Si el stock queda en 10 o menos, insertar alerta
    IF NEW.stock <= 10 AND OLD.stock > 10 THEN
        INSERT INTO alertas_inventario (tipo_alerta, producto, detalle)
        VALUES ('STOCK_BAJO', NEW.nombre, 'Stock actual: ' || NEW.stock || ' unidades');
    END IF;
    
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.verificar_stock_bajo() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 230 (class 1259 OID 16888)
-- Name: alertas_inventario; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.alertas_inventario (
    id bigint NOT NULL,
    tipo_alerta character varying(30) NOT NULL,
    producto character varying(200) NOT NULL,
    detalle text NOT NULL,
    fecha_alerta timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    estado character varying(20) DEFAULT 'PENDIENTE'::character varying,
    usuario_revision character varying(100),
    fecha_revision timestamp without time zone,
    observaciones_revision text,
    CONSTRAINT alertas_inventario_estado_check CHECK (((estado)::text = ANY ((ARRAY['PENDIENTE'::character varying, 'REVISADA'::character varying, 'RESUELTA'::character varying])::text[]))),
    CONSTRAINT alertas_inventario_tipo_alerta_check CHECK (((tipo_alerta)::text = ANY ((ARRAY['STOCK_BAJO'::character varying, 'PROXIMO_VENCER'::character varying, 'VENCIDO'::character varying, 'SIN_LOTE'::character varying])::text[])))
);


ALTER TABLE public.alertas_inventario OWNER TO postgres;

--
-- TOC entry 229 (class 1259 OID 16887)
-- Name: alertas_inventario_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.alertas_inventario_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alertas_inventario_id_seq OWNER TO postgres;

--
-- TOC entry 5127 (class 0 OID 0)
-- Dependencies: 229
-- Name: alertas_inventario_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.alertas_inventario_id_seq OWNED BY public.alertas_inventario.id;


--
-- TOC entry 242 (class 1259 OID 17047)
-- Name: auditoria_log; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.auditoria_log (
    id integer NOT NULL,
    usuario character varying(50) NOT NULL,
    tipo_evento character varying(50) NOT NULL,
    detalles text,
    fecha_hora timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    ip_origen character varying(45)
);


ALTER TABLE public.auditoria_log OWNER TO postgres;

--
-- TOC entry 241 (class 1259 OID 17046)
-- Name: auditoria_log_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.auditoria_log_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.auditoria_log_id_seq OWNER TO postgres;

--
-- TOC entry 5128 (class 0 OID 0)
-- Dependencies: 241
-- Name: auditoria_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.auditoria_log_id_seq OWNED BY public.auditoria_log.id;


--
-- TOC entry 228 (class 1259 OID 16872)
-- Name: auditoria_precios; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.auditoria_precios (
    id bigint NOT NULL,
    producto_id bigint NOT NULL,
    precio_anterior numeric(10,2) NOT NULL,
    precio_nuevo numeric(10,2) NOT NULL,
    usuario character varying(100) NOT NULL,
    fecha_cambio timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    accion character varying(20) NOT NULL,
    observaciones text,
    CONSTRAINT auditoria_precios_accion_check CHECK (((accion)::text = ANY ((ARRAY['MODIFICACION'::character varying, 'REVERSION'::character varying])::text[])))
);


ALTER TABLE public.auditoria_precios OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 16871)
-- Name: auditoria_precios_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.auditoria_precios_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.auditoria_precios_id_seq OWNER TO postgres;

--
-- TOC entry 5129 (class 0 OID 0)
-- Dependencies: 227
-- Name: auditoria_precios_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.auditoria_precios_id_seq OWNED BY public.auditoria_precios.id;


--
-- TOC entry 220 (class 1259 OID 16788)
-- Name: categorias; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.categorias (
    id bigint NOT NULL,
    nombre character varying(100) NOT NULL,
    descripcion text,
    activo boolean DEFAULT true,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.categorias OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 16787)
-- Name: categorias_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.categorias_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.categorias_id_seq OWNER TO postgres;

--
-- TOC entry 5130 (class 0 OID 0)
-- Dependencies: 219
-- Name: categorias_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.categorias_id_seq OWNED BY public.categorias.id;


--
-- TOC entry 234 (class 1259 OID 16922)
-- Name: configuracion_sistema; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.configuracion_sistema (
    id bigint NOT NULL,
    parametro character varying(100) NOT NULL,
    valor text NOT NULL,
    descripcion text,
    tipo_dato character varying(20) DEFAULT 'STRING'::character varying,
    usuario_modificacion character varying(100),
    fecha_modificacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT configuracion_sistema_tipo_dato_check CHECK (((tipo_dato)::text = ANY ((ARRAY['STRING'::character varying, 'INTEGER'::character varying, 'DECIMAL'::character varying, 'BOOLEAN'::character varying, 'DATE'::character varying])::text[])))
);


ALTER TABLE public.configuracion_sistema OWNER TO postgres;

--
-- TOC entry 233 (class 1259 OID 16921)
-- Name: configuracion_sistema_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.configuracion_sistema_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.configuracion_sistema_id_seq OWNER TO postgres;

--
-- TOC entry 5131 (class 0 OID 0)
-- Dependencies: 233
-- Name: configuracion_sistema_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.configuracion_sistema_id_seq OWNED BY public.configuracion_sistema.id;


--
-- TOC entry 226 (class 1259 OID 16851)
-- Name: detalle_ventas; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.detalle_ventas (
    id bigint NOT NULL,
    venta_id bigint NOT NULL,
    producto_id bigint NOT NULL,
    cantidad integer NOT NULL,
    precio_unitario numeric(10,2) NOT NULL,
    subtotal numeric(10,2) NOT NULL,
    lote_vendido character varying(100),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT detalle_ventas_cantidad_check CHECK ((cantidad > 0)),
    CONSTRAINT detalle_ventas_precio_unitario_check CHECK ((precio_unitario >= (0)::numeric)),
    CONSTRAINT detalle_ventas_subtotal_check CHECK ((subtotal >= (0)::numeric))
);


ALTER TABLE public.detalle_ventas OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 16850)
-- Name: detalle_ventas_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.detalle_ventas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.detalle_ventas_id_seq OWNER TO postgres;

--
-- TOC entry 5132 (class 0 OID 0)
-- Dependencies: 225
-- Name: detalle_ventas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.detalle_ventas_id_seq OWNED BY public.detalle_ventas.id;


--
-- TOC entry 236 (class 1259 OID 16936)
-- Name: logs_sistema; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.logs_sistema (
    id bigint NOT NULL,
    nivel character varying(20) NOT NULL,
    mensaje text NOT NULL,
    usuario character varying(100),
    modulo character varying(100),
    ip_address inet,
    detalles jsonb,
    fecha_log timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT logs_sistema_nivel_check CHECK (((nivel)::text = ANY ((ARRAY['INFO'::character varying, 'WARNING'::character varying, 'ERROR'::character varying, 'DEBUG'::character varying])::text[])))
);


ALTER TABLE public.logs_sistema OWNER TO postgres;

--
-- TOC entry 235 (class 1259 OID 16935)
-- Name: logs_sistema_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.logs_sistema_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.logs_sistema_id_seq OWNER TO postgres;

--
-- TOC entry 5133 (class 0 OID 0)
-- Dependencies: 235
-- Name: logs_sistema_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.logs_sistema_id_seq OWNED BY public.logs_sistema.id;


--
-- TOC entry 244 (class 1259 OID 17057)
-- Name: lotes_producto; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.lotes_producto (
    id integer NOT NULL,
    producto_id bigint NOT NULL,
    codigo_lote character varying(50) NOT NULL,
    fecha_entrada date DEFAULT CURRENT_DATE,
    fecha_vencimiento date,
    cantidad integer NOT NULL,
    CONSTRAINT lotes_producto_cantidad_check CHECK ((cantidad >= 0))
);


ALTER TABLE public.lotes_producto OWNER TO postgres;

--
-- TOC entry 243 (class 1259 OID 17056)
-- Name: lotes_producto_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.lotes_producto_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.lotes_producto_id_seq OWNER TO postgres;

--
-- TOC entry 5134 (class 0 OID 0)
-- Dependencies: 243
-- Name: lotes_producto_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.lotes_producto_id_seq OWNED BY public.lotes_producto.id;


--
-- TOC entry 222 (class 1259 OID 16801)
-- Name: productos; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.productos (
    id bigint NOT NULL,
    codigo character varying(50) NOT NULL,
    nombre character varying(200) NOT NULL,
    descripcion text,
    precio numeric(10,2) NOT NULL,
    stock integer DEFAULT 0 NOT NULL,
    categoria_id bigint,
    categoria character varying(100),
    lote character varying(100),
    fecha_vencimiento date,
    requiere_lote boolean DEFAULT false,
    requiere_fecha_vencimiento boolean DEFAULT false,
    vendible boolean DEFAULT true,
    motivo_no_vendible character varying(200),
    activo boolean DEFAULT true,
    fecha_ultima_modificacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT productos_precio_check CHECK ((precio >= (0)::numeric)),
    CONSTRAINT productos_stock_check CHECK ((stock >= 0))
);


ALTER TABLE public.productos OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 16800)
-- Name: productos_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.productos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.productos_id_seq OWNER TO postgres;

--
-- TOC entry 5135 (class 0 OID 0)
-- Dependencies: 221
-- Name: productos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.productos_id_seq OWNED BY public.productos.id;


--
-- TOC entry 232 (class 1259 OID 16901)
-- Name: solicitudes_aprobacion; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.solicitudes_aprobacion (
    id bigint NOT NULL,
    codigo_solicitud character varying(100) NOT NULL,
    tipo_solicitud character varying(50) NOT NULL,
    usuario_solicitante_id bigint NOT NULL,
    descripcion text NOT NULL,
    parametros jsonb,
    prioridad character varying(20) DEFAULT 'NORMAL'::character varying,
    estado character varying(20) DEFAULT 'PENDIENTE'::character varying,
    nivel_requerido character varying(20) NOT NULL,
    usuario_aprobador character varying(100),
    fecha_aprobacion timestamp without time zone,
    observaciones text,
    fecha_solicitud timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT solicitudes_aprobacion_estado_check CHECK (((estado)::text = ANY ((ARRAY['PENDIENTE'::character varying, 'APROBADA'::character varying, 'RECHAZADA'::character varying, 'ESCALADA'::character varying])::text[]))),
    CONSTRAINT solicitudes_aprobacion_prioridad_check CHECK (((prioridad)::text = ANY ((ARRAY['BAJA'::character varying, 'NORMAL'::character varying, 'ALTA'::character varying, 'URGENTE'::character varying])::text[])))
);


ALTER TABLE public.solicitudes_aprobacion OWNER TO postgres;

--
-- TOC entry 231 (class 1259 OID 16900)
-- Name: solicitudes_aprobacion_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.solicitudes_aprobacion_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.solicitudes_aprobacion_id_seq OWNER TO postgres;

--
-- TOC entry 5136 (class 0 OID 0)
-- Dependencies: 231
-- Name: solicitudes_aprobacion_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.solicitudes_aprobacion_id_seq OWNED BY public.solicitudes_aprobacion.id;


--
-- TOC entry 218 (class 1259 OID 16770)
-- Name: usuarios; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.usuarios (
    id bigint NOT NULL,
    username character varying(50) NOT NULL,
    password character varying(255) NOT NULL,
    nombre character varying(100) NOT NULL,
    apellido character varying(100) NOT NULL,
    email character varying(150) NOT NULL,
    rol character varying(20) NOT NULL,
    activo boolean DEFAULT true,
    fecha_creacion timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso timestamp without time zone,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT usuarios_rol_check CHECK (((rol)::text = ANY ((ARRAY['CAJERO'::character varying, 'SUPERVISOR'::character varying, 'ADMINISTRADOR'::character varying])::text[])))
);


ALTER TABLE public.usuarios OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 16769)
-- Name: usuarios_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.usuarios_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.usuarios_id_seq OWNER TO postgres;

--
-- TOC entry 5137 (class 0 OID 0)
-- Dependencies: 217
-- Name: usuarios_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.usuarios_id_seq OWNED BY public.usuarios.id;


--
-- TOC entry 224 (class 1259 OID 16827)
-- Name: ventas; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ventas (
    id bigint NOT NULL,
    numero character varying(50) NOT NULL,
    fecha_hora timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    cajera_id bigint NOT NULL,
    metodo_pago character varying(30) NOT NULL,
    subtotal numeric(10,2) NOT NULL,
    igv numeric(10,2) NOT NULL,
    total numeric(10,2) NOT NULL,
    observaciones text,
    estado character varying(20) DEFAULT 'ACTIVA'::character varying,
    fecha_anulacion timestamp without time zone,
    usuario_anulacion character varying(100),
    motivo_anulacion text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ventas_estado_check CHECK (((estado)::text = ANY ((ARRAY['ACTIVA'::character varying, 'ANULADA'::character varying])::text[]))),
    CONSTRAINT ventas_igv_check CHECK ((igv >= (0)::numeric)),
    CONSTRAINT ventas_metodo_pago_check CHECK (((metodo_pago)::text = ANY ((ARRAY['EFECTIVO'::character varying, 'TARJETA_DEBITO'::character varying, 'TARJETA_CREDITO'::character varying, 'YAPE'::character varying, 'PLIN'::character varying, 'TRANSFERENCIA'::character varying])::text[]))),
    CONSTRAINT ventas_subtotal_check CHECK ((subtotal >= (0)::numeric)),
    CONSTRAINT ventas_total_check CHECK ((total >= (0)::numeric))
);


ALTER TABLE public.ventas OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 16826)
-- Name: ventas_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.ventas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.ventas_id_seq OWNER TO postgres;

--
-- TOC entry 5138 (class 0 OID 0)
-- Dependencies: 223
-- Name: ventas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.ventas_id_seq OWNED BY public.ventas.id;


--
-- TOC entry 238 (class 1259 OID 16976)
-- Name: vista_productos_proximo_vencer; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.vista_productos_proximo_vencer AS
 SELECT id,
    codigo,
    nombre,
    stock,
    fecha_vencimiento,
    (fecha_vencimiento - CURRENT_DATE) AS dias_restantes
   FROM public.productos p
  WHERE ((fecha_vencimiento IS NOT NULL) AND ((fecha_vencimiento >= CURRENT_DATE) AND (fecha_vencimiento <= (CURRENT_DATE + '7 days'::interval))) AND (activo = true));


ALTER VIEW public.vista_productos_proximo_vencer OWNER TO postgres;

--
-- TOC entry 237 (class 1259 OID 16971)
-- Name: vista_productos_stock_bajo; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.vista_productos_stock_bajo AS
 SELECT p.id,
    p.codigo,
    p.nombre,
    p.stock,
    p.precio,
    c.nombre AS categoria_nombre,
    p.fecha_vencimiento
   FROM (public.productos p
     LEFT JOIN public.categorias c ON ((p.categoria_id = c.id)))
  WHERE ((p.stock <= 10) AND (p.activo = true));


ALTER VIEW public.vista_productos_stock_bajo OWNER TO postgres;

--
-- TOC entry 240 (class 1259 OID 16985)
-- Name: vista_resumen_cajeras; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.vista_resumen_cajeras AS
 SELECT u.id,
    (((u.nombre)::text || ' '::text) || (u.apellido)::text) AS cajera_nombre,
    count(v.id) AS total_ventas,
    COALESCE(sum(v.total), (0)::numeric) AS total_vendido,
    COALESCE(avg(v.total), (0)::numeric) AS promedio_venta
   FROM (public.usuarios u
     LEFT JOIN public.ventas v ON (((u.id = v.cajera_id) AND (date(v.fecha_hora) = CURRENT_DATE) AND ((v.estado)::text = 'ACTIVA'::text))))
  WHERE (((u.rol)::text = 'CAJERA'::text) AND (u.activo = true))
  GROUP BY u.id, u.nombre, u.apellido;


ALTER VIEW public.vista_resumen_cajeras OWNER TO postgres;

--
-- TOC entry 239 (class 1259 OID 16980)
-- Name: vista_ventas_hoy; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.vista_ventas_hoy AS
 SELECT v.id,
    v.numero,
    v.fecha_hora,
    (((u.nombre)::text || ' '::text) || (u.apellido)::text) AS cajera_nombre,
    v.metodo_pago,
    v.total,
    v.estado
   FROM (public.ventas v
     JOIN public.usuarios u ON ((v.cajera_id = u.id)))
  WHERE (date(v.fecha_hora) = CURRENT_DATE);


ALTER VIEW public.vista_ventas_hoy OWNER TO postgres;

--
-- TOC entry 4842 (class 2604 OID 16891)
-- Name: alertas_inventario id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.alertas_inventario ALTER COLUMN id SET DEFAULT nextval('public.alertas_inventario_id_seq'::regclass);


--
-- TOC entry 4854 (class 2604 OID 17050)
-- Name: auditoria_log id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auditoria_log ALTER COLUMN id SET DEFAULT nextval('public.auditoria_log_id_seq'::regclass);


--
-- TOC entry 4840 (class 2604 OID 16875)
-- Name: auditoria_precios id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auditoria_precios ALTER COLUMN id SET DEFAULT nextval('public.auditoria_precios_id_seq'::regclass);


--
-- TOC entry 4822 (class 2604 OID 16791)
-- Name: categorias id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categorias ALTER COLUMN id SET DEFAULT nextval('public.categorias_id_seq'::regclass);


--
-- TOC entry 4849 (class 2604 OID 16925)
-- Name: configuracion_sistema id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.configuracion_sistema ALTER COLUMN id SET DEFAULT nextval('public.configuracion_sistema_id_seq'::regclass);


--
-- TOC entry 4838 (class 2604 OID 16854)
-- Name: detalle_ventas id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_ventas ALTER COLUMN id SET DEFAULT nextval('public.detalle_ventas_id_seq'::regclass);


--
-- TOC entry 4852 (class 2604 OID 16939)
-- Name: logs_sistema id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.logs_sistema ALTER COLUMN id SET DEFAULT nextval('public.logs_sistema_id_seq'::regclass);


--
-- TOC entry 4856 (class 2604 OID 17060)
-- Name: lotes_producto id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lotes_producto ALTER COLUMN id SET DEFAULT nextval('public.lotes_producto_id_seq'::regclass);


--
-- TOC entry 4825 (class 2604 OID 16804)
-- Name: productos id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.productos ALTER COLUMN id SET DEFAULT nextval('public.productos_id_seq'::regclass);


--
-- TOC entry 4845 (class 2604 OID 16904)
-- Name: solicitudes_aprobacion id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitudes_aprobacion ALTER COLUMN id SET DEFAULT nextval('public.solicitudes_aprobacion_id_seq'::regclass);


--
-- TOC entry 4817 (class 2604 OID 16773)
-- Name: usuarios id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios ALTER COLUMN id SET DEFAULT nextval('public.usuarios_id_seq'::regclass);


--
-- TOC entry 4834 (class 2604 OID 16830)
-- Name: ventas id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ventas ALTER COLUMN id SET DEFAULT nextval('public.ventas_id_seq'::regclass);


--
-- TOC entry 5108 (class 0 OID 16888)
-- Dependencies: 230
-- Data for Name: alertas_inventario; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.alertas_inventario VALUES (1, 'STOCK_BAJO', 'Arroz Costeño', 'Stock: 8 unidades', '2025-11-26 16:19:45.989841', 'PENDIENTE', NULL, NULL, NULL);
INSERT INTO public.alertas_inventario VALUES (2, 'PROXIMO_VENCER', 'Yogurt Natural', 'Vence: 2025-11-28 (2 días)', '2025-11-26 16:19:45.999383', 'PENDIENTE', NULL, NULL, NULL);
INSERT INTO public.alertas_inventario VALUES (3, 'STOCK_BAJO', 'Arroz Costeño 1kg', 'Stock: 8 unidades', '2025-11-26 16:19:46.007307', 'PENDIENTE', NULL, NULL, NULL);
INSERT INTO public.alertas_inventario VALUES (4, 'STOCK_BAJO', 'Arroz Costeño', 'Stock: 8 unidades', '2025-11-26 16:26:11.865449', 'PENDIENTE', NULL, NULL, NULL);
INSERT INTO public.alertas_inventario VALUES (5, 'PROXIMO_VENCER', 'Yogurt Natural', 'Vence: 2025-11-28 (2 días)', '2025-11-26 16:26:11.876531', 'PENDIENTE', NULL, NULL, NULL);
INSERT INTO public.alertas_inventario VALUES (6, 'STOCK_BAJO', 'Arroz Costeño 1kg', 'Stock: 8 unidades', '2025-11-26 16:26:11.882065', 'PENDIENTE', NULL, NULL, NULL);
INSERT INTO public.alertas_inventario VALUES (7, 'STOCK_BAJO', 'Arroz Costeño', 'Stock: 8 unidades', '2025-11-26 16:31:31.37129', 'PENDIENTE', NULL, NULL, NULL);
INSERT INTO public.alertas_inventario VALUES (8, 'PROXIMO_VENCER', 'Yogurt Natural', 'Vence: 2025-11-28 (2 días)', '2025-11-26 16:31:31.380566', 'PENDIENTE', NULL, NULL, NULL);
INSERT INTO public.alertas_inventario VALUES (9, 'STOCK_BAJO', 'Arroz Costeño 1kg', 'Stock: 8 unidades', '2025-11-26 16:31:31.388326', 'PENDIENTE', NULL, NULL, NULL);
INSERT INTO public.alertas_inventario VALUES (10, 'STOCK_BAJO', 'Arroz Costeño', 'Stock: 8 unidades', '2025-11-26 16:40:44.191745', 'PENDIENTE', NULL, NULL, NULL);
INSERT INTO public.alertas_inventario VALUES (11, 'PROXIMO_VENCER', 'Yogurt Natural', 'Vence: 2025-11-28 (2 días)', '2025-11-26 16:40:44.198712', 'PENDIENTE', NULL, NULL, NULL);
INSERT INTO public.alertas_inventario VALUES (12, 'STOCK_BAJO', 'Arroz Costeño 1kg', 'Stock: 8 unidades', '2025-11-26 16:40:44.20215', 'PENDIENTE', NULL, NULL, NULL);


--
-- TOC entry 5116 (class 0 OID 17047)
-- Dependencies: 242
-- Data for Name: auditoria_log; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.auditoria_log VALUES (1, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-26 19:23:58.324758', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (2, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-26 19:24:15.824632', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (3, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-26 19:25:37.720187', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (4, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-26 19:26:25.020295', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (5, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-26 19:27:30.631748', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (6, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-26 19:30:15.778763', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (7, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-26 19:32:06.900551', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (8, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-26 19:32:29.698763', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (9, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-26 19:47:15.661169', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (10, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-26 19:52:50.446415', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (11, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-26 20:53:16.507064', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (12, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-26 20:59:38.580964', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (13, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-26 21:23:15.566297', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (14, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-26 21:28:44.025197', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (15, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-26 22:45:44.274552', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (16, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-26 22:49:25.924836', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (17, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-26 22:54:24.168951', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (18, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-26 23:09:36.043837', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (19, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-26 23:21:48.923989', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (20, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-26 23:36:23.817986', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (21, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-26 23:42:12.435169', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (22, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-26 23:52:01.438825', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (23, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-26 23:59:33.690608', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (24, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 00:11:17.207555', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (25, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 00:23:22.120567', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (26, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 01:27:12.13627', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (27, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 01:45:07.923876', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (28, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 01:51:13.814841', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (29, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 09:58:55.528256', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (30, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 10:10:21.023646', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (31, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 10:15:58.37061', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (32, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 10:25:10.427344', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (33, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 10:32:46.362334', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (34, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 10:35:02.791308', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (35, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 10:40:14.865195', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (36, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 11:11:03.915707', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (37, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 11:11:37.707496', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (38, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 11:14:23.388597', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (39, 'supervisor', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 11:17:24.634432', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (40, 'supervisor', 'LOGOUT', 'Cierre de sesión', '2025-11-27 11:17:51.446185', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (41, 'cajera1', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 11:31:32.840592', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (42, 'cajera1', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 11:41:17.16601', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (43, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 11:42:47.239947', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (44, 'cajera1', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 11:53:22.168263', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (45, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 16:50:07.077185', '127.0.0.1');


--
-- TOC entry 5106 (class 0 OID 16872)
-- Dependencies: 228
-- Data for Name: auditoria_precios; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.auditoria_precios VALUES (1, 1, 4.50, 5.50, 'supervisor.juan', '2025-11-26 16:19:46.017967', 'MODIFICACION', NULL);


--
-- TOC entry 5098 (class 0 OID 16788)
-- Dependencies: 220
-- Data for Name: categorias; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.categorias VALUES (1, 'Lácteos', 'Productos lácteos y derivados', true, '2025-11-26 13:58:09.483174');
INSERT INTO public.categorias VALUES (2, 'Panadería', 'Pan, galletas y productos de panadería', true, '2025-11-26 13:58:09.483174');
INSERT INTO public.categorias VALUES (3, 'Bebidas', 'Bebidas gaseosas, jugos y agua', true, '2025-11-26 13:58:09.483174');
INSERT INTO public.categorias VALUES (4, 'Limpieza', 'Productos de limpieza e higiene', true, '2025-11-26 13:58:09.483174');
INSERT INTO public.categorias VALUES (5, 'Abarrotes', 'Productos secos y enlatados', true, '2025-11-26 13:58:09.483174');
INSERT INTO public.categorias VALUES (6, 'Carnes', 'Carnes y embutidos', true, '2025-11-26 13:58:09.483174');
INSERT INTO public.categorias VALUES (7, 'Frutas y Verduras', 'Productos frescos', true, '2025-11-26 13:58:09.483174');


--
-- TOC entry 5112 (class 0 OID 16922)
-- Dependencies: 234
-- Data for Name: configuracion_sistema; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.configuracion_sistema VALUES (1, 'STOCK_MINIMO_ALERTA', '10', 'Cantidad mínima de stock para generar alerta', 'INTEGER', NULL, '2025-11-26 13:58:09.483174');
INSERT INTO public.configuracion_sistema VALUES (2, 'DIAS_VENCIMIENTO_ALERTA', '7', 'Días antes del vencimiento para generar alerta', 'INTEGER', NULL, '2025-11-26 13:58:09.483174');
INSERT INTO public.configuracion_sistema VALUES (3, 'IGV_PORCENTAJE', '18', 'Porcentaje de IGV aplicado a las ventas', 'DECIMAL', NULL, '2025-11-26 13:58:09.483174');
INSERT INTO public.configuracion_sistema VALUES (4, 'DESCUENTO_MAXIMO_CAJERA', '5', 'Porcentaje máximo de descuento que puede aplicar una cajera', 'DECIMAL', NULL, '2025-11-26 13:58:09.483174');
INSERT INTO public.configuracion_sistema VALUES (5, 'DESCUENTO_MAXIMO_SUPERVISOR', '25', 'Porcentaje máximo de descuento que puede aplicar un supervisor', 'DECIMAL', NULL, '2025-11-26 13:58:09.483174');
INSERT INTO public.configuracion_sistema VALUES (6, 'PRECIO_MAXIMO_SUPERVISOR', '1000', 'Precio máximo que puede establecer un supervisor', 'DECIMAL', NULL, '2025-11-26 13:58:09.483174');
INSERT INTO public.configuracion_sistema VALUES (7, 'BACKUP_AUTOMATICO', 'true', 'Indica si se realizan backups automáticos', 'BOOLEAN', NULL, '2025-11-26 13:58:09.483174');
INSERT INTO public.configuracion_sistema VALUES (8, 'NOTIFICACIONES_EMAIL', 'true', 'Indica si están habilitadas las notificaciones por email', 'BOOLEAN', NULL, '2025-11-26 13:58:09.483174');


--
-- TOC entry 5104 (class 0 OID 16851)
-- Dependencies: 226
-- Data for Name: detalle_ventas; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.detalle_ventas VALUES (1, 1, 8, 5, 4.20, 21.00, NULL, '2025-11-27 00:00:18.235524');
INSERT INTO public.detalle_ventas VALUES (2, 2, 6, 5, 2.00, 10.00, NULL, '2025-11-27 10:00:30.073563');
INSERT INTO public.detalle_ventas VALUES (3, 2, 2, 2, 3.20, 6.40, NULL, '2025-11-27 10:00:30.073563');
INSERT INTO public.detalle_ventas VALUES (4, 3, 7, 3, 8.90, 26.70, NULL, '2025-11-27 10:20:30.057437');
INSERT INTO public.detalle_ventas VALUES (5, 4, 6, 4, 2.00, 8.00, NULL, '2025-11-27 10:41:13.512171');
INSERT INTO public.detalle_ventas VALUES (6, 5, 3, 3, 1.50, 4.50, NULL, '2025-11-27 11:34:22.778019');
INSERT INTO public.detalle_ventas VALUES (7, 6, 8, 4, 4.20, 16.80, NULL, '2025-11-27 11:34:53.686256');


--
-- TOC entry 5114 (class 0 OID 16936)
-- Dependencies: 236
-- Data for Name: logs_sistema; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 5118 (class 0 OID 17057)
-- Dependencies: 244
-- Data for Name: lotes_producto; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.lotes_producto VALUES (1, 5, 'LOTE_202511_005', '2025-11-21', '2025-12-26', 12);
INSERT INTO public.lotes_producto VALUES (2, 2, 'LOTE_202511_002', '2025-11-21', '2025-12-26', 15);
INSERT INTO public.lotes_producto VALUES (3, 1, 'LOTE_202511_001', '2025-11-21', '2025-12-26', 30);


--
-- TOC entry 5100 (class 0 OID 16801)
-- Dependencies: 222
-- Data for Name: productos; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.productos VALUES (4, 'ARROZ001', 'Arroz Costeño 1kg', 'Arroz extra superior', 3.80, 8, NULL, 'Abarrotes', NULL, NULL, false, false, true, NULL, true, '2025-11-26 13:58:09.483174', '2025-11-26 13:58:09.483174', '2025-11-26 13:58:09.483174');
INSERT INTO public.productos VALUES (5, 'YOGURT001', 'Yogurt Gloria Fresa', 'Yogurt con sabor a fresa', 2.50, 12, NULL, 'Lácteos', 'LOTE_2024_001', '2025-12-26', true, true, true, NULL, true, '2025-11-26 13:58:09.483174', '2025-11-26 13:58:09.483174', '2025-11-26 13:58:09.483174');
INSERT INTO public.productos VALUES (1, 'LECHE001', 'Leche Entera Gloria 1L', 'Leche entera pasteurizada', 5.50, 30, NULL, 'Lácteos', 'LOTE_2024_001', '2025-12-26', true, true, true, NULL, true, '2025-11-26 16:19:46.00923', '2025-11-26 13:58:09.483174', '2025-11-26 16:19:46.00923');
INSERT INTO public.productos VALUES (2, 'PAN001', 'Pan Integral Bimbo', 'Pan integral en rebanadas', 3.20, 13, NULL, 'Panadería', 'LOTE_2024_002', '2025-12-11', true, true, true, NULL, true, '2025-11-26 13:58:09.483174', '2025-11-26 13:58:09.483174', '2025-11-27 10:00:30.073563');
INSERT INTO public.productos VALUES (7, 'DETERG001', 'Detergente Ace 1kg', 'Detergente en polvo', 8.90, 17, NULL, 'Limpieza', NULL, NULL, false, false, true, NULL, true, '2025-11-26 13:58:09.483174', '2025-11-26 13:58:09.483174', '2025-11-27 10:20:30.057437');
INSERT INTO public.productos VALUES (6, 'GALLETA001', 'Galletas Soda Field', 'Galletas saladas', 2.00, 16, NULL, 'Panadería', NULL, NULL, false, false, true, NULL, true, '2025-11-26 13:58:09.483174', '2025-11-26 13:58:09.483174', '2025-11-27 10:41:13.512171');
INSERT INTO public.productos VALUES (3, 'AGUA001', 'Agua San Luis 625ml', 'Agua mineral sin gas', 1.50, 47, NULL, 'Bebidas', NULL, NULL, false, false, true, NULL, true, '2025-11-26 13:58:09.483174', '2025-11-26 13:58:09.483174', '2025-11-27 11:34:22.778019');
INSERT INTO public.productos VALUES (8, 'ATUN001', 'Atún A1 en aceite', 'Atún en conserva', 4.20, 26, NULL, 'Abarrotes', NULL, NULL, false, false, true, NULL, true, '2025-11-26 13:58:09.483174', '2025-11-26 13:58:09.483174', '2025-11-27 11:34:53.686256');


--
-- TOC entry 5110 (class 0 OID 16901)
-- Dependencies: 232
-- Data for Name: solicitudes_aprobacion; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 5096 (class 0 OID 16770)
-- Dependencies: 218
-- Data for Name: usuarios; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.usuarios VALUES (1, 'admin', 'admin123', 'Carlos', 'Administrador', 'admin@minimarket.com', 'ADMINISTRADOR', true, '2025-11-26 13:58:09.483174', NULL, '2025-11-26 13:58:09.483174', '2025-11-26 13:58:09.483174');
INSERT INTO public.usuarios VALUES (2, 'supervisor', 'super123', 'Juan', 'López', 'supervisor@minimarket.com', 'SUPERVISOR', true, '2025-11-26 13:58:09.483174', NULL, '2025-11-26 13:58:09.483174', '2025-11-26 13:58:09.483174');
INSERT INTO public.usuarios VALUES (3, 'cajera1', 'cajera123', 'María', 'García', 'maria@minimarket.com', 'CAJERO', true, '2025-11-26 13:58:09.483174', NULL, '2025-11-26 13:58:09.483174', '2025-11-26 19:20:33.409506');
INSERT INTO public.usuarios VALUES (4, 'cajera2', 'cajera456', 'Ana', 'López', 'ana@minimarket.com', 'CAJERO', true, '2025-11-26 13:58:09.483174', NULL, '2025-11-26 13:58:09.483174', '2025-11-26 19:20:33.409506');


--
-- TOC entry 5102 (class 0 OID 16827)
-- Dependencies: 224
-- Data for Name: ventas; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.ventas VALUES (1, 'VTA-20251127-8234', '2025-11-27 00:00:18.235524', 1, 'YAPE', 17.80, 3.20, 21.00, NULL, 'ACTIVA', NULL, NULL, NULL, '2025-11-27 00:00:18.235524');
INSERT INTO public.ventas VALUES (2, 'VTA-20251127-0072', '2025-11-27 10:00:30.073563', 1, 'TARJETA_DEBITO', 13.90, 2.50, 16.40, NULL, 'ACTIVA', NULL, NULL, NULL, '2025-11-27 10:00:30.073563');
INSERT INTO public.ventas VALUES (3, 'VTA-20251127-0056', '2025-11-27 10:20:30.057437', 1, 'EFECTIVO', 22.63, 4.07, 26.70, NULL, 'ACTIVA', NULL, NULL, NULL, '2025-11-27 10:20:30.057437');
INSERT INTO public.ventas VALUES (4, 'VTA-20251127-3511', '2025-11-27 10:41:13.512171', 1, 'PLIN', 6.78, 1.22, 8.00, NULL, 'ACTIVA', NULL, NULL, NULL, '2025-11-27 10:41:13.512171');
INSERT INTO public.ventas VALUES (5, 'VTA-20251127-2776', '2025-11-27 11:34:22.778019', 1, 'EFECTIVO', 3.81, 0.69, 4.50, NULL, 'ACTIVA', NULL, NULL, NULL, '2025-11-27 11:34:22.778019');
INSERT INTO public.ventas VALUES (6, 'VTA-20251127-3684', '2025-11-27 11:34:53.686256', 1, 'TARJETA_CREDITO', 14.24, 2.56, 16.80, NULL, 'ACTIVA', NULL, NULL, NULL, '2025-11-27 11:34:53.686256');


--
-- TOC entry 5139 (class 0 OID 0)
-- Dependencies: 229
-- Name: alertas_inventario_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.alertas_inventario_id_seq', 12, true);


--
-- TOC entry 5140 (class 0 OID 0)
-- Dependencies: 241
-- Name: auditoria_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.auditoria_log_id_seq', 45, true);


--
-- TOC entry 5141 (class 0 OID 0)
-- Dependencies: 227
-- Name: auditoria_precios_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.auditoria_precios_id_seq', 1, true);


--
-- TOC entry 5142 (class 0 OID 0)
-- Dependencies: 219
-- Name: categorias_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.categorias_id_seq', 7, true);


--
-- TOC entry 5143 (class 0 OID 0)
-- Dependencies: 233
-- Name: configuracion_sistema_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.configuracion_sistema_id_seq', 8, true);


--
-- TOC entry 5144 (class 0 OID 0)
-- Dependencies: 225
-- Name: detalle_ventas_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.detalle_ventas_id_seq', 7, true);


--
-- TOC entry 5145 (class 0 OID 0)
-- Dependencies: 235
-- Name: logs_sistema_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.logs_sistema_id_seq', 2, true);


--
-- TOC entry 5146 (class 0 OID 0)
-- Dependencies: 243
-- Name: lotes_producto_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.lotes_producto_id_seq', 3, true);


--
-- TOC entry 5147 (class 0 OID 0)
-- Dependencies: 221
-- Name: productos_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.productos_id_seq', 8, true);


--
-- TOC entry 5148 (class 0 OID 0)
-- Dependencies: 231
-- Name: solicitudes_aprobacion_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.solicitudes_aprobacion_id_seq', 1, false);


--
-- TOC entry 5149 (class 0 OID 0)
-- Dependencies: 217
-- Name: usuarios_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.usuarios_id_seq', 4, true);


--
-- TOC entry 5150 (class 0 OID 0)
-- Dependencies: 223
-- Name: ventas_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.ventas_id_seq', 6, true);


--
-- TOC entry 4917 (class 2606 OID 16899)
-- Name: alertas_inventario alertas_inventario_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.alertas_inventario
    ADD CONSTRAINT alertas_inventario_pkey PRIMARY KEY (id);


--
-- TOC entry 4932 (class 2606 OID 17055)
-- Name: auditoria_log auditoria_log_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auditoria_log
    ADD CONSTRAINT auditoria_log_pkey PRIMARY KEY (id);


--
-- TOC entry 4912 (class 2606 OID 16881)
-- Name: auditoria_precios auditoria_precios_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auditoria_precios
    ADD CONSTRAINT auditoria_precios_pkey PRIMARY KEY (id);


--
-- TOC entry 4887 (class 2606 OID 16799)
-- Name: categorias categorias_nombre_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categorias
    ADD CONSTRAINT categorias_nombre_key UNIQUE (nombre);


--
-- TOC entry 4889 (class 2606 OID 16797)
-- Name: categorias categorias_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categorias
    ADD CONSTRAINT categorias_pkey PRIMARY KEY (id);


--
-- TOC entry 4926 (class 2606 OID 16934)
-- Name: configuracion_sistema configuracion_sistema_parametro_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.configuracion_sistema
    ADD CONSTRAINT configuracion_sistema_parametro_key UNIQUE (parametro);


--
-- TOC entry 4928 (class 2606 OID 16932)
-- Name: configuracion_sistema configuracion_sistema_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.configuracion_sistema
    ADD CONSTRAINT configuracion_sistema_pkey PRIMARY KEY (id);


--
-- TOC entry 4908 (class 2606 OID 16860)
-- Name: detalle_ventas detalle_ventas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_ventas
    ADD CONSTRAINT detalle_ventas_pkey PRIMARY KEY (id);


--
-- TOC entry 4930 (class 2606 OID 16945)
-- Name: logs_sistema logs_sistema_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.logs_sistema
    ADD CONSTRAINT logs_sistema_pkey PRIMARY KEY (id);


--
-- TOC entry 4935 (class 2606 OID 17064)
-- Name: lotes_producto lotes_producto_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lotes_producto
    ADD CONSTRAINT lotes_producto_pkey PRIMARY KEY (id);


--
-- TOC entry 4896 (class 2606 OID 16820)
-- Name: productos productos_codigo_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.productos
    ADD CONSTRAINT productos_codigo_key UNIQUE (codigo);


--
-- TOC entry 4898 (class 2606 OID 16818)
-- Name: productos productos_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.productos
    ADD CONSTRAINT productos_pkey PRIMARY KEY (id);


--
-- TOC entry 4922 (class 2606 OID 16915)
-- Name: solicitudes_aprobacion solicitudes_aprobacion_codigo_solicitud_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitudes_aprobacion
    ADD CONSTRAINT solicitudes_aprobacion_codigo_solicitud_key UNIQUE (codigo_solicitud);


--
-- TOC entry 4924 (class 2606 OID 16913)
-- Name: solicitudes_aprobacion solicitudes_aprobacion_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitudes_aprobacion
    ADD CONSTRAINT solicitudes_aprobacion_pkey PRIMARY KEY (id);


--
-- TOC entry 4881 (class 2606 OID 16786)
-- Name: usuarios usuarios_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_email_key UNIQUE (email);


--
-- TOC entry 4883 (class 2606 OID 16782)
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);


--
-- TOC entry 4885 (class 2606 OID 16784)
-- Name: usuarios usuarios_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_username_key UNIQUE (username);


--
-- TOC entry 4904 (class 2606 OID 16844)
-- Name: ventas ventas_numero_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ventas
    ADD CONSTRAINT ventas_numero_key UNIQUE (numero);


--
-- TOC entry 4906 (class 2606 OID 16842)
-- Name: ventas ventas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ventas
    ADD CONSTRAINT ventas_pkey PRIMARY KEY (id);


--
-- TOC entry 4918 (class 1259 OID 16964)
-- Name: idx_alertas_estado; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_alertas_estado ON public.alertas_inventario USING btree (estado);


--
-- TOC entry 4919 (class 1259 OID 16965)
-- Name: idx_alertas_fecha; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_alertas_fecha ON public.alertas_inventario USING btree (fecha_alerta);


--
-- TOC entry 4920 (class 1259 OID 16963)
-- Name: idx_alertas_tipo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_alertas_tipo ON public.alertas_inventario USING btree (tipo_alerta);


--
-- TOC entry 4913 (class 1259 OID 16961)
-- Name: idx_auditoria_fecha; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_auditoria_fecha ON public.auditoria_precios USING btree (fecha_cambio);


--
-- TOC entry 4914 (class 1259 OID 16960)
-- Name: idx_auditoria_producto; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_auditoria_producto ON public.auditoria_precios USING btree (producto_id);


--
-- TOC entry 4915 (class 1259 OID 16962)
-- Name: idx_auditoria_usuario; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_auditoria_usuario ON public.auditoria_precios USING btree (usuario);


--
-- TOC entry 4909 (class 1259 OID 16959)
-- Name: idx_detalle_producto_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_detalle_producto_id ON public.detalle_ventas USING btree (producto_id);


--
-- TOC entry 4910 (class 1259 OID 16958)
-- Name: idx_detalle_venta_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_detalle_venta_id ON public.detalle_ventas USING btree (venta_id);


--
-- TOC entry 4933 (class 1259 OID 17070)
-- Name: idx_lotes_producto_fifo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_lotes_producto_fifo ON public.lotes_producto USING btree (producto_id, fecha_vencimiento, fecha_entrada);


--
-- TOC entry 4890 (class 1259 OID 16951)
-- Name: idx_productos_categoria; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_productos_categoria ON public.productos USING btree (categoria_id);


--
-- TOC entry 4891 (class 1259 OID 16949)
-- Name: idx_productos_codigo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_productos_codigo ON public.productos USING btree (codigo);


--
-- TOC entry 4892 (class 1259 OID 16950)
-- Name: idx_productos_nombre; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_productos_nombre ON public.productos USING btree (nombre);


--
-- TOC entry 4893 (class 1259 OID 16952)
-- Name: idx_productos_stock_bajo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_productos_stock_bajo ON public.productos USING btree (stock) WHERE (stock <= 10);


--
-- TOC entry 4894 (class 1259 OID 16953)
-- Name: idx_productos_vencimiento; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_productos_vencimiento ON public.productos USING btree (fecha_vencimiento) WHERE (fecha_vencimiento IS NOT NULL);


--
-- TOC entry 4877 (class 1259 OID 16947)
-- Name: idx_usuarios_email; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usuarios_email ON public.usuarios USING btree (email);


--
-- TOC entry 4878 (class 1259 OID 16948)
-- Name: idx_usuarios_rol; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usuarios_rol ON public.usuarios USING btree (rol);


--
-- TOC entry 4879 (class 1259 OID 16946)
-- Name: idx_usuarios_username; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usuarios_username ON public.usuarios USING btree (username);


--
-- TOC entry 4899 (class 1259 OID 16956)
-- Name: idx_ventas_cajera; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ventas_cajera ON public.ventas USING btree (cajera_id);


--
-- TOC entry 4900 (class 1259 OID 16957)
-- Name: idx_ventas_estado; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ventas_estado ON public.ventas USING btree (estado);


--
-- TOC entry 4901 (class 1259 OID 16955)
-- Name: idx_ventas_fecha; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ventas_fecha ON public.ventas USING btree (fecha_hora);


--
-- TOC entry 4902 (class 1259 OID 16954)
-- Name: idx_ventas_numero; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ventas_numero ON public.ventas USING btree (numero);


--
-- TOC entry 4944 (class 2620 OID 16968)
-- Name: productos trigger_productos_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_productos_updated_at BEFORE UPDATE ON public.productos FOR EACH ROW EXECUTE FUNCTION public.actualizar_timestamp();


--
-- TOC entry 4945 (class 2620 OID 16970)
-- Name: productos trigger_stock_bajo; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_stock_bajo AFTER UPDATE OF stock ON public.productos FOR EACH ROW EXECUTE FUNCTION public.verificar_stock_bajo();


--
-- TOC entry 4943 (class 2620 OID 16967)
-- Name: usuarios trigger_usuarios_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_usuarios_updated_at BEFORE UPDATE ON public.usuarios FOR EACH ROW EXECUTE FUNCTION public.actualizar_timestamp();


--
-- TOC entry 4940 (class 2606 OID 16882)
-- Name: auditoria_precios auditoria_precios_producto_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auditoria_precios
    ADD CONSTRAINT auditoria_precios_producto_id_fkey FOREIGN KEY (producto_id) REFERENCES public.productos(id);


--
-- TOC entry 4938 (class 2606 OID 16866)
-- Name: detalle_ventas detalle_ventas_producto_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_ventas
    ADD CONSTRAINT detalle_ventas_producto_id_fkey FOREIGN KEY (producto_id) REFERENCES public.productos(id);


--
-- TOC entry 4939 (class 2606 OID 16861)
-- Name: detalle_ventas detalle_ventas_venta_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_ventas
    ADD CONSTRAINT detalle_ventas_venta_id_fkey FOREIGN KEY (venta_id) REFERENCES public.ventas(id) ON DELETE CASCADE;


--
-- TOC entry 4942 (class 2606 OID 17065)
-- Name: lotes_producto fk_producto_lote; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lotes_producto
    ADD CONSTRAINT fk_producto_lote FOREIGN KEY (producto_id) REFERENCES public.productos(id) ON DELETE CASCADE;


--
-- TOC entry 4936 (class 2606 OID 16821)
-- Name: productos productos_categoria_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.productos
    ADD CONSTRAINT productos_categoria_id_fkey FOREIGN KEY (categoria_id) REFERENCES public.categorias(id);


--
-- TOC entry 4941 (class 2606 OID 16916)
-- Name: solicitudes_aprobacion solicitudes_aprobacion_usuario_solicitante_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.solicitudes_aprobacion
    ADD CONSTRAINT solicitudes_aprobacion_usuario_solicitante_id_fkey FOREIGN KEY (usuario_solicitante_id) REFERENCES public.usuarios(id);


--
-- TOC entry 4937 (class 2606 OID 16845)
-- Name: ventas ventas_cajera_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ventas
    ADD CONSTRAINT ventas_cajera_id_fkey FOREIGN KEY (cajera_id) REFERENCES public.usuarios(id);


-- Completed on 2025-11-27 17:06:32

--
-- PostgreSQL database dump complete
--

