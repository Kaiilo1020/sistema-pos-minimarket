package com.minimarket.ui.swing;

import com.minimarket.config.DatabaseConnection;
import com.minimarket.model.Usuario;
import com.minimarket.security.Rol;
import com.minimarket.security.UsuarioSesion;
import com.minimarket.ui.theme.EstilosApp;
import com.minimarket.ui.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.*;

/**
 * Pantalla de login del sistema POS
 */
public class LoginFrame extends JFrame {
    
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnSalir;
    private JLabel lblEstado;
    
    public LoginFrame() {
        configurarVentana();
        construirUI();
        configurarEventos();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    /* ========================== UI ========================== */
    
    private void configurarVentana() {
        setTitle("Sistema POS MiniMarket - Iniciar Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 550);
        setResizable(false);
        
        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(
                getClass().getResource("/images/minimarket-logo.png")));
        } catch (Exception e) {
            // Continuar sin icono
        }
    }
    
    private void construirUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 247, 250));
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        JPanel headerPanel = createHeaderPanel();
        JPanel formPanel = createFormPanel();
        JPanel buttonPanel = createButtonPanel();
        JPanel statusPanel = createStatusPanel();
        
        mainPanel.add(headerPanel);
        mainPanel.add(Box.createVerticalStrut(30));
        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(25));
        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(statusPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 247, 250));
        
        // Logo (emoji como placeholder)
        JLabel lblLogo = new JLabel("🏪", JLabel.CENTER);
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Título
        JLabel lblTitulo = new JLabel("MINIMARKET PRO", JLabel.CENTER);
        lblTitulo.setFont(UIUtils.HEADER_FONT);
        lblTitulo.setForeground(new Color(44, 62, 80));
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
            BorderFactory.createEmptyBorder(35, 35, 40, 35)
        ));
        
        // Campo usuario - Panel envoltorio
        JPanel panelUsuario = new JPanel();
        panelUsuario.setLayout(new BoxLayout(panelUsuario, BoxLayout.Y_AXIS));
        panelUsuario.setBackground(Color.WHITE);
        panelUsuario.setOpaque(false); // Transparente
        panelUsuario.setPreferredSize(new Dimension(350, 60));
        panelUsuario.setMaximumSize(new Dimension(350, 60));
        panelUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblUsuario = new JLabel("Usuario:");
        lblUsuario.setFont(UIUtils.BOLD_FONT);
        lblUsuario.setForeground(new Color(44, 62, 80));
        lblUsuario.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtUsuario = new JTextField();
        txtUsuario.setFont(UIUtils.DEFAULT_FONT);
        txtUsuario.setHorizontalAlignment(JTextField.LEFT);
        txtUsuario.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtUsuario.setPreferredSize(new Dimension(350, 45));
        txtUsuario.setMaximumSize(new Dimension(350, 45));
        txtUsuario.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        panelUsuario.add(lblUsuario);
        panelUsuario.add(Box.createVerticalStrut(10));
        panelUsuario.add(txtUsuario);
        
        // Campo contraseña - Panel envoltorio
        JPanel panelPassword = new JPanel();
        panelPassword.setLayout(new BoxLayout(panelPassword, BoxLayout.Y_AXIS));
        panelPassword.setBackground(Color.WHITE);
        panelPassword.setOpaque(false); // Transparente
        panelPassword.setPreferredSize(new Dimension(350, 60));
        panelPassword.setMaximumSize(new Dimension(350, 60));
        panelPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setFont(UIUtils.BOLD_FONT);
        lblPassword.setForeground(new Color(44, 62, 80));
        lblPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        txtPassword = new JPasswordField();
        txtPassword.setFont(UIUtils.DEFAULT_FONT);
        txtPassword.setHorizontalAlignment(JPasswordField.LEFT);
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPassword.setPreferredSize(new Dimension(350, 45));
        txtPassword.setMaximumSize(new Dimension(350, 45));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        panelPassword.add(lblPassword);
        panelPassword.add(Box.createVerticalStrut(10));
        panelPassword.add(txtPassword);
        
        // Botón Iniciar Sesión (dentro del formulario)
        btnLogin = new JButton("Iniciar Sesión");
        EstilosApp.estilizarBoton(btnLogin);
        Dimension preferred = btnLogin.getPreferredSize();
        preferred.width = Math.max(preferred.width, 350);
        btnLogin.setPreferredSize(preferred);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferred.height));
        btnLogin.setFont(new Font(UIUtils.BOLD_FONT.getName(), Font.BOLD, 16));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> realizarLogin());
        
        panel.add(panelUsuario);
        panel.add(Box.createVerticalStrut(15));
        panel.add(panelPassword);
        panel.add(Box.createVerticalStrut(30));
        panel.add(btnLogin);
        panel.add(Box.createVerticalStrut(15));
        
        // Enlace "¿Olvidaste tu contraseña?" - Dentro del panel blanco, centrado
        JLabel lblOlvidoPassword = new JLabel("<html><u>¿Olvidaste tu contraseña?</u></html>");
        lblOlvidoPassword.setFont(new Font(UIUtils.DEFAULT_FONT.getName(), Font.PLAIN, 13));
        lblOlvidoPassword.setForeground(new Color(100, 100, 100)); // Gris oscuro, no azul
        lblOlvidoPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblOlvidoPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblOlvidoPassword.setHorizontalAlignment(JLabel.CENTER); // Centrado horizontal
        lblOlvidoPassword.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                mostrarDialogoRecuperacion();
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                lblOlvidoPassword.setForeground(new Color(70, 70, 70)); // Más oscuro al hover
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                lblOlvidoPassword.setForeground(new Color(100, 100, 100)); // Vuelve al gris
            }
        });
        
        panel.add(lblOlvidoPassword);
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panel.setBackground(new Color(245, 247, 250));
        
        btnSalir = new JButton("Salir del Sistema");
        EstilosApp.estilizarBotonNeutro(btnSalir);
        btnSalir.setPreferredSize(new Dimension(180, 45));
        panel.add(btnSalir);
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(new Color(245, 247, 250));
        
        lblEstado = new JLabel("Ingrese sus credenciales para continuar");
        lblEstado.setFont(UIUtils.DEFAULT_FONT);
        lblEstado.setForeground(Color.GRAY);
        
        panel.add(lblEstado);
        return panel;
    }
    
    /* ========================== EVENTOS ========================== */
    
    private void configurarEventos() {
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
                        UsuarioSesion.getInstance().login(usuarioAutenticado);
                        
                        lblEstado.setText("¡Bienvenido " + usuarioAutenticado.getNombreCompleto() + "!");
                        lblEstado.setForeground(EstilosApp.COLOR_PRIMARIO);
                        
                        // Esperar un momento y abrir dashboard
                        Timer timer = new Timer(1000, ev -> {
                            try {
                                // Crear dashboard en el hilo de Swing
                                SwingUtilities.invokeLater(() -> {
                                    try {
                                        DashboardFrame dashboard = new DashboardFrame(); // Crear dashboard
                                        dashboard.setVisible(true); // Mostrar dashboard
                                        dispose(); // Cerrar login solo si el dashboard se creó correctamente
                                    } catch (Exception ex) {
                                        // Mostrar error y mantener login abierto
                                        lblEstado.setText("Error al abrir el sistema: " + ex.getMessage());
                                        lblEstado.setForeground(Color.RED);
                                        btnLogin.setEnabled(true);
                                    }
                                });
                                
                            } catch (Exception ex) {
                                // Mostrar error y mantener login abierto
                                lblEstado.setText("Error al abrir el sistema: " + ex.getMessage());
                                lblEstado.setForeground(Color.RED);
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
        // Consultar SOLO la base de datos PostgreSQL
        String sql = "SELECT id, username, email, rol, activo, password " +
                    "FROM usuarios " +
                    "WHERE username = ? AND activo = true";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String passwordBD = rs.getString("password");
                    String rol = rs.getString("rol");
                    
                    // Verificar contraseña (comparación directa ya que están en texto plano)
                    if (password.equals(passwordBD)) {
                        // Crear objeto usuario
                        Usuario usuario = new Usuario();
                        usuario.setId(rs.getLong("id"));
                        usuario.setUsername(rs.getString("username"));
                        
                        // Asignar nombre basado en el username y rol
                        String nombreDisplay = "";
                        switch (rol.toUpperCase()) {
                            case "ADMINISTRADOR":
                                nombreDisplay = "Administrador";
                                break;
                            case "SUPERVISOR":
                                nombreDisplay = "Supervisor";
                                break;
                            case "CAJERO":
                                nombreDisplay = "Cajero";
                                break;
                            default:
                                nombreDisplay = username;
                        }
                        usuario.setNombre(nombreDisplay);
                        usuario.setApellido("Sistema");
                        
                        usuario.setEmail(rs.getString("email"));
                        
                        // Mapear rol de la base de datos
                        String rolBD = rs.getString("rol").toUpperCase();
                        Rol rolUsuario;
                        switch (rolBD) {
                            case "ADMINISTRADOR":
                            case "ADMIN":
                                rolUsuario = Rol.ADMINISTRADOR;
                                break;
                            case "SUPERVISOR":
                                rolUsuario = Rol.SUPERVISOR;
                                break;
                            case "CAJERO":
                            case "CAJERA":
                                rolUsuario = Rol.CAJERO;
                                break;
                            default:
                                rolUsuario = Rol.CAJERO;
                        }
                        
                        usuario.setRol(rolUsuario);
                        usuario.setActivo(rs.getBoolean("activo"));
                        
                        return usuario;
                    }
                }
            }
        } catch (SQLException e) {
            // Error silencioso - no revelar información
        }
        return null; // Credenciales inválidas
    }
    
    private void mostrarError(String mensaje) {
        lblEstado.setText(mensaje);
        lblEstado.setForeground(Color.RED);
        
        // Limpiar mensaje después de 3 segundos
        Timer timer = new Timer(3000, e -> {
            lblEstado.setText("Ingrese sus credenciales para continuar");
            lblEstado.setForeground(Color.GRAY);
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    /**
     * Muestra el diálogo de recuperación de contraseña
     */
    private void mostrarDialogoRecuperacion() {
        JDialog dialog = new JDialog(this, "Recuperación de Contraseña", true);
        dialog.setSize(400, 180);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        // Panel principal
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Label instrucción
        JLabel lblInstruccion = new JLabel("Ingresa tu nombre de usuario:");
        lblInstruccion.setFont(UIUtils.DEFAULT_FONT);
        lblInstruccion.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Campo de texto para usuario
        JTextField txtUsuarioRecuperacion = new JTextField();
        txtUsuarioRecuperacion.setFont(UIUtils.DEFAULT_FONT);
        txtUsuarioRecuperacion.setPreferredSize(new Dimension(300, 35));
        txtUsuarioRecuperacion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        txtUsuarioRecuperacion.setHorizontalAlignment(JTextField.CENTER);
        txtUsuarioRecuperacion.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        JButton btnAceptar = new JButton("Aceptar");
        btnAceptar.setFont(UIUtils.DEFAULT_FONT);
        btnAceptar.setPreferredSize(new Dimension(120, 40));
        EstilosApp.estilizarBotonSecundario(btnAceptar);
        
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(UIUtils.DEFAULT_FONT);
        btnCancelar.setPreferredSize(new Dimension(100, 35));
        btnCancelar.setBackground(new Color(108, 117, 125));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setBorder(null);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panelBotones.add(btnAceptar);
        panelBotones.add(btnCancelar);
        
        // Agregar componentes
        panel.add(lblInstruccion);
        panel.add(Box.createVerticalStrut(15));
        panel.add(txtUsuarioRecuperacion);
        panel.add(Box.createVerticalStrut(20));
        panel.add(panelBotones);
        
        dialog.add(panel, BorderLayout.CENTER);
        
        // Acción del botón Aceptar
        btnAceptar.addActionListener(e -> {
            String username = txtUsuarioRecuperacion.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "Por favor ingrese un nombre de usuario", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Verificar que el usuario existe
            if (verificarUsuarioExiste(username)) {
                dialog.dispose();
                mostrarDialogoNuevaPassword(username);
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "El usuario no existe en el sistema", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Acción del botón Cancelar
        btnCancelar.addActionListener(e -> dialog.dispose());
        
        // Enter en el campo de texto
        txtUsuarioRecuperacion.addActionListener(e -> btnAceptar.doClick());
        
        dialog.setVisible(true);
        SwingUtilities.invokeLater(() -> txtUsuarioRecuperacion.requestFocus());
    }
    
    /**
     * Muestra el diálogo para establecer nueva contraseña
     */
    private void mostrarDialogoNuevaPassword(String username) {
        JDialog dialog = new JDialog(this, "Establecer Nueva Contraseña", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        // Panel principal
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Campo nueva contraseña
        JLabel lblNuevaPassword = new JLabel("Nueva contraseña:");
        lblNuevaPassword.setFont(UIUtils.DEFAULT_FONT);
        lblNuevaPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPasswordField txtNuevaPassword = new JPasswordField();
        txtNuevaPassword.setFont(UIUtils.DEFAULT_FONT);
        txtNuevaPassword.setPreferredSize(new Dimension(300, 35));
        txtNuevaPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        txtNuevaPassword.setHorizontalAlignment(JPasswordField.CENTER);
        txtNuevaPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        // Campo confirmar contraseña
        JLabel lblConfirmarPassword = new JLabel("Confirmar contraseña:");
        lblConfirmarPassword.setFont(UIUtils.DEFAULT_FONT);
        lblConfirmarPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPasswordField txtConfirmarPassword = new JPasswordField();
        txtConfirmarPassword.setFont(UIUtils.DEFAULT_FONT);
        txtConfirmarPassword.setPreferredSize(new Dimension(300, 35));
        txtConfirmarPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        txtConfirmarPassword.setHorizontalAlignment(JPasswordField.CENTER);
        txtConfirmarPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        JButton btnAceptar = new JButton("Aceptar");
        btnAceptar.setFont(UIUtils.DEFAULT_FONT);
        btnAceptar.setPreferredSize(new Dimension(120, 40));
        EstilosApp.estilizarBoton(btnAceptar);
        
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(UIUtils.DEFAULT_FONT);
        btnCancelar.setPreferredSize(new Dimension(100, 35));
        btnCancelar.setBackground(new Color(108, 117, 125));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setBorder(null);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panelBotones.add(btnAceptar);
        panelBotones.add(btnCancelar);
        
        // Agregar componentes
        panel.add(lblNuevaPassword);
        panel.add(Box.createVerticalStrut(8));
        panel.add(txtNuevaPassword);
        panel.add(Box.createVerticalStrut(15));
        panel.add(lblConfirmarPassword);
        panel.add(Box.createVerticalStrut(8));
        panel.add(txtConfirmarPassword);
        panel.add(Box.createVerticalStrut(20));
        panel.add(panelBotones);
        
        dialog.add(panel, BorderLayout.CENTER);
        
        // Acción del botón Aceptar
        btnAceptar.addActionListener(e -> {
            String nuevaPassword = new String(txtNuevaPassword.getPassword());
            String confirmarPassword = new String(txtConfirmarPassword.getPassword());
            
            if (nuevaPassword.isEmpty() || confirmarPassword.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, 
                    "Por favor complete ambos campos", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!nuevaPassword.equals(confirmarPassword)) {
                JOptionPane.showMessageDialog(dialog, 
                    "Las contraseñas no coinciden", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                txtNuevaPassword.setText("");
                txtConfirmarPassword.setText("");
                txtNuevaPassword.requestFocus();
                return;
            }
            
            if (nuevaPassword.length() < 4) {
                JOptionPane.showMessageDialog(dialog, 
                    "La contraseña debe tener al menos 4 caracteres", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Actualizar contraseña en la base de datos
            if (actualizarPasswordEnBD(username, nuevaPassword)) {
                JOptionPane.showMessageDialog(dialog, 
                    "Contraseña actualizada exitosamente", 
                    "Éxito", 
                    JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "Error al actualizar la contraseña. Intente nuevamente.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Acción del botón Cancelar
        btnCancelar.addActionListener(e -> dialog.dispose());
        
        dialog.setVisible(true);
        SwingUtilities.invokeLater(() -> txtNuevaPassword.requestFocus());
    }
    
    /**
     * Verifica si un usuario existe en la base de datos
     */
    private boolean verificarUsuarioExiste(String username) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ? AND activo = true";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            // Error silencioso - no revelar información
        }
        
        return false;
    }
    
    /**
     * Actualiza la contraseña de un usuario en la base de datos
     */
    private boolean actualizarPasswordEnBD(String username, String nuevaPassword) {
        String sql = "UPDATE usuarios SET password = ? WHERE username = ? AND activo = true";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nuevaPassword);
            stmt.setString(2, username);
            
            int filasAfectadas = stmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                return true;
            } else {
                return false;
            }
            
        } catch (SQLException e) {
            return false;
        }
    }
}
