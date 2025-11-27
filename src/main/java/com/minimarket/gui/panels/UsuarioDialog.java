package com.minimarket.gui.panels;

import com.minimarket.patterns.creational.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * Diálogo para crear o editar usuarios
 */
public class UsuarioDialog extends JDialog {
    
    private JTextField campoUsername;
    private JPasswordField campoPassword;
    private JTextField campoNombre;
    private JTextField campoApellido;
    private JTextField campoEmail;
    private JComboBox<String> comboRol;
    
    private boolean confirmado = false;
    private boolean esEdicion;
    private Long usuarioId;
    
    public UsuarioDialog(JFrame parent, String titulo, boolean esNuevo, Long usuarioId) {
        super(parent, titulo, true);
        this.esEdicion = !esNuevo;
        this.usuarioId = usuarioId;
        
        initializeComponents();
        
        if (esEdicion && usuarioId != null) {
            cargarDatosUsuario();
        }
        
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setSize(450, 350);
        
        // Panel principal
        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Username
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panelPrincipal.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        campoUsername = new JTextField(20);
        panelPrincipal.add(campoUsername, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panelPrincipal.add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        campoPassword = new JPasswordField(20);
        panelPrincipal.add(campoPassword, gbc);
        
        // Nombre
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panelPrincipal.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        campoNombre = new JTextField(20);
        panelPrincipal.add(campoNombre, gbc);
        
        // Apellido
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panelPrincipal.add(new JLabel("Apellido:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        campoApellido = new JTextField(20);
        panelPrincipal.add(campoApellido, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panelPrincipal.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        campoEmail = new JTextField(20);
        panelPrincipal.add(campoEmail, gbc);
        
        // Rol
        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panelPrincipal.add(new JLabel("Rol:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        comboRol = new JComboBox<>(new String[]{"CAJERO", "SUPERVISOR", "ADMINISTRADOR"});
        panelPrincipal.add(comboRol, gbc);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new JButton(esEdicion ? "Actualizar" : "Crear Usuario");
        JButton btnCancelar = new JButton("Cancelar");
        
        btnGuardar.setBackground(new Color(46, 125, 50));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        
        btnCancelar.setBackground(new Color(158, 158, 158));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        
        btnGuardar.addActionListener(e -> guardarUsuario());
        btnCancelar.addActionListener(e -> dispose());
        
        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        
        add(panelPrincipal, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }
    
    private void cargarDatosUsuario() {
        String sql = "SELECT username, nombre, apellido, email, rol FROM usuarios WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                campoUsername.setText(rs.getString("username"));
                campoNombre.setText(rs.getString("nombre"));
                campoApellido.setText(rs.getString("apellido"));
                campoEmail.setText(rs.getString("email"));
                comboRol.setSelectedItem(rs.getString("rol"));
                
                // En edición, no mostrar la contraseña actual
                campoPassword.setToolTipText("Dejar vacío para mantener la contraseña actual");
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar datos del usuario: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void guardarUsuario() {
        // Validaciones
        if (campoUsername.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre de usuario es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!esEdicion && new String(campoPassword.getPassword()).trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "La contraseña es obligatoria para usuarios nuevos.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (campoNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (campoApellido.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El apellido es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (campoEmail.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El email es obligatorio.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Validar formato de email básico
        String email = campoEmail.getText().trim();
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "El formato del email no es válido.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Verificar si el username ya existe (solo para usuarios nuevos o si cambió)
        if (!esEdicion || !campoUsername.getText().trim().equals(obtenerUsernameActual())) {
            if (existeUsername(campoUsername.getText().trim())) {
                JOptionPane.showMessageDialog(this, "El nombre de usuario ya existe. Por favor, elige otro.", "Usuario Duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
        // Verificar si el email ya existe (solo para usuarios nuevos o si cambió)
        if (!esEdicion || !email.equals(obtenerEmailActual())) {
            if (existeEmail(email)) {
                JOptionPane.showMessageDialog(this, "El email ya está registrado. Por favor, usa otro.", "Email Duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
        // Guardar en base de datos
        String sql;
        if (esEdicion) {
            if (new String(campoPassword.getPassword()).trim().isEmpty()) {
                // No actualizar contraseña
                sql = """
                    UPDATE usuarios SET username = ?, nombre = ?, apellido = ?, email = ?, rol = ?, 
                           updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                """;
            } else {
                // Actualizar con nueva contraseña
                sql = """
                    UPDATE usuarios SET username = ?, password = ?, nombre = ?, apellido = ?, email = ?, rol = ?, 
                           updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                """;
            }
        } else {
            sql = """
                INSERT INTO usuarios (username, password, nombre, apellido, email, rol, activo) 
                VALUES (?, ?, ?, ?, ?, ?, true)
            """;
        }
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            int paramIndex = 1;
            pstmt.setString(paramIndex++, campoUsername.getText().trim());
            
            String password = new String(campoPassword.getPassword()).trim();
            if (!esEdicion || !password.isEmpty()) {
                pstmt.setString(paramIndex++, password); // En producción, debería estar hasheada
            }
            
            pstmt.setString(paramIndex++, campoNombre.getText().trim());
            pstmt.setString(paramIndex++, campoApellido.getText().trim());
            pstmt.setString(paramIndex++, email);
            pstmt.setString(paramIndex++, (String) comboRol.getSelectedItem());
            
            if (esEdicion) {
                pstmt.setLong(paramIndex, usuarioId);
            }
            
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                confirmado = true;
                JOptionPane.showMessageDialog(this, 
                    "Usuario " + (esEdicion ? "actualizado" : "creado") + " exitosamente.", 
                    "Éxito", 
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "No se pudo " + (esEdicion ? "actualizar" : "crear") + " el usuario.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al " + (esEdicion ? "actualizar" : "crear") + " usuario: " + e.getMessage(), 
                "Error de Base de Datos", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean existeUsername(String username) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ? AND activo = true";
        if (esEdicion) {
            sql += " AND id != ?";
        }
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            if (esEdicion) {
                pstmt.setLong(2, usuarioId);
            }
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            // Log error but don't show to user
        }
        
        return false;
    }
    
    private boolean existeEmail(String email) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ? AND activo = true";
        if (esEdicion) {
            sql += " AND id != ?";
        }
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            if (esEdicion) {
                pstmt.setLong(2, usuarioId);
            }
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            // Log error but don't show to user
        }
        
        return false;
    }
    
    private String obtenerUsernameActual() {
        String sql = "SELECT username FROM usuarios WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("username");
            }
            
        } catch (SQLException e) {
            // Log error
        }
        
        return "";
    }
    
    private String obtenerEmailActual() {
        String sql = "SELECT email FROM usuarios WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("email");
            }
            
        } catch (SQLException e) {
            // Log error
        }
        
        return "";
    }
    
    public boolean isConfirmado() {
        return confirmado;
    }
}
