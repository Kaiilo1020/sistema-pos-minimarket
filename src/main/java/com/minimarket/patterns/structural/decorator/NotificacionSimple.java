package com.minimarket.patterns.structural.decorator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementación básica de notificación
 * Componente concreto que será decorado
 */
public class NotificacionSimple implements NotificacionBase {
    
    private String mensaje;
    private TipoNotificacion tipo;
    private LocalDateTime fechaHora;
    
    public NotificacionSimple(String mensaje, TipoNotificacion tipo) {
        this.mensaje = mensaje;
        this.tipo = tipo;
        this.fechaHora = LocalDateTime.now();
    }
    
    @Override
    public void mostrar() {
        System.out.println(formatearNotificacion());
    }
    
    @Override
    public String getContenido() {
        return mensaje;
    }
    
    @Override
    public TipoNotificacion getTipo() {
        return tipo;
    }
    
    /**
     * Formatea la notificación básica
     */
    private String formatearNotificacion() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return String.format("[%s] %s %s: %s", 
                           fechaHora.format(formatter),
                           tipo.getIcono(),
                           tipo.getDescripcion().toUpperCase(),
                           mensaje);
    }
    
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
}
