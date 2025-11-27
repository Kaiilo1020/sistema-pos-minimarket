package com.minimarket.patterns.behavioral.observer;

/**
 * Interfaz Observer para el patrón Observer
 * Permite a los objetos recibir notificaciones de cambios en el sistema
 */
public interface Observer {
    
    /**
     * Método llamado cuando ocurre un evento en el sujeto observado
     * @param evento El evento que ocurrió
     */
    void actualizar(EventoSistema evento);
    
    /**
     * Obtiene el identificador único del observer
     */
    String getId();
    
    /**
     * Obtiene la descripción del observer
     */
    String getDescripcion();
    
    /**
     * Verifica si el observer está activo
     */
    boolean isActivo();
}
