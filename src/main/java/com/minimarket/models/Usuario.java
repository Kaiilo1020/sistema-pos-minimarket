package com.minimarket.models;

import com.minimarket.security.Rol;
import java.time.LocalDateTime;

/**
 * Modelo de Usuario con roles y permisos RBAC
 * Actualizado para usar el sistema de seguridad centralizado
 */
public class Usuario {
    private Long id;
    private String username;
    private String password;
    private String nombre;
    private String apellido;
    private String email;
    private Rol rol;
    private boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimoAcceso;
    
    // Constructores
    public Usuario() {}
    
    public Usuario(String username, String password, String nombre, String apellido, 
                  String email, Rol rol) {
        this.username = username;
        this.password = password;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.rol = rol;
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }
    
    /**
     * Verifica si el usuario tiene permisos para una acción específica
     */
    public boolean tienePermiso(String accion) {
        if (!activo || rol == null) return false;
        
        switch (accion.toUpperCase()) {
            case "MODIFICAR_PRECIOS":
                return rol.puedeModificarPrecios();
            case "ACCEDER_REPORTES":
                return rol.puedeAccederReportes();
            case "GESTIONAR_INVENTARIO":
                return rol.puedeGestionarInventario();
            case "REGISTRAR_VENTAS":
                return rol.puedeRegistrarVentas();
            case "CONSULTAR_PRECIOS":
                return rol.puedeConsultarPrecios();
            default:
                return false;
        }
    }
    
    /**
     * Verifica si el usuario es administrador o supervisor
     */
    public boolean esAdministrativo() {
        return rol != null && rol.esAdministrativo();
    }
    
    /**
     * Obtiene el nombre completo del usuario
     */
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
    
    /**
     * Actualiza el último acceso
     */
    public void actualizarUltimoAcceso() {
        this.ultimoAcceso = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return String.format("Usuario{id=%d, username='%s', nombre='%s', rol=%s}", 
                           id, username, getNombreCompleto(), rol);
    }
}
