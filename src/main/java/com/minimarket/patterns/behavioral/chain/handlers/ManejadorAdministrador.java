package com.minimarket.patterns.behavioral.chain.handlers;

import com.minimarket.patterns.behavioral.chain.*;
import com.minimarket.models.Usuario;
import com.minimarket.security.Rol;
import com.minimarket.patterns.creational.DatabaseConnection;
import java.sql.Connection;

/**
 * Manejador para aprobaciones de nivel Administrador
 * Tiene autoridad máxima y puede aprobar cualquier solicitud
 */
public class ManejadorAdministrador extends ManejadorAprobacion {
    
    private Usuario administrador;
    private Connection connection;
    
    public ManejadorAdministrador(Usuario administrador) {
        super(Rol.ADMINISTRADOR);
        this.administrador = administrador;
        this.connection = DatabaseConnection.getInstance().getConnection();
    }
    
    @Override
    protected ResultadoAprobacion procesarAprobacion(SolicitudAprobacion solicitud) {
        System.out.println("🔍 Procesando solicitud en nivel ADMINISTRADOR...");
        System.out.println("   Administrador: " + administrador.getNombreCompleto());
        System.out.println("   Solicitud: " + solicitud.getDescripcion());
        
        // Verificar que el administrador esté activo
        if (!administrador.isActivo()) {
            return ResultadoAprobacion.rechazado(
                "El administrador no está activo en el sistema", 
                "USUARIO_INACTIVO"
            );
        }
        
        // El administrador puede aprobar cualquier tipo de solicitud
        switch (solicitud.getTipo()) {
            case CREAR_USUARIO:
                return procesarCreacionUsuario(solicitud);
                
            case MODIFICAR_CONFIGURACION:
                return procesarModificacionConfiguracion(solicitud);
                
            case BACKUP_SISTEMA:
                return procesarBackupSistema(solicitud);
                
            case MODIFICAR_PRECIO:
                return procesarModificacionPrecioAdmin(solicitud);
                
            case DESCUENTO_ESPECIAL:
                return procesarDescuentoEspecialAdmin(solicitud);
                
            case ANULAR_VENTA:
                return procesarAnulacionVentaAdmin(solicitud);
                
            case ELIMINAR_PRODUCTO:
                return procesarEliminacionProductoAdmin(solicitud);
                
            default:
                // El administrador puede aprobar cualquier solicitud no específica
                return aprobarSolicitudGenerica(solicitud);
        }
    }
    
    /**
     * Procesa solicitud de creación de usuario
     */
    private ResultadoAprobacion procesarCreacionUsuario(SolicitudAprobacion solicitud) {
        String nuevoUsername = solicitud.getParametro("nuevoUsername", String.class);
        Rol nuevoRol = solicitud.getParametro("nuevoRol", Rol.class);
        
        System.out.println("👤 Evaluando creación de usuario:");
        System.out.println("   Nuevo username: " + nuevoUsername);
        System.out.println("   Rol: " + nuevoRol.getDescripcion());
        
        // Validar que el username no exista
        if (existeUsername(nuevoUsername)) {
            return ResultadoAprobacion.rechazado(
                "El username ya existe en el sistema",
                "USERNAME_DUPLICADO"
            );
        }
        
        // Validar creación de administradores (requiere justificación especial)
        if (nuevoRol == Rol.ADMINISTRADOR) {
            String justificacion = solicitud.getParametro("justificacion", String.class);
            if (justificacion == null || justificacion.trim().length() < 20) {
                return ResultadoAprobacion.requiereInformacion(
                    "La creación de administradores requiere justificación detallada",
                    "Justificación de al menos 20 caracteres"
                );
            }
        }
        
        // Registrar la creación del usuario
        registrarCreacionUsuario(nuevoUsername, nuevoRol, solicitud.getUsuarioSolicitante().getUsername());
        
        return ResultadoAprobacion.aprobado(
            "Creación de usuario aprobada por administrador",
            administrador.getNombreCompleto()
        ).conObservacion("Usuario: " + nuevoUsername)
         .conObservacion("Rol: " + nuevoRol.getDescripcion());
    }
    
    /**
     * Procesa solicitud de modificación de configuración
     */
    private ResultadoAprobacion procesarModificacionConfiguracion(SolicitudAprobacion solicitud) {
        String parametro = solicitud.getParametro("parametro", String.class);
        String nuevoValor = solicitud.getParametro("nuevoValor", String.class);
        
        System.out.println("⚙️ Evaluando modificación de configuración:");
        System.out.println("   Parámetro: " + parametro);
        System.out.println("   Nuevo valor: " + nuevoValor);
        
        // Validar parámetros críticos
        if (esParametroCritico(parametro)) {
            String confirmacion = solicitud.getParametro("confirmacion", String.class);
            if (!"CONFIRMADO".equals(confirmacion)) {
                return ResultadoAprobacion.requiereInformacion(
                    "La modificación de parámetros críticos requiere confirmación explícita",
                    "Agregar parámetro 'confirmacion' con valor 'CONFIRMADO'"
                );
            }
        }
        
        // Crear backup antes de modificar configuración crítica
        if (esParametroCritico(parametro)) {
            crearBackupConfiguracion();
        }
        
        return ResultadoAprobacion.aprobado(
            "Modificación de configuración aprobada por administrador",
            administrador.getNombreCompleto()
        ).conObservacion("Parámetro: " + parametro)
         .conObservacion("Nuevo valor: " + nuevoValor)
         .conObservacion(esParametroCritico(parametro) ? "Backup creado automáticamente" : "");
    }
    
    /**
     * Procesa solicitud de backup del sistema
     */
    private ResultadoAprobacion procesarBackupSistema(SolicitudAprobacion solicitud) {
        String tipoBackup = solicitud.getParametro("tipoBackup", String.class);
        
        System.out.println("💾 Evaluando backup del sistema:");
        System.out.println("   Tipo: " + tipoBackup);
        
        // Verificar espacio disponible
        if (!hayEspacioSuficiente()) {
            return ResultadoAprobacion.rechazado(
                "No hay espacio suficiente para realizar el backup",
                "ESPACIO_INSUFICIENTE"
            );
        }
        
        // Programar el backup
        programarBackup(tipoBackup);
        
        return ResultadoAprobacion.aprobado(
            "Backup del sistema aprobado y programado",
            administrador.getNombreCompleto()
        ).conObservacion("Tipo de backup: " + tipoBackup)
         .conObservacion("Programado para ejecución inmediata");
    }
    
    /**
     * Procesa modificación de precio con autoridad de administrador
     */
    private ResultadoAprobacion procesarModificacionPrecioAdmin(SolicitudAprobacion solicitud) {
        java.math.BigDecimal nuevoPrecio = solicitud.getParametro("nuevoPrecio", java.math.BigDecimal.class);
        
        // Los administradores pueden aprobar cualquier precio
        return ResultadoAprobacion.aprobado(
            "Modificación de precio aprobada por administrador (sin límites)",
            administrador.getNombreCompleto()
        ).conObservacion("Precio autorizado: S/ " + nuevoPrecio)
         .conObservacion("Aprobación administrativa - Sin restricciones");
    }
    
    /**
     * Procesa descuento especial con autoridad de administrador
     */
    private ResultadoAprobacion procesarDescuentoEspecialAdmin(SolicitudAprobacion solicitud) {
        java.math.BigDecimal porcentaje = solicitud.getParametro("porcentajeDescuento", java.math.BigDecimal.class);
        
        // Los administradores pueden aprobar cualquier descuento
        return ResultadoAprobacion.aprobado(
            "Descuento especial aprobado por administrador (sin límites)",
            administrador.getNombreCompleto()
        ).conObservacion("Descuento autorizado: " + porcentaje + "%")
         .conObservacion("Aprobación administrativa - Sin restricciones");
    }
    
    /**
     * Procesa anulación de venta con autoridad de administrador
     */
    private ResultadoAprobacion procesarAnulacionVentaAdmin(SolicitudAprobacion solicitud) {
        String numeroBoleta = solicitud.getParametro("numeroBoleta", String.class);
        
        // Los administradores pueden anular cualquier venta
        return ResultadoAprobacion.aprobado(
            "Anulación de venta aprobada por administrador",
            administrador.getNombreCompleto()
        ).conObservacion("Boleta: " + numeroBoleta)
         .conObservacion("Autorización administrativa completa");
    }
    
    /**
     * Procesa eliminación de producto con autoridad de administrador
     */
    private ResultadoAprobacion procesarEliminacionProductoAdmin(SolicitudAprobacion solicitud) {
        String productoId = solicitud.getParametro("productoId", String.class);
        
        // Los administradores pueden eliminar cualquier producto
        return ResultadoAprobacion.aprobado(
            "Eliminación de producto aprobada por administrador",
            administrador.getNombreCompleto()
        ).conObservacion("Producto: " + productoId)
         .conObservacion("Eliminación forzada por administrador");
    }
    
    /**
     * Aprueba solicitudes genéricas
     */
    private ResultadoAprobacion aprobarSolicitudGenerica(SolicitudAprobacion solicitud) {
        return ResultadoAprobacion.aprobado(
            "Solicitud aprobada por administrador",
            administrador.getNombreCompleto()
        ).conObservacion("Tipo: " + solicitud.getTipo().getDescripcion())
         .conObservacion("Aprobación administrativa general");
    }
    
    // Métodos auxiliares
    private boolean existeUsername(String username) {
        // Verificar en base de datos si el username existe
        return false; // Simplificado para el ejemplo
    }
    
    private void registrarCreacionUsuario(String username, Rol rol, String solicitante) {
        System.out.println("📝 Registrando creación de usuario en auditoría...");
        System.out.println("   Username: " + username);
        System.out.println("   Rol: " + rol.getDescripcion());
        System.out.println("   Solicitado por: " + solicitante);
    }
    
    private boolean esParametroCritico(String parametro) {
        String[] parametrosCriticos = {
            "DATABASE_URL", "SECURITY_KEY", "ADMIN_PASSWORD", 
            "BACKUP_LOCATION", "IGV_RATE", "SYSTEM_MODE"
        };
        
        for (String critico : parametrosCriticos) {
            if (parametro.toUpperCase().contains(critico)) {
                return true;
            }
        }
        return false;
    }
    
    private void crearBackupConfiguracion() {
        System.out.println("💾 Creando backup de configuración antes de modificar...");
    }
    
    private boolean hayEspacioSuficiente() {
        // Verificar espacio en disco
        return true; // Simplificado para el ejemplo
    }
    
    private void programarBackup(String tipo) {
        System.out.println("📅 Programando backup tipo: " + tipo);
    }
    
    /**
     * Muestra las capacidades completas del administrador
     */
    public void mostrarCapacidades() {
        System.out.println("=== CAPACIDADES DE APROBACIÓN - ADMINISTRADOR ===");
        System.out.println("Administrador: " + administrador.getNombreCompleto());
        System.out.println("🔑 AUTORIDAD MÁXIMA - Puede aprobar TODO:");
        System.out.println("✅ Creación de usuarios (todos los roles)");
        System.out.println("✅ Modificación de configuración del sistema");
        System.out.println("✅ Backup y restauración del sistema");
        System.out.println("✅ Modificación de precios (sin límites)");
        System.out.println("✅ Descuentos especiales (sin límites)");
        System.out.println("✅ Anulación de ventas (sin restricciones)");
        System.out.println("✅ Eliminación de productos (forzada)");
        System.out.println("✅ Acceso completo a reportes y auditoría");
        System.out.println("✅ Override de cualquier restricción del sistema");
        System.out.println("\n🚨 RESPONSABILIDADES:");
        System.out.println("- Todas las aprobaciones son registradas en auditoría");
        System.out.println("- Cambios críticos requieren confirmación explícita");
        System.out.println("- Backups automáticos antes de cambios importantes");
    }
    
    public Usuario getAdministrador() {
        return administrador;
    }
}
