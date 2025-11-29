package com.minimarket.ui.handlers;

import com.minimarket.config.DatabaseConnection;
import com.minimarket.dao.ProductoDAO;
import com.minimarket.model.Producto;
import com.minimarket.ui.util.UIUtils;

import java.awt.Component;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Handler que coordina la lógica de negocio del módulo de productos/inventario.
 * Separa la lógica de la UI.
 */
public class ProductoHandler {
    
    private final ProductoDAO productoDAO;
    
    public ProductoHandler() {
        this.productoDAO = new ProductoDAO();
    }
    
    /**
     * Carga todos los productos activos
     */
    public List<Producto> cargarProductos() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            return productoDAO.listarActivos(conn, null);
        } catch (SQLException e) {
            throw new RuntimeException("Error al cargar productos: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene un producto por ID
     */
    public Producto obtenerProducto(Long productoId) {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            return productoDAO.obtenerPorId(conn, productoId);
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener producto: " + e.getMessage(), e);
        }
    }
    
    /**
     * Elimina (desactiva) un producto
     */
    public void eliminarProducto(Long productoId, Component parent) {
        String sql = "UPDATE productos SET activo = false WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, productoId);
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                UIUtils.mostrarExito(parent, "Producto eliminado exitosamente.");
            } else {
                UIUtils.mostrarError(parent, "No se pudo eliminar el producto.");
            }
        } catch (SQLException e) {
            UIUtils.mostrarError(parent, "Error al eliminar producto: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
}

