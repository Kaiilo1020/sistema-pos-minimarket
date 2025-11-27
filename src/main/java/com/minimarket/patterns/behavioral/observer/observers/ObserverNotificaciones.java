package com.minimarket.patterns.behavioral.observer.observers;

import com.minimarket.patterns.behavioral.observer.*;
import com.minimarket.patterns.structural.decorator.*;

/**
 * Observer que maneja notificaciones del sistema usando decoradores
 * Convierte eventos en notificaciones visuales y auditivas
 */
public class ObserverNotificaciones implements Observer {
    
    private String id;
    private String descripcion;
    private boolean activo;
    private boolean sonidoHabilitado;
    private boolean emailHabilitado;
    private boolean logHabilitado;
    
    public ObserverNotificaciones(String id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
        this.activo = true;
        this.sonidoHabilitado = true;
        this.emailHabilitado = false;
        this.logHabilitado = true;
    }
    
    @Override
    public void actualizar(EventoSistema evento) {
        if (!activo) {
            return;
        }
        
        // Crear notificación base según el tipo de evento
        NotificacionBase notificacion = crearNotificacionBase(evento);
        
        // Aplicar decoradores según la configuración y prioridad
        notificacion = aplicarDecoradores(notificacion, evento);
        
        // Mostrar la notificación final
        notificacion.mostrar();
    }
    
    /**
     * Crea la notificación base según el tipo de evento
     */
    private NotificacionBase crearNotificacionBase(EventoSistema evento) {
        NotificacionBase.TipoNotificacion tipo = mapearTipoNotificacion(evento);
        String mensaje = construirMensaje(evento);
        
        return new NotificacionSimple(mensaje, tipo);
    }
    
    /**
     * Mapea el tipo de evento a tipo de notificación
     */
    private NotificacionBase.TipoNotificacion mapearTipoNotificacion(EventoSistema evento) {
        switch (evento.getTipo()) {
            case ERROR_SISTEMA:
            case PRODUCTO_VENCIDO:
                return NotificacionBase.TipoNotificacion.ERROR;
                
            case USUARIO_ACCESO_DENEGADO:
            case PRODUCTO_PROXIMO_VENCER:
                return NotificacionBase.TipoNotificacion.ALERTA;
                
            case PRODUCTO_STOCK_BAJO:
            case PRODUCTO_SIN_LOTE:
            case VENTA_ANULADA:
                return NotificacionBase.TipoNotificacion.ADVERTENCIA;
                
            case VENTA_REGISTRADA:
            case BACKUP_REALIZADO:
                return NotificacionBase.TipoNotificacion.EXITO;
                
            case PRECIO_MODIFICADO:
            case CONFIGURACION_CAMBIADA:
            default:
                return NotificacionBase.TipoNotificacion.INFO;
        }
    }
    
    /**
     * Construye el mensaje de la notificación
     */
    private String construirMensaje(EventoSistema evento) {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append(evento.getDescripcion());
        
        // Agregar información adicional según el tipo de evento
        switch (evento.getTipo()) {
            case PRODUCTO_STOCK_BAJO:
                Integer stockActual = evento.getDato("stockActual", Integer.class);
                if (stockActual != null) {
                    mensaje.append(" (Stock actual: ").append(stockActual).append(" unidades)");
                }
                break;
                
            case PRODUCTO_PROXIMO_VENCER:
                java.time.LocalDate fechaVencimiento = evento.getDato("fechaVencimiento", java.time.LocalDate.class);
                if (fechaVencimiento != null) {
                    mensaje.append(" (Vence: ").append(fechaVencimiento).append(")");
                }
                break;
                
            case VENTA_REGISTRADA:
                java.math.BigDecimal total = evento.getDato("total", java.math.BigDecimal.class);
                if (total != null) {
                    mensaje.append(" (Total: S/ ").append(total).append(")");
                }
                break;
        }
        
        // Agregar información del usuario si está disponible
        if (evento.getUsuarioOrigen() != null) {
            mensaje.append(" - Usuario: ").append(evento.getUsuarioOrigen());
        }
        
        return mensaje.toString();
    }
    
    /**
     * Aplica decoradores según la configuración y prioridad del evento
     */
    private NotificacionBase aplicarDecoradores(NotificacionBase notificacion, EventoSistema evento) {
        NotificacionBase resultado = notificacion;
        
        // Aplicar decorador de log si está habilitado
        if (logHabilitado) {
            resultado = aplicarDecoradorLog(resultado, evento);
        }
        
        // Aplicar decorador de sonido para eventos importantes
        if (sonidoHabilitado && evento.requiereAccionInmediata()) {
            resultado = new DecoradorSonido(resultado);
        }
        
        // Aplicar decorador de email para eventos críticos
        if (emailHabilitado && evento.getPrioridad() == EventoSistema.NivelPrioridad.CRITICA) {
            DecoradorEmail decoradorEmail = new DecoradorEmail(resultado, "supervisor@minimarket.com");
            decoradorEmail.configurarDestinatariosAutomaticos();
            resultado = decoradorEmail;
        }
        
        return resultado;
    }
    
    /**
     * Aplica el decorador de log apropiado según el tipo de evento
     */
    private NotificacionBase aplicarDecoradorLog(NotificacionBase notificacion, EventoSistema evento) {
        switch (evento.getTipo()) {
            case ERROR_SISTEMA:
                return DecoradorLog.crearLogError(notificacion);
                
            case VENTA_REGISTRADA:
            case VENTA_ANULADA:
                return DecoradorLog.crearLogVentas(notificacion);
                
            case PRODUCTO_STOCK_BAJO:
            case PRODUCTO_PROXIMO_VENCER:
            case PRODUCTO_VENCIDO:
            case PRODUCTO_SIN_LOTE:
                return DecoradorLog.crearLogInventario(notificacion);
                
            case USUARIO_ACCESO_DENEGADO:
                return DecoradorLog.crearLogSeguridad(notificacion);
                
            default:
                return new DecoradorLog(notificacion);
        }
    }
    
    /**
     * Configura qué decoradores están habilitados
     */
    public void configurarDecoradores(boolean sonido, boolean email, boolean log) {
        this.sonidoHabilitado = sonido;
        this.emailHabilitado = email;
        this.logHabilitado = log;
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getDescripcion() {
        return descripcion;
    }
    
    @Override
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    public boolean isSonidoHabilitado() {
        return sonidoHabilitado;
    }
    
    public void setSonidoHabilitado(boolean sonidoHabilitado) {
        this.sonidoHabilitado = sonidoHabilitado;
    }
    
    public boolean isEmailHabilitado() {
        return emailHabilitado;
    }
    
    public void setEmailHabilitado(boolean emailHabilitado) {
        this.emailHabilitado = emailHabilitado;
    }
    
    public boolean isLogHabilitado() {
        return logHabilitado;
    }
    
    public void setLogHabilitado(boolean logHabilitado) {
        this.logHabilitado = logHabilitado;
    }
    
    @Override
    public String toString() {
        return String.format("ObserverNotificaciones{id='%s', descripcion='%s', activo=%s}", 
                           id, descripcion, activo);
    }
}
