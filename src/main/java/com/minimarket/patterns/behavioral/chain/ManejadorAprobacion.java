package com.minimarket.patterns.behavioral.chain;

import com.minimarket.models.Usuario;
import com.minimarket.security.Rol;

/**
 * Clase abstracta para el patrón Chain of Responsibility
 * Define la estructura para manejar aprobaciones según jerarquía
 */
public abstract class ManejadorAprobacion {
    
    protected ManejadorAprobacion siguienteManejador;
    protected Rol nivelRequerido;
    
    public ManejadorAprobacion(Rol nivelRequerido) {
        this.nivelRequerido = nivelRequerido;
    }
    
    /**
     * Establece el siguiente manejador en la cadena
     */
    public ManejadorAprobacion setSiguiente(ManejadorAprobacion siguiente) {
        this.siguienteManejador = siguiente;
        return siguiente;
    }
    
    /**
     * Procesa la solicitud de aprobación
     */
    public ResultadoAprobacion manejarSolicitud(SolicitudAprobacion solicitud) {
        // Verificar si este manejador puede procesar la solicitud
        if (puedeAprobar(solicitud)) {
            return procesarAprobacion(solicitud);
        }
        
        // Si no puede, pasar al siguiente en la cadena
        if (siguienteManejador != null) {
            return siguienteManejador.manejarSolicitud(solicitud);
        }
        
        // Si no hay más manejadores, rechazar
        return ResultadoAprobacion.rechazado(
            "No hay autoridad suficiente para aprobar esta solicitud",
            "AUTORIDAD_INSUFICIENTE"
        );
    }
    
    /**
     * Verifica si este manejador puede aprobar la solicitud
     */
    protected boolean puedeAprobar(SolicitudAprobacion solicitud) {
        // Verificar nivel de autoridad
        if (solicitud.getUsuarioSolicitante().getRol().getNivel() > nivelRequerido.getNivel()) {
            return false; // El usuario no tiene suficiente nivel
        }
        
        // Verificar si el tipo de solicitud requiere este nivel
        return solicitud.getNivelAprobacionRequerido().getNivel() <= nivelRequerido.getNivel();
    }
    
    /**
     * Procesa la aprobación específica (implementado por cada manejador)
     */
    protected abstract ResultadoAprobacion procesarAprobacion(SolicitudAprobacion solicitud);
    
    /**
     * Obtiene el nombre del rol del manejador
     */
    public String getNombreRol() {
        return nivelRequerido.getDescripcion();
    }
    
    /**
     * Obtiene el nivel de autoridad del manejador
     */
    public Rol getNivelRequerido() {
        return nivelRequerido;
    }
}
