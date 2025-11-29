package com.minimarket.dao;

import com.minimarket.model.Boleta;
import com.minimarket.model.DetalleVenta;
import com.minimarket.model.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para persistir ventas y sus detalles dentro de transacciones ACID.
 */
public class VentaDAO {

    private static final String INSERT_VENTA = """
        INSERT INTO ventas (numero, fecha_hora, cajera_id, metodo_pago, subtotal, igv, total, estado, observaciones)
        VALUES (?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, 'ACTIVA', ?)
        """;

    private static final String INSERT_DETALLE = """
        INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, precio_unitario, subtotal, lote_vendido)
        VALUES (?, ?, ?, ?, ?, ?)
        """;

    public long registrarVenta(Connection conn, Boleta boleta, String resumenCliente) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_VENTA, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, boleta.getNumero());
            ps.setLong(2, boleta.getCajera().getId());
            ps.setString(3, boleta.getMetodoPago().name());
            ps.setBigDecimal(4, boleta.getSubtotal());
            ps.setBigDecimal(5, boleta.getIgv());
            ps.setBigDecimal(6, boleta.getTotal());
            String observaciones = boleta.getObservaciones();
            if (resumenCliente != null && !resumenCliente.isBlank()) {
                observaciones = (observaciones == null || observaciones.isBlank())
                        ? resumenCliente
                        : observaciones + " | " + resumenCliente;
            }
            ps.setString(7, observaciones);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener el ID de la venta registrada");
    }

    public void registrarDetalle(Connection conn, long ventaId, DetalleVenta detalle) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_DETALLE)) {
            Producto producto = detalle.getProducto();
            ps.setLong(1, ventaId);
            if (producto != null) {
                ps.setLong(2, producto.getId());
            } else {
                ps.setNull(2, java.sql.Types.BIGINT);
            }
            ps.setInt(3, detalle.getCantidad());
            ps.setBigDecimal(4, detalle.getPrecioUnitario());
            ps.setBigDecimal(5, detalle.getSubtotal());
            ps.setString(6, detalle.getLoteVendido());
            ps.executeUpdate();
        }
    }

    /**
     * Obtiene el historial completo de ventas (detalles)
     */
    public List<HistorialVenta> obtenerHistorial(Connection conn) throws SQLException {
        List<HistorialVenta> historial = new ArrayList<>();
        
        String sql = """
            SELECT dv.id, dv.producto_id, v.cajera_id as usuario_id, 
                   dv.cantidad, dv.precio_unitario, dv.subtotal as total,
                   v.fecha_hora
            FROM detalle_ventas dv
            INNER JOIN ventas v ON dv.venta_id = v.id
            WHERE v.estado = 'ACTIVA'
            ORDER BY v.fecha_hora DESC
        """;
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                HistorialVenta item = new HistorialVenta();
                item.id = rs.getLong("id");
                item.productoId = rs.getLong("producto_id");
                item.usuarioId = rs.getLong("usuario_id");
                item.cantidad = rs.getInt("cantidad");
                item.precioUnitario = rs.getDouble("precio_unitario");
                item.total = rs.getDouble("total");
                item.fechaHora = rs.getTimestamp("fecha_hora");
                historial.add(item);
            }
        }
        
        return historial;
    }

    /**
     * Clase auxiliar para historial de ventas
     */
    public static class HistorialVenta {
        public Long id;
        public Long productoId;
        public Long usuarioId;
        public int cantidad;
        public double precioUnitario;
        public double total;
        public java.sql.Timestamp fechaHora;
    }
}

