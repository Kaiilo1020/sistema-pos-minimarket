package com.minimarket.inventory;

/**
 * Representa el resultado de una operación de venta
 */
public class ResultadoVenta {
    private boolean exito;
    private String mensaje;
    private int nuevoStock;
    
    public ResultadoVenta(boolean exito, String mensaje, int nuevoStock) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.nuevoStock = nuevoStock;
    }
    
    // Getters
    public boolean isExito() { return exito; }
    public String getMensaje() { return mensaje; }
    public int getNuevoStock() { return nuevoStock; }
    
    @Override
    public String toString() {
        return String.format("ResultadoVenta{exito=%s, mensaje='%s', nuevoStock=%d}", 
                           exito, mensaje, nuevoStock);
    }
}
