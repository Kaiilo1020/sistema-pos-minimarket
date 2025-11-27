package com.minimarket.patterns.behavioral.observer.observers;

import com.minimarket.patterns.behavioral.observer.*;
import com.minimarket.patterns.creational.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Logger;

/**
 * Observer especializado en eventos de inventario
 * Maneja alertas de stock bajo, productos próximos a vencer, etc.
 */
public class ObserverInventario implements Observer {
    
    private String id;
    private String descripcion;
    private boolean activo;
    private Connection connection;
    private static final Logger logger = Logger.getLogger(ObserverInventario.class.getName());
    
    // Configuración de alertas
    private int stockMinimoAlerta = 10;
    private int diasVencimientoAlerta = 7;
    
    public ObserverInventario(String id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
        this.activo = true;
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    @Override
    public void actualizar(EventoSistema evento) {
        if (!activo) {
            return;
        }
        
        // Procesar solo eventos relacionados con inventario
        switch (evento.getTipo()) {
            case PRODUCTO_STOCK_BAJO:
                manejarStockBajo(evento);
                break;
                
            case PRODUCTO_PROXIMO_VENCER:
                manejarProductoProximoVencer(evento);
                break;
                
            case PRODUCTO_VENCIDO:
                manejarProductoVencido(evento);
                break;
                
            case PRODUCTO_SIN_LOTE:
                manejarProductoSinLote(evento);
                break;
                
            case VENTA_REGISTRADA:
                verificarStockDespuesVenta(evento);
                break;
                
            default:
                // No procesar otros tipos de eventos
                break;
        }
    }
    
    /**
     * Maneja alertas de stock bajo
     */
    private void manejarStockBajo(EventoSistema evento) {
        String nombreProducto = evento.getDato("nombreProducto", String.class);
        Integer stockActual = evento.getDato("stockActual", Integer.class);
        
        System.out.println("📦 ALERTA DE INVENTARIO - STOCK BAJO");
        System.out.println("   Producto: " + nombreProducto);
        System.out.println("   Stock actual: " + stockActual + " unidades");
        System.out.println("   Acción requerida: Reabastecer inventario");
        
        // Registrar en base de datos para seguimiento
        registrarAlertaInventario("STOCK_BAJO", nombreProducto, 
                                "Stock: " + stockActual + " unidades");
        
        // Generar recomendación de reabastecimiento
        generarRecomendacionReabastecimiento(nombreProducto, stockActual);
    }
    
    /**
     * Maneja productos próximos a vencer
     */
    private void manejarProductoProximoVencer(EventoSistema evento) {
        String nombreProducto = evento.getDato("nombreProducto", String.class);
        LocalDate fechaVencimiento = evento.getDato("fechaVencimiento", LocalDate.class);
        
        long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
        
        System.out.println("⏰ ALERTA DE INVENTARIO - PRÓXIMO VENCIMIENTO");
        System.out.println("   Producto: " + nombreProducto);
        System.out.println("   Fecha de vencimiento: " + fechaVencimiento);
        System.out.println("   Días restantes: " + diasRestantes);
        
        if (diasRestantes <= 3) {
            System.out.println("   🚨 URGENTE: Considerar descuento o promoción");
        } else if (diasRestantes <= 7) {
            System.out.println("   ⚠️ IMPORTANTE: Planificar estrategia de venta");
        }
        
        registrarAlertaInventario("PROXIMO_VENCER", nombreProducto, 
                                "Vence: " + fechaVencimiento + " (" + diasRestantes + " días)");
        
        // Sugerir acciones según días restantes
        sugerirAccionesVencimiento(nombreProducto, diasRestantes);
    }
    
    /**
     * Maneja productos vencidos
     */
    private void manejarProductoVencido(EventoSistema evento) {
        String nombreProducto = evento.getDato("nombreProducto", String.class);
        LocalDate fechaVencimiento = evento.getDato("fechaVencimiento", LocalDate.class);
        
        System.out.println("🚫 ALERTA CRÍTICA - PRODUCTO VENCIDO");
        System.out.println("   Producto: " + nombreProducto);
        System.out.println("   Fecha de vencimiento: " + fechaVencimiento);
        System.out.println("   🔴 ACCIÓN INMEDIATA: Retirar del inventario");
        
        registrarAlertaInventario("VENCIDO", nombreProducto, 
                                "Vencido desde: " + fechaVencimiento);
        
        // Marcar producto como no vendible
        marcarProductoNoVendible(nombreProducto);
    }
    
    /**
     * Maneja productos sin lote
     */
    private void manejarProductoSinLote(EventoSistema evento) {
        String nombreProducto = evento.getDato("nombreProducto", String.class);
        
        System.out.println("📋 ALERTA DE INVENTARIO - FALTA INFORMACIÓN");
        System.out.println("   Producto: " + nombreProducto);
        System.out.println("   Problema: Falta información de lote");
        System.out.println("   Acción requerida: Actualizar datos del producto");
        
        registrarAlertaInventario("SIN_LOTE", nombreProducto, 
                                "Requiere actualización de lote");
    }
    
    /**
     * Verifica stock después de una venta
     */
    private void verificarStockDespuesVenta(EventoSistema evento) {
        String numeroBoleta = evento.getDato("numeroBoleta", String.class);
        
        // Aquí se podría consultar la base de datos para verificar
        // si algún producto quedó con stock bajo después de la venta
        System.out.println("🔍 Verificando stock después de venta: " + numeroBoleta);
        
        // Simulación de verificación
        verificarTodosLosProductosStockBajo();
    }
    
    /**
     * Registra alertas de inventario en la base de datos
     */
    private void registrarAlertaInventario(String tipoAlerta, String producto, String detalle) {
        try {
            String sql = "INSERT INTO alertas_inventario (tipo_alerta, producto, detalle, " +
                        "fecha_alerta, estado) VALUES (?, ?, ?, CURRENT_TIMESTAMP, 'PENDIENTE')";
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, tipoAlerta);
                stmt.setString(2, producto);
                stmt.setString(3, detalle);
                
                stmt.executeUpdate();
                logger.info("Alerta de inventario registrada: " + tipoAlerta + " - " + producto);
            }
            
        } catch (SQLException e) {
            logger.severe("Error al registrar alerta de inventario: " + e.getMessage());
        }
    }
    
    /**
     * Genera recomendación de reabastecimiento
     */
    private void generarRecomendacionReabastecimiento(String producto, int stockActual) {
        // Lógica simple de recomendación
        int cantidadRecomendada = Math.max(50, stockMinimoAlerta * 3);
        
        System.out.println("💡 RECOMENDACIÓN DE REABASTECIMIENTO:");
        System.out.println("   Producto: " + producto);
        System.out.println("   Stock actual: " + stockActual);
        System.out.println("   Cantidad recomendada: " + cantidadRecomendada + " unidades");
        System.out.println("   Prioridad: " + (stockActual <= 5 ? "ALTA" : "MEDIA"));
    }
    
    /**
     * Sugiere acciones según días restantes para vencimiento
     */
    private void sugerirAccionesVencimiento(String producto, long diasRestantes) {
        System.out.println("💡 ACCIONES SUGERIDAS:");
        
        if (diasRestantes <= 1) {
            System.out.println("   🚨 Descuento del 50% o más");
            System.out.println("   🚨 Promoción 2x1");
            System.out.println("   🚨 Considerar donación");
        } else if (diasRestantes <= 3) {
            System.out.println("   ⚠️ Descuento del 30%");
            System.out.println("   ⚠️ Ubicar en zona visible");
            System.out.println("   ⚠️ Promoción especial");
        } else if (diasRestantes <= 7) {
            System.out.println("   ℹ️ Descuento del 15%");
            System.out.println("   ℹ️ Incluir en ofertas semanales");
        }
    }
    
    /**
     * Marca un producto como no vendible
     */
    private void marcarProductoNoVendible(String nombreProducto) {
        try {
            String sql = "UPDATE productos SET vendible = false, " +
                        "motivo_no_vendible = 'VENCIDO', fecha_actualizacion = CURRENT_TIMESTAMP " +
                        "WHERE nombre = ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, nombreProducto);
                
                int filasAfectadas = stmt.executeUpdate();
                if (filasAfectadas > 0) {
                    System.out.println("✅ Producto marcado como no vendible: " + nombreProducto);
                }
            }
            
        } catch (SQLException e) {
            logger.severe("Error al marcar producto como no vendible: " + e.getMessage());
        }
    }
    
    /**
     * Verifica todos los productos con stock bajo
     */
    private void verificarTodosLosProductosStockBajo() {
        try {
            String sql = "SELECT nombre, stock FROM productos WHERE stock <= ? AND activo = true";
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, stockMinimoAlerta);
                
                var rs = stmt.executeQuery();
                while (rs.next()) {
                    String nombre = rs.getString("nombre");
                    int stock = rs.getInt("stock");
                    
                    // Generar evento de stock bajo
                    GestorEventos.getInstance().notificarStockBajo(nombre, stock, "SISTEMA");
                }
            }
            
        } catch (SQLException e) {
            logger.severe("Error al verificar productos con stock bajo: " + e.getMessage());
        }
    }
    
    // Getters y Setters
    @Override
    public String getId() { return id; }
    
    @Override
    public String getDescripcion() { return descripcion; }
    
    @Override
    public boolean isActivo() { return activo; }
    
    public void setActivo(boolean activo) { this.activo = activo; }
    
    public int getStockMinimoAlerta() { return stockMinimoAlerta; }
    
    public void setStockMinimoAlerta(int stockMinimoAlerta) { 
        this.stockMinimoAlerta = stockMinimoAlerta; 
    }
    
    public int getDiasVencimientoAlerta() { return diasVencimientoAlerta; }
    
    public void setDiasVencimientoAlerta(int diasVencimientoAlerta) { 
        this.diasVencimientoAlerta = diasVencimientoAlerta; 
    }
}
