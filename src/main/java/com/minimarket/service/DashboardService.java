package com.minimarket.service;

import com.minimarket.config.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para obtener datos reales del dashboard
 */
public class DashboardService {
    
    public static class DashboardData {
        public double ventasDelDia;
        public int transacciones;
        public String metodoPago;
        public List<AlertaStock> alertasStock;
        public List<LoteVencer> lotesVencer;
        
        public DashboardData() {
            this.alertasStock = new ArrayList<>();
            this.lotesVencer = new ArrayList<>();
        }
    }
    
    public static class AlertaStock {
        public String producto;
        public int stock;
        public boolean critico; // true si stock < 5, false si stock < 10
        
        public AlertaStock(String producto, int stock) {
            this.producto = producto;
            this.stock = stock;
            this.critico = stock < 5;
        }
        
        public String getIcono() {
            return critico ? "🔴" : "🟡";
        }
    }
    
    public static class LoteVencer {
        public String producto;
        public String fechaVencimiento;
        public int diasRestantes;
        
        public LoteVencer(String producto, String fechaVencimiento, int diasRestantes) {
            this.producto = producto;
            this.fechaVencimiento = fechaVencimiento;
            this.diasRestantes = diasRestantes;
        }
        
        public String getIcono() {
            return diasRestantes <= 1 ? "🔴" : "🟠";
        }
        
        public String getTextoVencimiento() {
            if (diasRestantes <= 0) return "Vencido";
            if (diasRestantes == 1) return "Mañana";
            return fechaVencimiento;
        }
    }
    
    /**
     * Obtiene todos los datos del dashboard
     */
    public static DashboardData obtenerDatosDashboard() {
        DashboardData data = new DashboardData();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            if (conn != null) {
                data.ventasDelDia = obtenerVentasDelDia(conn);
                data.transacciones = obtenerTransaccionesDelDia(conn);
                data.metodoPago = obtenerMetodosPago(conn);
                data.alertasStock = obtenerAlertasStock(conn);
                data.lotesVencer = obtenerLotesVencer(conn);
            }
        } catch (SQLException e) {
            // En caso de error, usar datos por defecto
            data.ventasDelDia = 0.0;
            data.transacciones = 0;
            data.metodoPago = "Sin datos";
        }
        
        return data;
    }
    
    /**
     * Obtiene las ventas del día actual
     */
    private static double obtenerVentasDelDia(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total), 0) as total_ventas " +
                    "FROM boletas " +
                    "WHERE DATE(fecha_emision) = CURRENT_DATE";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getDouble("total_ventas");
            }
        }
        return 0.0;
    }
    
    /**
     * Obtiene el número de transacciones del día
     */
    private static int obtenerTransaccionesDelDia(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as total_transacciones " +
                    "FROM boletas " +
                    "WHERE DATE(fecha_emision) = CURRENT_DATE";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("total_transacciones");
            }
        }
        return 0;
    }
    
    /**
     * Obtiene la distribución de métodos de pago del día
     */
    private static String obtenerMetodosPago(Connection conn) throws SQLException {
        String sql = "SELECT metodo_pago, COUNT(*) as cantidad " +
                    "FROM boletas " +
                    "WHERE DATE(fecha_emision) = CURRENT_DATE " +
                    "GROUP BY metodo_pago";
        
        int efectivo = 0;
        int yape = 0;
        int total = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String metodo = rs.getString("metodo_pago");
                int cantidad = rs.getInt("cantidad");
                total += cantidad;
                
                if ("EFECTIVO".equalsIgnoreCase(metodo)) {
                    efectivo = cantidad;
                } else if ("YAPE".equalsIgnoreCase(metodo)) {
                    yape = cantidad;
                }
            }
        }
        
        if (total == 0) {
            return "Sin ventas hoy";
        }
        
        int porcentajeEfectivo = (efectivo * 100) / total;
        int porcentajeYape = (yape * 100) / total;
        
        return porcentajeEfectivo + "% Efectivo | " + porcentajeYape + "% Yape";
    }
    
    /**
     * Obtiene productos con stock bajo (< 10 unidades)
     */
    private static List<AlertaStock> obtenerAlertasStock(Connection conn) throws SQLException {
        List<AlertaStock> alertas = new ArrayList<>();
        
        String sql = "SELECT nombre, stock " +
                    "FROM productos " +
                    "WHERE stock < 10 AND activo = true " +
                    "ORDER BY stock ASC " +
                    "LIMIT 5";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String nombre = rs.getString("nombre");
                int stock = rs.getInt("stock");
                alertas.add(new AlertaStock(nombre, stock));
            }
        }
        
        return alertas;
    }
    
    /**
     * Obtiene productos próximos a vencer (siguiente semana)
     */
    private static List<LoteVencer> obtenerLotesVencer(Connection conn) throws SQLException {
        List<LoteVencer> lotes = new ArrayList<>();
        
        String sql = "SELECT nombre, fecha_vencimiento, " +
                    "DATE_PART('day', fecha_vencimiento - CURRENT_DATE) as dias_restantes " +
                    "FROM productos " +
                    "WHERE fecha_vencimiento IS NOT NULL " +
                    "AND fecha_vencimiento <= CURRENT_DATE + INTERVAL '7 days' " +
                    "AND activo = true " +
                    "ORDER BY fecha_vencimiento ASC " +
                    "LIMIT 5";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            while (rs.next()) {
                String nombre = rs.getString("nombre");
                Date fechaVenc = rs.getDate("fecha_vencimiento");
                int diasRestantes = rs.getInt("dias_restantes");
                
                String fechaFormateada = fechaVenc.toLocalDate().format(formatter);
                lotes.add(new LoteVencer(nombre, fechaFormateada, diasRestantes));
            }
        }
        
        return lotes;
    }
}
