package com.minimarket.ui.handlers;

import com.minimarket.config.DatabaseConnection;
import com.minimarket.dao.VentaDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Handler que coordina la lógica de negocio del módulo de historial de ventas.
 */
public class HistorialVentaHandler {
    
    private final VentaDAO ventaDAO;
    
    public HistorialVentaHandler() {
        this.ventaDAO = new VentaDAO();
    }
    
    /**
     * Carga el historial completo de ventas
     */
    public List<VentaDAO.HistorialVenta> cargarHistorial() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            return ventaDAO.obtenerHistorial(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Error al cargar historial de ventas: " + e.getMessage(), e);
        }
    }
    
    /**
     * Formatea una fecha para mostrar
     */
    public String formatearFecha(java.sql.Timestamp fechaHora) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(fechaHora);
    }
}

