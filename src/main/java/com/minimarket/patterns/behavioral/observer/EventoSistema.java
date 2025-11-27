package com.minimarket.patterns.behavioral.observer;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Clase que representa un evento del sistema
 * Contiene información sobre qué ocurrió y datos relevantes
 */
public class EventoSistema {
    
    private TipoEvento tipo;
    private String descripcion;
    private LocalDateTime fechaHora;
    private String usuarioOrigen;
    private Map<String, Object> datos;
    private NivelPrioridad prioridad;
    
    public enum TipoEvento {
        PRODUCTO_STOCK_BAJO,
        PRODUCTO_PROXIMO_VENCER,
        PRODUCTO_VENCIDO,
        PRODUCTO_SIN_LOTE,
        VENTA_REGISTRADA,
        VENTA_ANULADA,
        PRECIO_MODIFICADO,
        USUARIO_ACCESO_DENEGADO,
        ERROR_SISTEMA,
        BACKUP_REALIZADO,
        CONFIGURACION_CAMBIADA
    }
    
    public enum NivelPrioridad {
        BAJA(1, "Baja"),
        MEDIA(2, "Media"),
        ALTA(3, "Alta"),
        CRITICA(4, "Crítica");
        
        private final int nivel;
        private final String descripcion;
        
        NivelPrioridad(int nivel, String descripcion) {
            this.nivel = nivel;
            this.descripcion = descripcion;
        }
        
        public int getNivel() { return nivel; }
        public String getDescripcion() { return descripcion; }
    }
    
    public EventoSistema(TipoEvento tipo, String descripcion, String usuarioOrigen) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.usuarioOrigen = usuarioOrigen;
        this.fechaHora = LocalDateTime.now();
        this.datos = new HashMap<>();
        this.prioridad = determinarPrioridadPorTipo(tipo);
    }
    
    public EventoSistema(TipoEvento tipo, String descripcion, String usuarioOrigen, 
                        NivelPrioridad prioridad) {
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.usuarioOrigen = usuarioOrigen;
        this.fechaHora = LocalDateTime.now();
        this.datos = new HashMap<>();
        this.prioridad = prioridad;
    }
    
    /**
     * Agrega datos adicionales al evento
     */
    public EventoSistema conDato(String clave, Object valor) {
        datos.put(clave, valor);
        return this;
    }
    
    /**
     * Agrega múltiples datos al evento
     */
    public EventoSistema conDatos(Map<String, Object> nuevosDatos) {
        datos.putAll(nuevosDatos);
        return this;
    }
    
    /**
     * Determina la prioridad por defecto según el tipo de evento
     */
    private NivelPrioridad determinarPrioridadPorTipo(TipoEvento tipo) {
        switch (tipo) {
            case ERROR_SISTEMA:
            case PRODUCTO_VENCIDO:
                return NivelPrioridad.CRITICA;
                
            case USUARIO_ACCESO_DENEGADO:
            case PRODUCTO_PROXIMO_VENCER:
            case VENTA_ANULADA:
                return NivelPrioridad.ALTA;
                
            case PRODUCTO_STOCK_BAJO:
            case PRODUCTO_SIN_LOTE:
            case PRECIO_MODIFICADO:
                return NivelPrioridad.MEDIA;
                
            case VENTA_REGISTRADA:
            case BACKUP_REALIZADO:
            case CONFIGURACION_CAMBIADA:
            default:
                return NivelPrioridad.BAJA;
        }
    }
    
    /**
     * Verifica si el evento requiere acción inmediata
     */
    public boolean requiereAccionInmediata() {
        return prioridad == NivelPrioridad.CRITICA || prioridad == NivelPrioridad.ALTA;
    }
    
    /**
     * Obtiene un dato específico del evento
     */
    @SuppressWarnings("unchecked")
    public <T> T getDato(String clave, Class<T> tipo) {
        Object valor = datos.get(clave);
        if (valor != null && tipo.isInstance(valor)) {
            return (T) valor;
        }
        return null;
    }
    
    /**
     * Verifica si el evento contiene un dato específico
     */
    public boolean tieneDato(String clave) {
        return datos.containsKey(clave);
    }
    
    // Getters y Setters
    public TipoEvento getTipo() { return tipo; }
    public void setTipo(TipoEvento tipo) { this.tipo = tipo; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    
    public String getUsuarioOrigen() { return usuarioOrigen; }
    public void setUsuarioOrigen(String usuarioOrigen) { this.usuarioOrigen = usuarioOrigen; }
    
    public Map<String, Object> getDatos() { return new HashMap<>(datos); }
    public void setDatos(Map<String, Object> datos) { this.datos = new HashMap<>(datos); }
    
    public NivelPrioridad getPrioridad() { return prioridad; }
    public void setPrioridad(NivelPrioridad prioridad) { this.prioridad = prioridad; }
    
    @Override
    public String toString() {
        return String.format("EventoSistema{tipo=%s, descripcion='%s', prioridad=%s, fecha=%s, usuario='%s'}", 
                           tipo, descripcion, prioridad, fechaHora, usuarioOrigen);
    }
    
    /**
     * Métodos estáticos para crear eventos comunes
     */
    public static EventoSistema stockBajo(String nombreProducto, int stockActual, String usuario) {
        return new EventoSistema(TipoEvento.PRODUCTO_STOCK_BAJO, 
                               "Stock bajo detectado en producto: " + nombreProducto, usuario)
                .conDato("nombreProducto", nombreProducto)
                .conDato("stockActual", stockActual);
    }
    
    public static EventoSistema productoProximoVencer(String nombreProducto, 
                                                     java.time.LocalDate fechaVencimiento, String usuario) {
        return new EventoSistema(TipoEvento.PRODUCTO_PROXIMO_VENCER, 
                               "Producto próximo a vencer: " + nombreProducto, usuario)
                .conDato("nombreProducto", nombreProducto)
                .conDato("fechaVencimiento", fechaVencimiento);
    }
    
    public static EventoSistema ventaRegistrada(String numeroBoleta, 
                                              java.math.BigDecimal total, String cajera) {
        return new EventoSistema(TipoEvento.VENTA_REGISTRADA, 
                               "Venta registrada: " + numeroBoleta, cajera)
                .conDato("numeroBoleta", numeroBoleta)
                .conDato("total", total);
    }
    
    public static EventoSistema accesoDesautorizado(String usuario, String accionIntentada) {
        return new EventoSistema(TipoEvento.USUARIO_ACCESO_DENEGADO, 
                               "Acceso denegado para acción: " + accionIntentada, usuario)
                .conDato("accionIntentada", accionIntentada);
    }
}
