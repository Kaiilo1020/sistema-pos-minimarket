package com.minimarket.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de Boleta con información completa
 */
public class Boleta {
    private Long id;
    private String numero;
    private LocalDateTime fechaHora;
    private Usuario cajera;
    private MetodoPago metodoPago;
    private List<DetalleVenta> detalles;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private String observaciones;
    
    // Enum para métodos de pago
    public enum MetodoPago {
        EFECTIVO("Efectivo"),
        TARJETA_DEBITO("Tarjeta de Débito"),
        TARJETA_CREDITO("Tarjeta de Crédito"),
        YAPE("Yape"),
        PLIN("Plin"),
        TRANSFERENCIA("Transferencia Bancaria");
        
        private final String descripcion;
        
        MetodoPago(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() { return descripcion; }
    }
    
    // Constructor privado para usar con Builder
    private Boleta() {
        this.detalles = new ArrayList<>();
        this.fechaHora = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    
    public Usuario getCajera() { return cajera; }
    public void setCajera(Usuario cajera) { this.cajera = cajera; }
    
    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }
    
    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }
    
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    
    public BigDecimal getIgv() { return igv; }
    public void setIgv(BigDecimal igv) { this.igv = igv; }
    
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    
    /**
     * Calcula los totales de la boleta
     */
    public void calcularTotales() {
        subtotal = detalles.stream()
                          .map(DetalleVenta::getSubtotal)
                          .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        igv = subtotal.multiply(new BigDecimal("0.18"));
        total = subtotal.add(igv);
    }
    
    /**
     * Valida que la boleta tenga todos los datos requeridos
     */
    public boolean esValida() {
        return numero != null && !numero.trim().isEmpty() &&
               fechaHora != null &&
               cajera != null &&
               metodoPago != null &&
               detalles != null && !detalles.isEmpty() &&
               detalles.stream().allMatch(DetalleVenta::esValido) &&
               total != null && total.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Clase Builder para construir boletas paso a paso
     */
    public static class Builder {
        private Boleta boleta;
        
        public Builder() {
            this.boleta = new Boleta();
        }
        
        public Builder setNumero(String numero) {
            boleta.numero = numero;
            return this;
        }
        
        public Builder setFechaHora(LocalDateTime fechaHora) {
            boleta.fechaHora = fechaHora;
            return this;
        }
        
        public Builder setCajera(Usuario cajera) {
            boleta.cajera = cajera;
            return this;
        }
        
        public Builder setMetodoPago(MetodoPago metodoPago) {
            boleta.metodoPago = metodoPago;
            return this;
        }
        
        public Builder agregarDetalle(DetalleVenta detalle) {
            boleta.detalles.add(detalle);
            return this;
        }
        
        public Builder agregarProducto(Producto producto, Integer cantidad, BigDecimal precioUnitario) {
            DetalleVenta detalle = new DetalleVenta(producto, cantidad, precioUnitario);
            boleta.detalles.add(detalle);
            return this;
        }
        
        public Builder setObservaciones(String observaciones) {
            boleta.observaciones = observaciones;
            return this;
        }
        
        public Boleta build() {
            // Calcular totales antes de construir
            boleta.calcularTotales();
            
            // Validar que la boleta esté completa
            if (!boleta.esValida()) {
                throw new IllegalStateException("La boleta no tiene todos los datos requeridos");
            }
            
            return boleta;
        }
    }
    
    @Override
    public String toString() {
        return String.format("Boleta{numero='%s', fechaHora=%s, cajera='%s', metodoPago=%s, total=%s}", 
                           numero, fechaHora, cajera != null ? cajera.getNombreCompleto() : "N/A", 
                           metodoPago, total);
    }
}
