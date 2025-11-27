package com.minimarket.security;

import com.minimarket.model.Usuario;

/**
 * Singleton para gestionar la sesión del usuario actual
 * Parte de la solución RBAC del sistema POS
 */
public class UsuarioSesion {
    private static UsuarioSesion instance;
    private Usuario usuarioActual;

    private UsuarioSesion() {
        // Constructor privado para Singleton
    }

    public static UsuarioSesion getInstance() {
        if (instance == null) {
            instance = new UsuarioSesion();
        }
        return instance;
    }

    public void login(Usuario usuario) {
        this.usuarioActual = usuario;
        AuditoriaManager.getInstance().registrarEvento(
            usuario.getUsername(), 
            "LOGIN", 
            "Inicio de sesión exitoso"
        );
    }

    public void logout() {
        if (usuarioActual != null) {
            AuditoriaManager.getInstance().registrarEvento(
                usuarioActual.getUsername(), 
                "LOGOUT", 
                "Cierre de sesión"
            );
        }
        this.usuarioActual = null;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public boolean isLoggedIn() {
        return usuarioActual != null;
    }

    public boolean tienePermiso(Rol rolRequerido) {
        return isLoggedIn() && usuarioActual.getRol().tienePermiso(rolRequerido);
    }
}
