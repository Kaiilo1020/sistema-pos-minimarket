package com.minimarket.security;

/**
 * Enum que define los roles del sistema POS
 * Implementa Role-Based Access Control (RBAC)
 */
public enum Rol {
    ADMINISTRADOR(3),
    SUPERVISOR(2),
    CAJERO(1);
    
    private final int nivel;
    
    Rol(int nivel) {
        this.nivel = nivel;
    }
    
    public int getNivel() { return nivel; }
    
    /**
     * Verifica si tiene permisos para un rol específico
     */
    public boolean tienePermiso(Rol rolRequerido) {
        return this.nivel >= rolRequerido.nivel;
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
    
}
