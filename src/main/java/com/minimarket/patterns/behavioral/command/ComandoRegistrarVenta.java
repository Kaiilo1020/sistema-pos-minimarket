package com.minimarket.patterns.behavioral.command;

import com.minimarket.models.*;
import com.minimarket.patterns.creational.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

/**
 * Comando para registrar una venta
 * Permite deshacer la operación si es necesario
 */
public class ComandoRegistrarVenta extends ComandoBase {
    
    private Boleta boleta;
    private List<Producto> productos;
    private List<Integer> cantidades;
    private Connection connection;
    private Long ventaId;
    
    public ComandoRegistrarVenta(Boleta boleta, List<Producto> productos, 
                               List<Integer> cantidades, String usuarioEjecutor) {
        super("Registrar venta " + boleta.getNumero(), usuarioEjecutor);
        this.boleta = boleta;
        this.productos = new ArrayList<>(productos);
        this.cantidades = new ArrayList<>(cantidades);
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    @Override
    protected boolean ejecutarComando() {
        try {
            // Iniciar transacción
            connection.setAutoCommit(false);
            
            // 1. Validar stock disponible
            if (!validarStock()) {
                connection.rollback();
                resultado = "Stock insuficiente para uno o más productos";
                return false;
            }
            
            // 2. Validar que los productos puedan venderse
            if (!validarProductos()) {
                connection.rollback();
                resultado = "Uno o más productos no pueden venderse (revisar lotes/fechas)";
                return false;
            }
            
            // 3. Insertar la venta principal
            ventaId = insertarVenta();
            if (ventaId == null) {
                connection.rollback();
                resultado = "Error al insertar la venta principal";
                return false;
            }
            
            // 4. Insertar detalles de venta
            if (!insertarDetallesVenta()) {
                connection.rollback();
                resultado = "Error al insertar detalles de venta";
                return false;
            }
            
            // 5. Actualizar stock de productos
            if (!actualizarStock()) {
                connection.rollback();
                resultado = "Error al actualizar stock de productos";
                return false;
            }
            
            // Confirmar transacción
            connection.commit();
            connection.setAutoCommit(true);
            
            resultado = "Venta registrada exitosamente. ID: " + ventaId;
            return true;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                // Log del error de rollback
            }
            resultado = "Error de base de datos: " + e.getMessage();
            return false;
        }
    }
    
    @Override
    protected boolean deshacerComando() {
        if (ventaId == null) {
            return false;
        }
        
        try {
            connection.setAutoCommit(false);
            
            // 1. Restaurar stock de productos
            if (!restaurarStock()) {
                connection.rollback();
                return false;
            }
            
            // 2. Eliminar detalles de venta
            if (!eliminarDetallesVenta()) {
                connection.rollback();
                return false;
            }
            
            // 3. Marcar venta como anulada (no eliminar para auditoría)
            if (!anularVenta()) {
                connection.rollback();
                return false;
            }
            
            connection.commit();
            connection.setAutoCommit(true);
            
            return true;
            
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException rollbackEx) {
                // Log del error
            }
            return false;
        }
    }
    
    private boolean validarStock() {
        String sql = "SELECT stock FROM productos WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < productos.size(); i++) {
                stmt.setLong(1, productos.get(i).getId());
                var rs = stmt.executeQuery();
                
                if (rs.next()) {
                    int stockActual = rs.getInt("stock");
                    if (stockActual < cantidades.get(i)) {
                        return false;
                    }
                } else {
                    return false; // Producto no encontrado
                }
                rs.close();
            }
            return true;
            
        } catch (SQLException e) {
            return false;
        }
    }
    
    private boolean validarProductos() {
        for (Producto producto : productos) {
            if (!producto.puedeVenderse()) {
                return false;
            }
        }
        return true;
    }
    
    private Long insertarVenta() throws SQLException {
        String sql = "INSERT INTO ventas (numero, fecha_hora, cajera_id, metodo_pago, " +
                    "subtotal, igv, total, estado) VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVA')";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, 
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, boleta.getNumero());
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(boleta.getFechaHora()));
            stmt.setLong(3, boleta.getCajera().getId());
            stmt.setString(4, boleta.getMetodoPago().name());
            stmt.setBigDecimal(5, boleta.getSubtotal());
            stmt.setBigDecimal(6, boleta.getIgv());
            stmt.setBigDecimal(7, boleta.getTotal());
            
            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas > 0) {
                var rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }
    
    private boolean insertarDetallesVenta() throws SQLException {
        String sql = "INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, " +
                    "precio_unitario, subtotal, lote_vendido) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < productos.size(); i++) {
                Producto producto = productos.get(i);
                Integer cantidad = cantidades.get(i);
                
                stmt.setLong(1, ventaId);
                stmt.setLong(2, producto.getId());
                stmt.setInt(3, cantidad);
                stmt.setBigDecimal(4, producto.getPrecio());
                stmt.setBigDecimal(5, producto.getPrecio().multiply(
                    new java.math.BigDecimal(cantidad)));
                stmt.setString(6, producto.getLote());
                
                stmt.addBatch();
            }
            
            int[] resultados = stmt.executeBatch();
            for (int resultado : resultados) {
                if (resultado <= 0) {
                    return false;
                }
            }
            return true;
        }
    }
    
    private boolean actualizarStock() throws SQLException {
        String sql = "UPDATE productos SET stock = stock - ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < productos.size(); i++) {
                stmt.setInt(1, cantidades.get(i));
                stmt.setLong(2, productos.get(i).getId());
                stmt.addBatch();
            }
            
            int[] resultados = stmt.executeBatch();
            for (int resultado : resultados) {
                if (resultado <= 0) {
                    return false;
                }
            }
            return true;
        }
    }
    
    private boolean restaurarStock() throws SQLException {
        String sql = "UPDATE productos SET stock = stock + ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < productos.size(); i++) {
                stmt.setInt(1, cantidades.get(i));
                stmt.setLong(2, productos.get(i).getId());
                stmt.addBatch();
            }
            
            int[] resultados = stmt.executeBatch();
            for (int resultado : resultados) {
                if (resultado <= 0) {
                    return false;
                }
            }
            return true;
        }
    }
    
    private boolean eliminarDetallesVenta() throws SQLException {
        String sql = "DELETE FROM detalle_ventas WHERE venta_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, ventaId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    private boolean anularVenta() throws SQLException {
        String sql = "UPDATE ventas SET estado = 'ANULADA', fecha_anulacion = CURRENT_TIMESTAMP, " +
                    "usuario_anulacion = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, usuarioEjecutor);
            stmt.setLong(2, ventaId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public Boleta getBoleta() {
        return boleta;
    }
    
    public Long getVentaId() {
        return ventaId;
    }
}
