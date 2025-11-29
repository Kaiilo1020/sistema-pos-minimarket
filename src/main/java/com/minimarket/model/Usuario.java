package com.minimarket.model;

import com.minimarket.security.Rol;

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
    
    /**
     * Obtiene el nombre completo del usuario
     */
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
    
    @Override
    public String toString() {
        return String.format("Usuario{id=%d, username='%s', nombre='%s', rol=%s}", 
                           id, username, getNombreCompleto(), rol);
    }
}
