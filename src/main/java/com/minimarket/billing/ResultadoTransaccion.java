package com.minimarket.billing;

/**
 * Representa el resultado de una transacción de venta
 */
public class ResultadoTransaccion {
    private boolean exitoso;
    private String mensaje;
    private String ticket; // Ticket de venta generado
    
    public ResultadoTransaccion(boolean exitoso, String mensaje) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.ticket = "";
    }
    
    public ResultadoTransaccion(boolean exitoso, String mensaje, String ticket) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.ticket = ticket;
    }
    
    // Getters
    public boolean isExitoso() { return exitoso; }
    public String getMensaje() { return mensaje; }
    public String getTicket() { return ticket; }
    
    @Override
    public String toString() {
        return String.format("ResultadoTransaccion{exitoso=%s, mensaje='%s'}", 
                           exitoso, mensaje);
    }
}
