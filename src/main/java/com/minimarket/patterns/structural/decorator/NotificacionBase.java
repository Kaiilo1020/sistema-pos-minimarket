package com.minimarket.patterns.structural.decorator;

/**
 * Interfaz base para notificaciones
 * Permite decorar notificaciones con funcionalidades adicionales
 */
public interface NotificacionBase {
    
    /**
     * Muestra la notificación
     */
    void mostrar();
    
    /**
     * Obtiene el contenido de la notificación
     */
    String getContenido();
    
    /**
     * Obtiene el tipo de notificación
     */
    TipoNotificacion getTipo();
    
    /**
     * Enum para tipos de notificación
     */
    enum TipoNotificacion {
        INFO("ℹ️", "Información"),
        ADVERTENCIA("⚠️", "Advertencia"),
        ERROR("❌", "Error"),
        EXITO("✅", "Éxito"),
        ALERTA("🚨", "Alerta Crítica");
        
        private final String icono;
        private final String descripcion;
        
        TipoNotificacion(String icono, String descripcion) {
            this.icono = icono;
            this.descripcion = descripcion;
        }
        
        public String getIcono() { return icono; }
        public String getDescripcion() { return descripcion; }
    }
}
