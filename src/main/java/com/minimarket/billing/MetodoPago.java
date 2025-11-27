package com.minimarket.billing;

/**
 * Enum para métodos de pago
 * Garantiza integridad de datos en facturación
 */
public enum MetodoPago {
    EFECTIVO(1, "Efectivo", "Pago en efectivo"),
    TARJETA_DEBITO(2, "Tarjeta Débito", "Pago con tarjeta de débito"),
    TARJETA_CREDITO(3, "Tarjeta Crédito", "Pago con tarjeta de crédito"),
    YAPE(4, "Yape", "Pago digital Yape"),
    PLIN(5, "Plin", "Pago digital Plin"),
    TRANSFERENCIA(6, "Transferencia", "Transferencia bancaria"),
    MIXTO(7, "Mixto", "Combinación de métodos de pago");
    
    private final int id;
    private final String nombre;
    private final String descripcion;
    
    MetodoPago(int id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
    
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    
    /**
     * Obtiene método de pago por ID
     */
    public static MetodoPago porId(int id) {
        for (MetodoPago metodo : values()) {
            if (metodo.getId() == id) {
                return metodo;
            }
        }
        return EFECTIVO; // Por defecto
    }
    
    /**
     * Verifica si requiere validación adicional
     */
    public boolean requiereValidacion() {
        return this == TARJETA_DEBITO || this == TARJETA_CREDITO || 
               this == YAPE || this == PLIN || this == TRANSFERENCIA;
    }
    
    /**
     * Verifica si es método digital
     */
    public boolean esDigital() {
        return this == YAPE || this == PLIN || this == TRANSFERENCIA;
    }
}
