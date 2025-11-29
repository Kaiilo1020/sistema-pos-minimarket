package com.minimarket.ui.swing;

import com.minimarket.security.UsuarioSesion;
import com.minimarket.security.Rol;
import com.minimarket.model.Usuario;
import com.minimarket.ui.theme.EstilosApp;
import com.minimarket.ui.util.UIUtils;
import com.minimarket.service.DashboardService;
import com.minimarket.service.ReportePDFService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dashboard principal del sistema con Swing
 * Diseño moderno inspirado en la imagen referencial
 */
public class DashboardFrame extends JFrame {
    
    // Colores del tema
    private static final Color SECONDARY_COLOR = new Color(244, 246, 248); // Gris Azulado Suave #F4F6F8
    private static final Color TEXT_PRIMARY = new Color(44, 62, 80);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color CARD_BACKGROUND = new Color(255, 255, 255); // Blanco #FFFFFF
    private static final Color CARD_BORDER = new Color(224, 224, 224); // Gris claro #E0E0E0
    private static final Color SIDEBAR_BACKGROUND = new Color(44, 62, 80); // Azul Acero Grisáceo #2C3E50
    private static final Color SIDEBAR_ACTIVE_BG = new Color(255, 255, 255); // Blanco para elemento activo
    private static final Color SIDEBAR_ACTIVE_TEXT = new Color(44, 62, 80); // Azul del sidebar para texto activo
    private static final Color SIDEBAR_INACTIVE_TEXT = Color.WHITE; // Blanco para texto inactivo
    private static final Color SIDEBAR_HOVER_BG = new Color(255, 255, 255); // Blanco para hover
    private static final Color SIDEBAR_HOVER_TEXT = new Color(44, 62, 80); // Azul del sidebar para texto hover
    
    // Componentes principales
    private JPanel mainContentArea;
    private CardLayout cardLayout;
    
    // Sistema de navegación
    private java.util.List<JButton> sidebarButtons = new java.util.ArrayList<>();
    private JButton currentActiveButton;
    
    // Usuario actual de la sesión
    private Usuario usuarioActual;
    
    // Referencia al panel de KPIs para actualización sin recrear todo
    private JPanel kpiSectionPanel;

    public DashboardFrame() {
        // Obtener usuario actual de la sesión
        usuarioActual = UsuarioSesion.getInstance().getUsuarioActual();
        
        // Verificar que hay un usuario logueado
        if (usuarioActual == null) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame());
            return;
        }
        
        try {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
            // startDashboardAutoRefresh(); // DESHABILITADO temporalmente
        } catch (Exception e) {
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
        Rol rolUsuario = obtenerRolUsuario();
        
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
        
        // Información del usuario y rol
        String nombreUsuario = usuarioActual != null ? usuarioActual.getNombreCompleto() : "Usuario";
        String rolUsuario = usuarioActual != null && usuarioActual.getRol() != null ? 
            usuarioActual.getRol().name() : "Sin rol";
        
        JLabel lblUsuarioInfo = new JLabel(nombreUsuario + " - " + rolUsuario);
        lblUsuarioInfo.setFont(UIUtils.DEFAULT_FONT);
        lblUsuarioInfo.setForeground(TEXT_PRIMARY);
        lblUsuarioInfo.setBorder(new EmptyBorder(0, 0, 0, 10));
        
        JButton btnSalir = new JButton("Cerrar Sesión");
        btnSalir.setFont(UIUtils.BOLD_FONT);
        btnSalir.setForeground(Color.WHITE);
        btnSalir.setBackground(new Color(220, 53, 69)); // Rojo para cerrar sesión
        btnSalir.setPreferredSize(new Dimension(150, 40)); // Más grande
        btnSalir.setBorder(null);
        btnSalir.setFocusPainted(false);
        btnSalir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efecto hover
        btnSalir.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnSalir.setBackground(new Color(220, 53, 69).darker());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnSalir.setBackground(new Color(220, 53, 69));
            }
        });
        
        btnSalir.addActionListener(e -> {
            if (UIUtils.confirmar(this, "¿Estás seguro de que quieres cerrar sesión?")) {
                UsuarioSesion.getInstance().logout();
                dispose();
                new LoginFrame();
            }
        });
        
        userPanel.add(lblUsuarioInfo);
        userPanel.add(btnSalir);
        
        header.add(logo, BorderLayout.WEST);
        header.add(userPanel, BorderLayout.EAST);
        
        return header;
    }


    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BACKGROUND);
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(35, 50, 70))); // Borde sutil
        
        // Título de navegación
        JLabel navTitle = new JLabel("NAVEGACIÓN");
        navTitle.setFont(new Font(UIUtils.DEFAULT_FONT.getName(), Font.BOLD, 12));
        navTitle.setForeground(new Color(200, 210, 220)); // Gris azulado claro para el título
        navTitle.setBorder(new EmptyBorder(25, 20, 15, 20));
        sidebar.add(navTitle);
        
        // Adapter Pattern: Adaptar opciones del sidebar según rol del usuario
        Rol rolUsuario = obtenerRolUsuario();
        java.util.List<SidebarOption> opciones = obtenerOpcionesSidebar(rolUsuario);
        boolean first = true;
        for (SidebarOption option : opciones) {
            JButton button = createSidebarButton(option.texto, option.panelKey, first);
            sidebar.add(button);
            sidebarButtons.add(button);
            if (first) {
                currentActiveButton = button;
                first = false;
            }
        }
        
        return sidebar;
    }


    private JButton createSidebarButton(String text, String action, boolean active) {
        // Usar el botón personalizado con flat design
        FlatSidebarButton button = new FlatSidebarButton(text, action, active);
        return button;
    }
    
    /**
     * Botón personalizado para el sidebar con diseño plano (Flat Design)
     * Implementa hover effect y estados visuales sin bordes ni efectos 3D
     */
    private class FlatSidebarButton extends JButton {
        private final String action;
        private boolean isActive;
        private boolean isHovered;
        
        public FlatSidebarButton(String text, String action, boolean active) {
            super(text);
            this.action = action;
            this.isActive = active;
            this.isHovered = false;
            
            // Configuración de diseño plano
            setHorizontalAlignment(SwingConstants.LEFT);
            setMaximumSize(new Dimension(280, 50));
            setPreferredSize(new Dimension(280, 50));
            setFont(new Font(UIUtils.DEFAULT_FONT.getName(), Font.PLAIN, 14));
            
            // Eliminar todos los efectos visuales por defecto de Swing
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(true);
            // Aplicar padding interno (arriba, izquierda, abajo, derecha) para separar el texto del borde
            setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 10));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Aplicar estado inicial
            updateAppearance();
            
            // MouseListener para efecto hover
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!isActive) {
                        isHovered = true;
                        updateAppearance();
                    }
                }
                
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    if (!isActive) {
                        isHovered = false;
                        updateAppearance();
                    }
                }
            });
            
            // ActionListener para cambiar de panel
            addActionListener(e -> {
            cambiarPanel(action);
                setActiveButton(this);
            });
        }
        
        /**
         * Actualiza la apariencia del botón según su estado (normal, hover, activo)
         */
        private void updateAppearance() {
            if (isActive) {
                // Estado seleccionado: Fondo blanco, texto azul
                setBackground(SIDEBAR_ACTIVE_BG);
                setForeground(SIDEBAR_ACTIVE_TEXT);
            } else if (isHovered) {
                // Estado hover: Fondo blanco, texto azul
                setBackground(SIDEBAR_HOVER_BG);
                setForeground(SIDEBAR_HOVER_TEXT);
            } else {
                // Estado normal: Fondo transparente (color del sidebar), texto blanco
                setBackground(SIDEBAR_BACKGROUND);
                setForeground(SIDEBAR_INACTIVE_TEXT);
            }
            repaint();
        }
        
        /**
         * Marca el botón como activo o inactivo
         */
        public void setActive(boolean active) {
            this.isActive = active;
            this.isHovered = false;
            updateAppearance();
        }
        
        /**
         * Obtiene la acción asociada al botón (nombre del panel)
         */
        public String getPanelAction() {
            return action;
        }
    }

    private JScrollPane createMainContentPanel() {
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(SECONDARY_COLOR);
        mainContent.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        DashboardService.DashboardData data = fetchDashboardData();
        
        // ZONA SUPERIOR: Tarjetas de Resumen (KPIs)
        mainContent.add(createKPISection(data));
        mainContent.add(Box.createVerticalStrut(25));
        
        // ZONA MEDIA: Centro de Notificaciones (Observer Pattern)
        mainContent.add(createNotificationCenter(data));
        mainContent.add(Box.createVerticalStrut(25));
        
        // ZONA INFERIOR: Accesos Rápidos (Command Pattern)
        mainContent.add(createQuickActionsSection());
        
        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        // Deshabilitar barra de desplazamiento horizontal
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return scrollPane;
    }

    /**
     * Actualiza solo las tarjetas de KPIs sin recrear todo el panel ni cambiar de vista
     * Método público para poder ser llamado desde otros paneles (ej: VentasPanel)
     */
    public void actualizarDashboard() {
        // Solo actualizar si estamos en el panel de inicio y tenemos referencia al panel de KPIs
        if (kpiSectionPanel != null) {
            DashboardService.DashboardData data = fetchDashboardData();
            renderKpiCards(kpiSectionPanel, data, obtenerRolUsuario());
        }
        // NO mostrar mensaje de éxito ni cambiar de panel
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

    private DashboardService.DashboardData fetchDashboardData() {
        return DashboardService.obtenerDatosDashboard();
    }

    private void renderKpiCards(JPanel targetPanel, DashboardService.DashboardData data, Rol rolUsuario) {
        if (targetPanel == null) {
            return;
        }
        
        DashboardService.DashboardData info = data != null ? data : fetchDashboardData();
        targetPanel.removeAll();
        
        if (rolUsuario == Rol.CAJERO) {
            JPanel metaCard = createKPICard("Mi Meta del Día", "Meta: 20 ventas", "🎯", new Color(54, 162, 235));
            metaCard.setToolTipText("Tu objetivo de ventas para el día de hoy");
            
            JPanel ventasRealizadasCard = createKPICard("Mis Ventas", String.valueOf(info.transacciones), "📊", SUCCESS_COLOR);
            ventasRealizadasCard.setToolTipText("Número de ventas que has realizado hoy");
            
            JPanel estadoCard = createKPICard("Estado del Sistema", "✅ Operativo", "🔧", new Color(75, 192, 192));
            estadoCard.setToolTipText("Estado actual del sistema de punto de venta");
            
            JPanel turnoCard = createKPICard("Turno Actual", "Mañana", "⏰", new Color(255, 159, 64));
            turnoCard.setToolTipText("Tu turno de trabajo actual");
            
            targetPanel.add(metaCard);
            targetPanel.add(ventasRealizadasCard);
            targetPanel.add(estadoCard);
            targetPanel.add(turnoCard);
        } else {
            String ventasTexto = String.format("S/. %.2f", info.ventasDelDia);
            JPanel ventasCard = createKPICard("Ventas del Día", ventasTexto, "💰", SUCCESS_COLOR);
            ventasCard.setToolTipText("Total de ingresos generados hoy por todas las ventas realizadas");
            
            JPanel transaccionesCard = createKPICard("Transacciones", String.valueOf(info.transacciones), "📊", new Color(54, 162, 235));
            transaccionesCard.setToolTipText("Número total de boletas/facturas emitidas en el día");
            
            JPanel productosCard = createKPICard("Productos Vendidos", String.valueOf(info.productosVendidos), "📦", new Color(255, 159, 64));
            productosCard.setToolTipText("Cantidad total de productos vendidos (suma de todas las cantidades)");
            
            JPanel metodoPagoCard = createKPICard("Método de Pago", info.metodoPago, "💳", new Color(75, 192, 192));
            metodoPagoCard.setToolTipText("Distribución porcentual de los métodos de pago utilizados hoy");
            
            targetPanel.add(ventasCard);
            targetPanel.add(transaccionesCard);
            targetPanel.add(productosCard);
            targetPanel.add(metodoPagoCard);
        }
        
        targetPanel.revalidate();
        targetPanel.repaint();
    }

    // ZONA SUPERIOR: Tarjetas de Resumen (KPIs) - Contenido según rol
    private JPanel createKPISection(DashboardService.DashboardData data) {
        JPanel kpiSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        kpiSection.setBackground(SECONDARY_COLOR);
        
        // Guardar referencia para poder actualizarlo después
        kpiSectionPanel = kpiSection;
        renderKpiCards(kpiSection, data, obtenerRolUsuario());
        return kpiSection;
    }
    
    private JPanel createKPICard(String title, String value, String icon, Color accentColor) {
        JPanel card = createCard(280, 120);
        card.setLayout(new BorderLayout(10, 10));
        // El borde y padding ya están definidos en createCard, no sobrescribir
        
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
        
        JLabel valueLabel;
        // Si es "Método de Pago", envolver el texto en HTML para permitir salto de línea
        if ("Método de Pago".equals(title) && value.contains("|")) {
            // Reemplazar " | " por "<br>" para salto de línea en HTML
            String htmlValue = "<html>" + value.replace(" | ", "<br>") + "</html>";
            valueLabel = new JLabel(htmlValue);
        } else {
            valueLabel = new JLabel(value);
        }
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
    private JPanel createNotificationCenter(DashboardService.DashboardData data) {
        // Usar GridLayout para distribución simétrica (1 fila, 2 columnas, gap horizontal de 20px)
        JPanel notificationCenter = new JPanel(new GridLayout(1, 2, 20, 0));
        notificationCenter.setBackground(SECONDARY_COLOR);
        
        // Panel A: Alertas de Stock Crítico
        JPanel stockAlertsPanel = createStockAlertsPanel(data);
        
        // Panel B: Lotes por Vencer
        JPanel lotesVencerPanel = createLotesVencerPanel(data);
        
        notificationCenter.add(stockAlertsPanel);
        notificationCenter.add(lotesVencerPanel);
        
        return notificationCenter;
    }
    
    private JPanel createStockAlertsPanel(DashboardService.DashboardData data) {
        JPanel panel = createCard(400, 250);
        panel.setLayout(new BorderLayout(10, 10));
        // El borde y padding ya están definidos en createCard
        
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
        
        DashboardService.DashboardData info = data != null ? data : fetchDashboardData();
        
        if (info.alertasStock.isEmpty()) {
            JLabel sinAlertasLabel = new JLabel("✅ No hay alertas de stock crítico");
            sinAlertasLabel.setFont(UIUtils.DEFAULT_FONT);
            sinAlertasLabel.setForeground(SUCCESS_COLOR);
            sinAlertasLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
            contentPanel.add(sinAlertasLabel);
                    } else {
            for (DashboardService.AlertaStock alerta : info.alertasStock) {
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
        EstilosApp.estilizarBotonSecundario(btnVerInventario);
        btnVerInventario.addActionListener(e -> navegarAPanel("inventario"));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(CARD_BACKGROUND);
        buttonPanel.add(btnVerInventario);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createLotesVencerPanel(DashboardService.DashboardData data) {
        JPanel panel = createCard(400, 250);
        panel.setLayout(new BorderLayout(10, 10));
        // El borde y padding ya están definidos en createCard
        
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
        
        DashboardService.DashboardData info = data != null ? data : fetchDashboardData();
        
        if (info.lotesVencer.isEmpty()) {
            JLabel sinLotesLabel = new JLabel("✅ No hay productos próximos a vencer");
            sinLotesLabel.setFont(UIUtils.DEFAULT_FONT);
            sinLotesLabel.setForeground(SUCCESS_COLOR);
            sinLotesLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
            contentPanel.add(sinLotesLabel);
        } else {
            for (DashboardService.LoteVencer lote : info.lotesVencer) {
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
        EstilosApp.estilizarBotonNeutro(btnVerAlertas);
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
        
        Rol rolUsuario = obtenerRolUsuario();
        
        if (rolUsuario == Rol.SUPERVISOR || rolUsuario == Rol.ADMINISTRADOR) {
            // SUPERVISOR y ADMINISTRADOR: Cierre de Caja
            JButton btnCierreCaja = createQuickActionButton(
                "Cierre de Caja 🔒", 
                "Reporte de ventas del día",
                new Color(96, 125, 139), // Gris azulado
                e -> navegarAPanel("reporte")
            );
            
            quickActions.add(btnCierreCaja);
        }
        
        if (rolUsuario == Rol.ADMINISTRADOR) {
            // ADMINISTRADOR: Reporte PDF
            JButton btnReportePDF = createQuickActionButton(
                "Reporte PDF 📄", 
                "Generar reporte de ventas del día en PDF",
                new Color(211, 47, 47), // Rojo Profesional Material Design #D32F2F
                e -> generarReportePDF()
            );
            
            quickActions.add(btnReportePDF);
        }
        
        return quickActions;
    }
    
    private Rol obtenerRolUsuario() {
        return (usuarioActual != null && usuarioActual.getRol() != null) ? usuarioActual.getRol() : Rol.CAJERO;
    }
    
    private JButton createQuickActionButton(String text, String tooltip, Color backgroundColor, java.awt.event.ActionListener action) {
        JButton button = new JButton("<html><center>" + text + "</center></html>");
        button.setFont(new Font(UIUtils.BOLD_FONT.getName(), Font.BOLD, 13));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(200, 70));
        // Sin borde visible - solo padding interno para un look limpio
        button.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText(tooltip);
        
        // Efecto hover sutil - solo cambia el color de fondo
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                // Oscurecer ligeramente para feedback visual
                Color hoverColor = new Color(
                    Math.max(0, backgroundColor.getRed() - 20),
                    Math.max(0, backgroundColor.getGreen() - 20),
                    Math.max(0, backgroundColor.getBlue() - 20)
                );
                button.setBackground(hoverColor);
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
        // Borde sutil gris claro y padding de 15px para efecto de tarjeta flotante
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        if (width > 0 && height > 0) {
            card.setPreferredSize(new Dimension(width, height));
        }
        
        return card;
    }

    private void setupEventHandlers() {
        // Los event handlers se configuran en los métodos de creación
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
        // Si es un FlatSidebarButton, usar su método getPanelAction()
        if (button instanceof FlatSidebarButton) {
            return ((FlatSidebarButton) button).getPanelAction();
        }
        // Fallback: mapear botones por su texto (más confiable)
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
        if (currentActiveButton != null && currentActiveButton instanceof FlatSidebarButton) {
            ((FlatSidebarButton) currentActiveButton).setActive(false);
        }
        
        // Activar el nuevo botón
        if (activeButton instanceof FlatSidebarButton) {
            ((FlatSidebarButton) activeButton).setActive(true);
        }
        
        // Actualizar referencia del botón activo
        currentActiveButton = activeButton;
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
        Rol rolUsuario = obtenerRolUsuario();
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
        Rol rolUsuario = obtenerRolUsuario();
        boolean soloLectura = (rolUsuario == Rol.CAJERO);
        return new com.minimarket.ui.panels.AlertasVencimientoPanel(soloLectura);
    }
    
    /**
     * Adapter Pattern: Adapta las opciones del sidebar según el rol del usuario.
     * Cada rol ve diferentes opciones de menú.
     */
    private java.util.List<SidebarOption> obtenerOpcionesSidebar(Rol rol) {
        return switch (rol) {
            case ADMINISTRADOR -> java.util.List.of(
                new SidebarOption("Inicio", "inicio"),
                new SidebarOption("Caja / Punto de Venta", "pos"),
                new SidebarOption("Inventario", "inventario"),
                new SidebarOption("Historial de Ventas", "historial"),
                new SidebarOption("Reporte Diario", "reporte"),
                new SidebarOption("Usuarios y Permisos", "usuarios"),
                new SidebarOption("Alertas de Vencimiento", "alertas")
            );
            case SUPERVISOR -> java.util.List.of(
                new SidebarOption("Inicio", "inicio"),
                new SidebarOption("Caja / Punto de Venta", "pos"),
                new SidebarOption("Inventario", "inventario"),
                new SidebarOption("Historial de Ventas", "historial"),
                new SidebarOption("Reporte Diario", "reporte"),
                new SidebarOption("Alertas de Vencimiento", "alertas")
            );
            default -> java.util.List.of( // CAJERO
                new SidebarOption("Inicio", "inicio"),
                new SidebarOption("Caja / Punto de Venta", "pos"),
                new SidebarOption("Inventario", "inventario"),
                new SidebarOption("Alertas de Vencimiento", "alertas")
            );
        };
    }
    
    /**
     * Clase interna para representar una opción del sidebar (Adapter Pattern)
     */
    private static class SidebarOption {
        final String texto;
        final String panelKey;
        
        SidebarOption(String texto, String panelKey) {
            this.texto = texto;
            this.panelKey = panelKey;
        }
    }
}
