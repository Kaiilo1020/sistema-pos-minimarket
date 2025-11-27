package com.minimarket.pos;

import com.minimarket.alerts.AlertaManager;
import com.minimarket.billing.MetodoPago;
import com.minimarket.billing.ResultadoTransaccion;
import com.minimarket.billing.VentaManager;
import com.minimarket.inventory.InventarioManager;
import com.minimarket.models.Producto;
import com.minimarket.models.Usuario;
import com.minimarket.patterns.creational.DatabaseConnection;
import com.minimarket.security.AuditoriaManager;
import com.minimarket.security.Rol;
import com.minimarket.security.UsuarioSesion;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Demostración completa del sistema POS con las 4 soluciones implementadas
 */
public class PosIntegrationExample {
    private static final Logger LOGGER = Logger.getLogger(PosIntegrationExample.class.getName());
    private final InventarioManager inventarioManager = new InventarioManager();
    private final VentaManager ventaManager = new VentaManager();
    private final AlertaManager alertaManager = new AlertaManager();

    public static void main(String[] args) {
        // Verificar conexión a BD
        if (!DatabaseConnection.getInstance().testConnection()) {
            LOGGER.severe("No se pudo establecer conexión con la base de datos. Abortando ejemplo.");
            return;
        }

        PosIntegrationExample pos = new PosIntegrationExample();

        LOGGER.info("\n=== DEMOSTRACIÓN SISTEMA POS MINIMARKET ===");
        LOGGER.info("Soluciones implementadas:");
        LOGGER.info("1. RBAC (Roles y Seguridad)");
        LOGGER.info("2. Inventario FIFO Flexible");
        LOGGER.info("3. Integridad de Facturación");
        LOGGER.info("4. Alertas Reactivas");

        // --- 1. DEMO RBAC ---
        LOGGER.info("\n--- 1. DEMOSTRACIÓN RBAC ---");
        pos.demoRBAC();

        // --- 2. DEMO INVENTARIO ---
        LOGGER.info("\n--- 2. DEMOSTRACIÓN INVENTARIO ---");
        pos.demoInventario();

        // --- 3. DEMO FACTURACIÓN ---
        LOGGER.info("\n--- 3. DEMOSTRACIÓN FACTURACIÓN ---");
        pos.demoFacturacion();

        // --- 4. DEMO ALERTAS ---
        LOGGER.info("\n--- 4. DEMOSTRACIÓN ALERTAS ---");
        pos.demoAlertas();

        LOGGER.info("\n=== FIN DE LA DEMOSTRACIÓN ===");
        DatabaseConnection.getInstance().closeConnection();
    }

    private void demoRBAC() {
        // Simular usuarios
        Usuario admin = new Usuario("admin", "pass", "Admin", "Sistema", "admin@minimarket.com", Rol.ADMINISTRADOR);
        admin.setId(1L);
        Usuario cajera = new Usuario("cajera1", "pass", "Ana", "Lopez", "ana@minimarket.com", Rol.CAJERO);
        cajera.setId(3L);

        // Intento de acción administrativa por cajera
        UsuarioSesion.getInstance().login(cajera);
        LOGGER.info("Cajera logueada: " + UsuarioSesion.getInstance().getUsuarioActual().getNombreCompleto());
        if (UsuarioSesion.getInstance().tienePermiso(Rol.ADMINISTRADOR)) {
            LOGGER.info("❌ ERROR: Cajera puede realizar acción administrativa");
        } else {
            LOGGER.info("✅ CORRECTO: Cajera NO puede realizar acción administrativa");
            AuditoriaManager.getInstance().registrarEvento(cajera.getUsername(), "ACCESO_DENEGADO", "Intento de acceso a función administrativa.");
        }
        UsuarioSesion.getInstance().logout();

        // Intento de acción administrativa por admin
        UsuarioSesion.getInstance().login(admin);
        LOGGER.info("Admin logueado: " + UsuarioSesion.getInstance().getUsuarioActual().getNombreCompleto());
        if (UsuarioSesion.getInstance().tienePermiso(Rol.ADMINISTRADOR)) {
            LOGGER.info("✅ CORRECTO: Admin PUEDE realizar acción administrativa");
            AuditoriaManager.getInstance().registrarEvento(admin.getUsername(), "CAMBIO_PRECIO", "Admin cambió precio de producto X.");
        } else {
            LOGGER.info("❌ ERROR: Admin NO puede realizar acción administrativa");
        }
        UsuarioSesion.getInstance().logout();
    }

    private void demoInventario() {
        // Producto que requiere lote (Yogurt Natural - ID 1)
        // Producto que NO requiere lote (Arroz Costeño - ID 2)
        Long idYogurt = 1L;
        Long idArroz = 2L;

        // Vender Yogurt (con lote)
        LOGGER.info("\n--- Venta de Yogurt (con lote) ---");
        Optional<Producto> yogurtOpt = inventarioManager.getProductoById(idYogurt);
        if (yogurtOpt.isPresent()) {
            Producto yogurt = yogurtOpt.get();
            LOGGER.info("Stock inicial de " + yogurt.getNombre() + ": " + yogurt.getStock());
            var resYogurt = inventarioManager.descontarStock(idYogurt, 2, "DEMO_USER");
            LOGGER.info("Resultado venta Yogurt: " + resYogurt.getMensaje() + ". Nuevo stock: " + resYogurt.getNuevoStock());
        }

        // Vender Arroz (sin lote)
        LOGGER.info("\n--- Venta de Arroz (sin lote) ---");
        Optional<Producto> arrozOpt = inventarioManager.getProductoById(idArroz);
        if (arrozOpt.isPresent()) {
            Producto arroz = arrozOpt.get();
            LOGGER.info("Stock inicial de " + arroz.getNombre() + ": " + arroz.getStock());
            var resArroz = inventarioManager.descontarStock(idArroz, 5, "DEMO_USER");
            LOGGER.info("Resultado venta Arroz: " + resArroz.getMensaje() + ". Nuevo stock: " + resArroz.getNuevoStock());
        }
    }

    private void demoFacturacion() {
        // Simular cajera logueada
        Usuario cajera = new Usuario("cajera1", "pass", "Ana", "Lopez", "ana@minimarket.com", Rol.CAJERO);
        cajera.setId(3L);
        UsuarioSesion.getInstance().login(cajera);

        // Productos para la venta
        Producto p1 = new Producto();
        p1.setId(2L); 
        p1.setNombre("Arroz Costeño"); 
        p1.setPrecio(new BigDecimal("5.50")); 
        p1.setStock(1); // Stock aquí es cantidad a vender

        Producto p2 = new Producto();
        p2.setId(4L); 
        p2.setNombre("Gaseosa Coca Cola"); 
        p2.setPrecio(new BigDecimal("3.00")); 
        p2.setStock(2);

        List<Producto> productosVenta = Arrays.asList(p1, p2);

        // Registrar venta con método de pago y referencia
        ResultadoTransaccion resTransaccion = ventaManager.registrarVenta(
                cajera.getId(),
                productosVenta,
                MetodoPago.TARJETA_CREDITO,
                "TRX123456789"
        );
        LOGGER.info("Resultado de la transacción: " + resTransaccion.getMensaje());
        UsuarioSesion.getInstance().logout();
    }

    private void demoAlertas() {
        // Simular una venta que deja el stock en nivel crítico
        Long productoId = 2L; // Arroz Costeño
        Optional<Producto> productoOpt = inventarioManager.getProductoById(productoId);
        if (productoOpt.isEmpty()) {
            LOGGER.warning("No se encontró el producto para demo de alertas.");
            return;
        }

        int stockInicial = productoOpt.get().getStock();
        LOGGER.info("Stock inicial de Arroz Costeño: " + stockInicial);

        // Vender una cantidad que deje el stock por debajo del umbral (ej. 8 unidades)
        int cantidadAVender = Math.max(1, stockInicial - 8); // Deja 8 unidades o menos
        
        var res = inventarioManager.descontarStock(productoId, cantidadAVender, "DEMO_ALERTA_USER");
        LOGGER.info("Resultado venta para alerta: " + res.getMensaje() + ". Nuevo stock: " + res.getNuevoStock());

        // Verificar si se debe disparar alerta
        alertaManager.verificarStockCritico(productoId, res.getNuevoStock(), "DEMO_ALERTA_USER");
    }
}
