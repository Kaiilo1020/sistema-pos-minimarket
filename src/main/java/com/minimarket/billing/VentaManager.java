package com.minimarket.billing;

import com.minimarket.inventory.InventarioManager;
import com.minimarket.inventory.ResultadoVenta;
import com.minimarket.models.Producto;
import com.minimarket.patterns.creational.DatabaseConnection;
import com.minimarket.security.AuditoriaManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Gestor de ventas con integridad de datos
 * Parte de la solución de facturación del sistema POS
 */
public class VentaManager {
    private static final Logger LOGGER = Logger.getLogger(VentaManager.class.getName());
    private final InventarioManager inventarioManager;

    public VentaManager() {
        this.inventarioManager = new InventarioManager();
    }

    public ResultadoTransaccion registrarVenta(Long cajeroId, List<Producto> productosVendidos, MetodoPago metodoPago, String referenciaPago) {
        if (productosVendidos == null || productosVendidos.isEmpty()) {
            return new ResultadoTransaccion(false, "No hay productos para registrar la venta.");
        }
        if (metodoPago == null) {
            return new ResultadoTransaccion(false, "Método de pago no especificado.");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            // 1. Registrar Venta
            String insertVentaSQL = "INSERT INTO ventas (fecha_hora_exacta, cajero_id, total, id_metodo_pago, referencia_pago) VALUES (?, ?, ?, ?, ?) RETURNING id";
            Long ventaId = null;
            double totalVenta = 0;

            try (PreparedStatement pstmtVenta = conn.prepareStatement(insertVentaSQL)) {
                pstmtVenta.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                pstmtVenta.setLong(2, cajeroId);
                pstmtVenta.setDouble(3, 0);
                pstmtVenta.setInt(4, metodoPago.getId());
                pstmtVenta.setString(5, referenciaPago);

                ResultSet rs = pstmtVenta.executeQuery();
                if (rs.next()) {
                    ventaId = rs.getLong("id");
                } else {
                    throw new SQLException("Fallo al obtener ID de venta.");
                }
            }

            // 2. Registrar Detalle de Venta y Descontar Stock
            String insertDetalleSQL = "INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmtDetalle = conn.prepareStatement(insertDetalleSQL)) {
                for (Producto p : productosVendidos) {
                    int cantidadVendida = p.getStock(); // Usamos stock como cantidad vendida
                    ResultadoVenta resInventario = inventarioManager.descontarStock(p.getId(), cantidadVendida, "CajeroID:" + cajeroId);
                    if (!resInventario.isExito()) {
                        throw new SQLException("Error de inventario para producto " + p.getNombre() + ": " + resInventario.getMensaje());
                    }

                    pstmtDetalle.setLong(1, ventaId);
                    pstmtDetalle.setLong(2, p.getId());
                    pstmtDetalle.setInt(3, cantidadVendida);
                    pstmtDetalle.setBigDecimal(4, p.getPrecio());
                    pstmtDetalle.addBatch();

                    totalVenta += p.getPrecio().doubleValue() * cantidadVendida;
                }
                pstmtDetalle.executeBatch();
            }

            // 3. Actualizar Total de Venta
            String updateVentaTotalSQL = "UPDATE ventas SET total = ? WHERE id = ?";
            try (PreparedStatement pstmtUpdateTotal = conn.prepareStatement(updateVentaTotalSQL)) {
                pstmtUpdateTotal.setDouble(1, totalVenta);
                pstmtUpdateTotal.setLong(2, ventaId);
                pstmtUpdateTotal.executeUpdate();
            }

            conn.commit();
            AuditoriaManager.getInstance().registrarEvento("CajeroID:" + cajeroId, "REGISTRO_VENTA", "Venta #" + ventaId + " registrada. Total: " + totalVenta);
            LOGGER.info("Venta #" + ventaId + " registrada exitosamente. Total: " + totalVenta);

            // Generar ticket
            generarTicket(ventaId, LocalDateTime.now(), cajeroId, metodoPago, totalVenta, productosVendidos);

            return new ResultadoTransaccion(true, "Venta registrada exitosamente. Total: " + totalVenta);

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.severe("Error al hacer rollback: " + ex.getMessage());
                }
            }
            AuditoriaManager.getInstance().registrarEvento("CajeroID:" + cajeroId, "ERROR_VENTA", "Fallo al registrar venta: " + e.getMessage());
            LOGGER.severe("Error al registrar venta: " + e.getMessage());
            return new ResultadoTransaccion(false, "Error al registrar venta: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.severe("Error al cerrar conexión: " + e.getMessage());
                }
            }
        }
    }

    private void generarTicket(Long ventaId, LocalDateTime fechaHora, Long cajeroId, MetodoPago metodoPago, double total, List<Producto> productos) {
        System.out.println("\n========== TICKET DE VENTA ==========");
        System.out.println("No. Venta: " + ventaId);
        System.out.println("Fecha/Hora: " + fechaHora.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        System.out.println("Cajero ID: " + cajeroId);
        System.out.println("Método de Pago: " + metodoPago.getDescripcion());
        System.out.println("-------------------------------------");
        productos.forEach(p -> System.out.printf("%-20s x%d %.2f\n", p.getNombre(), p.getStock(), p.getPrecio().doubleValue()));
        System.out.println("-------------------------------------");
        System.out.printf("TOTAL: %.2f\n", total);
        System.out.println("=====================================\n");
    }
}
