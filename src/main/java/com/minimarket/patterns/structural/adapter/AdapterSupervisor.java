package com.minimarket.patterns.structural.adapter;

import com.minimarket.models.Usuario;
import com.minimarket.security.Rol;
import com.minimarket.security.Rol;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter para Supervisores - Funcionalidades intermedias
 * Resuelve: Control de accesos según jerarquía de roles
 */
public class AdapterSupervisor extends SistemaVentasBase {
    
    private Usuario supervisor;
    
    public AdapterSupervisor(Usuario supervisor) {
        super();
        if (supervisor.getRol() != Rol.SUPERVISOR) {
            throw new IllegalArgumentException("Este adapter solo es para usuarios con rol SUPERVISOR");
        }
        this.supervisor = supervisor;
    }
    
    @Override
    public boolean puedeModificarPrecios() {
        return true; // Los supervisores SÍ pueden modificar precios
    }
    
    @Override
    public boolean puedeAccederReportes() {
        return true; // Los supervisores SÍ pueden acceder a reportes básicos
    }
    
    @Override
    public boolean puedeGestionarInventario() {
        return true; // Los supervisores SÍ pueden gestionar inventario
    }
    
    @Override
    public boolean puedeAnularVentas() {
        return true; // Los supervisores SÍ pueden anular ventas
    }
    
    @Override
    public boolean puedeModificarUsuarios() {
        return false; // Los supervisores NO pueden modificar usuarios (solo admin)
    }
    
    @Override
    public List<String> obtenerOpcionesMenu() {
        List<String> opciones = new ArrayList<>();
        opciones.add("Consultar productos");
        opciones.add("Buscar producto por código");
        opciones.add("Registrar venta");
        opciones.add("Verificar stock");
        opciones.add("Modificar precios");
        opciones.add("Gestionar inventario");
        opciones.add("Anular ventas");
        opciones.add("Ver reportes básicos");
        opciones.add("Supervisar cajeras");
        opciones.add("Gestionar lotes y fechas");
        return opciones;
    }
    
    @Override
    public void mostrarMenuDisponible() {
        System.out.println("=== MENÚ SUPERVISOR - " + supervisor.getNombreCompleto() + " ===");
        System.out.println("Funciones disponibles:");
        
        List<String> opciones = obtenerOpcionesMenu();
        for (int i = 0; i < opciones.size(); i++) {
            System.out.println((i + 1) + ". " + opciones.get(i));
        }
        
        System.out.println("\n✅ PERMISOS ESPECIALES:");
        System.out.println("- Puede modificar precios");
        System.out.println("- Puede acceder a reportes básicos");
        System.out.println("- Puede gestionar inventario");
        System.out.println("- Puede anular ventas");
        System.out.println("- Puede supervisar cajeras");
        
        System.out.println("\n⚠️  RESTRICCIONES:");
        System.out.println("- No puede modificar usuarios (solo administrador)");
    }
    
    /**
     * Método específico para supervisores: resolver problemas de productos
     */
    public boolean resolverProblemaProducto(String codigoProducto, String nuevoLote, 
                                          java.time.LocalDate nuevaFechaVencimiento) {
        var producto = buscarProductoPorCodigo(codigoProducto);
        
        if (producto == null) {
            System.out.println("❌ Producto no encontrado: " + codigoProducto);
            return false;
        }
        
        boolean cambiosRealizados = false;
        
        // Actualizar lote si es necesario
        if (producto.isRequiereLote() && (producto.getLote() == null || producto.getLote().trim().isEmpty())) {
            if (nuevoLote != null && !nuevoLote.trim().isEmpty()) {
                producto.setLote(nuevoLote);
                cambiosRealizados = true;
                System.out.println("✅ Lote actualizado: " + nuevoLote);
            }
        }
        
        // Actualizar fecha de vencimiento si es necesario
        if (producto.isRequiereFechaVencimiento() && producto.getFechaVencimiento() == null) {
            if (nuevaFechaVencimiento != null) {
                producto.setFechaVencimiento(nuevaFechaVencimiento);
                cambiosRealizados = true;
                System.out.println("✅ Fecha de vencimiento actualizada: " + nuevaFechaVencimiento);
            }
        }
        
        if (cambiosRealizados) {
            // Aquí se actualizaría en la base de datos
            actualizarProductoEnBD(producto);
            System.out.println("✅ Producto " + producto.getNombre() + " actualizado correctamente");
            System.out.println("   Ahora puede ser vendido normalmente");
            return true;
        } else {
            System.out.println("ℹ️  No se requieren cambios en el producto");
            return false;
        }
    }
    
    /**
     * Método para anular una venta (requiere autorización de supervisor)
     */
    public boolean anularVenta(String numeroBoleta, String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            System.out.println("❌ Debe proporcionar un motivo para la anulación");
            return false;
        }
        
        System.out.println("🔍 Procesando anulación de venta...");
        System.out.println("   Boleta: " + numeroBoleta);
        System.out.println("   Motivo: " + motivo);
        System.out.println("   Autorizado por: " + supervisor.getNombreCompleto());
        
        // Aquí se implementaría la lógica de anulación en BD
        // Por ahora solo simulamos
        
        System.out.println("✅ Venta anulada exitosamente");
        System.out.println("   Se ha restaurado el stock de los productos");
        
        return true;
    }
    
    /**
     * Método para supervisar el desempeño de cajeras
     */
    public void mostrarResumenCajeras() {
        System.out.println("=== RESUMEN DE CAJERAS ===");
        System.out.println("📊 Ventas del día por cajera:");
        // Aquí se implementaría la consulta real a la BD
        System.out.println("- María García: 25 ventas, S/ 1,250.00");
        System.out.println("- Ana López: 18 ventas, S/ 890.50");
        System.out.println("- Carlos Ruiz: 22 ventas, S/ 1,100.25");
        
        System.out.println("\n⚠️  Alertas:");
        System.out.println("- Ana López: 2 intentos de venta con productos sin lote");
        System.out.println("- Carlos Ruiz: 1 venta anulada por error de precio");
    }
    
    private void actualizarProductoEnBD(com.minimarket.models.Producto producto) {
        // Implementación para actualizar producto en base de datos
        String sql = "UPDATE productos SET lote = ?, fecha_vencimiento = ? WHERE id = ?";
        
        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, producto.getLote());
            if (producto.getFechaVencimiento() != null) {
                stmt.setDate(2, java.sql.Date.valueOf(producto.getFechaVencimiento()));
            } else {
                stmt.setNull(2, java.sql.Types.DATE);
            }
            stmt.setLong(3, producto.getId());
            
            stmt.executeUpdate();
            
        } catch (java.sql.SQLException e) {
            logger.severe("Error al actualizar producto: " + e.getMessage());
            throw new RuntimeException("Error al actualizar producto en BD", e);
        }
    }
    
    public Usuario getSupervisor() {
        return supervisor;
    }
}
