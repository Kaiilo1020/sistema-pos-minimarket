package com.minimarket.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo de Producto con gestión de lotes y fechas de vencimiento
 */
public class Producto {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private String categoria;
    private String lote;
    private LocalDate fechaVencimiento;
    private boolean requiereLote;
    private boolean requiereFechaVencimiento;
    
    // Constructores
    public Producto() {}
    
    public Producto(Long id, String codigo, String nombre, BigDecimal precio, 
                   Integer stock, boolean requiereLote, boolean requiereFechaVencimiento) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.requiereLote = requiereLote;
        this.requiereFechaVencimiento = requiereFechaVencimiento;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    
    
    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }
    
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    
    public boolean isRequiereLote() { return requiereLote; }
    public void setRequiereLote(boolean requiereLote) { this.requiereLote = requiereLote; }
    
    public boolean isRequiereFechaVencimiento() { return requiereFechaVencimiento; }
    public void setRequiereFechaVencimiento(boolean requiereFechaVencimiento) { 
        this.requiereFechaVencimiento = requiereFechaVencimiento; 
    }
    
    /**
     * Verifica si el producto puede ser vendido
     */
    public boolean puedeVenderse() {
        // Si requiere lote y no lo tiene, no puede venderse
        if (requiereLote && (lote == null || lote.trim().isEmpty())) {
            return false;
        }
        
        // Si requiere fecha de vencimiento y no la tiene, no puede venderse
        if (requiereFechaVencimiento && fechaVencimiento == null) {
            return false;
        }
        
        // Si tiene fecha de vencimiento, verificar que no esté vencido
        if (fechaVencimiento != null && fechaVencimiento.isBefore(LocalDate.now())) {
            return false;
        }
        
        // Verificar stock disponible
        return stock != null && stock > 0;
    }
    
    /**
     * Verifica si el producto está próximo a vencer (dentro de 7 días)
     */
    public boolean proximoAVencer() {
        if (fechaVencimiento == null) return false;
        return fechaVencimiento.isBefore(LocalDate.now().plusDays(7)) && 
               fechaVencimiento.isAfter(LocalDate.now());
    }
    
    /**
     * Verifica si el stock está bajo (menos de 10 unidades)
     */
    public boolean stockBajo() {
        return stock != null && stock <= 10;
    }
    
    @Override
    public String toString() {
        return String.format("Producto{id=%d, codigo='%s', nombre='%s', precio=%s, stock=%d}", 
                           id, codigo, nombre, precio, stock);
    }
}
