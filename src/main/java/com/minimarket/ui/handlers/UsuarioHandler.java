package com.minimarket.ui.handlers;

import com.minimarket.config.DatabaseConnection;
import com.minimarket.dao.UsuarioDAO;
import com.minimarket.ui.util.UIUtils;

import java.awt.Component;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Handler que coordina la lógica de negocio del módulo de usuarios.
 * Separa la lógica de la UI.
 */
public class UsuarioHandler {
    
    private final UsuarioDAO usuarioDAO;
    
    public UsuarioHandler() {
        this.usuarioDAO = new UsuarioDAO();
    }
    
    /**
     * Carga todos los usuarios con información de ventas
     */
    public List<UsuarioDAO.UsuarioInfo> cargarUsuarios() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            return usuarioDAO.listarConVentas(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error al cargar usuarios: " + e.getMessage(), e);
        }
    }
    
    /**
     * Elimina un usuario (solo si no tiene ventas)
     */
    public void eliminarUsuario(Long usuarioId, Component parent) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            // Verificar si tiene ventas
            if (usuarioDAO.tieneVentas(conn, usuarioId)) {
                UIUtils.mostrarError(parent, 
                    "No se puede eliminar el usuario porque tiene ventas registradas.");
                return;
            }
            
            // Eliminar (desactivar) usuario
            usuarioDAO.eliminar(conn, usuarioId);
            UIUtils.mostrarExito(parent, "Usuario eliminado exitosamente.");
            
        } catch (SQLException e) {
            UIUtils.mostrarError(parent, "Error al eliminar usuario: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}

