package com.minimarket.patterns.behavioral.command;

import com.minimarket.patterns.creational.DatabaseConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Comando para modificar el precio de un producto
 * Permite deshacer la operación restaurando el precio anterior
 */
public class ComandoModificarPrecio extends ComandoBase {
    
    private Long productoId;
    private BigDecimal nuevoPrecio;
    private BigDecimal precioAnterior;
    private String nombreProducto;
    private Connection connection;
    
    public ComandoModificarPrecio(Long productoId, BigDecimal nuevoPrecio, String usuarioEjecutor) {
        super("Modificar precio del producto ID: " + productoId, usuarioEjecutor);
        this.productoId = productoId;
        this.nuevoPrecio = nuevoPrecio;
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    @Override
    protected boolean ejecutarComando() {
        try {
            // 1. Validar que el producto existe y obtener precio actual
            if (!obtenerDatosProducto()) {
                resultado = "Producto no encontrado";
                return false;
            }
            
            // 2. Validar el nuevo precio
            if (!validarNuevoPrecio()) {
                resultado = "El nuevo precio no es válido";
                return false;
            }
            
            // 3. Actualizar el precio en la base de datos
            if (!actualizarPrecio()) {
                resultado = "Error al actualizar el precio en la base de datos";
                return false;
            }
            
            // 4. Registrar el cambio en auditoría
            registrarCambioAuditoria();
            
            resultado = String.format("Precio del producto '%s' cambiado de S/ %s a S/ %s", 
                                    nombreProducto, precioAnterior, nuevoPrecio);
            return true;
            
        } catch (SQLException e) {
            resultado = "Error de base de datos: " + e.getMessage();
            return false;
        }
    }
    
    @Override
    protected boolean deshacerComando() {
        if (precioAnterior == null) {
            return false;
        }
        
        try {
            String sql = "UPDATE productos SET precio = ? WHERE id = ?";
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setBigDecimal(1, precioAnterior);
                stmt.setLong(2, productoId);
                
                int filasAfectadas = stmt.executeUpdate();
                if (filasAfectadas > 0) {
                    // Registrar la reversión en auditoría
                    registrarReversionAuditoria();
                    return true;
                }
            }
            
        } catch (SQLException e) {
            // Log del error
        }
        
        return false;
    }
    
    private boolean obtenerDatosProducto() throws SQLException {
        String sql = "SELECT nombre, precio FROM productos WHERE id = ? AND activo = true";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, productoId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    nombreProducto = rs.getString("nombre");
                    precioAnterior = rs.getBigDecimal("precio");
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean validarNuevoPrecio() {
        // Validar que el precio sea positivo
        if (nuevoPrecio == null || nuevoPrecio.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        // Validar que el precio no sea igual al actual
        if (precioAnterior != null && nuevoPrecio.compareTo(precioAnterior) == 0) {
            resultado = "El nuevo precio es igual al precio actual";
            return false;
        }
        
        // Validar que el cambio no sea demasiado drástico (más del 50%)
        if (precioAnterior != null) {
            BigDecimal diferencia = nuevoPrecio.subtract(precioAnterior).abs();
            BigDecimal porcentajeCambio = diferencia.divide(precioAnterior, 4, 
                java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
            
            if (porcentajeCambio.compareTo(new BigDecimal("50")) > 0) {
                resultado = String.format("Cambio de precio muy drástico: %.2f%%. " +
                          "Requiere autorización especial.", porcentajeCambio);
                return false;
            }
        }
        
        return true;
    }
    
    private boolean actualizarPrecio() throws SQLException {
        String sql = "UPDATE productos SET precio = ?, fecha_ultima_modificacion = CURRENT_TIMESTAMP " +
                    "WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBigDecimal(1, nuevoPrecio);
            stmt.setLong(2, productoId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    private void registrarCambioAuditoria() {
        try {
            String sql = "INSERT INTO auditoria_precios (producto_id, precio_anterior, " +
                        "precio_nuevo, usuario, fecha_cambio, accion) " +
                        "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, 'MODIFICACION')";
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, productoId);
                stmt.setBigDecimal(2, precioAnterior);
                stmt.setBigDecimal(3, nuevoPrecio);
                stmt.setString(4, usuarioEjecutor);
                
                stmt.executeUpdate();
            }
            
        } catch (SQLException e) {
            // Log del error pero no fallar el comando principal
            System.err.println("Error al registrar auditoría: " + e.getMessage());
        }
    }
    
    private void registrarReversionAuditoria() {
        try {
            String sql = "INSERT INTO auditoria_precios (producto_id, precio_anterior, " +
                        "precio_nuevo, usuario, fecha_cambio, accion) " +
                        "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, 'REVERSION')";
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, productoId);
                stmt.setBigDecimal(2, nuevoPrecio);
                stmt.setBigDecimal(3, precioAnterior);
                stmt.setString(4, usuarioEjecutor);
                
                stmt.executeUpdate();
            }
            
        } catch (SQLException e) {
            System.err.println("Error al registrar auditoría de reversión: " + e.getMessage());
        }
    }
    
    @Override
    public boolean puedeDeshacer() {
        return super.puedeDeshacer() && precioAnterior != null;
    }
    
    // Getters
    public Long getProductoId() { return productoId; }
    public BigDecimal getNuevoPrecio() { return nuevoPrecio; }
    public BigDecimal getPrecioAnterior() { return precioAnterior; }
    public String getNombreProducto() { return nombreProducto; }
}
