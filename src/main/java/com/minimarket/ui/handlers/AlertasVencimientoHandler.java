package com.minimarket.ui.handlers;

import com.minimarket.config.DatabaseConnection;
import com.minimarket.dao.ProductoDAO;
import com.minimarket.model.Producto;
import com.minimarket.ui.util.UIUtils;

import java.awt.Component;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Handler que coordina la lógica de negocio del módulo de alertas de vencimiento.
 */
public class AlertasVencimientoHandler {
    
    private static final int DIAS_CRITICOS = 7;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private final ProductoDAO productoDAO;
    
    public AlertasVencimientoHandler() {
        this.productoDAO = new ProductoDAO();
    }
    
    /**
     * Información de producto con alerta
     */
    public static class ProductoAlerta {
        public Long id;
        public String nombre;
        public int stock;
        public String fechaVencimiento;
        public String diasTexto;
        public String estado;
        public boolean vencido;
    }
    
    /**
     * Carga productos con alertas de vencimiento
     * Siempre obtiene datos frescos de la base de datos
     */
    public List<ProductoAlerta> cargarAlertas() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            // Obtener productos directamente de la BD (siempre datos actualizados)
            List<Producto> productos = productoDAO.listarConAlertasVencimiento(conn);
            return convertirAAlertas(productos);
        } catch (SQLException e) {
            throw new RuntimeException("Error al cargar alertas: " + e.getMessage(), e);
        }
    }
    
    /**
     * Retira un producto específico por ID del inventario
     */
    public void retirarProductoPorId(Component parent, Long productoId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            productoDAO.desactivarProducto(conn, productoId);
            UIUtils.mostrarExito(parent, "Producto retirado exitosamente del inventario.");
        } catch (SQLException e) {
            UIUtils.mostrarError(parent, "Error al retirar producto: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Retira productos vencidos del inventario
     */
    public int retirarProductosVencidos(Component parent) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            int cantidad = productoDAO.retirarVencidos(conn);
            if (cantidad > 0) {
                UIUtils.mostrarExito(parent, 
                    "Se han retirado " + cantidad + " productos vencidos del inventario.");
            } else {
                UIUtils.mostrarExito(parent, "No hay productos vencidos para retirar.");
            }
            return cantidad;
        } catch (SQLException e) {
            UIUtils.mostrarError(parent, "Error al retirar productos: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    private List<ProductoAlerta> convertirAAlertas(List<Producto> productos) {
        List<ProductoAlerta> alertas = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        
        for (Producto producto : productos) {
            if (producto.getFechaVencimiento() == null) {
                continue;
            }
            
            LocalDate fechaVenc = producto.getFechaVencimiento();
            long diasRestantes = ChronoUnit.DAYS.between(hoy, fechaVenc);
            
            ProductoAlerta alerta = new ProductoAlerta();
            alerta.id = producto.getId();
            alerta.nombre = producto.getNombre();
            alerta.stock = producto.getStock();
            alerta.fechaVencimiento = fechaVenc.format(FORMATO_FECHA);
            alerta.vencido = diasRestantes < 0;
            
            if (diasRestantes < 0) {
                alerta.estado = "VENCIDO";
                alerta.diasTexto = "Hace " + Math.abs(diasRestantes) + " días";
            } else if (diasRestantes <= DIAS_CRITICOS) {
                alerta.estado = "POR VENCER";
                alerta.diasTexto = diasRestantes + " días";
            } else {
                alerta.estado = "PRÓXIMO";
                alerta.diasTexto = diasRestantes + " días";
            }
            
            alertas.add(alerta);
        }
        
        return alertas;
    }
}

