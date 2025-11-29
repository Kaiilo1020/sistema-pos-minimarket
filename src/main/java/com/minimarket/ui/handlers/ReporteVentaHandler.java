package com.minimarket.ui.handlers;

import com.minimarket.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handler que coordina la lógica de negocio del módulo de reportes de ventas.
 */
public class ReporteVentaHandler {
    
    /**
     * Información de reporte por trabajador
     */
    public static class ReporteTrabajador {
        public String trabajador;
        public int transacciones;
        public int productosVendidos;
        public double totalRecaudado;
    }
    
    /**
     * Totales del día
     */
    public static class TotalesDia {
        public int totalTransacciones;
        public int totalProductos;
        public double totalIngresos;
    }
    
    /**
     * Obtiene el reporte de ventas por trabajador del día actual
     */
    public List<ReporteTrabajador> obtenerReportePorTrabajador() {
        List<ReporteTrabajador> reportes = new ArrayList<>();
        
        String sql = """
            SELECT u.username as trabajador,
                   COUNT(DISTINCT v.id) as transacciones,
                   COALESCE(SUM(dv.cantidad), 0) as productos_vendidos,
                   COALESCE(SUM(v.total), 0) as total_recaudado
            FROM usuarios u
            LEFT JOIN ventas v ON u.id = v.cajera_id 
                              AND DATE(v.fecha_hora) = CURRENT_DATE 
                              AND v.estado = 'ACTIVA'
            LEFT JOIN detalle_ventas dv ON v.id = dv.venta_id
            WHERE u.activo = true AND u.rol IN ('CAJERO', 'SUPERVISOR', 'ADMINISTRADOR')
            GROUP BY u.id, u.username
            HAVING COUNT(DISTINCT v.id) > 0
            ORDER BY total_recaudado DESC
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                ReporteTrabajador reporte = new ReporteTrabajador();
                reporte.trabajador = rs.getString("trabajador");
                reporte.transacciones = rs.getInt("transacciones");
                reporte.productosVendidos = rs.getInt("productos_vendidos");
                reporte.totalRecaudado = rs.getDouble("total_recaudado");
                reportes.add(reporte);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Error al cargar reporte de ventas: " + e.getMessage(), e);
        }
        
        return reportes;
    }
    
    /**
     * Calcula los totales del día
     */
    public TotalesDia calcularTotalesDia(List<ReporteTrabajador> reportes) {
        TotalesDia totales = new TotalesDia();
        for (ReporteTrabajador reporte : reportes) {
            totales.totalTransacciones += reporte.transacciones;
            totales.totalProductos += reporte.productosVendidos;
            totales.totalIngresos += reporte.totalRecaudado;
        }
        return totales;
    }
}

