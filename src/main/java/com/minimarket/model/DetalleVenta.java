package com.minimarket.model;

import java.math.BigDecimal;

/**
 * Modelo de Detalle de Venta
 */
public class DetalleVenta {
    private Long id;
    private Producto producto;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private String loteVendido;
    
    // Constructores
    public DetalleVenta() {}
    
    public DetalleVenta(Producto producto, Integer cantidad, BigDecimal precioUnitario) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.loteVendido = producto.getLote();
        calcularSubtotal();
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { 
        this.cantidad = cantidad;
        calcularSubtotal();
    }
    
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { 
        this.precioUnitario = precioUnitario;
        calcularSubtotal();
    }
    
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    
    public String getLoteVendido() { return loteVendido; }
    public void setLoteVendido(String loteVendido) { this.loteVendido = loteVendido; }
    
    /**
     * Calcula el subtotal del detalle
     */
    private void calcularSubtotal() {
        if (cantidad != null && precioUnitario != null) {
            subtotal = precioUnitario.multiply(new BigDecimal(cantidad));
        }
    }
    
    /**
     * Valida que el detalle sea correcto
     */
    public boolean esValido() {
        return producto != null &&
               cantidad != null && cantidad > 0 &&
               precioUnitario != null && precioUnitario.compareTo(BigDecimal.ZERO) > 0 &&
               subtotal != null;
    }
    
    @Override
    public String toString() {
        return String.format("DetalleVenta{producto='%s', cantidad=%d, precioUnitario=%s, subtotal=%s}", 
                           producto != null ? producto.getNombre() : "N/A", 
                           cantidad, precioUnitario, subtotal);
    }
}
