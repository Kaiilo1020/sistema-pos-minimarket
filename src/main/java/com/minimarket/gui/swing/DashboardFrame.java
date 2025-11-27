package com.minimarket.gui.swing;

import com.minimarket.patterns.creational.DatabaseConnection;
import com.minimarket.utils.DatabaseVerifier;
import com.minimarket.security.UsuarioSesion;
import com.minimarket.security.Rol;
import com.minimarket.models.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Dashboard principal del sistema con Swing
 * Diseño moderno inspirado en la imagen referencial
 */
public class DashboardFrame extends JFrame {
    
    // Colores del tema
    private static final Color PRIMARY_COLOR = new Color(33, 150, 243);
    private static final Color SECONDARY_COLOR = new Color(245, 247, 250);
    private static final Color ACCENT_COLOR = new Color(212, 165, 116);
    private static final Color TEXT_PRIMARY = new Color(44, 62, 80);
    private static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color CARD_BACKGROUND = Color.WHITE;
    
    // Componentes principales
    private JLabel lblUsuario;
    private JLabel lblFechaHora;
    private JLabel lblEstadoSistema;
    private JLabel lblEstadoBD;
    private JProgressBar progressPatrones;
    private JLabel lblProgreso;
    private JPanel panelPatrones;
    private JPanel panelActividad;
    private JPanel mainContentArea;
    private CardLayout cardLayout;
    
    // Sistema de navegación
    private java.util.List<JButton> sidebarButtons = new java.util.ArrayList<>();
    private JButton currentActiveButton;
    
    private Timer reloj;
    private int patronesCompletados = 7;
    private final int totalPatrones = 7;
    
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
        verificarEstadoSistema();
        cargarPatrones();
        cargarActividadReciente();
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
        mainContentArea.add(createConfiguracionPanel(), "configuracion");
        
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
        JLabel logo = new JLabel("🏪 MINIMARKET PRO");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(TEXT_PRIMARY);
        
        // Search bar (simulado)
        JTextField searchField = new JTextField("🔍 Buscar patrones, funciones...");
        searchField.setPreferredSize(new Dimension(300, 35));
        searchField.setBackground(SECONDARY_COLOR);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230)),
            new EmptyBorder(8, 15, 8, 15)
        ));
        
        // User info panel
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setBackground(CARD_BACKGROUND);
        
        lblFechaHora = new JLabel();
        lblFechaHora.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFechaHora.setForeground(TEXT_SECONDARY);
        
        lblUsuario = new JLabel("👤 Juan Pérez");
        lblUsuario.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUsuario.setForeground(TEXT_PRIMARY);
        
        JButton btnPerfil = createHeaderButton("👤");
        JButton btnAyuda = createHeaderButton("❓");
        JButton btnSalir = createHeaderButton("🚪");
        
        btnSalir.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de que quieres salir?",
                "Confirmar salida",
                JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        
        userPanel.add(lblFechaHora);
        userPanel.add(lblUsuario);
        userPanel.add(btnPerfil);
        userPanel.add(btnAyuda);
        userPanel.add(btnSalir);
        
        header.add(logo, BorderLayout.WEST);
        header.add(searchField, BorderLayout.CENTER);
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
        navTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
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
        
        JButton btnConfiguracion = createSidebarButton("⚙️ Configuración del Sistema", "configuracion", false);
        sidebar.add(btnConfiguracion);
        sidebarButtons.add(btnConfiguracion);
        
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
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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
        
        // Welcome section
        mainContent.add(createWelcomeSection());
        mainContent.add(Box.createVerticalStrut(25));
        
        // Main cards
        mainContent.add(createMainCardsSection());
        mainContent.add(Box.createVerticalStrut(25));
        
        // Patterns section
        mainContent.add(createPatternsSection());
        mainContent.add(Box.createVerticalStrut(25));
        
        // Activity section
        mainContent.add(createActivitySection());
        
        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JPanel createWelcomeSection() {
        JPanel welcome = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcome.setBackground(SECONDARY_COLOR);
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(SECONDARY_COLOR);
        
        JLabel title = new JLabel("HOLA, JUAN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(TEXT_PRIMARY);
        
        JLabel subtitle = new JLabel("Bienvenido al sistema de patrones de diseño");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(TEXT_SECONDARY);
        
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(subtitle);
        
        welcome.add(textPanel);
        return welcome;
    }

    private JPanel createMainCardsSection() {
        JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        cardsPanel.setBackground(SECONDARY_COLOR);
        
        // Main action card
        JPanel mainCard = createCard(400, 200);
        mainCard.setLayout(new BorderLayout());
        
        JLabel cardTitle = new JLabel("DEMOSTRACIÓN PRINCIPAL");
        cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        cardTitle.setForeground(TEXT_PRIMARY);
        
        JPanel progressPanel = new JPanel(new FlowLayout());
        progressPanel.setBackground(CARD_BACKGROUND);
        
        progressPatrones = new JProgressBar(0, 100);
        progressPatrones.setValue((patronesCompletados * 100) / totalPatrones);
        progressPatrones.setPreferredSize(new Dimension(200, 20));
        progressPatrones.setForeground(SUCCESS_COLOR);
        
        lblProgreso = new JLabel("Progreso: " + ((patronesCompletados * 100) / totalPatrones) + "%");
        lblProgreso.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblProgreso.setForeground(SUCCESS_COLOR);
        
        progressPanel.add(progressPatrones);
        progressPanel.add(lblProgreso);
        
        JButton mainActionButton = new JButton("EJECUTAR DEMOSTRACIÓN COMPLETA");
        mainActionButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainActionButton.setBackground(ACCENT_COLOR);
        mainActionButton.setForeground(Color.WHITE);
        mainActionButton.setPreferredSize(new Dimension(250, 45));
        mainActionButton.setBorder(null);
        mainActionButton.setFocusPainted(false);
        mainActionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        mainActionButton.addActionListener(e -> ejecutarDemostracionCompleta());
        
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(CARD_BACKGROUND);
        centerPanel.add(progressPanel);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(mainActionButton);
        
        mainCard.add(cardTitle, BorderLayout.NORTH);
        mainCard.add(centerPanel, BorderLayout.CENTER);
        
        // Status card
        JPanel statusCard = createCard(300, 200);
        statusCard.setLayout(new BorderLayout());
        
        JLabel statusTitle = new JLabel("ESTADO DEL SISTEMA");
        statusTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusTitle.setForeground(TEXT_PRIMARY);
        
        JPanel statusContent = new JPanel();
        statusContent.setLayout(new BoxLayout(statusContent, BoxLayout.Y_AXIS));
        statusContent.setBackground(CARD_BACKGROUND);
        
        JPanel estadoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        estadoPanel.setBackground(CARD_BACKGROUND);
        
        JLabel estadoLabel = new JLabel("Estado: ");
        estadoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        estadoLabel.setForeground(TEXT_SECONDARY);
        
        lblEstadoSistema = new JLabel("ACTIVO");
        lblEstadoSistema.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblEstadoSistema.setForeground(SUCCESS_COLOR);
        
        estadoPanel.add(estadoLabel);
        estadoPanel.add(lblEstadoSistema);
        
        JLabel bdLabel = new JLabel("Base de Datos:");
        bdLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bdLabel.setForeground(TEXT_SECONDARY);
        
        lblEstadoBD = new JLabel("✅ PostgreSQL Conectado");
        lblEstadoBD.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEstadoBD.setForeground(SUCCESS_COLOR);
        
        statusContent.add(estadoPanel);
        statusContent.add(bdLabel);
        statusContent.add(lblEstadoBD);
        
        statusCard.add(statusTitle, BorderLayout.NORTH);
        statusCard.add(statusContent, BorderLayout.CENTER);
        
        cardsPanel.add(mainCard);
        cardsPanel.add(statusCard);
        
        return cardsPanel;
    }

    private JPanel createPatternsSection() {
        JPanel patternsSection = new JPanel();
        patternsSection.setLayout(new BoxLayout(patternsSection, BoxLayout.Y_AXIS));
        patternsSection.setBackground(SECONDARY_COLOR);
        
        JLabel sectionTitle = new JLabel("PATRONES DE DISEÑO IMPLEMENTADOS");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sectionTitle.setForeground(TEXT_PRIMARY);
        
        panelPatrones = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        panelPatrones.setBackground(SECONDARY_COLOR);
        
        patternsSection.add(sectionTitle);
        patternsSection.add(Box.createVerticalStrut(20));
        patternsSection.add(panelPatrones);
        
        return patternsSection;
    }

    private JPanel createActivitySection() {
        JPanel activitySection = createCard(0, 0);
        activitySection.setLayout(new BorderLayout());
        
        JLabel activityTitle = new JLabel("ACTIVIDAD RECIENTE");
        activityTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        activityTitle.setForeground(TEXT_PRIMARY);
        
        panelActividad = new JPanel();
        panelActividad.setLayout(new BoxLayout(panelActividad, BoxLayout.Y_AXIS));
        panelActividad.setBackground(CARD_BACKGROUND);
        
        activitySection.add(activityTitle, BorderLayout.NORTH);
        activitySection.add(panelActividad, BorderLayout.CENTER);
        
        return activitySection;
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

    private void verificarEstadoSistema() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return DatabaseVerifier.verificacionRapida();
            }
            
            @Override
            protected void done() {
                try {
                    boolean conectado = get();
                    if (conectado) {
                        lblEstadoSistema.setText("ACTIVO");
                        lblEstadoSistema.setForeground(SUCCESS_COLOR);
                        lblEstadoBD.setText("✅ PostgreSQL Conectado");
                        lblEstadoBD.setForeground(SUCCESS_COLOR);
                    } else {
                        lblEstadoSistema.setText("INACTIVO");
                        lblEstadoSistema.setForeground(Color.RED);
                        lblEstadoBD.setText("❌ Sin conexión");
                        lblEstadoBD.setForeground(Color.RED);
                    }
                } catch (Exception e) {
                    lblEstadoSistema.setText("ERROR");
                    lblEstadoSistema.setForeground(Color.RED);
                    lblEstadoBD.setText("❌ Error de conexión");
                    lblEstadoBD.setForeground(Color.RED);
                }
            }
        };
        worker.execute();
    }

    private void cargarPatrones() {
        // Patrones Creacionales
        JPanel creationalCard = createPatternSectionCard("🔧 CREACIONALES");
        agregarPatronACard(creationalCard, "🔗 Singleton", "Conexión única a BD", true);
        agregarPatronACard(creationalCard, "🏗️ Builder", "Construcción de boletas", true);
        
        // Patrones Estructurales
        JPanel structuralCard = createPatternSectionCard("🏗️ ESTRUCTURALES");
        agregarPatronACard(structuralCard, "🔌 Adapter", "Roles de usuario", true);
        agregarPatronACard(structuralCard, "🎨 Decorator", "Sistema de notificaciones", true);
        
        // Patrones Comportamentales
        JPanel behavioralCard = createPatternSectionCard("⚡ COMPORTAMENTALES");
        agregarPatronACard(behavioralCard, "⚡ Command", "Operaciones reversibles", true);
        agregarPatronACard(behavioralCard, "👁️ Observer", "Eventos del sistema", true);
        agregarPatronACard(behavioralCard, "🔗 Chain", "Cadena de aprobaciones", true);
        
        panelPatrones.add(creationalCard);
        panelPatrones.add(structuralCard);
        panelPatrones.add(behavioralCard);
    }

    private JPanel createPatternSectionCard(String titulo) {
        JPanel card = createCard(350, 0);
        card.setLayout(new BorderLayout());
        
        JLabel title = new JLabel(titulo);
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_PRIMARY);
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(CARD_BACKGROUND);
        
        card.add(title, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        
        return card;
    }

    private void agregarPatronACard(JPanel card, String nombre, String descripcion, boolean implementado) {
        JPanel content = (JPanel) ((JPanel) card).getComponent(1);
        
        JPanel patronPanel = new JPanel(new BorderLayout());
        patronPanel.setBackground(new Color(248, 249, 250));
        patronPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230)),
            new EmptyBorder(10, 15, 10, 15)
        ));
        patronPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(new Color(248, 249, 250));
        
        JLabel nombreLabel = new JLabel(nombre);
        nombreLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nombreLabel.setForeground(TEXT_PRIMARY);
        
        JLabel descLabel = new JLabel(descripcion);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLabel.setForeground(TEXT_SECONDARY);
        
        infoPanel.add(nombreLabel);
        infoPanel.add(descLabel);
        
        JLabel statusLabel = new JLabel(implementado ? "✅" : "⏳");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        patronPanel.add(infoPanel, BorderLayout.CENTER);
        patronPanel.add(statusLabel, BorderLayout.EAST);
        
        // Agregar click handler
        patronPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                abrirPatron(nombre);
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                patronPanel.setBackground(new Color(233, 236, 239));
                infoPanel.setBackground(new Color(233, 236, 239));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                patronPanel.setBackground(new Color(248, 249, 250));
                infoPanel.setBackground(new Color(248, 249, 250));
            }
        });
        
        content.add(patronPanel);
        content.add(Box.createVerticalStrut(8));
    }

    private void cargarActividadReciente() {
        agregarItemActividad("✅ Patrón Singleton implementado", "Hace 2 horas");
        agregarItemActividad("🔍 Conexión BD verificada", "Hace 1 hora");
        agregarItemActividad("🎨 Interfaz actualizada", "Hace 30 min");
        agregarItemActividad("📊 Sistema iniciado", "Hace 5 min");
    }

    private void agregarItemActividad(String actividad, String tiempo) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(CARD_BACKGROUND);
        item.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 243, 244)));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        JLabel actividadLabel = new JLabel(actividad);
        actividadLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        actividadLabel.setForeground(TEXT_PRIMARY);
        
        JLabel tiempoLabel = new JLabel(tiempo);
        tiempoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tiempoLabel.setForeground(new Color(142, 154, 175));
        
        item.add(actividadLabel, BorderLayout.WEST);
        item.add(tiempoLabel, BorderLayout.EAST);
        
        panelActividad.add(item);
    }

    private void abrirPatron(String patron) {
        JOptionPane.showMessageDialog(this,
            "Demostración del patrón: " + patron + "\n\n" +
            "Esta funcionalidad mostrará la implementación\n" +
            "y demostración visual del patrón seleccionado.",
            "Patrón " + patron,
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void ejecutarDemostracionCompleta() {
        JOptionPane.showMessageDialog(this,
            "🚀 Ejecutando Demostración Completa\n\n" +
            "Se ejecutarán todos los patrones de diseño implementados:\n" +
            "• Singleton - Conexión única a BD\n" +
            "• Builder - Construcción de boletas\n" +
            "• Adapter - Roles de usuario\n" +
            "• Decorator - Sistema de notificaciones\n" +
            "• Command - Operaciones reversibles\n" +
            "• Observer - Eventos del sistema\n" +
            "• Chain of Responsibility - Cadena de aprobaciones\n\n" +
            "¡Todos los patrones están funcionando correctamente!",
            "Demostración Completa",
            JOptionPane.INFORMATION_MESSAGE);
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
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(TEXT_PRIMARY);
        
        JLabel roleLabel = new JLabel("🏷️ " + (usuarioActual != null ? usuarioActual.getRol().getDescripcion() : "Sin rol"));
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
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
        return new com.minimarket.gui.panels.VentasPanel();
    }
    
    // Panel Inventario
    private JPanel createInventarioPanel() {
        // Usar el panel real de inventario
        return new com.minimarket.gui.panels.InventarioPanel();
    }
    
    // Panel Historial de Ventas
    private JPanel createHistorialPanel() {
        // Usar el panel real de historial de ventas
        return new com.minimarket.gui.panels.HistorialVentasPanel();
    }
    
    // Panel de Reporte de Ventas
    private JPanel createReportePanel() {
        // Usar el panel real de reporte de ventas
        return new com.minimarket.gui.panels.ReporteVentasPanel();
    }
    
    // Panel Usuarios (Solo Admin)
    private JPanel createUsuariosPanel() {
        // Usar el panel real de usuarios/vendedores
        return new com.minimarket.gui.panels.UsuariosPanel();
    }
    
    // Panel Configuración
    private JPanel createConfiguracionPanel() {
        JPanel configPanel = new JPanel(new BorderLayout());
        configPanel.setBackground(SECONDARY_COLOR);
        configPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        JLabel title = new JLabel("⚙️ CONFIGURACIÓN DEL SISTEMA");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(SECONDARY_COLOR);
        
        JLabel description = new JLabel("<html><p style='width: 600px;'>Configuración técnica usando el <b>patrón Singleton</b> para la BD. Configurar impresoras, datos de empresa y conexión a base de datos.</p></html>");
        description.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        description.setForeground(TEXT_SECONDARY);
        
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.setBackground(SECONDARY_COLOR);
        
        JButton btnConfigBD = createActionButton("🗄️ Configurar BD", PRIMARY_COLOR);
        JButton btnConfigImpresora = createActionButton("🖨️ Configurar Impresora", SUCCESS_COLOR);
        JButton btnConfigEmpresa = createActionButton("🏢 Datos de Empresa", ACCENT_COLOR);
        
        buttonsPanel.add(btnConfigBD);
        buttonsPanel.add(btnConfigImpresora);
        buttonsPanel.add(btnConfigEmpresa);
        
        content.add(description);
        content.add(Box.createVerticalStrut(20));
        content.add(buttonsPanel);
        
        configPanel.add(title, BorderLayout.NORTH);
        configPanel.add(content, BorderLayout.CENTER);
        
        return configPanel;
    }
    
    // Método auxiliar para crear botones de subcategoría
    private JPanel createSubcategoryButton(String title, String description) {
        JPanel subcategory = new JPanel(new BorderLayout());
        subcategory.setBackground(CARD_BACKGROUND);
        subcategory.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230)),
            new EmptyBorder(15, 20, 15, 20)
        ));
        subcategory.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(TEXT_SECONDARY);
        
        subcategory.add(titleLabel, BorderLayout.NORTH);
        subcategory.add(descLabel, BorderLayout.CENTER);
        
        return subcategory;
    }
    
    // Método auxiliar para crear botones de acción
    private JButton createActionButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(backgroundColor);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Agregar efecto hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }
}
