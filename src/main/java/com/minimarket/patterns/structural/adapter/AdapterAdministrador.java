package com.minimarket.patterns.structural.adapter;

import com.minimarket.models.Usuario;
import com.minimarket.security.Rol;
import com.minimarket.security.Rol;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter para Administradores - Acceso completo al sistema
 * Resuelve: Control total de funcionalidades para el rol más alto
 */
public class AdapterAdministrador extends SistemaVentasBase {
    
    private Usuario administrador;
    
    public AdapterAdministrador(Usuario administrador) {
        super();
        if (administrador.getRol() != Rol.ADMINISTRADOR) {
            throw new IllegalArgumentException("Este adapter solo es para usuarios con rol ADMINISTRADOR");
        }
        this.administrador = administrador;
    }
    
    @Override
    public boolean puedeModificarPrecios() {
        return true; // Los administradores tienen acceso completo
    }
    
    @Override
    public boolean puedeAccederReportes() {
        return true; // Los administradores tienen acceso completo
    }
    
    @Override
    public boolean puedeGestionarInventario() {
        return true; // Los administradores tienen acceso completo
    }
    
    @Override
    public boolean puedeAnularVentas() {
        return true; // Los administradores tienen acceso completo
    }
    
    @Override
    public boolean puedeModificarUsuarios() {
        return true; // Solo los administradores pueden modificar usuarios
    }
    
    @Override
    public List<String> obtenerOpcionesMenu() {
        List<String> opciones = new ArrayList<>();
        opciones.add("Consultar productos");
        opciones.add("Buscar producto por código");
        opciones.add("Registrar venta");
        opciones.add("Verificar stock");
        opciones.add("Modificar precios");
        opciones.add("Gestionar inventario completo");
        opciones.add("Anular ventas");
        opciones.add("Ver reportes completos");
        opciones.add("Gestionar usuarios");
        opciones.add("Configurar sistema");
        opciones.add("Auditoría de operaciones");
        opciones.add("Respaldos y restauración");
        return opciones;
    }
    
    @Override
    public void mostrarMenuDisponible() {
        System.out.println("=== MENÚ ADMINISTRADOR - " + administrador.getNombreCompleto() + " ===");
        System.out.println("🔑 ACCESO COMPLETO AL SISTEMA");
        
        List<String> opciones = obtenerOpcionesMenu();
        for (int i = 0; i < opciones.size(); i++) {
            System.out.println((i + 1) + ". " + opciones.get(i));
        }
        
        System.out.println("\n✅ PERMISOS COMPLETOS:");
        System.out.println("- Modificar precios y productos");
        System.out.println("- Acceso a reportes completos");
        System.out.println("- Gestión completa de inventario");
        System.out.println("- Anular cualquier venta");
        System.out.println("- Gestionar usuarios y roles");
        System.out.println("- Configurar parámetros del sistema");
        System.out.println("- Auditoría completa de operaciones");
    }
    
    /**
     * Método exclusivo para administradores: gestionar usuarios
     */
    public boolean crearUsuario(String username, String password, String nombre, 
                              String apellido, String email, Rol rol) {
        
        System.out.println("🔧 Creando nuevo usuario...");
        System.out.println("   Username: " + username);
        System.out.println("   Nombre: " + nombre + " " + apellido);
        System.out.println("   Email: " + email);
        System.out.println("   Rol: " + rol.getDescripcion());
        System.out.println("   Creado por: " + administrador.getNombreCompleto());
        
        // Validaciones
        if (username == null || username.trim().isEmpty()) {
            System.out.println("❌ Username es requerido");
            return false;
        }
        
        if (password == null || password.length() < 6) {
            System.out.println("❌ Password debe tener al menos 6 caracteres");
            return false;
        }
        
        // Aquí se implementaría la creación en BD
        Usuario nuevoUsuario = new Usuario(username, password, nombre, apellido, email, rol);
        
        System.out.println("✅ Usuario creado exitosamente");
        return true;
    }
    
    /**
     * Método para generar reportes completos
     */
    public void generarReporteCompleto() {
        System.out.println("=== REPORTE COMPLETO DEL SISTEMA ===");
        System.out.println("📅 Fecha: " + java.time.LocalDate.now());
        System.out.println("👤 Generado por: " + administrador.getNombreCompleto());
        
        System.out.println("\n📊 VENTAS:");
        System.out.println("- Ventas del día: 65 transacciones");
        System.out.println("- Total vendido: S/ 3,240.75");
        System.out.println("- Promedio por venta: S/ 49.86");
        
        System.out.println("\n📦 INVENTARIO:");
        System.out.println("- Productos en stock: 1,247");
        System.out.println("- Productos con stock bajo: 23");
        System.out.println("- Productos próximos a vencer: 8");
        System.out.println("- Productos sin lote: 5");
        
        System.out.println("\n👥 USUARIOS:");
        System.out.println("- Cajeras activas: 3");
        System.out.println("- Supervisores activos: 1");
        System.out.println("- Administradores activos: 1");
        
        System.out.println("\n⚠️  ALERTAS CRÍTICAS:");
        System.out.println("- 5 productos requieren actualización de lote");
        System.out.println("- 2 cajeras con más de 3 errores en el día");
        System.out.println("- 1 producto vencido en inventario");
    }
    
    /**
     * Método para configurar parámetros del sistema
     */
    public void configurarSistema(String parametro, String valor) {
        System.out.println("🔧 Configurando sistema...");
        System.out.println("   Parámetro: " + parametro);
        System.out.println("   Nuevo valor: " + valor);
        System.out.println("   Configurado por: " + administrador.getNombreCompleto());
        
        // Aquí se implementaría la actualización en BD
        switch (parametro.toLowerCase()) {
            case "stock_minimo":
                System.out.println("✅ Stock mínimo actualizado a: " + valor + " unidades");
                break;
            case "dias_vencimiento_alerta":
                System.out.println("✅ Alerta de vencimiento configurada para: " + valor + " días");
                break;
            case "igv":
                System.out.println("✅ IGV actualizado a: " + valor + "%");
                break;
            default:
                System.out.println("⚠️  Parámetro no reconocido: " + parametro);
        }
    }
    
    /**
     * Método para auditoría de operaciones
     */
    public void mostrarAuditoria() {
        System.out.println("=== AUDITORÍA DE OPERACIONES ===");
        System.out.println("📋 Últimas 10 operaciones críticas:");
        
        System.out.println("1. 14:30 - María García - Venta BOL-001234 - S/ 45.50");
        System.out.println("2. 14:25 - Supervisor Juan - Modificó precio producto P001");
        System.out.println("3. 14:20 - Ana López - Intentó venta sin lote (BLOQUEADA)");
        System.out.println("4. 14:15 - Admin " + administrador.getNombre() + " - Creó usuario carlos.ruiz");
        System.out.println("5. 14:10 - Supervisor Juan - Anuló venta BOL-001230");
        System.out.println("6. 14:05 - María García - Venta BOL-001229 - S/ 78.25");
        System.out.println("7. 14:00 - Sistema - Alerta stock bajo: Producto P045");
        System.out.println("8. 13:55 - Ana López - Venta BOL-001228 - S/ 23.75");
        System.out.println("9. 13:50 - Supervisor Juan - Actualizó lote producto P067");
        System.out.println("10. 13:45 - María García - Venta BOL-001227 - S/ 156.00");
        
        System.out.println("\n🔍 Estadísticas de seguridad:");
        System.out.println("- Intentos de acceso no autorizado: 0");
        System.out.println("- Operaciones bloqueadas por permisos: 3");
        System.out.println("- Ventas anuladas: 1");
        System.out.println("- Modificaciones de precios: 2");
    }
    
    public Usuario getAdministrador() {
        return administrador;
    }
}
