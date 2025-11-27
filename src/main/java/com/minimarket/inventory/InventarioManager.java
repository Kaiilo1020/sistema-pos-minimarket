package com.minimarket.inventory;

import com.minimarket.models.Producto;
import com.minimarket.patterns.creational.DatabaseConnection;
import com.minimarket.security.AuditoriaManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Gestor de inventario con lógica FIFO flexible
 * Parte de la solución de inventario del sistema POS
 */
public class InventarioManager {
    private static final Logger LOGGER = Logger.getLogger(InventarioManager.class.getName());

    public Optional<Producto> getProductoById(Long id) {
        String sql = "SELECT id, nombre, precio, stock, categoria_id, requiere_lote FROM productos WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Producto p = new Producto();
                p.setId(rs.getLong("id"));
                p.setNombre(rs.getString("nombre"));
                p.setPrecio(rs.getBigDecimal("precio"));
                p.setStock(rs.getInt("stock"));
                p.setCategoriaId(rs.getLong("categoria_id"));
                p.setRequiereLote(rs.getBoolean("requiere_lote"));
                return Optional.of(p);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error al obtener producto por ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<LoteProducto> getLotesByProductoId(Long productoId) {
        List<LoteProducto> lotes = new ArrayList<>();
        String sql = "SELECT id, producto_id, codigo_lote, fecha_entrada, fecha_vencimiento, cantidad FROM lotes_producto WHERE producto_id = ? AND cantidad > 0 ORDER BY fecha_vencimiento ASC, fecha_entrada ASC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, productoId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                LoteProducto lote = new LoteProducto(
                        rs.getLong("id"),
                        rs.getLong("producto_id"),
                        rs.getString("codigo_lote"),
                        rs.getDate("fecha_entrada").toLocalDate(),
                        rs.getDate("fecha_vencimiento").toLocalDate(),
                        rs.getInt("cantidad")
                );
                lotes.add(lote);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error al obtener lotes por producto ID: " + e.getMessage());
        }
        return lotes;
    }

    public ResultadoVenta descontarStock(Long productoId, int cantidadVendida, String usuario) {
        Optional<Producto> productoOpt = getProductoById(productoId);
        if (productoOpt.isEmpty()) {
            return new ResultadoVenta(false, "Producto no encontrado.", 0);
        }

        Producto producto = productoOpt.get();
        if (producto.getStock() < cantidadVendida) {
            return new ResultadoVenta(false, "Stock insuficiente para " + producto.getNombre(), producto.getStock());
        }

        if (producto.isRequiereLote()) {
            return descontarStockConLote(producto, cantidadVendida, usuario);
        } else {
            return descontarStockSinLote(producto, cantidadVendida, usuario);
        }
    }

    private ResultadoVenta descontarStockConLote(Producto producto, int cantidadVendida, String usuario) {
        List<LoteProducto> lotes = getLotesByProductoId(producto.getId());
        lotes.sort(Comparator.comparing(LoteProducto::getFechaVencimiento).thenComparing(LoteProducto::getFechaEntrada));

        int cantidadRestante = cantidadVendida;
        List<MovimientoInventario> movimientos = new ArrayList<>();

        for (LoteProducto lote : lotes) {
            if (cantidadRestante <= 0) break;

            if (lote.getCantidad() > 0 && lote.getFechaVencimiento().isAfter(LocalDate.now())) {
                int cantidadADescontar = Math.min(cantidadRestante, lote.getCantidad());
                movimientos.add(new MovimientoInventario((long)lote.getId(), cantidadADescontar, "VENTA"));
                cantidadRestante -= cantidadADescontar;
            } else if (lote.getFechaVencimiento().isBefore(LocalDate.now().plusDays(7))) {
                LOGGER.warning("Lote " + lote.getCodigoLote() + " del producto " + producto.getNombre() + " próximo a vencer o vencido. Cantidad: " + lote.getCantidad());
                AuditoriaManager.getInstance().registrarEvento(usuario, "ALERTA_VENCIMIENTO", "Lote " + lote.getCodigoLote() + " del producto " + producto.getNombre() + " próximo a vencer o vencido.");
            }
        }

        if (cantidadRestante > 0) {
            return new ResultadoVenta(false, "No hay suficientes lotes válidos para cubrir la venta de " + producto.getNombre(), producto.getStock());
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            String updateLoteSql = "UPDATE lotes_producto SET cantidad = cantidad - ? WHERE id = ?";
            String updateProductoSql = "UPDATE productos SET stock = stock - ? WHERE id = ?";

            try (PreparedStatement pstmtLote = conn.prepareStatement(updateLoteSql);
                 PreparedStatement pstmtProducto = conn.prepareStatement(updateProductoSql)) {

                for (MovimientoInventario mov : movimientos) {
                    pstmtLote.setInt(1, mov.getCantidad());
                    pstmtLote.setLong(2, mov.getLoteId());
                    pstmtLote.addBatch();
                }
                pstmtLote.executeBatch();

                pstmtProducto.setInt(1, cantidadVendida);
                pstmtProducto.setLong(2, producto.getId());
                pstmtProducto.executeUpdate();

                conn.commit();
                AuditoriaManager.getInstance().registrarEvento(usuario, "DESCUENTO_STOCK", "Se descontaron " + cantidadVendida + " unidades de " + producto.getNombre() + " (con lote)");
                return new ResultadoVenta(true, "Stock descontado exitosamente (con lote).", producto.getStock() - cantidadVendida);

            } catch (SQLException e) {
                conn.rollback();
                LOGGER.severe("Error al descontar stock con lote: " + e.getMessage());
                AuditoriaManager.getInstance().registrarEvento(usuario, "ERROR_DESCUENTO_STOCK", "Fallo al descontar stock con lote para " + producto.getNombre() + ": " + e.getMessage());
                return new ResultadoVenta(false, "Error al descontar stock (con lote): " + e.getMessage(), producto.getStock());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.severe("Error de conexión al descontar stock con lote: " + e.getMessage());
            return new ResultadoVenta(false, "Error de conexión al descontar stock (con lote).", producto.getStock());
        }
    }

    private ResultadoVenta descontarStockSinLote(Producto producto, int cantidadVendida, String usuario) {
        String updateProductoSql = "UPDATE productos SET stock = stock - ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateProductoSql)) {
            pstmt.setInt(1, cantidadVendida);
            pstmt.setLong(2, producto.getId());
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                LOGGER.info("Venta de producto sin lote: " + producto.getNombre() + ". Se descontaron " + cantidadVendida + " unidades del stock general.");
                AuditoriaManager.getInstance().registrarEvento(usuario, "DESCUENTO_STOCK_SIN_LOTE", "Se descontaron " + cantidadVendida + " unidades de " + producto.getNombre() + " (sin lote)");
                return new ResultadoVenta(true, "Stock descontado exitosamente (sin lote).", producto.getStock() - cantidadVendida);
            } else {
                return new ResultadoVenta(false, "No se pudo actualizar el stock del producto " + producto.getNombre(), producto.getStock());
            }
        } catch (SQLException e) {
            LOGGER.severe("Error al descontar stock sin lote: " + e.getMessage());
            AuditoriaManager.getInstance().registrarEvento(usuario, "ERROR_DESCUENTO_STOCK_SIN_LOTE", "Fallo al descontar stock sin lote para " + producto.getNombre() + ": " + e.getMessage());
            return new ResultadoVenta(false, "Error al descontar stock (sin lote): " + e.getMessage(), producto.getStock());
        }
    }

    public void agregarStock(Long productoId, int cantidad, String usuario) {
        String sql = "UPDATE productos SET stock = stock + ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cantidad);
            pstmt.setLong(2, productoId);
            pstmt.executeUpdate();
            AuditoriaManager.getInstance().registrarEvento(usuario, "AGREGAR_STOCK", "Se agregaron " + cantidad + " unidades al producto " + productoId);
        } catch (SQLException e) {
            LOGGER.severe("Error al agregar stock: " + e.getMessage());
        }
    }
}
