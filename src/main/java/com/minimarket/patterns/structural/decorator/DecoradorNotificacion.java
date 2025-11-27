package com.minimarket.patterns.structural.decorator;

/**
 * Decorador base para notificaciones
 * Permite agregar funcionalidades adicionales a las notificaciones
 */
public abstract class DecoradorNotificacion implements NotificacionBase {
    
    protected NotificacionBase notificacion;
    
    public DecoradorNotificacion(NotificacionBase notificacion) {
        this.notificacion = notificacion;
    }
    
    @Override
    public void mostrar() {
        notificacion.mostrar();
    }
    
    @Override
    public String getContenido() {
        return notificacion.getContenido();
    }
    
    @Override
    public TipoNotificacion getTipo() {
        return notificacion.getTipo();
    }
}
