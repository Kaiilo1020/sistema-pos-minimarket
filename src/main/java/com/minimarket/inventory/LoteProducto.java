package com.minimarket.inventory;

import java.time.LocalDate;

/**
 * Representa un lote de producto con fecha de vencimiento
 * Usado para implementar lógica FIFO
 */
public class LoteProducto {
    private int id;
    private int productoId;
    private String numeroLote;
    private LocalDate fechaVencimiento;
    private LocalDate fechaIngreso;
    private int stockDisponible;
    private int stockInicial;
    private String proveedor;
    private double costoUnitario;
    
    // Constructores
    public LoteProducto() {}
    
    public LoteProducto(Long id, Long productoId, String codigoLote, LocalDate fechaEntrada, LocalDate fechaVencimiento, int cantidad) {
        this.id = id.intValue();
        this.productoId = productoId.intValue();
        this.numeroLote = codigoLote;
        this.fechaIngreso = fechaEntrada;
        this.fechaVencimiento = fechaVencimiento;
        this.stockDisponible = cantidad;
        this.stockInicial = cantidad;
    }
    
    public LoteProducto(int productoId, String numeroLote, LocalDate fechaVencimiento, 
                       int stockInicial, String proveedor, double costoUnitario) {
        this.productoId = productoId;
        this.numeroLote = numeroLote;
        this.fechaVencimiento = fechaVencimiento;
        this.fechaIngreso = LocalDate.now();
        this.stockInicial = stockInicial;
        this.stockDisponible = stockInicial;
        this.proveedor = proveedor;
        this.costoUnitario = costoUnitario;
    }
    
    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }
    
    public String getNumeroLote() { return numeroLote; }
    public void setNumeroLote(String numeroLote) { this.numeroLote = numeroLote; }
    
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    
    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    
    public LocalDate getFechaEntrada() { return fechaIngreso; }
    public String getCodigoLote() { return numeroLote; }
    public int getCantidad() { return stockDisponible; }
    public void setCantidad(int cantidad) { this.stockDisponible = cantidad; }
    
    public int getStockDisponible() { return stockDisponible; }
    public void setStockDisponible(int stockDisponible) { this.stockDisponible = stockDisponible; }
    
    public int getStockInicial() { return stockInicial; }
    public void setStockInicial(int stockInicial) { this.stockInicial = stockInicial; }
    
    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    
    public double getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(double costoUnitario) { this.costoUnitario = costoUnitario; }
    
    /**
     * Verifica si el lote está vencido
     */
    public boolean estaVencido() {
        return fechaVencimiento != null && fechaVencimiento.isBefore(LocalDate.now());
    }
    
    /**
     * Verifica si el lote está próximo a vencer (dentro de X días)
     */
    public boolean proximoAVencer(int dias) {
        if (fechaVencimiento == null) return false;
        return fechaVencimiento.isBefore(LocalDate.now().plusDays(dias));
    }
    
    /**
     * Calcula el porcentaje de stock utilizado
     */
    public double getPorcentajeUtilizado() {
        if (stockInicial == 0) return 0;
        return ((double)(stockInicial - stockDisponible) / stockInicial) * 100;
    }
    
    @Override
    public String toString() {
        return String.format("LoteProducto{id=%d, numeroLote='%s', fechaVencimiento=%s, " +
                           "stockDisponible=%d, estaVencido=%s}", 
                           id, numeroLote, fechaVencimiento, stockDisponible, estaVencido());
    }
}
