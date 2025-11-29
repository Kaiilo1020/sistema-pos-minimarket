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
        EFECTIVO,
        TARJETA_DEBITO,
        TARJETA_CREDITO,
        YAPE,
        PLIN,
        TRANSFERENCIA;
        
        MetodoPago() {
        }
    }
    
    // Constructor package-private para uso con BoletaBuilder
    Boleta() {
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
    
    @Override
    public String toString() {
        return String.format("Boleta{numero='%s', fechaHora=%s, cajera='%s', metodoPago=%s, total=%s}", 
                           numero, fechaHora, cajera != null ? cajera.getNombreCompleto() : "N/A", 
                           metodoPago, total);
    }
}
