package com.minimarket.patterns.behavioral.chain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa el resultado de un proceso de aprobación
 * Contiene información sobre si fue aprobado, rechazado o requiere más información
 */
public class ResultadoAprobacion {
    
    private EstadoAprobacion estado;
    private String mensaje;
    private String codigoRespuesta;
    private LocalDateTime fechaRespuesta;
    private String aprobadoPor;
    private List<String> observaciones;
    private Object datosAdicionales;
    
    public enum EstadoAprobacion {
        APROBADO("Aprobado"),
        RECHAZADO("Rechazado"),
        PENDIENTE("Pendiente"),
        REQUIERE_INFO("Requiere más información"),
        ESCALADO("Escalado a nivel superior");
        
        private final String descripcion;
        
        EstadoAprobacion(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() { return descripcion; }
    }
    
    private ResultadoAprobacion(EstadoAprobacion estado, String mensaje, String codigoRespuesta) {
        this.estado = estado;
        this.mensaje = mensaje;
        this.codigoRespuesta = codigoRespuesta;
        this.fechaRespuesta = LocalDateTime.now();
        this.observaciones = new ArrayList<>();
    }
    
    /**
     * Crea un resultado aprobado
     */
    public static ResultadoAprobacion aprobado(String mensaje, String aprobadoPor) {
        ResultadoAprobacion resultado = new ResultadoAprobacion(
            EstadoAprobacion.APROBADO, mensaje, "APPROVED");
        resultado.aprobadoPor = aprobadoPor;
        return resultado;
    }
    
    /**
     * Crea un resultado rechazado
     */
    public static ResultadoAprobacion rechazado(String mensaje, String codigoRechazo) {
        return new ResultadoAprobacion(EstadoAprobacion.RECHAZADO, mensaje, codigoRechazo);
    }
    
    /**
     * Crea un resultado pendiente
     */
    public static ResultadoAprobacion pendiente(String mensaje) {
        return new ResultadoAprobacion(EstadoAprobacion.PENDIENTE, mensaje, "PENDING");
    }
    
    /**
     * Crea un resultado que requiere más información
     */
    public static ResultadoAprobacion requiereInformacion(String mensaje, String infoRequerida) {
        ResultadoAprobacion resultado = new ResultadoAprobacion(
            EstadoAprobacion.REQUIERE_INFO, mensaje, "NEED_INFO");
        resultado.datosAdicionales = infoRequerida;
        return resultado;
    }
    
    /**
     * Crea un resultado escalado
     */
    public static ResultadoAprobacion escalado(String mensaje, String siguienteNivel) {
        ResultadoAprobacion resultado = new ResultadoAprobacion(
            EstadoAprobacion.ESCALADO, mensaje, "ESCALATED");
        resultado.datosAdicionales = siguienteNivel;
        return resultado;
    }
    
    /**
     * Agrega una observación al resultado
     */
    public ResultadoAprobacion conObservacion(String observacion) {
        observaciones.add(observacion);
        return this;
    }
    
    /**
     * Agrega datos adicionales al resultado
     */
    public ResultadoAprobacion conDatos(Object datos) {
        this.datosAdicionales = datos;
        return this;
    }
    
    /**
     * Verifica si la solicitud fue aprobada
     */
    public boolean fueAprobado() {
        return estado == EstadoAprobacion.APROBADO;
    }
    
    /**
     * Verifica si la solicitud fue rechazada
     */
    public boolean fueRechazado() {
        return estado == EstadoAprobacion.RECHAZADO;
    }
    
    /**
     * Verifica si la solicitud está pendiente
     */
    public boolean estaPendiente() {
        return estado == EstadoAprobacion.PENDIENTE;
    }
    
    /**
     * Verifica si requiere más información
     */
    public boolean requiereInformacion() {
        return estado == EstadoAprobacion.REQUIERE_INFO;
    }
    
    /**
     * Verifica si fue escalado
     */
    public boolean fueEscalado() {
        return estado == EstadoAprobacion.ESCALADO;
    }
    
    /**
     * Obtiene un resumen del resultado
     */
    public String getResumen() {
        StringBuilder resumen = new StringBuilder();
        resumen.append("Estado: ").append(estado.getDescripcion()).append("\n");
        resumen.append("Mensaje: ").append(mensaje).append("\n");
        
        if (aprobadoPor != null) {
            resumen.append("Aprobado por: ").append(aprobadoPor).append("\n");
        }
        
        if (fechaRespuesta != null) {
            resumen.append("Fecha: ").append(fechaRespuesta).append("\n");
        }
        
        if (!observaciones.isEmpty()) {
            resumen.append("Observaciones:\n");
            for (String obs : observaciones) {
                resumen.append("- ").append(obs).append("\n");
            }
        }
        
        return resumen.toString();
    }
    
    /**
     * Muestra el resultado en consola con formato
     */
    public void mostrarResultado() {
        String icono = obtenerIcono();
        System.out.println("\n" + icono + " RESULTADO DE APROBACIÓN " + icono);
        System.out.println("Estado: " + estado.getDescripcion());
        System.out.println("Mensaje: " + mensaje);
        
        if (aprobadoPor != null) {
            System.out.println("Aprobado por: " + aprobadoPor);
        }
        
        if (codigoRespuesta != null) {
            System.out.println("Código: " + codigoRespuesta);
        }
        
        System.out.println("Fecha: " + fechaRespuesta);
        
        if (!observaciones.isEmpty()) {
            System.out.println("\nObservaciones:");
            for (String obs : observaciones) {
                System.out.println("• " + obs);
            }
        }
        
        if (datosAdicionales != null) {
            System.out.println("Información adicional: " + datosAdicionales);
        }
    }
    
    /**
     * Obtiene el icono apropiado según el estado
     */
    private String obtenerIcono() {
        switch (estado) {
            case APROBADO:
                return "✅";
            case RECHAZADO:
                return "❌";
            case PENDIENTE:
                return "⏳";
            case REQUIERE_INFO:
                return "❓";
            case ESCALADO:
                return "⬆️";
            default:
                return "ℹ️";
        }
    }
    
    // Getters y Setters
    public EstadoAprobacion getEstado() { return estado; }
    public void setEstado(EstadoAprobacion estado) { this.estado = estado; }
    
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    
    public String getCodigoRespuesta() { return codigoRespuesta; }
    public void setCodigoRespuesta(String codigoRespuesta) { this.codigoRespuesta = codigoRespuesta; }
    
    public LocalDateTime getFechaRespuesta() { return fechaRespuesta; }
    public void setFechaRespuesta(LocalDateTime fechaRespuesta) { this.fechaRespuesta = fechaRespuesta; }
    
    public String getAprobadoPor() { return aprobadoPor; }
    public void setAprobadoPor(String aprobadoPor) { this.aprobadoPor = aprobadoPor; }
    
    public List<String> getObservaciones() { return new ArrayList<>(observaciones); }
    public void setObservaciones(List<String> observaciones) { 
        this.observaciones = new ArrayList<>(observaciones); 
    }
    
    public Object getDatosAdicionales() { return datosAdicionales; }
    public void setDatosAdicionales(Object datosAdicionales) { this.datosAdicionales = datosAdicionales; }
    
    @Override
    public String toString() {
        return String.format("ResultadoAprobacion{estado=%s, mensaje='%s', aprobadoPor='%s', fecha=%s}", 
                           estado, mensaje, aprobadoPor, fechaRespuesta);
    }
}
