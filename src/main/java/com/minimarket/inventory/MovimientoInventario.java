package com.minimarket.inventory;

import java.time.LocalDateTime;

/**
 * Representa un movimiento de inventario para auditoría
 */
public class MovimientoInventario {
    private int id;
    private int productoId;
    private Integer loteId; // Puede ser null para productos sin lote
    private int cantidad;
    private String tipoMovimiento; // ENTRADA, SALIDA, VENTA, AJUSTE, etc.
    private String descripcion;
    private LocalDateTime fechaHora;
    private Integer usuarioId;
    
    // Constructores
    public MovimientoInventario() {
        this.fechaHora = LocalDateTime.now();
    }
    
    public MovimientoInventario(Long loteId, int cantidad, String tipoMovimiento) {
        this.loteId = loteId != null ? loteId.intValue() : null;
        this.cantidad = cantidad;
        this.tipoMovimiento = tipoMovimiento;
        this.fechaHora = LocalDateTime.now();
    }
    
    public MovimientoInventario(int productoId, Integer loteId, int cantidad, 
                              String tipoMovimiento, String descripcion) {
        this.productoId = productoId;
        this.loteId = loteId;
        this.cantidad = cantidad;
        this.tipoMovimiento = tipoMovimiento;
        this.descripcion = descripcion;
        this.fechaHora = LocalDateTime.now();
    }
    
    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }
    
    public Integer getLoteId() { return loteId; }
    public void setLoteId(Integer loteId) { this.loteId = loteId; }
    
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    
    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    
    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }
    
    @Override
    public String toString() {
        return String.format("MovimientoInventario{productoId=%d, loteId=%s, cantidad=%d, " +
                           "tipo='%s', fecha=%s}", 
                           productoId, loteId, cantidad, tipoMovimiento, fechaHora);
    }
}
