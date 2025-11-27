package com.minimarket.ui.swing;

import com.minimarket.config.DatabaseConnection;
import com.minimarket.security.UsuarioSesion;
import com.minimarket.security.Rol;
import com.minimarket.model.Usuario;
import com.minimarket.ui.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Pantalla de login del sistema POS
 */
public class LoginFrame extends JFrame {
    
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnSalir;
    private JLabel lblEstado;
    
    // Colores del tema
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color SECONDARY_COLOR = new Color(245, 247, 250);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private static final Color TEXT_PRIMARY = new Color(44, 62, 80);
    
    public LoginFrame() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        
        // Centrar en pantalla
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void initializeComponents() {
        setTitle("Sistema POS MiniMarket - Iniciar Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 420);
        setResizable(false);
        
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
        getContentPane().setBackground(SECONDARY_COLOR);
        
        // Panel principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(SECONDARY_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        // Logo y título
        JPanel headerPanel = createHeaderPanel();
        
        // Formulario de login
        JPanel formPanel = createFormPanel();
        
        // Botones
        JPanel buttonPanel = createButtonPanel();
        
        // Estado
        JPanel statusPanel = createStatusPanel();
        
        mainPanel.add(headerPanel);
        mainPanel.add(Box.createVerticalStrut(25));
        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(statusPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(SECONDARY_COLOR);
        
        // Logo (emoji como placeholder)
        JLabel lblLogo = new JLabel("🏪", JLabel.CENTER);
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Título
        JLabel lblTitulo = new JLabel("MINIMARKET PRO", JLabel.CENTER);
        lblTitulo.setFont(UIUtils.HEADER_FONT);
        lblTitulo.setForeground(TEXT_PRIMARY);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Subtítulo
        JLabel lblSubtitulo = new JLabel("Sistema de Punto de Venta", JLabel.CENTER);
        lblSubtitulo.setFont(UIUtils.DEFAULT_FONT);
        lblSubtitulo.setForeground(Color.GRAY);
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(lblLogo);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblTitulo);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lblSubtitulo);
        
        return panel;
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230)),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        
        // Campo usuario
        JLabel lblUsuario = new JLabel("Usuario:");
        lblUsuario.setFont(UIUtils.BOLD_FONT);
        lblUsuario.setForeground(TEXT_PRIMARY);
        lblUsuario.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtUsuario = new JTextField();
        txtUsuario.setFont(UIUtils.DEFAULT_FONT);
        txtUsuario.setPreferredSize(new Dimension(320, 40));
        txtUsuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtUsuario.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Campo contraseña
        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setFont(UIUtils.BOLD_FONT);
        lblPassword.setForeground(TEXT_PRIMARY);
        lblPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtPassword = new JPasswordField();
        txtPassword.setFont(UIUtils.DEFAULT_FONT);
        txtPassword.setPreferredSize(new Dimension(320, 40));
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Botón Iniciar Sesión (dentro del formulario)
        btnLogin = new JButton("Iniciar Sesión");
        btnLogin.setFont(UIUtils.BOLD_FONT);
        btnLogin.setBackground(PRIMARY_COLOR);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setPreferredSize(new Dimension(320, 45));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnLogin.setBorder(null);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Configurar acción del botón
        btnLogin.addActionListener(e -> realizarLogin());
        
        // Efecto hover para el botón
        btnLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnLogin.setBackground(PRIMARY_COLOR.darker());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnLogin.setBackground(PRIMARY_COLOR);
            }
        });
        
        panel.add(lblUsuario);
        panel.add(Box.createVerticalStrut(8));
        panel.add(txtUsuario);
        panel.add(Box.createVerticalStrut(20));
        panel.add(lblPassword);
        panel.add(Box.createVerticalStrut(8));
        panel.add(txtPassword);
        panel.add(Box.createVerticalStrut(25));
        panel.add(btnLogin);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panel.setBackground(SECONDARY_COLOR);
        
        btnSalir = new JButton("Salir del Sistema");
        btnSalir.setFont(UIUtils.DEFAULT_FONT);
        btnSalir.setBackground(new Color(108, 117, 125));
        btnSalir.setForeground(Color.WHITE);
        btnSalir.setPreferredSize(new Dimension(140, 35));
        btnSalir.setBorder(null);
        btnSalir.setFocusPainted(false);
        btnSalir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efecto hover para el botón salir
        btnSalir.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btnSalir.setBackground(new Color(108, 117, 125).darker());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btnSalir.setBackground(new Color(108, 117, 125));
            }
        });
        
        panel.add(btnSalir);
        
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(SECONDARY_COLOR);
        
        lblEstado = new JLabel("Ingrese sus credenciales para continuar");
        lblEstado.setFont(UIUtils.DEFAULT_FONT);
        lblEstado.setForeground(Color.GRAY);
        
        panel.add(lblEstado);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // Enter en los campos para hacer login
        KeyListener enterListener = new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    realizarLogin();
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
            @Override
            public void keyTyped(KeyEvent e) {}
        };
        
        txtUsuario.addKeyListener(enterListener);
        txtPassword.addKeyListener(enterListener);
        
        // Botón salir
        btnSalir.addActionListener(e -> System.exit(0));
        
        // Focus inicial en usuario
        SwingUtilities.invokeLater(() -> txtUsuario.requestFocus());
    }
    
    private void realizarLogin() {
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        System.out.println("=== INTENTO DE LOGIN ===");
        System.out.println("Usuario: " + usuario);
        System.out.println("Password length: " + password.length());
        
        if (usuario.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor ingrese usuario y contraseña");
            return;
        }
        
        // Deshabilitar botón mientras se procesa
        btnLogin.setEnabled(false);
        lblEstado.setText("Verificando credenciales...");
        lblEstado.setForeground(Color.BLUE);
        
        // Verificar credenciales en hilo separado
        SwingWorker<Usuario, Void> worker = new SwingWorker<Usuario, Void>() {
            @Override
            protected Usuario doInBackground() throws Exception {
                return verificarCredenciales(usuario, password);
            }
            
            @Override
            protected void done() {
                try {
                    Usuario usuarioAutenticado = get();
                    
                    if (usuarioAutenticado != null) {
                        // Login exitoso
                        System.out.println("Login exitoso para: " + usuarioAutenticado.getUsername());
                        UsuarioSesion.getInstance().login(usuarioAutenticado);
                        
                        // Verificar que se guardó correctamente
                        Usuario usuarioEnSesion = UsuarioSesion.getInstance().getUsuarioActual();
                        System.out.println("Usuario guardado en sesión: " + (usuarioEnSesion != null ? usuarioEnSesion.getUsername() : "null"));
                        
                        lblEstado.setText("¡Bienvenido " + usuarioAutenticado.getNombreCompleto() + "!");
                        lblEstado.setForeground(SUCCESS_COLOR);
                        
                        // Esperar un momento y abrir dashboard
                        Timer timer = new Timer(1000, ev -> {
                            try {
                                System.out.println("Creando DashboardFrame...");
                                
                                // Crear dashboard en el hilo de Swing
                                SwingUtilities.invokeLater(() -> {
                                    try {
                                        DashboardFrame dashboard = new DashboardFrame(); // Crear dashboard
                                        dashboard.setVisible(true); // Mostrar dashboard
                                        System.out.println("Dashboard creado exitosamente");
                                        dispose(); // Cerrar login solo si el dashboard se creó correctamente
                                    } catch (Exception ex) {
                                        System.out.println("ERROR al crear dashboard: " + ex.getMessage());
                                        ex.printStackTrace();
                                        
                                        // Mostrar error y mantener login abierto
                                        lblEstado.setText("Error al abrir el sistema: " + ex.getMessage());
                                        lblEstado.setForeground(DANGER_COLOR);
                                        btnLogin.setEnabled(true);
                                    }
                                });
                                
                            } catch (Exception ex) {
                                System.out.println("ERROR general: " + ex.getMessage());
                                ex.printStackTrace();
                                
                                // Mostrar error y mantener login abierto
                                lblEstado.setText("Error al abrir el sistema: " + ex.getMessage());
                                lblEstado.setForeground(DANGER_COLOR);
                                btnLogin.setEnabled(true);
                            }
                        });
                        timer.setRepeats(false);
                        timer.start();
                        
                    } else {
                        // Login fallido
                        mostrarError("Usuario o contraseña incorrectos");
                        txtPassword.setText("");
                        txtUsuario.requestFocus();
                    }
                } catch (Exception e) {
                    mostrarError("Error al conectar con la base de datos: " + e.getMessage());
                } finally {
                    btnLogin.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    private Usuario verificarCredenciales(String username, String password) throws SQLException {
        System.out.println("=== VERIFICANDO CREDENCIALES ===");
        System.out.println("Buscando usuario: " + username);
        
        // MODO DE PRUEBA: Usuarios hardcodeados mientras verificamos la BD
        if ("admin".equals(username) && "admin123".equals(password)) {
            System.out.println("LOGIN EXITOSO - ADMIN (modo prueba)");
            Usuario usuario = new Usuario();
            usuario.setId(1L);
            usuario.setUsername("admin");
            usuario.setNombre("Administrador");
            usuario.setApellido("Sistema");
            usuario.setEmail("admin@minimarket.com");
            usuario.setRol(Rol.ADMINISTRADOR);
            usuario.setActivo(true);
            return usuario;
        }
        
        if ("supervisor".equals(username) && "super123".equals(password)) {
            System.out.println("LOGIN EXITOSO - SUPERVISOR (modo prueba)");
            Usuario usuario = new Usuario();
            usuario.setId(2L);
            usuario.setUsername("supervisor");
            usuario.setNombre("Supervisor");
            usuario.setApellido("Tienda");
            usuario.setEmail("supervisor@minimarket.com");
            usuario.setRol(Rol.SUPERVISOR);
            usuario.setActivo(true);
            return usuario;
        }
        
        if ("cajero".equals(username) && "cajero123".equals(password)) {
            System.out.println("LOGIN EXITOSO - CAJERO (modo prueba)");
            Usuario usuario = new Usuario();
            usuario.setId(3L);
            usuario.setUsername("cajero");
            usuario.setNombre("Cajero");
            usuario.setApellido("Principal");
            usuario.setEmail("cajero@minimarket.com");
            usuario.setRol(Rol.CAJERO);
            usuario.setActivo(true);
            return usuario;
        }
        
        // Intentar con la base de datos
        String sql = "SELECT id, username, nombre_completo, email, rol, activo, password_hash " +
                    "FROM usuarios " +
                    "WHERE username = ? AND activo = true";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String passwordHash = rs.getString("password_hash");
                    String rol = rs.getString("rol");
                    
                    System.out.println("Usuario encontrado en BD: " + username);
                    System.out.println("Rol: " + rol);
                    System.out.println("Password hash: " + passwordHash);
                    
                    // Verificar contraseña
                    if (verificarPassword(password, passwordHash)) {
                        System.out.println("Contraseña correcta!");
                        // Crear objeto usuario
                        Usuario usuario = new Usuario();
                        usuario.setId(rs.getLong("id"));
                        usuario.setUsername(rs.getString("username"));
                        
                        // Separar nombre completo en nombre y apellido
                        String nombreCompleto = rs.getString("nombre_completo");
                        if (nombreCompleto != null && nombreCompleto.contains(" ")) {
                            String[] partes = nombreCompleto.split(" ", 2);
                            usuario.setNombre(partes[0]);
                            usuario.setApellido(partes[1]);
                        } else {
                            usuario.setNombre(nombreCompleto != null ? nombreCompleto : "Usuario");
                            usuario.setApellido("");
                        }
                        
                        usuario.setEmail(rs.getString("email"));
                        usuario.setRol(Rol.valueOf(rs.getString("rol")));
                        usuario.setActivo(rs.getBoolean("activo"));
                        
                        return usuario;
                    } else {
                        System.out.println("Contraseña incorrecta");
                    }
                } else {
                    System.out.println("Usuario no encontrado en BD: " + username);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error de BD: " + e.getMessage());
        }
        
        System.out.println("Credenciales inválidas");
        return null; // Credenciales inválidas
    }
    
    private boolean verificarPassword(String password, String hash) {
        try {
            // Si no hay hash, comparar directamente (para usuarios de prueba)
            if (hash == null || hash.isEmpty()) {
                return false;
            }
            
            // Si el hash es texto plano (para pruebas), comparar directamente
            if (!hash.startsWith("$") && hash.length() < 32) {
                return password.equals(hash);
            }
            
            // Hash MD5 simple para compatibilidad
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            String passwordHash = sb.toString();
            
            return passwordHash.equals(hash) || password.equals(hash);
            
        } catch (NoSuchAlgorithmException e) {
            return password.equals(hash); // Fallback a comparación directa
        }
    }
    
    private void mostrarError(String mensaje) {
        lblEstado.setText(mensaje);
        lblEstado.setForeground(DANGER_COLOR);
        
        // Limpiar mensaje después de 3 segundos
        Timer timer = new Timer(3000, e -> {
            lblEstado.setText("Ingrese sus credenciales para continuar");
            lblEstado.setForeground(Color.GRAY);
        });
        timer.setRepeats(false);
        timer.start();
    }
}
