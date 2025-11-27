package com.minimarket.security;

/**
 * Enum que define los roles del sistema POS
 * Implementa Role-Based Access Control (RBAC)
 */
public enum Rol {
    ADMINISTRADOR("ADMIN", "Acceso total al sistema", 3),
    SUPERVISOR("SUPERVISOR", "Acceso a reportes y supervisión", 2),
    CAJERO("CAJERO", "Solo ventas y consultas de precios", 1);
    
    private final String codigo;
    private final String descripcion;
    private final int nivel;
    
    Rol(String codigo, String descripcion, int nivel) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.nivel = nivel;
    }
    
    public String getCodigo() { return codigo; }
    public String getDescripcion() { return descripcion; }
    public int getNivel() { return nivel; }
    
    /**
     * Verifica si puede acceder a un rol requerido
     */
    public boolean puedeAcceder(Rol rolRequerido) {
        return this.nivel >= rolRequerido.nivel;
    }
    
    /**
     * Verifica si tiene permisos para un rol específico
     */
    public boolean tienePermiso(Rol rolRequerido) {
        return this.nivel >= rolRequerido.nivel;
    }
    
    /**
     * Verifica si el rol tiene permisos administrativos
     */
    public boolean esAdministrativo() {
        return this == ADMINISTRADOR || this == SUPERVISOR;
    }
    
    /**
     * Verifica si puede modificar precios
     */
    public boolean puedeModificarPrecios() {
        return this == ADMINISTRADOR;
    }
    
    /**
     * Verifica si puede acceder a reportes
     */
    public boolean puedeAccederReportes() {
        return this == ADMINISTRADOR || this == SUPERVISOR;
    }
    
    /**
     * Verifica si puede gestionar inventario
     */
    public boolean puedeGestionarInventario() {
        return this == ADMINISTRADOR || this == SUPERVISOR;
    }
    
    /**
     * Verifica si puede registrar ventas
     */
    public boolean puedeRegistrarVentas() {
        return true; // Todos los roles pueden registrar ventas
    }
    
    /**
     * Verifica si puede consultar precios
     */
    public boolean puedeConsultarPrecios() {
        return true; // Todos los roles pueden consultar precios
    }
}
