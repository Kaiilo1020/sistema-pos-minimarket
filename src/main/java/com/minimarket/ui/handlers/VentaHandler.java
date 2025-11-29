package com.minimarket.ui.handlers;

import com.minimarket.dao.ProductoDAO;
import com.minimarket.model.Boleta;
import com.minimarket.model.DetalleVenta;
import com.minimarket.model.Producto;
import com.minimarket.security.UsuarioSesion;
import com.minimarket.service.ReportePDFService;
import com.minimarket.service.venta.VentaContext;
import com.minimarket.service.venta.VentaService;
import com.minimarket.ui.util.UIUtils;

import java.awt.Component;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Handler que coordina la lógica de negocio del módulo de ventas.
 * Separa la lógica de la UI, siguiendo el patrón de arquitectura limpia.
 */
public class VentaHandler {
    
    private final VentaService ventaService;
    private final ProductoDAO productoDAO;
    private final ReportePDFService reportePDFService;
    
    // Estado del carrito
    private final Map<Long, Producto> catalogoProductos;
    private final List<DetalleVenta> carrito;
    private BigDecimal totalVenta;
    private VentaContext ultimaVentaRegistrada; // Para imprimir
    
    public VentaHandler() {
        this.ventaService = new VentaService();
        this.productoDAO = new ProductoDAO();
        this.reportePDFService = new ReportePDFService();
        this.catalogoProductos = new HashMap<>();
        this.carrito = new ArrayList<>();
        this.totalVenta = BigDecimal.ZERO;
    }
    
    /**
     * Carga productos disponibles para el catálogo
     */
    public List<Producto> cargarProductos(String filtro) {
        try {
            Connection conn = com.minimarket.config.DatabaseConnection.getInstance().getConnection();
            List<Producto> productos = productoDAO.listarActivos(conn, filtro);
            catalogoProductos.clear();
            for (Producto p : productos) {
                catalogoProductos.put(p.getId(), p);
            }
            return productos;
        } catch (SQLException e) {
            throw new RuntimeException("Error al cargar productos: " + e.getMessage(), e);
        }
    }
    
    /**
     * Agrega un producto al carrito
     * Retorna el producto para que el panel pueda evaluar decoración
     */
    public Producto agregarAlCarrito(Producto producto, int cantidad) {
        if (producto == null || cantidad <= 0) {
            return producto;
        }
        
        // Validación crítica: Verificar que el producto puede venderse
        if (!producto.puedeVenderse()) {
            String motivo = "";
            if (producto.getFechaVencimiento() != null && producto.getFechaVencimiento().isBefore(java.time.LocalDate.now())) {
                motivo = "El producto está VENCIDO (venció el " + producto.getFechaVencimiento() + ").";
            } else if (producto.isRequiereLote() && (producto.getLote() == null || producto.getLote().trim().isEmpty())) {
                motivo = "El producto requiere un lote y no tiene uno asignado.";
            } else if (producto.isRequiereFechaVencimiento() && producto.getFechaVencimiento() == null) {
                motivo = "El producto requiere fecha de vencimiento y no tiene una asignada.";
            } else if (producto.getStock() == null || producto.getStock() <= 0) {
                motivo = "El producto no tiene stock disponible.";
            } else {
                motivo = "El producto no puede ser vendido.";
            }
            throw new IllegalStateException("No se puede agregar este producto al carrito. " + motivo);
        }
        
        // Verificar stock disponible
        int stockDisponible = producto.getStock() != null ? producto.getStock() : 0;
        int cantidadEnCarrito = obtenerCantidadEnCarrito(producto.getId());
        int stockRestante = stockDisponible - cantidadEnCarrito;
        
        if (stockRestante < cantidad) {
            throw new IllegalStateException("No hay suficiente stock disponible. Stock restante: " + stockRestante);
        }
        
        // Buscar si ya existe en el carrito
        Optional<DetalleVenta> detalleExistente = carrito.stream()
            .filter(d -> d.getProducto() != null && d.getProducto().getId().equals(producto.getId()))
            .findFirst();
        
        if (detalleExistente.isPresent()) {
            // Actualizar cantidad
            DetalleVenta detalle = detalleExistente.get();
            detalle.setCantidad(detalle.getCantidad() + cantidad);
        } else {
            // Agregar nuevo detalle
            BigDecimal precio = producto.getPrecio() != null ? producto.getPrecio() : BigDecimal.ZERO;
            DetalleVenta nuevoDetalle = new DetalleVenta(producto, cantidad, precio);
            carrito.add(nuevoDetalle);
        }
        
        calcularTotal();
        return producto; // Retornar para evaluación de decoración
    }
    
    /**
     * Quita un producto del carrito
     */
    public void quitarDelCarrito(Long productoId) {
        carrito.removeIf(d -> d.getProducto() != null && d.getProducto().getId().equals(productoId));
        calcularTotal();
    }
    
    /**
     * Limpia el carrito completamente
     */
    public void limpiarCarrito() {
        carrito.clear();
        totalVenta = BigDecimal.ZERO;
    }
    
    /**
     * Actualiza la cantidad de un producto en el carrito
     */
    public void actualizarCantidadCarrito(Long productoId, int nuevaCantidad) {
        if (nuevaCantidad <= 0) {
            quitarDelCarrito(productoId);
            return;
        }
        
        Optional<DetalleVenta> detalle = carrito.stream()
            .filter(d -> d.getProducto() != null && d.getProducto().getId().equals(productoId))
            .findFirst();
        
        if (detalle.isPresent()) {
            Producto producto = detalle.get().getProducto();
            int stockDisponible = producto.getStock() != null ? producto.getStock() : 0;
            
            if (nuevaCantidad > stockDisponible) {
                throw new IllegalStateException("No hay suficiente stock. Stock disponible: " + stockDisponible);
            }
            
            detalle.get().setCantidad(nuevaCantidad);
            calcularTotal();
        }
    }
    
    /**
     * Registra la venta completa
     */
    public void registrarVenta(String tipoComprobante, String nombreCliente, String documentoCliente, 
                               String metodoPago, String observaciones, 
                               Component parent) {
        try {
            // Crear contexto de venta
            VentaContext context = new VentaContext();
            context.setNumeroBoleta(generarNumeroVenta());
            context.setCajero(UsuarioSesion.getInstance().getUsuarioActual());
            context.setTipoComprobante(VentaContext.TipoComprobante.valueOf(tipoComprobante));
            context.setNombreCliente(nombreCliente);
            context.setDocumentoCliente(documentoCliente);
            context.setMetodoPago(Boleta.MetodoPago.valueOf(metodoPago));
            for (DetalleVenta detalle : carrito) {
                context.agregarDetalle(detalle);
            }
            context.setTotal(totalVenta.doubleValue());
            context.setObservaciones(observaciones);
            
            // Registrar venta (las validaciones están dentro de VentaService)
            ventaService.registrarVenta(context);
            
            // Guardar total ANTES de limpiar el carrito para el mensaje
            double totalRegistrado = context.getTotal();
            
            // Guardar última venta para imprimir
            this.ultimaVentaRegistrada = context;
            
            // Limpiar carrito
            limpiarCarrito();
            
            UIUtils.mostrarExito(parent, 
                "Venta registrada exitosamente.\nNúmero: " + context.getNumeroBoleta() + 
                "\nTotal: S/" + String.format("%.2f", totalRegistrado));
            
        } catch (Exception e) {
            UIUtils.mostrarError(parent, "Error al registrar la venta: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Command Pattern: Ejecuta la acción de anular (limpiar carrito)
     */
    public void ejecutarAnular(Component parent) {
        limpiarCarrito();
    }
    
    /**
     * Command Pattern: Ejecuta la acción de imprimir recibo
     */
    public void ejecutarImprimir(Component parent) {
        if (ultimaVentaRegistrada == null) {
            UIUtils.mostrarError(parent, "No existe una venta reciente para imprimir.");
            return;
        }
        reportePDFService.generarReciboPOS(
            ultimaVentaRegistrada.getNumeroBoleta(),
            ultimaVentaRegistrada.getNombreCliente(),
            ultimaVentaRegistrada.getTotal()
        );
    }
    
    /**
     * Calcula el total de la venta
     */
    private void calcularTotal() {
        totalVenta = carrito.stream()
            .map(DetalleVenta::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private int obtenerCantidadEnCarrito(Long productoId) {
        return carrito.stream()
            .filter(d -> d.getProducto() != null && d.getProducto().getId().equals(productoId))
            .mapToInt(DetalleVenta::getCantidad)
            .sum();
    }
    
    private String generarNumeroVenta() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String fecha = sdf.format(new Date());
        long timestamp = System.currentTimeMillis() % 10000;
        return "VTA-" + fecha + "-" + String.format("%04d", timestamp);
    }
    
    // Getters para el estado
    public List<DetalleVenta> getCarrito() {
        return new ArrayList<>(carrito);
    }
    
    public BigDecimal getTotalVenta() {
        return totalVenta;
    }
    
    public Producto obtenerProducto(Long productoId) {
        return catalogoProductos.get(productoId);
    }
}

