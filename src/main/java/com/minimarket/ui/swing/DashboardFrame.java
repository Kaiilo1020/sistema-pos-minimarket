package com.minimarket.ui.swing;

import com.minimarket.security.UsuarioSesion;
import com.minimarket.security.Rol;
import com.minimarket.model.Usuario;
import com.minimarket.ui.util.UIUtils;
import com.minimarket.service.DashboardService;

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
    
    // Usuario actual (simulado para demo)
    private Usuario usuarioActual;

    public DashboardFrame() {
        // Simular usuario logueado (Admin para demo)
        usuarioActual = new Usuario("admin", "pass", "Juan", "Pérez", "admin@minimarket.com", Rol.ADMINISTRADOR);
        usuarioActual.setId(1L);
        UsuarioSesion.getInstance().login(usuarioActual);
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        startClock();
    }

    private void initializeComponents() {
        setTitle("MiniMarket Pro - Dashboard de Patrones de Diseño");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
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
        
        // Agregar todos los paneles
        mainContentArea.add(createDashboardPanel(), "inicio");
        mainContentArea.add(createPOSPanel(), "pos");
        mainContentArea.add(createInventarioPanel(), "inventario");
        mainContentArea.add(createHistorialPanel(), "historial");
        mainContentArea.add(createReportePanel(), "reporte");
        if (usuarioActual != null && usuarioActual.getRol() == Rol.ADMINISTRADOR) {
            mainContentArea.add(createUsuariosPanel(), "usuarios");
        }
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
        
        lblUsuario = new JLabel("👤 Juan Pérez");
        lblUsuario.setFont(UIUtils.BOLD_FONT);
        lblUsuario.setForeground(TEXT_PRIMARY);
        
        JButton btnPerfil = createHeaderButton("👤");
        JButton btnAyuda = createHeaderButton("❓");
        JButton btnSalir = createHeaderButton("🚪");
        
        btnSalir.addActionListener(e -> {
            if (UIUtils.confirmar(this, "¿Estás seguro de que quieres salir?")) {
                System.exit(0);
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
        
        // Botones de navegación principales
        JButton btnInicio = createSidebarButton("🏠 Inicio", "inicio", true);
        JButton btnPOS = createSidebarButton("🛒 Caja / Punto de Venta", "pos", false);
        JButton btnInventario = createSidebarButton("📦 Inventario y Kardex", "inventario", false);
        JButton btnHistorial = createSidebarButton("📄 Historial de Ventas", "historial", false);
        JButton btnReporte = createSidebarButton("📊 Reporte Diario", "reporte", false);
        
        sidebar.add(btnInicio);
        sidebar.add(btnPOS);
        sidebar.add(btnInventario);
        sidebar.add(btnHistorial);
        sidebar.add(btnReporte);
        
        // Agregar a la lista de botones
        sidebarButtons.add(btnInicio);
        sidebarButtons.add(btnPOS);
        sidebarButtons.add(btnInventario);
        sidebarButtons.add(btnHistorial);
        sidebarButtons.add(btnReporte);
        
        // Solo mostrar "Usuarios y Permisos" si es Admin
        if (usuarioActual != null && usuarioActual.getRol() == Rol.ADMINISTRADOR) {
            JButton btnUsuarios = createSidebarButton("👥 Usuarios y Permisos", "usuarios", false);
            sidebar.add(btnUsuarios);
            sidebarButtons.add(btnUsuarios);
        }
        
        JButton btnAlertas = createSidebarButton("⚠️ Alertas de Vencimiento", "alertas", false);
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
        // Mostrar el panel de inicio actualizado
        cardLayout.show(mainContentArea, "inicio");
        
        // Recrear el contenido del dashboard
        mainContentArea.remove(mainContentArea.getComponent(0)); // Remover el panel anterior
        mainContentArea.add(createDashboardPanel(), "inicio", 0); // Agregar el nuevo panel
        
        UIUtils.mostrarExito(this, "Dashboard actualizado con datos en tiempo real");
    }

    // ZONA SUPERIOR: Tarjetas de Resumen (KPIs)
    private JPanel createKPISection() {
        JPanel kpiSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        kpiSection.setBackground(SECONDARY_COLOR);
        
        // Obtener datos reales del dashboard
        DashboardService.DashboardData data = DashboardService.obtenerDatosDashboard();
        
        // Tarjeta 1: Ventas del Día (datos reales)
        String ventasTexto = String.format("S/. %.2f", data.ventasDelDia);
        JPanel ventasCard = createKPICard("Ventas del Día", ventasTexto, "💰", SUCCESS_COLOR);
        
        // Tarjeta 2: Transacciones (datos reales)
        JPanel transaccionesCard = createKPICard("Transacciones", String.valueOf(data.transacciones), "📊", new Color(54, 162, 235));
        
        // Tarjeta 3: Método de Pago (datos reales)
        JPanel metodoPagoCard = createKPICard("Método de Pago", data.metodoPago, "💳", new Color(255, 159, 64));
        
        // Tarjeta 4: Integridad de Datos
        String integridadTexto = data.transacciones > 0 ? "✅ Boletas Correctas" : "⚠️ Sin ventas hoy";
        Color integridadColor = data.transacciones > 0 ? new Color(75, 192, 192) : new Color(255, 193, 7);
        JPanel integridadCard = createKPICard("Integridad Datos", integridadTexto, "🔒", integridadColor);
        
        kpiSection.add(ventasCard);
        kpiSection.add(transaccionesCard);
        kpiSection.add(metodoPagoCard);
        kpiSection.add(integridadCard);
        
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
        btnVerAlertas.addActionListener(e -> cardLayout.show(mainContentArea, "alertas"));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(CARD_BACKGROUND);
        buttonPanel.add(btnVerAlertas);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // ZONA INFERIOR: Accesos Rápidos (Command Pattern)
    private JPanel createQuickActionsSection() {
        JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        quickActions.setBackground(SECONDARY_COLOR);
        
        // Botón Nueva Venta
        JButton btnNuevaVenta = createQuickActionButton(
            "Nueva Venta 🛒", 
            "Iniciar proceso de venta",
            new Color(40, 167, 69),
            e -> cardLayout.show(mainContentArea, "pos")
        );
        
        // Botón Cierre de Caja
        JButton btnCierreCaja = createQuickActionButton(
            "Cierre de Caja 🔒", 
            "Reporte de ventas del día",
            new Color(108, 117, 125),
            e -> cardLayout.show(mainContentArea, "reporte")
        );
        
        // Botón Consultar Precio
        JButton btnConsultarPrecio = createQuickActionButton(
            "Consultar Precio 🔍", 
            "Buscar productos y precios",
            new Color(108, 117, 125),
            e -> cardLayout.show(mainContentArea, "inventario")
        );
        
        quickActions.add(btnNuevaVenta);
        quickActions.add(btnCierreCaja);
        quickActions.add(btnConsultarPrecio);
        
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

    
    // Método para cambiar entre paneles
    private void cambiarPanel(String panelName) {
        cardLayout.show(mainContentArea, panelName);
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
        // Usar el panel real de inventario
        return new com.minimarket.ui.panels.InventarioPanel();
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
        return new com.minimarket.ui.panels.AlertasVencimientoPanel();
    }
}
