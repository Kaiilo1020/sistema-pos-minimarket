package com.minimarket.patterns.behavioral.command;

import java.time.LocalDateTime;

/**
 * Clase base abstracta para comandos
 * Proporciona funcionalidad común para todos los comandos
 */
public abstract class ComandoBase implements Comando {
    
    protected String descripcion;
    protected String usuarioEjecutor;
    protected LocalDateTime fechaEjecucion;
    protected String resultado;
    protected boolean ejecutado;
    protected boolean exitoso;
    
    public ComandoBase(String descripcion, String usuarioEjecutor) {
        this.descripcion = descripcion;
        this.usuarioEjecutor = usuarioEjecutor;
        this.ejecutado = false;
        this.exitoso = false;
    }
    
    @Override
    public final boolean ejecutar() {
        if (ejecutado) {
            resultado = "Comando ya fue ejecutado anteriormente";
            return false;
        }
        
        fechaEjecucion = LocalDateTime.now();
        
        try {
            exitoso = ejecutarComando();
            ejecutado = true;
            
            if (exitoso) {
                resultado = "Comando ejecutado exitosamente";
            } else {
                resultado = "Comando falló durante la ejecución";
            }
            
            return exitoso;
            
        } catch (Exception e) {
            ejecutado = true;
            exitoso = false;
            resultado = "Error durante la ejecución: " + e.getMessage();
            return false;
        }
    }
    
    @Override
    public final boolean deshacer() {
        if (!ejecutado) {
            return false;
        }
        
        if (!puedeDeshacer()) {
            return false;
        }
        
        try {
            boolean deshecho = deshacerComando();
            if (deshecho) {
                ejecutado = false;
                exitoso = false;
                resultado = "Comando deshecho exitosamente";
            }
            return deshecho;
            
        } catch (Exception e) {
            resultado = "Error al deshacer comando: " + e.getMessage();
            return false;
        }
    }
    
    /**
     * Método abstracto que debe implementar cada comando específico
     */
    protected abstract boolean ejecutarComando();
    
    /**
     * Método abstracto para deshacer el comando
     */
    protected abstract boolean deshacerComando();
    
    @Override
    public boolean puedeDeshacer() {
        return ejecutado && exitoso;
    }
    
    @Override
    public String getDescripcion() {
        return descripcion;
    }
    
    @Override
    public String getUsuarioEjecutor() {
        return usuarioEjecutor;
    }
    
    @Override
    public LocalDateTime getFechaEjecucion() {
        return fechaEjecucion;
    }
    
    @Override
    public String getResultado() {
        return resultado;
    }
    
    public boolean isEjecutado() {
        return ejecutado;
    }
    
    public boolean isExitoso() {
        return exitoso;
    }
    
    @Override
    public String toString() {
        return String.format("Comando{descripcion='%s', usuario='%s', fecha=%s, exitoso=%s}", 
                           descripcion, usuarioEjecutor, fechaEjecucion, exitoso);
    }
}
