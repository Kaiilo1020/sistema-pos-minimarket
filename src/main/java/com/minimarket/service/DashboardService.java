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
        public int productosVendidos;
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
                data.productosVendidos = obtenerProductosVendidos(conn);
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
     * Obtiene las ventas del día actual (consulta simple que funciona en pgAdmin)
     */
    private static double obtenerVentasDelDia(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total), 0) as total_ventas " +
                    "FROM ventas " +
                    "WHERE DATE(fecha_hora) = CURRENT_DATE AND estado = 'ACTIVA'";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getDouble("total_ventas");
            }
        }
        return 0.0;
    }
    
    /**
     * Obtiene el número de transacciones del día (consulta simple)
     */
    private static int obtenerTransaccionesDelDia(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) as total_transacciones " +
                    "FROM ventas " +
                    "WHERE DATE(fecha_hora) = CURRENT_DATE AND estado = 'ACTIVA'";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("total_transacciones");
            }
        }
        return 0;
    }
    
    /**
     * Obtiene el total de productos vendidos del día (consulta simple con JOIN básico)
     */
    private static int obtenerProductosVendidos(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(SUM(dv.cantidad), 0) as productos_vendidos " +
                    "FROM detalle_ventas dv, ventas v " +
                    "WHERE dv.venta_id = v.id " +
                    "AND DATE(v.fecha_hora) = CURRENT_DATE " +
                    "AND v.estado = 'ACTIVA'";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("productos_vendidos");
            }
        }
        return 0;
    }
    
    /**
     * Obtiene la distribución de métodos de pago del día (consulta simple)
     */
    private static String obtenerMetodosPago(Connection conn) throws SQLException {
        String sql = "SELECT metodo_pago, COUNT(*) as cantidad " +
                    "FROM ventas " +
                    "WHERE DATE(fecha_hora) = CURRENT_DATE AND estado = 'ACTIVA' " +
                    "GROUP BY metodo_pago";
        
        int efectivo = 0;
        int yape = 0;
        int otros = 0;
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
                } else {
                    otros += cantidad;
                }
            }
        }
        
        if (total == 0) {
            return "Sin ventas hoy";
        }
        
        int porcentajeEfectivo = (efectivo * 100) / total;
        int porcentajeYape = (yape * 100) / total;
        int porcentajeOtros = (otros * 100) / total;
        
        if (otros > 0) {
            return porcentajeEfectivo + "% Efectivo | " + porcentajeYape + "% Yape | " + porcentajeOtros + "% Otros";
        } else {
            return porcentajeEfectivo + "% Efectivo | " + porcentajeYape + "% Yape";
        }
    }
    
    /**
     * Obtiene productos con stock bajo (< 10 unidades) - consulta simple
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
        } catch (SQLException e) {
            // Si hay error, devolver lista vacía
            System.out.println("Error en alertas de stock: " + e.getMessage());
        }
        
        return alertas;
    }
    
    /**
     * Obtiene productos próximos a vencer (siguiente semana) - consulta simplificada
     */
    private static List<LoteVencer> obtenerLotesVencer(Connection conn) throws SQLException {
        List<LoteVencer> lotes = new ArrayList<>();
        
        // Consulta más simple sin funciones complejas de fecha
        String sql = "SELECT nombre, fecha_vencimiento " +
                    "FROM productos " +
                    "WHERE fecha_vencimiento IS NOT NULL " +
                    "AND fecha_vencimiento >= CURRENT_DATE " +
                    "AND fecha_vencimiento <= CURRENT_DATE + 7 " +
                    "AND activo = true " +
                    "ORDER BY fecha_vencimiento ASC " +
                    "LIMIT 5";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            java.time.LocalDate hoy = java.time.LocalDate.now();
            
            while (rs.next()) {
                String nombre = rs.getString("nombre");
                Date fechaVenc = rs.getDate("fecha_vencimiento");
                
                if (fechaVenc != null) {
                    java.time.LocalDate fechaVencimiento = fechaVenc.toLocalDate();
                    int diasRestantes = (int) java.time.temporal.ChronoUnit.DAYS.between(hoy, fechaVencimiento);
                    
                    String fechaFormateada = fechaVencimiento.format(formatter);
                    lotes.add(new LoteVencer(nombre, fechaFormateada, diasRestantes));
                }
            }
        } catch (SQLException e) {
            // Si hay error, devolver lista vacía
            System.out.println("Error en lotes por vencer: " + e.getMessage());
        }
        
        return lotes;
    }
}
