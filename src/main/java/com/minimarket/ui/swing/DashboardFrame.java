package com.minimarket.ui.swing;

import com.minimarket.security.UsuarioSesion;
import com.minimarket.security.Rol;
import com.minimarket.model.Usuario;
import com.minimarket.ui.util.UIUtils;
import com.minimarket.service.DashboardService;
import com.minimarket.service.ReportePDFService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Dashboard principal del sistema con Swing
 * Diseño moderno inspirado en la imagen referencial
 */
public class DashboardFrame extends JFrame {
    
    // Colores del tema
    private static final Color SECONDARY_COLOR = new Color(245, 247, 250);
    private static final Color TEXT_PRIMARY = new Color(44, 62, 80);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    
    // Componentes principales
    private JLabel lblUsuario;
    private JLabel lblFechaHora;
    private JPanel mainContentArea;
    private CardLayout cardLayout;
    
    // Sistema de navegación
    private java.util.List<JButton> sidebarButtons = new java.util.ArrayList<>();
    private JButton currentActiveButton;
    
    private Timer reloj;
    private Timer actualizadorDashboard;
    
    // Usuario actual de la sesión
    private Usuario usuarioActual;

    public DashboardFrame() {
        System.out.println("=== INICIANDO DASHBOARD ===");
        
        // Obtener usuario actual de la sesión
        usuarioActual = UsuarioSesion.getInstance().getUsuarioActual();
        
        System.out.println("Usuario en sesión: " + (usuarioActual != null ? usuarioActual.getUsername() : "null"));
        
        // Verificar que hay un usuario logueado
        if (usuarioActual == null) {
            System.out.println("ERROR: No hay usuario en sesión, regresando al login");
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame());
            return;
        }
        
        System.out.println("Usuario válido: " + usuarioActual.getUsername() + " - Rol: " + usuarioActual.getRol());
        
        try {
            System.out.println("Inicializando componentes...");
            initializeComponents();
            
            System.out.println("Configurando layout...");
            setupLayout();
            
            System.out.println("Configurando event handlers...");
            setupEventHandlers();
            
            System.out.println("Iniciando reloj...");
            startClock();
            
            System.out.println("Auto-refresh deshabilitado para evitar navegación automática");
            // startDashboardAutoRefresh(); // DESHABILITADO temporalmente
            
            System.out.println("Dashboard inicializado correctamente");
        } catch (Exception e) {
            System.out.println("ERROR al inicializar dashboard: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-lanzar para que se maneje en el login
        }
    }

    private void initializeComponents() {
        setTitle("MiniMarket Pro - Dashboard de Patrones de Diseño");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        // Asegurar que no hay barras extra
        setResizable(true);
        
        // Configurar icono
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(
                getClass().getResource("/images/minimarket-logo.png")));
        } catch (Exception e) {
            // Continuar sin icono
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Sidebar
        add(createSidebarPanel(), BorderLayout.WEST);
        
        // Main content con CardLayout
        cardLayout = new CardLayout();
        mainContentArea = new JPanel(cardLayout);
        
        // Agregar paneles según permisos del rol
        Rol rolUsuario = usuarioActual != null ? usuarioActual.getRol() : Rol.CAJERO;
        
        // INICIO - Todos pueden ver (pero contenido diferente según rol)
        mainContentArea.add(createDashboardPanel(), "inicio");
        
        // CAJA/POS - Todos pueden usar
        mainContentArea.add(createPOSPanel(), "pos");
        
        // INVENTARIO - Todos pueden ver (modo diferente según rol)
        mainContentArea.add(createInventarioPanel(), "inventario");
        
        // HISTORIAL - Solo SUPERVISOR y ADMINISTRADOR
        if (rolUsuario == Rol.SUPERVISOR || rolUsuario == Rol.ADMINISTRADOR) {
            mainContentArea.add(createHistorialPanel(), "historial");
        }
        
        // REPORTE - Solo SUPERVISOR y ADMINISTRADOR
        if (rolUsuario == Rol.SUPERVISOR || rolUsuario == Rol.ADMINISTRADOR) {
            mainContentArea.add(createReportePanel(), "reporte");
        }
        
        // USUARIOS - Solo ADMINISTRADOR
        if (rolUsuario == Rol.ADMINISTRADOR) {
            mainContentArea.add(createUsuariosPanel(), "usuarios");
        }
        
        // ALERTAS - Todos pueden ver (modo diferente según rol)
        mainContentArea.add(createAlertasPanel(), "alertas");
        
        add(mainContentArea, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BACKGROUND);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 232, 237)),
            new EmptyBorder(15, 25, 15, 25)
        ));
        
        // Logo
        JLabel logo = new JLabel("MINIMARKET PRO");
        logo.setFont(UIUtils.HEADER_FONT);
        logo.setForeground(TEXT_PRIMARY);
        
        // User info panel
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setBackground(CARD_BACKGROUND);
        
        lblFechaHora = new JLabel();
        lblFechaHora.setFont(UIUtils.DEFAULT_FONT);
        lblFechaHora.setForeground(TEXT_SECONDARY);
        
        String nombreUsuario = usuarioActual != null ? usuarioActual.getNombreCompleto() : "Usuario";
        String rolUsuario = usuarioActual != null ? usuarioActual.getRol().getDescripcion() : "Sin rol";
        lblUsuario = new JLabel("👤 " + nombreUsuario + " (" + rolUsuario + ")");
        lblUsuario.setFont(UIUtils.BOLD_FONT);
        lblUsuario.setForeground(TEXT_PRIMARY);
        
        JButton btnPerfil = createHeaderButton("👤");
        JButton btnAyuda = createHeaderButton("❓");
        JButton btnSalir = createHeaderButton("🚪");
        
        btnSalir.addActionListener(e -> {
            if (UIUtils.confirmar(this, "¿Estás seguro de que quieres cerrar sesión?")) {
                UsuarioSesion.getInstance().logout();
                dispose();
                new LoginFrame();
            }
        });
        
        userPanel.add(lblFechaHora);
        userPanel.add(lblUsuario);
        userPanel.add(btnPerfil);
        userPanel.add(btnAyuda);
        userPanel.add(btnSalir);
        
        header.add(logo, BorderLayout.WEST);
        header.add(userPanel, BorderLayout.EAST);
        
        return header;
    }

    private JButton createHeaderButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(35, 35));
        button.setBackground(new Color(0, 0, 0, 0));
        button.setBorder(BorderFactory.createLineBorder(new Color(222, 226, 230)));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(CARD_BACKGROUND);
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(225, 232, 237)));
        
        // Título de navegación
        JLabel navTitle = new JLabel("NAVEGACIÓN");
        navTitle.setFont(UIUtils.DEFAULT_FONT);
        navTitle.setForeground(new Color(142, 154, 175));
        navTitle.setBorder(new EmptyBorder(20, 20, 15, 20));
        sidebar.add(navTitle);
        
        // Botones según permisos del rol
        Rol rolUsuario = usuarioActual != null ? usuarioActual.getRol() : Rol.CAJERO;
        
        // INICIO - Todos los roles pueden ver (pero contenido diferente)
        JButton btnInicio = createSidebarButton("🏠 Inicio", "inicio", true);
        sidebar.add(btnInicio);
        sidebarButtons.add(btnInicio);
        
        // CAJA/POS - Todos los roles pueden usar
        JButton btnPOS = createSidebarButton("💰 Caja / Punto de Venta", "pos", false);
        sidebar.add(btnPOS);
        sidebarButtons.add(btnPOS);
        
        // INVENTARIO - Todos pueden ver (CAJERO solo lectura, otros con permisos completos)
        String inventarioText = rolUsuario == Rol.CAJERO ? "📦 Consultar Inventario" : "📦 Inventario y Kardex";
        JButton btnInventario = createSidebarButton(inventarioText, "inventario", false);
        sidebar.add(btnInventario);
        sidebarButtons.add(btnInventario);
        
        // HISTORIAL DE VENTAS - Solo SUPERVISOR y ADMINISTRADOR
        if (rolUsuario == Rol.SUPERVISOR || rolUsuario == Rol.ADMINISTRADOR) {
            JButton btnHistorial = createSidebarButton("📋 Historial de Ventas", "historial", false);
            sidebar.add(btnHistorial);
            sidebarButtons.add(btnHistorial);
        }
        
        // REPORTE DIARIO - Solo SUPERVISOR y ADMINISTRADOR
        if (rolUsuario == Rol.SUPERVISOR || rolUsuario == Rol.ADMINISTRADOR) {
            JButton btnReporte = createSidebarButton("📊 Reporte Diario", "reporte", false);
            sidebar.add(btnReporte);
            sidebarButtons.add(btnReporte);
        }
        
        // USUARIOS Y PERMISOS - Solo ADMINISTRADOR
        if (rolUsuario == Rol.ADMINISTRADOR) {
            JButton btnUsuarios = createSidebarButton("👥 Usuarios y Permisos", "usuarios", false);
            sidebar.add(btnUsuarios);
            sidebarButtons.add(btnUsuarios);
        }
        
        // ALERTAS DE VENCIMIENTO - Todos pueden ver (CAJERO solo lectura, otros con permisos completos)
        String alertasText = rolUsuario == Rol.CAJERO ? "⚠️ Consultar Alertas" : "⚠️ Alertas de Vencimiento";
        JButton btnAlertas = createSidebarButton(alertasText, "alertas", false);
        sidebar.add(btnAlertas);
        sidebarButtons.add(btnAlertas);
        
        // Establecer el botón inicial como activo
        currentActiveButton = btnInicio;
        
        // Espaciador
        sidebar.add(Box.createVerticalGlue());
        
        // Información del usuario actual
        sidebar.add(createUserInfoPanel());
        
        return sidebar;
    }

    private JButton createSidebarButton(String text, String action, boolean active) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(280, 45));
        button.setPreferredSize(new Dimension(280, 45));
        button.setFont(UIUtils.DEFAULT_FONT);
        button.setBorder(new EmptyBorder(12, 20, 12, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (active) {
            button.setBackground(new Color(227, 242, 253));
            button.setForeground(new Color(25, 118, 210));
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 3, new Color(25, 118, 210)),
                new EmptyBorder(12, 20, 12, 17)
            ));
        } else {
            button.setBackground(new Color(0, 0, 0, 0));
            button.setForeground(TEXT_PRIMARY);
        }
        
        // Agregar acción al botón
        button.addActionListener(e -> {
            cambiarPanel(action);
            setActiveButton(button);
        });
        
        return button;
    }

    private JScrollPane createMainContentPanel() {
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(SECONDARY_COLOR);
        mainContent.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        // Botón de actualización
        JPanel updatePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        updatePanel.setBackground(SECONDARY_COLOR);
        
        JButton btnActualizar = new JButton("🔄 Actualizar Datos");
        UIUtils.configurarBotonSecundario(btnActualizar);
        btnActualizar.addActionListener(e -> actualizarDashboard());
        updatePanel.add(btnActualizar);
        
        mainContent.add(updatePanel);
        mainContent.add(Box.createVerticalStrut(10));
        
        // ZONA SUPERIOR: Tarjetas de Resumen (KPIs)
        mainContent.add(createKPISection());
        mainContent.add(Box.createVerticalStrut(25));
        
        // ZONA MEDIA: Centro de Notificaciones (Observer Pattern)
        mainContent.add(createNotificationCenter());
        mainContent.add(Box.createVerticalStrut(25));
        
        // ZONA INFERIOR: Accesos Rápidos (Command Pattern)
        mainContent.add(createQuickActionsSection());
        
        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }
    
    /**
     * Actualiza los datos del dashboard
     */
    private void actualizarDashboard() {
        // NO cambiar de panel automáticamente - solo actualizar datos
        // Solo recrear el contenido si estamos en el panel de inicio
        Component currentComponent = null;
        for (Component comp : mainContentArea.getComponents()) {
            if (comp.isVisible()) {
                currentComponent = comp;
                break;
            }
        }
        
        // Solo actualizar si estamos viendo el dashboard de inicio
        if (currentComponent instanceof JScrollPane) {
            // Recrear el contenido del dashboard sin cambiar de panel
            mainContentArea.remove(0);
            mainContentArea.add(createDashboardPanel(), "inicio", 0);
            mainContentArea.revalidate();
            mainContentArea.repaint();
        }
        
        UIUtils.mostrarExito(this, "Datos actualizados correctamente");
    }
    
    /**
     * Genera un reporte PDF de las ventas del día
     */
    private void generarReportePDF() {
        try {
            // Mostrar mensaje de progreso
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            
            // Generar el reporte
            String rutaArchivo = ReportePDFService.generarReporteVentasDelDia();
            
            // Restaurar cursor
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            
            // Mostrar mensaje de éxito
            UIUtils.mostrarExito(this, 
                "¡Reporte PDF generado exitosamente!\n\n" +
                "Archivo guardado en:\n" + rutaArchivo + "\n\n" +
                "El archivo se ha guardado en tu carpeta de Descargas.");
            
            // Opcional: Abrir el archivo automáticamente
            try {
                java.awt.Desktop.getDesktop().open(new java.io.File(rutaArchivo));
            } catch (Exception e) {
                // Si no se puede abrir automáticamente, no es crítico
            }
            
        } catch (Exception e) {
            // Restaurar cursor en caso de error
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            
            UIUtils.mostrarError(this, 
                "Error al generar el reporte PDF:\n" + e.getMessage() + "\n\n" +
                "Verifica que tengas permisos de escritura en la carpeta de Descargas.");
        }
    }

    // ZONA SUPERIOR: Tarjetas de Resumen (KPIs) - Contenido según rol
    private JPanel createKPISection() {
        JPanel kpiSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        kpiSection.setBackground(SECONDARY_COLOR);
        
        Rol rolUsuario = usuarioActual != null ? usuarioActual.getRol() : Rol.CAJERO;
        DashboardService.DashboardData data = DashboardService.obtenerDatosDashboard();
        
        if (rolUsuario == Rol.CAJERO) {
            // TRABAJADOR: Dashboard simplificado sin datos financieros sensibles
            JPanel metaCard = createKPICard("Mi Meta del Día", "Meta: 20 ventas", "🎯", new Color(54, 162, 235));
            metaCard.setToolTipText("Tu objetivo de ventas para el día de hoy");
            
            JPanel ventasRealizadasCard = createKPICard("Mis Ventas", String.valueOf(data.transacciones), "📊", SUCCESS_COLOR);
            ventasRealizadasCard.setToolTipText("Número de ventas que has realizado hoy");
            
            JPanel estadoCard = createKPICard("Estado del Sistema", "✅ Operativo", "🔧", new Color(75, 192, 192));
            estadoCard.setToolTipText("Estado actual del sistema de punto de venta");
            
            JPanel turnoCard = createKPICard("Turno Actual", "Mañana", "⏰", new Color(255, 159, 64));
            turnoCard.setToolTipText("Tu turno de trabajo actual");
            
            kpiSection.add(metaCard);
            kpiSection.add(ventasRealizadasCard);
            kpiSection.add(estadoCard);
            kpiSection.add(turnoCard);
            
        } else {
            // SUPERVISOR y ADMINISTRADOR: Dashboard completo con datos financieros
            String ventasTexto = String.format("S/. %.2f", data.ventasDelDia);
            JPanel ventasCard = createKPICard("Ventas del Día", ventasTexto, "💰", SUCCESS_COLOR);
            ventasCard.setToolTipText("Total de ingresos generados hoy por todas las ventas realizadas");
            
            JPanel transaccionesCard = createKPICard("Transacciones", String.valueOf(data.transacciones), "📊", new Color(54, 162, 235));
            transaccionesCard.setToolTipText("Número total de boletas/facturas emitidas en el día");
            
            JPanel productosCard = createKPICard("Productos Vendidos", String.valueOf(data.productosVendidos), "📦", new Color(255, 159, 64));
            productosCard.setToolTipText("Cantidad total de productos vendidos (suma de todas las cantidades)");
            
            JPanel metodoPagoCard = createKPICard("Método de Pago", data.metodoPago, "💳", new Color(75, 192, 192));
            metodoPagoCard.setToolTipText("Distribución porcentual de los métodos de pago utilizados hoy");
            
            kpiSection.add(ventasCard);
            kpiSection.add(transaccionesCard);
            kpiSection.add(productosCard);
            kpiSection.add(metodoPagoCard);
        }
        
        return kpiSection;
    }
    
    private JPanel createKPICard(String title, String value, String icon, Color accentColor) {
        JPanel card = createCard(280, 120);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Icono
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setPreferredSize(new Dimension(40, 40));
        
        // Contenido
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(CARD_BACKGROUND);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIUtils.DEFAULT_FONT);
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(UIUtils.BOLD_FONT);
        valueLabel.setForeground(accentColor);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(valueLabel);
        
        card.add(iconLabel, BorderLayout.WEST);
        card.add(contentPanel, BorderLayout.CENTER);
        
        return card;
    }

    // ZONA MEDIA: Centro de Notificaciones (Observer Pattern)
    private JPanel createNotificationCenter() {
        JPanel notificationCenter = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        notificationCenter.setBackground(SECONDARY_COLOR);
        
        // Panel A: Alertas de Stock Crítico
        JPanel stockAlertsPanel = createStockAlertsPanel();
        
        // Panel B: Lotes por Vencer
        JPanel lotesVencerPanel = createLotesVencerPanel();
        
        notificationCenter.add(stockAlertsPanel);
        notificationCenter.add(lotesVencerPanel);
        
        return notificationCenter;
    }
    
    private JPanel createStockAlertsPanel() {
        JPanel panel = createCard(400, 250);
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Título
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setBackground(CARD_BACKGROUND);
        
        JLabel titleLabel = new JLabel("⚠️ ALERTAS DE STOCK (Crítico)");
        titleLabel.setFont(UIUtils.BOLD_FONT);
        titleLabel.setForeground(new Color(220, 53, 69)); // Color de alerta
        
        headerPanel.add(titleLabel);
        
        // Contenido de alertas (datos reales)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(CARD_BACKGROUND);
        
        // Obtener alertas reales de stock
        DashboardService.DashboardData data = DashboardService.obtenerDatosDashboard();
        
        if (data.alertasStock.isEmpty()) {
            JLabel sinAlertasLabel = new JLabel("✅ No hay alertas de stock crítico");
            sinAlertasLabel.setFont(UIUtils.DEFAULT_FONT);
            sinAlertasLabel.setForeground(SUCCESS_COLOR);
            sinAlertasLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
            contentPanel.add(sinAlertasLabel);
        } else {
            for (DashboardService.AlertaStock alerta : data.alertasStock) {
                String textoAlerta = alerta.getIcono() + " " + alerta.producto + " (Stock: " + alerta.stock + " un.)";
                JLabel productoLabel = new JLabel(textoAlerta);
                productoLabel.setFont(UIUtils.DEFAULT_FONT);
                productoLabel.setForeground(TEXT_PRIMARY);
                productoLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
                contentPanel.add(productoLabel);
            }
        }
        
        // Botón de acción
        JButton btnVerInventario = new JButton("Ver Inventario Completo");
        UIUtils.configurarBotonPrimario(btnVerInventario);
        btnVerInventario.addActionListener(e -> cardLayout.show(mainContentArea, "inventario"));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(CARD_BACKGROUND);
        buttonPanel.add(btnVerInventario);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createLotesVencerPanel() {
        JPanel panel = createCard(400, 250);
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Título
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.setBackground(CARD_BACKGROUND);
        
        JLabel titleLabel = new JLabel("📅 LOTES POR VENCER");
        titleLabel.setFont(UIUtils.BOLD_FONT);
        titleLabel.setForeground(new Color(255, 193, 7)); // Color de advertencia
        
        headerPanel.add(titleLabel);
        
        // Contenido de lotes (datos reales)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(CARD_BACKGROUND);
        
        // Obtener lotes reales próximos a vencer
        DashboardService.DashboardData data = DashboardService.obtenerDatosDashboard();
        
        if (data.lotesVencer.isEmpty()) {
            JLabel sinLotesLabel = new JLabel("✅ No hay productos próximos a vencer");
            sinLotesLabel.setFont(UIUtils.DEFAULT_FONT);
            sinLotesLabel.setForeground(SUCCESS_COLOR);
            sinLotesLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
            contentPanel.add(sinLotesLabel);
        } else {
            for (DashboardService.LoteVencer lote : data.lotesVencer) {
                String textoLote = lote.getIcono() + " " + lote.producto + " (Vence: " + lote.getTextoVencimiento() + ")";
                JLabel loteLabel = new JLabel(textoLote);
                loteLabel.setFont(UIUtils.DEFAULT_FONT);
                loteLabel.setForeground(TEXT_PRIMARY);
                loteLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
                contentPanel.add(loteLabel);
            }
        }
        
        // Botón de acción
        JButton btnVerAlertas = new JButton("Ver Alertas Completas");
        UIUtils.configurarBotonSecundario(btnVerAlertas);
        btnVerAlertas.addActionListener(e -> navegarAPanel("alertas"));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(CARD_BACKGROUND);
        buttonPanel.add(btnVerAlertas);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // ZONA INFERIOR: Accesos Rápidos (Command Pattern) - Según rol
    private JPanel createQuickActionsSection() {
        JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        quickActions.setBackground(SECONDARY_COLOR);
        
        Rol rolUsuario = usuarioActual != null ? usuarioActual.getRol() : Rol.CAJERO;
        
        // Nueva Venta - Todos los roles
        JButton btnNuevaVenta = createQuickActionButton(
            "Nueva Venta 🛒", 
            "Iniciar proceso de venta",
            new Color(40, 167, 69),
            e -> navegarAPanel("pos")
        );
        quickActions.add(btnNuevaVenta);
        
        if (rolUsuario == Rol.CAJERO) {
            // CAJERO: Accesos básicos + consulta de inventario y alertas
            JButton btnConsultarInventario = createQuickActionButton(
                "Consultar Inventario 📦", 
                "Ver productos disponibles y fechas",
                new Color(255, 159, 64),
                e -> navegarAPanel("inventario")
            );
            
            JButton btnVerAlertas = createQuickActionButton(
                "Ver Alertas ⚠️", 
                "Productos próximos a vencer",
                new Color(255, 87, 34),
                e -> navegarAPanel("alertas")
            );
            
            quickActions.add(btnConsultarInventario);
            quickActions.add(btnVerAlertas);
            
        } else if (rolUsuario == Rol.SUPERVISOR) {
            // SUPERVISOR: Accesos operativos
            JButton btnInventario = createQuickActionButton(
                "Ver Inventario 📦", 
                "Revisar stock y productos",
                new Color(255, 159, 64),
                e -> navegarAPanel("inventario")
            );
            
            JButton btnCierreCaja = createQuickActionButton(
                "Cierre de Caja 🔒", 
                "Reporte de ventas del día",
                new Color(108, 117, 125),
                e -> navegarAPanel("reporte")
            );
            
            quickActions.add(btnInventario);
            quickActions.add(btnCierreCaja);
            
        } else if (rolUsuario == Rol.ADMINISTRADOR) {
            // ADMINISTRADOR: Accesos completos
            JButton btnCierreCaja = createQuickActionButton(
                "Cierre de Caja 🔒", 
                "Reporte de ventas del día",
                new Color(108, 117, 125),
                e -> navegarAPanel("reporte")
            );
            
            JButton btnInventario = createQuickActionButton(
                "Ver Inventario 📦", 
                "Gestionar stock y productos",
                new Color(255, 159, 64),
                e -> navegarAPanel("inventario")
            );
            
            JButton btnReportePDF = createQuickActionButton(
                "Reporte PDF 📄", 
                "Generar reporte de ventas del día en PDF",
                new Color(220, 53, 69),
                e -> generarReportePDF()
            );
            
            quickActions.add(btnCierreCaja);
            quickActions.add(btnInventario);
            quickActions.add(btnReportePDF);
        }
        
        return quickActions;
    }
    
    private JButton createQuickActionButton(String text, String tooltip, Color backgroundColor, java.awt.event.ActionListener action) {
        JButton button = new JButton("<html><center>" + text + "</center></html>");
        button.setFont(UIUtils.BOLD_FONT);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(200, 80));
        button.setBorder(BorderFactory.createRaisedSoftBevelBorder());
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText(tooltip);
        
        // Efecto hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(backgroundColor.darker());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(backgroundColor);
            }
        });
        
        button.addActionListener(action);
        return button;
    }


    private JPanel createCard(int width, int height) {
        JPanel card = new JPanel();
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(233, 236, 239)),
            new EmptyBorder(25, 25, 25, 25)
        ));
        
        if (width > 0 && height > 0) {
            card.setPreferredSize(new Dimension(width, height));
        }
        
        return card;
    }

    private void setupEventHandlers() {
        // Los event handlers se configuran en los métodos de creación
    }

    private void startClock() {
        reloj = new Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            lblFechaHora.setText(now.format(formatter));
        });
        reloj.start();
    }
    
    private void startDashboardAutoRefresh() {
        // Actualizar dashboard cada 60 segundos (menos frecuente)
        actualizadorDashboard = new Timer(60000, e -> {
            SwingUtilities.invokeLater(() -> {
                // Solo actualizar datos si estamos en el panel de inicio
                // NO cambiar de panel automáticamente
                Component currentPanel = null;
                for (Component comp : mainContentArea.getComponents()) {
                    if (comp.isVisible()) {
                        currentPanel = comp;
                        break;
                    }
                }
                
                // Solo actualizar si estamos viendo el dashboard de inicio
                if (currentPanel instanceof JScrollPane) {
                    // Actualizar silenciosamente sin cambiar navegación
                    mainContentArea.remove(0);
                    mainContentArea.add(createDashboardPanel(), "inicio", 0);
                    mainContentArea.revalidate();
                    mainContentArea.repaint();
                    System.out.println("Dashboard actualizado automáticamente (solo datos)");
                }
            });
        });
        actualizadorDashboard.start();
    }

    
    // Método para cambiar entre paneles
    private void cambiarPanel(String panelName) {
        cardLayout.show(mainContentArea, panelName);
    }
    
    // Método para navegar a un panel y actualizar el sidebar automáticamente
    private void navegarAPanel(String panelName) {
        // Cambiar el panel
        cardLayout.show(mainContentArea, panelName);
        
        // Encontrar y activar el botón correspondiente en el sidebar
        JButton botonCorrespondiente = encontrarBotonSidebar(panelName);
        if (botonCorrespondiente != null) {
            setActiveButton(botonCorrespondiente);
        }
    }
    
    // Método para encontrar el botón del sidebar que corresponde a un panel
    private JButton encontrarBotonSidebar(String panelName) {
        for (JButton button : sidebarButtons) {
            // Obtener la acción del botón desde sus ActionListeners
            String buttonAction = obtenerAccionDelBoton(button);
            if (panelName.equals(buttonAction)) {
                return button;
            }
        }
        return null;
    }
    
    // Método auxiliar para obtener la acción de un botón del sidebar
    private String obtenerAccionDelBoton(JButton button) {
        // Mapear botones por su texto (más confiable)
        String texto = button.getText();
        if (texto.contains("Inicio")) return "inicio";
        if (texto.contains("Caja") || texto.contains("Punto de Venta")) return "pos";
        if (texto.contains("Inventario")) return "inventario";
        if (texto.contains("Historial")) return "historial";
        if (texto.contains("Reporte")) return "reporte";
        if (texto.contains("Usuarios")) return "usuarios";
        if (texto.contains("Alertas")) return "alertas";
        return "";
    }
    
    // Método para establecer el botón activo
    private void setActiveButton(JButton activeButton) {
        // Desactivar el botón anterior
        if (currentActiveButton != null) {
            currentActiveButton.setBackground(new Color(0, 0, 0, 0));
            currentActiveButton.setForeground(TEXT_PRIMARY);
            currentActiveButton.setBorder(new EmptyBorder(12, 20, 12, 20));
        }
        
        // Activar el nuevo botón
        activeButton.setBackground(new Color(227, 242, 253));
        activeButton.setForeground(new Color(25, 118, 210));
        activeButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 3, new Color(25, 118, 210)),
            new EmptyBorder(12, 20, 12, 17)
        ));
        
        // Actualizar referencia del botón activo
        currentActiveButton = activeButton;
    }
    
    // Panel de información del usuario
    private JPanel createUserInfoPanel() {
        JPanel userInfo = new JPanel();
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
        userInfo.setBackground(new Color(248, 249, 250));
        userInfo.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel userLabel = new JLabel("👤 " + (usuarioActual != null ? usuarioActual.getNombreCompleto() : "Usuario"));
        userLabel.setFont(UIUtils.BOLD_FONT);
        userLabel.setForeground(TEXT_PRIMARY);
        
        JLabel roleLabel = new JLabel("🏷️ " + (usuarioActual != null ? usuarioActual.getRol().getDescripcion() : "Sin rol"));
        roleLabel.setFont(UIUtils.DEFAULT_FONT);
        roleLabel.setForeground(TEXT_SECONDARY);
        
        userInfo.add(userLabel);
        userInfo.add(Box.createVerticalStrut(5));
        userInfo.add(roleLabel);
        
        return userInfo;
    }
    
    // Panel principal (Dashboard)
    private JScrollPane createDashboardPanel() {
        return createMainContentPanel(); // Usar el panel existente
    }
    
    // Panel POS (Punto de Venta)
    private JPanel createPOSPanel() {
        // Usar el panel real de ventas/facturación
        return new com.minimarket.ui.panels.VentasPanel();
    }
    
    // Panel Inventario
    private JPanel createInventarioPanel() {
        // Crear panel de inventario con permisos según rol
        Rol rolUsuario = usuarioActual != null ? usuarioActual.getRol() : Rol.CAJERO;
        boolean soloLectura = (rolUsuario == Rol.CAJERO);
        return new com.minimarket.ui.panels.InventarioPanel(soloLectura);
    }
    
    // Panel Historial de Ventas
    private JPanel createHistorialPanel() {
        // Usar el panel real de historial de ventas
        return new com.minimarket.ui.panels.HistorialVentasPanel();
    }
    
    // Panel de Reporte de Ventas
    private JPanel createReportePanel() {
        // Usar el panel real de reporte de ventas
        return new com.minimarket.ui.panels.ReporteVentasPanel();
    }
    
    // Panel Usuarios (Solo Admin)
    private JPanel createUsuariosPanel() {
        // Usar el panel real de usuarios/vendedores
        return new com.minimarket.ui.panels.UsuariosPanel();
    }
    
    // Panel Alertas de Vencimiento
    private JPanel createAlertasPanel() {
        // Crear panel de alertas con permisos según rol
        Rol rolUsuario = usuarioActual != null ? usuarioActual.getRol() : Rol.CAJERO;
        boolean soloLectura = (rolUsuario == Rol.CAJERO);
        return new com.minimarket.ui.panels.AlertasVencimientoPanel(soloLectura);
    }
}
