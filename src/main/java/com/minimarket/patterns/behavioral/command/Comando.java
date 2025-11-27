package com.minimarket.patterns.behavioral.command;

import java.time.LocalDateTime;

/**
 * Interfaz Command para operaciones del sistema
 * Permite encapsular operaciones como objetos, facilitando undo/redo y auditoría
 */
public interface Comando {
    
    /**
     * Ejecuta el comando
     */
    boolean ejecutar();
    
    /**
     * Deshace el comando (si es posible)
     */
    boolean deshacer();
    
    /**
     * Verifica si el comando puede deshacerse
     */
    boolean puedeDeshacer();
    
    /**
     * Obtiene la descripción del comando
     */
    String getDescripcion();
    
    /**
     * Obtiene el usuario que ejecutó el comando
     */
    String getUsuarioEjecutor();
    
    /**
     * Obtiene la fecha y hora de ejecución
     */
    LocalDateTime getFechaEjecucion();
    
    /**
     * Obtiene el resultado de la ejecución
     */
    String getResultado();
}
