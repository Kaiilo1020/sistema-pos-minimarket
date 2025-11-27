package com.minimarket.patterns.behavioral.chain;

import com.minimarket.models.Usuario;
import com.minimarket.security.Rol;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Representa una solicitud de aprobación en el sistema
 * Contiene toda la información necesaria para el proceso de aprobación
 */
public class SolicitudAprobacion {
    
    private String id;
    private TipoSolicitud tipo;
    private Usuario usuarioSolicitante;
    private Rol nivelAprobacionRequerido;
    private String descripcion;
    private LocalDateTime fechaSolicitud;
    private Map<String, Object> parametros;
    private PrioridadSolicitud prioridad;
    
    public enum TipoSolicitud {
        MODIFICAR_PRECIO("Modificar precio de producto"),
        ANULAR_VENTA("Anular venta"),
        DESCUENTO_ESPECIAL("Aplicar descuento especial"),
        ELIMINAR_PRODUCTO("Eliminar producto del inventario"),
        CREAR_USUARIO("Crear nuevo usuario"),
        MODIFICAR_CONFIGURACION("Modificar configuración del sistema"),
        ACCESO_REPORTES("Acceso a reportes avanzados"),
        VENTA_SIN_LOTE("Vender producto sin lote"),
        OVERRIDE_PRECIO("Sobrescribir precio en venta"),
        BACKUP_SISTEMA("Realizar backup del sistema");
        
        private final String descripcion;
        
        TipoSolicitud(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() { return descripcion; }
    }
    
    public enum PrioridadSolicitud {
        BAJA(1, "Baja"),
        NORMAL(2, "Normal"),
        ALTA(3, "Alta"),
        URGENTE(4, "Urgente");
        
        private final int nivel;
        private final String descripcion;
        
        PrioridadSolicitud(int nivel, String descripcion) {
            this.nivel = nivel;
            this.descripcion = descripcion;
        }
        
        public int getNivel() { return nivel; }
        public String getDescripcion() { return descripcion; }
    }
    
    public SolicitudAprobacion(String id, TipoSolicitud tipo, Usuario usuarioSolicitante, 
                              String descripcion) {
        this.id = id;
        this.tipo = tipo;
        this.usuarioSolicitante = usuarioSolicitante;
        this.descripcion = descripcion;
        this.fechaSolicitud = LocalDateTime.now();
        this.parametros = new HashMap<>();
        this.prioridad = PrioridadSolicitud.NORMAL;
        this.nivelAprobacionRequerido = determinarNivelRequerido(tipo);
    }
    
    /**
     * Determina el nivel de aprobación requerido según el tipo de solicitud
     */
    private Rol determinarNivelRequerido(TipoSolicitud tipo) {
        switch (tipo) {
            case CREAR_USUARIO:
            case MODIFICAR_CONFIGURACION:
            case BACKUP_SISTEMA:
                return Rol.ADMINISTRADOR;
                
            case ANULAR_VENTA:
            case DESCUENTO_ESPECIAL:
            case ELIMINAR_PRODUCTO:
            case ACCESO_REPORTES:
                return Rol.SUPERVISOR;
                
            case MODIFICAR_PRECIO:
            case VENTA_SIN_LOTE:
            case OVERRIDE_PRECIO:
            default:
                return Rol.CAJERO;
        }
    }
    
    /**
     * Agrega un parámetro a la solicitud
     */
    public SolicitudAprobacion conParametro(String clave, Object valor) {
        parametros.put(clave, valor);
        return this;
    }
    
    /**
     * Establece la prioridad de la solicitud
     */
    public SolicitudAprobacion conPrioridad(PrioridadSolicitud prioridad) {
        this.prioridad = prioridad;
        return this;
    }
    
    /**
     * Obtiene un parámetro específico
     */
    @SuppressWarnings("unchecked")
    public <T> T getParametro(String clave, Class<T> tipo) {
        Object valor = parametros.get(clave);
        if (valor != null && tipo.isInstance(valor)) {
            return (T) valor;
        }
        return null;
    }
    
    /**
     * Verifica si la solicitud tiene un parámetro específico
     */
    public boolean tieneParametro(String clave) {
        return parametros.containsKey(clave);
    }
    
    /**
     * Verifica si la solicitud es urgente
     */
    public boolean esUrgente() {
        return prioridad == PrioridadSolicitud.URGENTE;
    }
    
    /**
     * Verifica si la solicitud requiere un nivel específico de aprobación
     */
    public boolean requiereNivel(Rol nivel) {
        return nivelAprobacionRequerido.getNivel() >= nivel.getNivel();
    }
    
    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public TipoSolicitud getTipo() { return tipo; }
    public void setTipo(TipoSolicitud tipo) { this.tipo = tipo; }
    
    public Usuario getUsuarioSolicitante() { return usuarioSolicitante; }
    public void setUsuarioSolicitante(Usuario usuarioSolicitante) { 
        this.usuarioSolicitante = usuarioSolicitante; 
    }
    
    public Rol getNivelAprobacionRequerido() { return nivelAprobacionRequerido; }
    public void setNivelAprobacionRequerido(Rol nivelAprobacionRequerido) { 
        this.nivelAprobacionRequerido = nivelAprobacionRequerido; 
    }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { 
        this.fechaSolicitud = fechaSolicitud; 
    }
    
    public Map<String, Object> getParametros() { return new HashMap<>(parametros); }
    public void setParametros(Map<String, Object> parametros) { 
        this.parametros = new HashMap<>(parametros); 
    }
    
    public PrioridadSolicitud getPrioridad() { return prioridad; }
    public void setPrioridad(PrioridadSolicitud prioridad) { this.prioridad = prioridad; }
    
    @Override
    public String toString() {
        return String.format("SolicitudAprobacion{id='%s', tipo=%s, usuario='%s', prioridad=%s, fecha=%s}", 
                           id, tipo, usuarioSolicitante.getUsername(), prioridad, fechaSolicitud);
    }
    
    /**
     * Métodos estáticos para crear solicitudes comunes
     */
    public static SolicitudAprobacion modificarPrecio(Usuario usuario, String productoId, 
                                                     java.math.BigDecimal nuevoPrecio) {
        return new SolicitudAprobacion("MOD_PRECIO_" + System.currentTimeMillis(), 
                                     TipoSolicitud.MODIFICAR_PRECIO, usuario,
                                     "Modificar precio del producto " + productoId)
                .conParametro("productoId", productoId)
                .conParametro("nuevoPrecio", nuevoPrecio);
    }
    
    public static SolicitudAprobacion anularVenta(Usuario usuario, String numeroBoleta, String motivo) {
        return new SolicitudAprobacion("ANULAR_" + System.currentTimeMillis(), 
                                     TipoSolicitud.ANULAR_VENTA, usuario,
                                     "Anular venta " + numeroBoleta)
                .conParametro("numeroBoleta", numeroBoleta)
                .conParametro("motivo", motivo)
                .conPrioridad(PrioridadSolicitud.ALTA);
    }
    
    public static SolicitudAprobacion ventaSinLote(Usuario usuario, String productoId) {
        return new SolicitudAprobacion("VENTA_SIN_LOTE_" + System.currentTimeMillis(), 
                                     TipoSolicitud.VENTA_SIN_LOTE, usuario,
                                     "Vender producto sin lote: " + productoId)
                .conParametro("productoId", productoId)
                .conPrioridad(PrioridadSolicitud.ALTA);
    }
    
    public static SolicitudAprobacion crearUsuario(Usuario solicitante, String nuevoUsername, 
                                                  Rol nuevoRol) {
        return new SolicitudAprobacion("CREAR_USER_" + System.currentTimeMillis(), 
                                     TipoSolicitud.CREAR_USUARIO, solicitante,
                                     "Crear usuario " + nuevoUsername + " con rol " + nuevoRol.getDescripcion())
                .conParametro("nuevoUsername", nuevoUsername)
                .conParametro("nuevoRol", nuevoRol);
    }
}
