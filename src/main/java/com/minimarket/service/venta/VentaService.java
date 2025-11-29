package com.minimarket.service.venta;

import com.minimarket.config.DatabaseConnection;
import com.minimarket.dao.ProductoDAO;
import com.minimarket.dao.VentaDAO;
import com.minimarket.model.Boleta;
import com.minimarket.model.BoletaBuilder;
import com.minimarket.model.DetalleVenta;
import com.minimarket.model.Producto;
import com.minimarket.security.AuditoriaManager;
import com.minimarket.security.UsuarioSesion;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Orquestador de la transacción ACID que registra ventas.
 * Integra: Chain of Responsibility (validaciones) y Observer (notificaciones de stock).
 */
public class VentaService {

    private final DatabaseConnection databaseConnection = DatabaseConnection.getInstance();
    private final VentaDAO ventaDAO = new VentaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();

    public VentaService() {
    }

    public void registrarVenta(VentaContext context) {
        // Chain of Responsibility: Validaciones en cadena
        validarVenta(context);

        Connection conn = null;
        try {
            conn = databaseConnection.beginTransaction();

            Boleta boleta = construirBoleta(context);
            String resumenCliente = String.format("%s - Cliente: %s - Doc: %s", 
                context.getTipoComprobante().name(), 
                context.getNombreCliente(), 
                context.getDocumentoCliente());
            long ventaId = ventaDAO.registrarVenta(conn, boleta, resumenCliente);

            for (DetalleVenta detalle : boleta.getDetalles()) {
                ventaDAO.registrarDetalle(conn, ventaId, detalle);
                productoDAO.descontarStock(conn, detalle.getProducto().getId(), detalle.getCantidad());

                // Observer: Notificar stock crítico directamente
                Producto actualizado = productoDAO.obtenerPorId(conn, detalle.getProducto().getId());
                if (actualizado != null && actualizado.stockBajo()) {
                    notificarStockCritico(actualizado);
                }
            }

            databaseConnection.commit(conn);
        } catch (SQLException | RuntimeException e) {
            databaseConnection.rollback(conn);
            throw new RuntimeException("Error al registrar la venta: " + e.getMessage(), e);
        }
    }

    /**
     * Chain of Responsibility: Validaciones en cadena
     * Valida: Datos del cliente (solo si es FACTURA) -> Carrito -> Autorización supervisor
     */
    private void validarVenta(VentaContext context) {
        // Validación 1: Datos del cliente (SOLO si es FACTURA)
        if (context.getTipoComprobante() == VentaContext.TipoComprobante.FACTURA) {
            if (context.getNombreCliente() == null || context.getNombreCliente().isBlank()) {
                throw new RuntimeException("El nombre del cliente es obligatorio para factura.");
            }
            if (context.getDocumentoCliente() == null || context.getDocumentoCliente().isBlank()) {
                throw new RuntimeException("Debe registrar el documento del cliente (DNI/RUC) para factura.");
            }
        } else {
            // Si es BOLETA, usar valores por defecto si están vacíos
            if (context.getNombreCliente() == null || context.getNombreCliente().isBlank()) {
                context.setNombreCliente("Consumidor Final");
            }
            if (context.getDocumentoCliente() == null || context.getDocumentoCliente().isBlank()) {
                context.setDocumentoCliente("-");
            }
        }

        // Validación 2: Carrito
        if (context.getDetalles().isEmpty()) {
            throw new RuntimeException("Debe agregar al menos un producto al carrito.");
        }
        if (context.getTotal() <= 0) {
            throw new RuntimeException("El total de la venta debe ser mayor a cero.");
        }

        // Validación 3: Autorización supervisor (si aplica)
        if (context.isRequiereAutorizacionSupervisor()) {
            if (context.getCajero() == null || context.getCajero().getRol() == null) {
                throw new RuntimeException("No se pudo validar el rol del usuario.");
            }
            var rol = context.getCajero().getRol();
            if (rol != com.minimarket.security.Rol.SUPERVISOR && rol != com.minimarket.security.Rol.ADMINISTRADOR) {
                throw new RuntimeException("Se requiere aprobación de un supervisor para completar esta venta.");
            }
        }
    }

    /**
     * Observer: Notifica stock crítico a los observadores
     * - Registra en auditoría (LogStockObserver)
     * - Podría actualizar dashboard (DashboardStockObserver)
     */
    private void notificarStockCritico(Producto producto) {
        // Observador 1: Log/Auditoría
        String usuario = UsuarioSesion.getInstance().getUsuarioActual() != null
                ? UsuarioSesion.getInstance().getUsuarioActual().getUsername()
                : "sistema";
        String detalle = String.format("Producto %s con stock crítico (%d unidades)", 
                producto.getNombre(), producto.getStock());
        AuditoriaManager.getInstance().registrarEvento(usuario, "STOCK_CRITICO", detalle);

        // Observador 2: Dashboard (opcional - podría mostrar notificación)
        // UIUtils.registrarNotificacionTemporal("Dashboard", 
        //     "Stock crítico de " + producto.getNombre() + " (" + producto.getStock() + " uds.)");
    }

    private Boleta construirBoleta(VentaContext context) {
        BoletaBuilder builder = new BoletaBuilder()
                .numero(context.getNumeroBoleta())
                .fecha(LocalDateTime.now())
                .cajera(context.getCajero())
                .metodoPago(context.getMetodoPago())
                .observaciones(context.getObservaciones());

        for (DetalleVenta detalle : context.getDetalles()) {
            builder.agregarDetalle(detalle);
        }

        return builder.build();
    }
}

