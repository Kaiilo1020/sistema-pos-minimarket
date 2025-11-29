--
-- PostgreSQL database dump
--

-- Dumped from database version 17.5
-- Dumped by pg_dump version 17.5

-- Started on 2025-11-28 17:06:52

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
-- TOC entry 5043 (class 1262 OID 16768)
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
-- TOC entry 5044 (class 0 OID 0)
-- Dependencies: 5043
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
-- TOC entry 5045 (class 0 OID 0)
-- Dependencies: 4
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: pg_database_owner
--

COMMENT ON SCHEMA public IS 'standard public schema';


--
-- TOC entry 235 (class 1255 OID 16966)
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
-- TOC entry 238 (class 1255 OID 16991)
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
-- TOC entry 237 (class 1255 OID 16990)
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
-- TOC entry 236 (class 1255 OID 16969)
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
-- TOC entry 234 (class 1259 OID 17047)
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
-- TOC entry 233 (class 1259 OID 17046)
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
-- TOC entry 5046 (class 0 OID 0)
-- Dependencies: 233
-- Name: auditoria_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.auditoria_log_id_seq OWNED BY public.auditoria_log.id;


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
-- TOC entry 5047 (class 0 OID 0)
-- Dependencies: 219
-- Name: categorias_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.categorias_id_seq OWNED BY public.categorias.id;


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
-- TOC entry 5048 (class 0 OID 0)
-- Dependencies: 225
-- Name: detalle_ventas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.detalle_ventas_id_seq OWNED BY public.detalle_ventas.id;


--
-- TOC entry 228 (class 1259 OID 16936)
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
-- TOC entry 227 (class 1259 OID 16935)
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
-- TOC entry 5049 (class 0 OID 0)
-- Dependencies: 227
-- Name: logs_sistema_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.logs_sistema_id_seq OWNED BY public.logs_sistema.id;


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
-- TOC entry 5050 (class 0 OID 0)
-- Dependencies: 221
-- Name: productos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.productos_id_seq OWNED BY public.productos.id;


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
-- TOC entry 5051 (class 0 OID 0)
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
-- TOC entry 5052 (class 0 OID 0)
-- Dependencies: 223
-- Name: ventas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.ventas_id_seq OWNED BY public.ventas.id;


--
-- TOC entry 230 (class 1259 OID 16976)
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
-- TOC entry 229 (class 1259 OID 16971)
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
-- TOC entry 232 (class 1259 OID 16985)
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
-- TOC entry 231 (class 1259 OID 16980)
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
-- TOC entry 4817 (class 2604 OID 17050)
-- Name: auditoria_log id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auditoria_log ALTER COLUMN id SET DEFAULT nextval('public.auditoria_log_id_seq'::regclass);


--
-- TOC entry 4797 (class 2604 OID 16791)
-- Name: categorias id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categorias ALTER COLUMN id SET DEFAULT nextval('public.categorias_id_seq'::regclass);


--
-- TOC entry 4813 (class 2604 OID 16854)
-- Name: detalle_ventas id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_ventas ALTER COLUMN id SET DEFAULT nextval('public.detalle_ventas_id_seq'::regclass);


--
-- TOC entry 4815 (class 2604 OID 16939)
-- Name: logs_sistema id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.logs_sistema ALTER COLUMN id SET DEFAULT nextval('public.logs_sistema_id_seq'::regclass);


--
-- TOC entry 4800 (class 2604 OID 16804)
-- Name: productos id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.productos ALTER COLUMN id SET DEFAULT nextval('public.productos_id_seq'::regclass);


--
-- TOC entry 4792 (class 2604 OID 16773)
-- Name: usuarios id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios ALTER COLUMN id SET DEFAULT nextval('public.usuarios_id_seq'::regclass);


--
-- TOC entry 4809 (class 2604 OID 16830)
-- Name: ventas id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ventas ALTER COLUMN id SET DEFAULT nextval('public.ventas_id_seq'::regclass);


--
-- TOC entry 5037 (class 0 OID 17047)
-- Dependencies: 234
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
INSERT INTO public.auditoria_log VALUES (46, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 17:49:29.942011', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (47, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 17:57:31.644877', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (48, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 18:48:18.626292', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (49, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 18:50:41.961078', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (50, 'admin', 'LOGOUT', 'Cierre de sesión', '2025-11-27 18:51:43.718315', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (51, 'cajera1', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 18:55:15.789433', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (52, 'cajera1', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 19:11:18.731381', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (53, 'cajera1', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 19:18:22.776524', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (54, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 19:29:06.381231', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (55, 'cajera1', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 19:29:29.473155', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (56, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 19:32:18.929609', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (57, 'admin', 'LOGOUT', 'Cierre de sesión', '2025-11-27 19:32:31.414918', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (58, 'cajera1', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 19:32:40.608546', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (59, 'cajera1', 'LOGOUT', 'Cierre de sesión', '2025-11-27 19:32:50.182763', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (60, 'supervisor', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 19:32:57.09649', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (61, 'supervisor', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 19:36:45.042774', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (62, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 19:42:35.314358', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (63, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 19:56:58.150647', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (64, 'admin', 'LOGOUT', 'Cierre de sesión', '2025-11-27 19:57:24.687519', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (65, 'cajera1', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 19:57:35.082666', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (66, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 20:06:25.391396', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (67, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 20:09:34.175421', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (68, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 20:35:19.138088', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (69, 'admin', 'LOGOUT', 'Cierre de sesión', '2025-11-27 20:36:43.84995', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (70, 'cajera1', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 20:36:55.607319', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (71, 'cajera1', 'LOGOUT', 'Cierre de sesión', '2025-11-27 22:51:14.497749', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (72, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 22:55:33.365188', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (73, 'admin', 'LOGOUT', 'Cierre de sesión', '2025-11-27 22:59:00.909922', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (74, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 22:59:21.17671', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (75, 'admin', 'LOGOUT', 'Cierre de sesión', '2025-11-27 23:01:54.632496', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (76, 'cajera1', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 23:01:59.90809', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (77, 'cajera1', 'LOGOUT', 'Cierre de sesión', '2025-11-27 23:02:05.560045', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (78, 'cajera1', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 23:04:02.92539', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (79, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 23:09:08.358737', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (80, 'admin', 'LOGOUT', 'Cierre de sesión', '2025-11-27 23:15:07.062119', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (81, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 23:16:45.96919', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (82, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 23:36:08.976732', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (83, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 23:39:19.602955', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (84, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 23:42:11.992786', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (85, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 23:45:37.742001', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (86, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 23:48:46.082026', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (87, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 23:52:04.663483', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (88, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 23:55:39.946294', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (89, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-27 23:59:10.843698', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (90, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 00:05:19.632084', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (91, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 01:14:41.36253', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (92, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 01:21:44.543203', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (93, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 01:24:30.903754', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (94, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 01:30:43.61226', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (95, 'admin', 'LOGOUT', 'Cierre de sesión', '2025-11-28 01:31:08.745797', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (96, 'cajera1', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 01:31:12.847946', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (97, 'cajera1', 'LOGOUT', 'Cierre de sesión', '2025-11-28 01:31:28.934637', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (98, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 01:43:15.229217', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (99, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 01:47:34.907331', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (100, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 01:49:24.060162', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (101, 'admin', 'LOGOUT', 'Cierre de sesión', '2025-11-28 01:49:36.661547', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (102, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 02:13:22.683662', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (103, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 02:35:45.075552', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (104, 'admin', 'LOGOUT', 'Cierre de sesión', '2025-11-28 02:36:11.320495', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (105, 'supervisor', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 02:36:18.887563', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (106, 'supervisor', 'LOGOUT', 'Cierre de sesión', '2025-11-28 02:36:36.303617', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (107, 'cajera1', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 02:36:41.9886', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (108, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 12:05:54.919067', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (109, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 12:37:59.593093', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (110, 'admin', 'LOGOUT', 'Cierre de sesión', '2025-11-28 12:42:54.429694', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (111, 'cajera1', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 12:43:05.180287', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (112, 'cajera1', 'LOGOUT', 'Cierre de sesión', '2025-11-28 12:48:07.890613', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (113, 'cajera1', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 12:48:18.320329', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (114, 'cajera1', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 12:50:55.078442', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (115, 'cajera1', 'LOGOUT', 'Cierre de sesión', '2025-11-28 12:57:07.203355', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (116, 'supervisor', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 12:57:41.85633', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (117, 'supervisor', 'LOGOUT', 'Cierre de sesión', '2025-11-28 13:03:26.118475', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (118, 'cajera1', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 13:03:33.066511', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (119, 'cajera1', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 13:05:31.530952', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (120, 'cajera1', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 13:19:13.387661', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (121, 'cajera1', 'LOGOUT', 'Cierre de sesión', '2025-11-28 13:20:10.097974', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (122, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 13:20:17.965881', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (123, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 13:24:01.603738', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (124, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 13:33:57.983707', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (125, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 13:36:02.853129', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (126, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 14:00:47.521267', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (127, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 14:03:54.302124', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (128, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 15:36:22.009833', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (129, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 15:49:08.871587', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (130, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 15:54:57.989335', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (131, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 16:14:42.972811', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (132, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 16:28:43.307891', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (133, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 16:50:37.983854', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (134, 'admin', 'LOGOUT', 'Cierre de sesión', '2025-11-28 17:03:32.112322', '127.0.0.1');
INSERT INTO public.auditoria_log VALUES (135, 'admin', 'LOGIN', 'Inicio de sesión exitoso', '2025-11-28 17:03:55.884199', '127.0.0.1');


--
-- TOC entry 5027 (class 0 OID 16788)
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
-- TOC entry 5033 (class 0 OID 16851)
-- Dependencies: 226
-- Data for Name: detalle_ventas; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.detalle_ventas VALUES (18, 14, 9, 2, 3.80, 7.60, NULL, '2025-11-27 19:55:51.983448');
INSERT INTO public.detalle_ventas VALUES (19, 15, 10, 2, 5.50, 11.00, NULL, '2025-11-27 19:55:51.983448');
INSERT INTO public.detalle_ventas VALUES (20, 15, 16, 1, 2.00, 2.00, NULL, '2025-11-27 19:55:51.983448');
INSERT INTO public.detalle_ventas VALUES (21, 16, 11, 1, 8.90, 8.90, NULL, '2025-11-27 19:55:51.983448');
INSERT INTO public.detalle_ventas VALUES (22, 16, 12, 2, 4.20, 8.40, NULL, '2025-11-27 19:55:51.983448');
INSERT INTO public.detalle_ventas VALUES (23, 16, 13, 2, 2.50, 5.00, NULL, '2025-11-27 19:55:51.983448');
INSERT INTO public.detalle_ventas VALUES (24, 17, 14, 2, 4.20, 8.40, NULL, '2025-11-27 19:55:51.983448');
INSERT INTO public.detalle_ventas VALUES (25, 17, 15, 1, 8.90, 8.90, NULL, '2025-11-27 19:55:51.983448');
INSERT INTO public.detalle_ventas VALUES (26, 18, 17, 2, 2.50, 5.00, NULL, '2025-11-27 19:55:51.983448');
INSERT INTO public.detalle_ventas VALUES (27, 18, 20, 1, 3.20, 3.20, NULL, '2025-11-27 19:55:51.983448');
INSERT INTO public.detalle_ventas VALUES (28, 19, 9, 2, 3.80, 7.60, NULL, '2025-11-27 19:55:51.983448');
INSERT INTO public.detalle_ventas VALUES (29, 19, 10, 2, 5.50, 11.00, NULL, '2025-11-27 19:55:51.983448');
INSERT INTO public.detalle_ventas VALUES (30, 19, 18, 1, 6.50, 6.50, NULL, '2025-11-27 19:55:51.983448');
INSERT INTO public.detalle_ventas VALUES (31, 20, 8, 3, 4.20, 12.60, NULL, '2025-11-28 01:17:27.89342');
INSERT INTO public.detalle_ventas VALUES (32, 21, 8, 2, 4.20, 8.40, NULL, '2025-11-28 01:25:11.43779');
INSERT INTO public.detalle_ventas VALUES (33, 22, 62, 4, 5.80, 23.20, NULL, '2025-11-28 13:19:35.094464');
INSERT INTO public.detalle_ventas VALUES (34, 23, 56, 4, 4.20, 16.80, NULL, '2025-11-28 14:01:03.340696');


--
-- TOC entry 5035 (class 0 OID 16936)
-- Dependencies: 228
-- Data for Name: logs_sistema; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- TOC entry 5029 (class 0 OID 16801)
-- Dependencies: 222
-- Data for Name: productos; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.productos VALUES (7, 'DETERG001', 'Detergente Ace 1kg', 'Detergente en polvo', 8.90, 17, NULL, 'Limpieza', NULL, '2026-07-27', false, false, true, NULL, true, '2025-11-26 13:58:09.483174', '2025-11-26 13:58:09.483174', '2025-11-27 20:19:05.408086');
INSERT INTO public.productos VALUES (4, 'ARROZ001', 'Arroz Costeño 1kg', 'Arroz extra superior', 3.80, 5, NULL, 'Abarrotes', NULL, '2026-07-27', false, false, true, NULL, true, '2025-11-26 13:58:09.483174', '2025-11-26 13:58:09.483174', '2025-11-27 20:19:05.408086');
INSERT INTO public.productos VALUES (16, 'PROD-008', 'Galletas Soda Field 400g', 'Galletas soda de 400 gramos', 2.00, 1, NULL, 'Galletas', NULL, '2026-07-27', true, true, true, NULL, true, '2025-11-27 19:47:39.719319', '2025-11-27 19:47:39.719319', '2025-11-27 20:19:05.408086');
INSERT INTO public.productos VALUES (11, 'PROD-003', 'Aceite Primor 900ml', 'Aceite vegetal de 900ml', 8.90, 2, NULL, 'Abarrotes', NULL, '2026-07-27', false, false, true, NULL, true, '2025-11-27 19:47:39.719319', '2025-11-27 19:47:39.719319', '2025-11-27 20:19:05.408086');
INSERT INTO public.productos VALUES (12, 'PROD-004', 'Azúcar Rubia Bella Flor 1kg', 'Azúcar rubia de 1 kilogramo', 4.20, 5, NULL, 'Abarrotes', NULL, '2026-07-27', false, false, true, NULL, true, '2025-11-27 19:47:39.719319', '2025-11-27 19:47:39.719319', '2025-11-27 20:19:05.408086');
INSERT INTO public.productos VALUES (13, 'PROD-005', 'Fideos Don Vittorio 400g', 'Fideos tallarín de 400 gramos', 2.50, 2, NULL, 'Abarrotes', NULL, '2026-07-27', false, false, true, NULL, true, '2025-11-27 19:47:39.719319', '2025-11-27 19:47:39.719319', '2025-11-27 20:19:05.408086');
INSERT INTO public.productos VALUES (14, 'PROD-006', 'Atún A1 en aceite 160g', 'Atún en conserva de 160 gramos', 4.20, 7, NULL, 'Conservas', NULL, '2026-07-27', true, true, true, NULL, true, '2025-11-27 19:47:39.719319', '2025-11-27 19:47:39.719319', '2025-11-27 20:19:05.408086');
INSERT INTO public.productos VALUES (6, 'GALLETA001', 'Galletas Soda Field', 'Galletas saladas', 2.00, 16, NULL, 'Panadería', NULL, '2026-02-27', false, false, true, NULL, true, '2025-11-26 13:58:09.483174', '2025-11-26 13:58:09.483174', '2025-11-27 20:19:05.408086');
INSERT INTO public.productos VALUES (3, 'AGUA001', 'Agua San Luis 625ml', 'Agua mineral sin gas', 1.50, 47, NULL, 'Bebidas', NULL, '2026-05-27', false, false, true, NULL, true, '2025-11-26 13:58:09.483174', '2025-11-26 13:58:09.483174', '2025-11-27 20:19:05.408086');
INSERT INTO public.productos VALUES (8, 'ATUN001', 'Atún A1 en aceite', 'Atún en conserva', 4.20, 21, NULL, 'Abarrotes', NULL, '2026-07-27', false, false, true, NULL, true, '2025-11-26 13:58:09.483174', '2025-11-26 13:58:09.483174', '2025-11-28 01:25:11.43779');
INSERT INTO public.productos VALUES (2, 'PAN001', 'Pan Integral Bimbo', 'Pan integral en rebanadas', 3.20, 13, NULL, 'Panadería', 'LOTE_2024_002', '2025-12-11', true, true, true, NULL, true, '2025-11-26 13:58:09.483174', '2025-11-26 13:58:09.483174', '2025-11-27 10:00:30.073563');
INSERT INTO public.productos VALUES (53, 'JAM-CRT-006', 'Jamón de Pavo 200g', 'Jamón de pavo en rebanadas', 7.90, 20, NULL, 'Carnes Frías', NULL, '2025-11-30', false, true, true, NULL, true, '2025-11-28 12:47:13.299378', '2025-11-28 12:47:13.299378', '2025-11-28 12:47:13.299378');
INSERT INTO public.productos VALUES (54, 'QUE-CRT-007', 'Queso Edam 300g', 'Queso edam en lonchas', 8.50, 15, NULL, 'Lácteos', NULL, '2025-12-01', false, true, true, NULL, true, '2025-11-28 12:47:13.299378', '2025-11-28 12:47:13.299378', '2025-11-28 12:47:13.299378');
INSERT INTO public.productos VALUES (55, 'TOR-CRT-008', 'Tortillas de Harina', 'Tortillas de harina de trigo', 3.50, 25, NULL, 'Panadería', NULL, '2025-12-03', false, true, true, NULL, true, '2025-11-28 12:47:13.299378', '2025-11-28 12:47:13.299378', '2025-11-28 12:47:13.299378');
INSERT INTO public.productos VALUES (57, 'BAG-CRT-010', 'Pan Baguette', 'Pan baguette artesanal', 2.80, 30, NULL, 'Panadería', NULL, '2025-12-05', false, true, true, NULL, true, '2025-11-28 12:47:13.299378', '2025-11-28 12:47:13.299378', '2025-11-28 12:47:13.299378');
INSERT INTO public.productos VALUES (58, 'MER-PRO-011', 'Mermelada de Fresa 250g', 'Mermelada de fresa', 5.50, 22, NULL, 'Conservas', NULL, '2025-12-08', false, true, true, NULL, true, '2025-11-28 12:47:20.137642', '2025-11-28 12:47:20.137642', '2025-11-28 12:47:20.137642');
INSERT INTO public.productos VALUES (59, 'MAN-PRO-012', 'Mantequilla de Maní 350g', 'Mantequilla de maní cremosa', 6.80, 14, NULL, 'Conservas', NULL, '2025-12-13', false, true, true, NULL, true, '2025-11-28 12:47:20.137642', '2025-11-28 12:47:20.137642', '2025-11-28 12:47:20.137642');
INSERT INTO public.productos VALUES (60, 'ACE-PRO-013', 'Aceitunas Verdes 200g', 'Aceitunas verdes sin hueso', 4.90, 28, NULL, 'Conservas', NULL, '2025-12-18', false, true, true, NULL, true, '2025-11-28 12:47:20.137642', '2025-11-28 12:47:20.137642', '2025-11-28 12:47:20.137642');
INSERT INTO public.productos VALUES (61, 'SAL-PRO-014', 'Salsa de Tomate 400g', 'Salsa de tomate natural', 3.20, 35, NULL, 'Conservas', NULL, '2025-12-23', false, true, true, NULL, true, '2025-11-28 12:47:20.137642', '2025-11-28 12:47:20.137642', '2025-11-28 12:47:20.137642');
INSERT INTO public.productos VALUES (62, 'ATU-PRO-015', 'Atún en Lata 160g', 'Atún en agua', 5.80, 36, NULL, 'Conservas', NULL, '2025-12-28', false, true, true, NULL, true, '2025-11-28 12:47:20.137642', '2025-11-28 12:47:20.137642', '2025-11-28 13:19:35.094464');
INSERT INTO public.productos VALUES (56, 'CRE-CRT-009', 'Crema de Leche 250ml', 'Crema de leche para cocinar', 4.20, 14, NULL, 'Lácteos', NULL, '2025-12-04', false, true, true, NULL, true, '2025-11-28 12:47:13.299378', '2025-11-28 12:47:13.299378', '2025-11-28 14:01:03.340696');
INSERT INTO public.productos VALUES (48, 'LEC-VEN-001', 'Leche Entera 1L', 'Leche entera pasteurizada', 4.50, 0, NULL, 'Lácteos', NULL, '2024-12-01', false, true, true, NULL, false, '2025-11-28 12:47:06.571221', '2025-11-28 12:47:06.571221', '2025-11-28 16:18:24.622109');
INSERT INTO public.productos VALUES (5, 'YOGURT001', 'Yogurt Gloria Fresa', 'Yogurt con sabor a fresa', 2.50, 8, NULL, 'Lácteos', 'LOTE-2025-001', '2025-11-30', true, true, true, NULL, true, '2025-11-26 13:58:09.483174', '2025-11-26 13:58:09.483174', '2025-11-27 19:55:51.983448');
INSERT INTO public.productos VALUES (1, 'LECHE001', 'Leche Entera Gloria 1L', 'Leche entera pasteurizada', 5.50, 8, NULL, 'Lácteos', 'LOTE-2025-001', '2025-11-30', true, true, true, NULL, true, '2025-11-26 16:19:46.00923', '2025-11-26 13:58:09.483174', '2025-11-27 19:55:51.983448');
INSERT INTO public.productos VALUES (49, 'YOG-VEN-002', 'Yogurt Natural 500g', 'Yogurt natural sin azúcar', 3.80, 0, NULL, 'Lácteos', NULL, '2024-11-25', false, true, true, NULL, false, '2025-11-28 12:47:06.571221', '2025-11-28 12:47:06.571221', '2025-11-28 16:18:24.622109');
INSERT INTO public.productos VALUES (50, 'PAN-VEN-003', 'Pan de Molde Integral', 'Pan integral en rebanadas', 5.20, 0, NULL, 'Panadería', NULL, '2024-12-05', false, true, true, NULL, false, '2025-11-28 12:47:06.571221', '2025-11-28 12:47:06.571221', '2025-11-28 16:18:24.622109');
INSERT INTO public.productos VALUES (51, 'QUE-VEN-004', 'Queso Fresco 250g', 'Queso fresco tipo requesón', 6.50, 0, NULL, 'Lácteos', NULL, '2024-11-20', false, true, true, NULL, false, '2025-11-28 12:47:06.571221', '2025-11-28 12:47:06.571221', '2025-11-28 16:18:24.622109');
INSERT INTO public.productos VALUES (21, 'PROD-013', 'Mantequilla Gloria 250g', 'Mantequilla de 250 gramos', 4.50, 8, NULL, 'Lácteos', 'LOTE-2025-001', '2025-11-30', true, true, true, NULL, true, '2025-11-27 19:47:54.227452', '2025-11-27 19:47:54.227452', '2025-11-27 19:55:51.983448');
INSERT INTO public.productos VALUES (52, 'MAN-VEN-005', 'Mantequilla 200g', 'Mantequilla sin sal', 4.80, 0, NULL, 'Lácteos', NULL, '2024-12-10', false, true, true, NULL, false, '2025-11-28 12:47:06.571221', '2025-11-28 12:47:06.571221', '2025-11-28 16:18:24.622109');
INSERT INTO public.productos VALUES (15, 'PROD-007', 'Detergente Ace 1kg', 'Detergente en polvo de 1 kilogramo', 8.90, 0, NULL, 'Limpieza', NULL, '2026-07-27', false, false, true, NULL, false, '2025-11-27 19:47:39.719319', '2025-11-27 19:47:39.719319', '2025-11-28 16:46:05.720068');
INSERT INTO public.productos VALUES (19, 'PROD-011', 'Queso Laive Fresco 250g', 'Queso fresco de 250 gramos', 7.80, 10, NULL, 'Lácteos', 'LOTE-2025-003', '2025-12-03', true, true, true, NULL, true, '2025-11-27 19:47:54.227452', '2025-11-27 19:47:54.227452', '2025-11-27 19:55:51.983448');
INSERT INTO public.productos VALUES (17, 'PROD-009', 'Yogurt Gloria Fresa 1L', 'Yogurt de fresa de 1 litro', 2.50, 6, NULL, 'Lácteos', 'LOTE-2025-001', '2025-11-30', true, true, true, NULL, true, '2025-11-27 19:47:54.227452', '2025-11-27 19:47:54.227452', '2025-11-27 19:55:51.983448');
INSERT INTO public.productos VALUES (20, 'PROD-012', 'Pan Integral Bimbo 680g', 'Pan integral en rebanadas', 3.20, 7, NULL, 'Panadería', 'LOTE-2025-004', '2025-12-01', true, true, true, NULL, true, '2025-11-27 19:47:54.227452', '2025-11-27 19:47:54.227452', '2025-11-27 19:55:51.983448');
INSERT INTO public.productos VALUES (18, 'PROD-010', 'Jamón San Fernando 200g', 'Jamón de pavo en rebanadas', 6.50, 11, NULL, 'Carnes', 'LOTE-2025-002', '2025-12-02', true, true, true, NULL, true, '2025-11-27 19:47:54.227452', '2025-11-27 19:47:54.227452', '2025-11-27 19:55:51.983448');
INSERT INTO public.productos VALUES (63, 'VENC-001', 'Leche Entera 1L', 'Leche entera pasteurizada', 4.50, 0, NULL, 'Lácteos', NULL, '2024-12-15', false, true, true, NULL, true, '2025-11-28 15:38:58.545812', '2025-11-28 15:38:58.545812', '2025-11-28 16:28:11.822304');
INSERT INTO public.productos VALUES (64, 'VENC-002', 'Yogurt Natural 500g', 'Yogurt natural sin azúcar', 3.80, 0, NULL, 'Lácteos', NULL, '2024-12-18', false, true, true, NULL, true, '2025-11-28 15:38:58.545812', '2025-11-28 15:38:58.545812', '2025-11-28 16:28:11.822304');
INSERT INTO public.productos VALUES (65, 'VENC-003', 'Queso Fresco 250g', 'Queso fresco tipo requesón', 5.20, 0, NULL, 'Lácteos', NULL, '2024-12-20', false, true, true, NULL, true, '2025-11-28 15:38:58.545812', '2025-11-28 15:38:58.545812', '2025-11-28 16:28:11.822304');
INSERT INTO public.productos VALUES (66, 'VENC-004', 'Mantequilla 200g', 'Mantequilla sin sal', 4.90, 0, NULL, 'Lácteos', NULL, '2024-12-22', false, true, true, NULL, true, '2025-11-28 15:38:58.545812', '2025-11-28 15:38:58.545812', '2025-11-28 16:28:11.822304');
INSERT INTO public.productos VALUES (69, 'VENC-007', 'Huevos AA x12', 'Docena de huevos tamaño AA', 8.90, 0, NULL, 'Huevos', NULL, '2024-12-25', false, true, true, NULL, true, '2025-11-28 15:38:58.545812', '2025-11-28 15:38:58.545812', '2025-11-28 16:28:11.822304');
INSERT INTO public.productos VALUES (70, 'VENC-008', 'Salsa de Tomate 500g', 'Salsa de tomate natural', 2.80, 15, NULL, 'Conservas', NULL, '2026-12-08', false, true, true, NULL, true, '2025-11-28 15:38:58.545812', '2025-11-28 15:38:58.545812', '2025-11-28 16:30:32.862161');
INSERT INTO public.productos VALUES (67, 'VENC-005', 'Jamón de Pavo 200g', 'Jamón de pavo en rebanadas', 6.50, 0, NULL, 'Carnes Frías', NULL, '2026-12-10', false, true, true, NULL, false, '2025-11-28 15:38:58.545812', '2025-11-28 15:38:58.545812', '2025-11-28 16:45:38.937694');
INSERT INTO public.productos VALUES (10, 'PROD-002', 'Leche Entera Gloria 1L', 'Leche entera en envase de 1 litro', 5.50, 0, NULL, 'Lácteos', 'LOTE-2025-001', '2025-11-30', true, true, true, NULL, false, '2025-11-27 19:47:39.719319', '2025-11-27 19:47:39.719319', '2025-11-28 16:46:05.720068');
INSERT INTO public.productos VALUES (9, 'PROD-001', 'Arroz Costeño 1kg', 'Arroz extra de 1 kilogramo', 3.80, 0, NULL, 'Abarrotes', NULL, '2026-07-27', false, false, true, NULL, false, '2025-11-27 19:47:39.719319', '2025-11-27 19:47:39.719319', '2025-11-28 16:46:05.720068');
INSERT INTO public.productos VALUES (68, 'VENC-006', 'Pan de Molde Integral', 'Pan integral en rebanadas', 3.20, 0, NULL, 'Panadería', NULL, '2024-12-12', false, true, true, NULL, false, '2025-11-28 15:38:58.545812', '2025-11-28 15:38:58.545812', '2025-11-28 17:01:33.840063');


--
-- TOC entry 5025 (class 0 OID 16770)
-- Dependencies: 218
-- Data for Name: usuarios; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.usuarios VALUES (1, 'admin', 'admin123', 'Carlos', 'Administrador', 'admin@minimarket.com', 'ADMINISTRADOR', true, '2025-11-26 13:58:09.483174', NULL, '2025-11-26 13:58:09.483174', '2025-11-26 13:58:09.483174');
INSERT INTO public.usuarios VALUES (2, 'supervisor', 'super123', 'Juan', 'López', 'supervisor@minimarket.com', 'SUPERVISOR', true, '2025-11-26 13:58:09.483174', NULL, '2025-11-26 13:58:09.483174', '2025-11-26 13:58:09.483174');
INSERT INTO public.usuarios VALUES (4, 'cajera2', 'cajera456', 'Ana', 'López', 'ana@minimarket.com', 'CAJERO', true, '2025-11-26 13:58:09.483174', NULL, '2025-11-26 13:58:09.483174', '2025-11-26 19:20:33.409506');
INSERT INTO public.usuarios VALUES (3, 'cajera1', '1234', 'María', 'García', 'maria@minimarket.com', 'CAJERO', true, '2025-11-26 13:58:09.483174', NULL, '2025-11-26 13:58:09.483174', '2025-11-27 18:54:59.458868');


--
-- TOC entry 5031 (class 0 OID 16827)
-- Dependencies: 224
-- Data for Name: ventas; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.ventas VALUES (14, 'V-20251127-001', '2025-11-27 19:55:51.983448', 4, 'EFECTIVO', 6.44, 1.16, 7.60, NULL, 'ACTIVA', NULL, NULL, NULL, '2025-11-27 19:55:51.983448');
INSERT INTO public.ventas VALUES (15, 'V-20251127-002', '2025-11-27 17:55:51.983448', 4, 'YAPE', 11.02, 1.98, 13.00, NULL, 'ACTIVA', NULL, NULL, NULL, '2025-11-27 19:55:51.983448');
INSERT INTO public.ventas VALUES (16, 'V-20251127-003', '2025-11-27 15:55:51.983448', 4, 'EFECTIVO', 18.98, 3.42, 22.40, NULL, 'ACTIVA', NULL, NULL, NULL, '2025-11-27 19:55:51.983448');
INSERT INTO public.ventas VALUES (17, 'V-20251127-004', '2025-11-27 18:55:51.983448', 4, 'TARJETA_DEBITO', 14.66, 2.64, 17.30, NULL, 'ACTIVA', NULL, NULL, NULL, '2025-11-27 19:55:51.983448');
INSERT INTO public.ventas VALUES (18, 'V-20251127-006', '2025-11-27 16:55:51.983448', 4, 'EFECTIVO', 7.29, 1.31, 8.60, NULL, 'ACTIVA', NULL, NULL, NULL, '2025-11-27 19:55:51.983448');
INSERT INTO public.ventas VALUES (19, 'V-20251127-007', '2025-11-27 14:55:51.983448', 4, 'YAPE', 21.19, 3.81, 25.10, NULL, 'ACTIVA', NULL, NULL, NULL, '2025-11-27 19:55:51.983448');
INSERT INTO public.ventas VALUES (20, 'VTA-20251128-7892', '2025-11-28 01:17:27.89342', 1, 'EFECTIVO', 10.68, 1.92, 12.60, NULL, 'ACTIVA', NULL, NULL, NULL, '2025-11-28 01:17:27.89342');
INSERT INTO public.ventas VALUES (21, 'VTA-20251128-1437', '2025-11-28 01:25:11.43779', 1, 'EFECTIVO', 7.12, 1.28, 8.40, NULL, 'ACTIVA', NULL, NULL, NULL, '2025-11-28 01:25:11.43779');
INSERT INTO public.ventas VALUES (22, 'VTA-20251128-4964', '2025-11-28 13:19:35.094464', 3, 'EFECTIVO', 23.20, 4.18, 27.38, 'BOLETA - Cliente: Consumidor Final - Doc: -', 'ACTIVA', NULL, NULL, NULL, '2025-11-28 13:19:35.094464');
INSERT INTO public.ventas VALUES (23, 'VTA-20251128-3220', '2025-11-28 14:01:03.340696', 1, 'YAPE', 16.80, 3.02, 19.82, 'BOLETA - Cliente: Consumidor Final - Doc: -', 'ACTIVA', NULL, NULL, NULL, '2025-11-28 14:01:03.340696');


--
-- TOC entry 5053 (class 0 OID 0)
-- Dependencies: 233
-- Name: auditoria_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.auditoria_log_id_seq', 135, true);


--
-- TOC entry 5054 (class 0 OID 0)
-- Dependencies: 219
-- Name: categorias_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.categorias_id_seq', 7, true);


--
-- TOC entry 5055 (class 0 OID 0)
-- Dependencies: 225
-- Name: detalle_ventas_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.detalle_ventas_id_seq', 34, true);


--
-- TOC entry 5056 (class 0 OID 0)
-- Dependencies: 227
-- Name: logs_sistema_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.logs_sistema_id_seq', 2, true);


--
-- TOC entry 5057 (class 0 OID 0)
-- Dependencies: 221
-- Name: productos_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.productos_id_seq', 70, true);


--
-- TOC entry 5058 (class 0 OID 0)
-- Dependencies: 217
-- Name: usuarios_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.usuarios_id_seq', 4, true);


--
-- TOC entry 5059 (class 0 OID 0)
-- Dependencies: 223
-- Name: ventas_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.ventas_id_seq', 23, true);


--
-- TOC entry 4868 (class 2606 OID 17055)
-- Name: auditoria_log auditoria_log_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auditoria_log
    ADD CONSTRAINT auditoria_log_pkey PRIMARY KEY (id);


--
-- TOC entry 4841 (class 2606 OID 16799)
-- Name: categorias categorias_nombre_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categorias
    ADD CONSTRAINT categorias_nombre_key UNIQUE (nombre);


--
-- TOC entry 4843 (class 2606 OID 16797)
-- Name: categorias categorias_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categorias
    ADD CONSTRAINT categorias_pkey PRIMARY KEY (id);


--
-- TOC entry 4862 (class 2606 OID 16860)
-- Name: detalle_ventas detalle_ventas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_ventas
    ADD CONSTRAINT detalle_ventas_pkey PRIMARY KEY (id);


--
-- TOC entry 4866 (class 2606 OID 16945)
-- Name: logs_sistema logs_sistema_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.logs_sistema
    ADD CONSTRAINT logs_sistema_pkey PRIMARY KEY (id);


--
-- TOC entry 4850 (class 2606 OID 16820)
-- Name: productos productos_codigo_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.productos
    ADD CONSTRAINT productos_codigo_key UNIQUE (codigo);


--
-- TOC entry 4852 (class 2606 OID 16818)
-- Name: productos productos_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.productos
    ADD CONSTRAINT productos_pkey PRIMARY KEY (id);


--
-- TOC entry 4835 (class 2606 OID 16786)
-- Name: usuarios usuarios_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_email_key UNIQUE (email);


--
-- TOC entry 4837 (class 2606 OID 16782)
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);


--
-- TOC entry 4839 (class 2606 OID 16784)
-- Name: usuarios usuarios_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_username_key UNIQUE (username);


--
-- TOC entry 4858 (class 2606 OID 16844)
-- Name: ventas ventas_numero_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ventas
    ADD CONSTRAINT ventas_numero_key UNIQUE (numero);


--
-- TOC entry 4860 (class 2606 OID 16842)
-- Name: ventas ventas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ventas
    ADD CONSTRAINT ventas_pkey PRIMARY KEY (id);


--
-- TOC entry 4863 (class 1259 OID 16959)
-- Name: idx_detalle_producto_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_detalle_producto_id ON public.detalle_ventas USING btree (producto_id);


--
-- TOC entry 4864 (class 1259 OID 16958)
-- Name: idx_detalle_venta_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_detalle_venta_id ON public.detalle_ventas USING btree (venta_id);


--
-- TOC entry 4844 (class 1259 OID 16951)
-- Name: idx_productos_categoria; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_productos_categoria ON public.productos USING btree (categoria_id);


--
-- TOC entry 4845 (class 1259 OID 16949)
-- Name: idx_productos_codigo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_productos_codigo ON public.productos USING btree (codigo);


--
-- TOC entry 4846 (class 1259 OID 16950)
-- Name: idx_productos_nombre; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_productos_nombre ON public.productos USING btree (nombre);


--
-- TOC entry 4847 (class 1259 OID 16952)
-- Name: idx_productos_stock_bajo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_productos_stock_bajo ON public.productos USING btree (stock) WHERE (stock <= 10);


--
-- TOC entry 4848 (class 1259 OID 16953)
-- Name: idx_productos_vencimiento; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_productos_vencimiento ON public.productos USING btree (fecha_vencimiento) WHERE (fecha_vencimiento IS NOT NULL);


--
-- TOC entry 4831 (class 1259 OID 16947)
-- Name: idx_usuarios_email; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usuarios_email ON public.usuarios USING btree (email);


--
-- TOC entry 4832 (class 1259 OID 16948)
-- Name: idx_usuarios_rol; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usuarios_rol ON public.usuarios USING btree (rol);


--
-- TOC entry 4833 (class 1259 OID 16946)
-- Name: idx_usuarios_username; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_usuarios_username ON public.usuarios USING btree (username);


--
-- TOC entry 4853 (class 1259 OID 16956)
-- Name: idx_ventas_cajera; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ventas_cajera ON public.ventas USING btree (cajera_id);


--
-- TOC entry 4854 (class 1259 OID 16957)
-- Name: idx_ventas_estado; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ventas_estado ON public.ventas USING btree (estado);


--
-- TOC entry 4855 (class 1259 OID 16955)
-- Name: idx_ventas_fecha; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ventas_fecha ON public.ventas USING btree (fecha_hora);


--
-- TOC entry 4856 (class 1259 OID 16954)
-- Name: idx_ventas_numero; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_ventas_numero ON public.ventas USING btree (numero);


--
-- TOC entry 4874 (class 2620 OID 16968)
-- Name: productos trigger_productos_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_productos_updated_at BEFORE UPDATE ON public.productos FOR EACH ROW EXECUTE FUNCTION public.actualizar_timestamp();


--
-- TOC entry 4873 (class 2620 OID 16967)
-- Name: usuarios trigger_usuarios_updated_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_usuarios_updated_at BEFORE UPDATE ON public.usuarios FOR EACH ROW EXECUTE FUNCTION public.actualizar_timestamp();


--
-- TOC entry 4871 (class 2606 OID 16866)
-- Name: detalle_ventas detalle_ventas_producto_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_ventas
    ADD CONSTRAINT detalle_ventas_producto_id_fkey FOREIGN KEY (producto_id) REFERENCES public.productos(id);


--
-- TOC entry 4872 (class 2606 OID 16861)
-- Name: detalle_ventas detalle_ventas_venta_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.detalle_ventas
    ADD CONSTRAINT detalle_ventas_venta_id_fkey FOREIGN KEY (venta_id) REFERENCES public.ventas(id) ON DELETE CASCADE;


--
-- TOC entry 4869 (class 2606 OID 16821)
-- Name: productos productos_categoria_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.productos
    ADD CONSTRAINT productos_categoria_id_fkey FOREIGN KEY (categoria_id) REFERENCES public.categorias(id);


--
-- TOC entry 4870 (class 2606 OID 16845)
-- Name: ventas ventas_cajera_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ventas
    ADD CONSTRAINT ventas_cajera_id_fkey FOREIGN KEY (cajera_id) REFERENCES public.usuarios(id);


-- Completed on 2025-11-28 17:06:53

--
-- PostgreSQL database dump complete
--

