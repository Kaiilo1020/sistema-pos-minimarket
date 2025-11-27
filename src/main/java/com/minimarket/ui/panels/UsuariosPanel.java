package com.minimarket.ui.panels;

import com.minimarket.config.DatabaseConnection;
import com.minimarket.ui.util.UIUtils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * Panel de gestión de usuarios del sistema
 */
public class UsuariosPanel extends JPanel {
    
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;
    private JButton btnCrearUsuario;
    private JButton btnEliminarUsuario;
    private JButton btnActualizar;
    private JLabel labelTotalUsuarios;
    
    public UsuariosPanel() {
        initializeComponents();
        cargarUsuarios();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Panel superior con botones
        JPanel panelSuperior = UIUtils.configurarPanel(new BorderLayout(), 10);
        
        // Panel de botones
        JPanel panelBotones = UIUtils.crearPanelBotones(FlowLayout.LEFT);
        
        btnCrearUsuario = new JButton("Crear Usuario");
        btnEliminarUsuario = new JButton("Eliminar Usuario");
        btnActualizar = new JButton("Actualizar");
        
        // Estilo de botones usando UIUtils
        UIUtils.configurarBotonExito(btnCrearUsuario);
        UIUtils.configurarBotonPeligro(btnEliminarUsuario);
        UIUtils.configurarBotonSecundario(btnActualizar);
        
        // Eventos de botones
        btnCrearUsuario.addActionListener(e -> abrirDialogoCrearUsuario());
        btnEliminarUsuario.addActionListener(e -> eliminarUsuarioSeleccionado());
        btnActualizar.addActionListener(e -> cargarUsuarios());
        
        panelBotones.add(btnCrearUsuario);
        panelBotones.add(btnEliminarUsuario);
        panelBotones.add(btnActualizar);
        
        panelSuperior.add(panelBotones, BorderLayout.WEST);
        
        // Tabla de usuarios
        String[] columnas = {"ID", "Usuario", "Rol", "Ventas Registradas"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer la tabla no editable
            }
        };
        
        tablaUsuarios = new JTable(modeloTabla);
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaUsuarios.setRowHeight(30);
        UIUtils.configurarTabla(tablaUsuarios);
        
        // Configurar ancho de columnas de forma flexible
        UIUtils.configurarColumnasTabla(tablaUsuarios, 50, 200, 150, 150);
        
        JScrollPane scrollPane = new JScrollPane(tablaUsuarios);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista de Usuarios"));
        
        // Panel inferior con información
        JPanel panelInferior = UIUtils.configurarPanel(new BorderLayout(), 10);
        
        // Panel de información (eliminamos navegación innecesaria)
        JPanel panelInfo = UIUtils.crearPanelBotones(FlowLayout.CENTER);
        
        labelTotalUsuarios = new JLabel("Total: 0 usuarios");
        labelTotalUsuarios.setFont(UIUtils.BOLD_FONT);
        
        panelInfo.add(labelTotalUsuarios);
        
        // Nota informativa
        JPanel panelNota = UIUtils.crearPanelBotones(FlowLayout.LEFT);
        
        JLabel labelNota = new JLabel("<html><b>Nota:</b> Solo se pueden eliminar usuarios que NO tengan ventas registradas.<br>" +
                                     "Si un trabajador tiene ventas, se conservan para el historial del negocio.</html>");
        labelNota.setFont(UIUtils.DEFAULT_FONT);
        labelNota.setForeground(Color.GRAY);
        
        panelNota.add(labelNota);
        
        panelInferior.add(panelInfo, BorderLayout.CENTER);
        panelInferior.add(panelNota, BorderLayout.SOUTH);
        
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    private void cargarUsuarios() {
        modeloTabla.setRowCount(0); // Limpiar tabla
        
        String sql = """
            SELECT u.id, u.username, u.rol, 
                   COUNT(v.id) as total_ventas
            FROM usuarios u
            LEFT JOIN ventas v ON u.id = v.cajera_id AND v.estado = 'ACTIVA'
            WHERE u.activo = true
            GROUP BY u.id, u.username, u.rol
            ORDER BY u.username
        """;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            int totalUsuarios = 0;
            
            while (rs.next()) {
                totalUsuarios++;
                
                String rol = rs.getString("rol");
                // Convertir rol para mostrar
                String rolMostrar = switch (rol) {
                    case "ADMINISTRADOR" -> "Administrador";
                    case "SUPERVISOR" -> "Supervisor";
                    case "CAJERO" -> "Trabajador";
                    default -> rol;
                };
                
                int totalVentas = rs.getInt("total_ventas");
                String ventasTexto = totalVentas + " venta" + (totalVentas != 1 ? "s" : "");
                
                Object[] fila = {
                    rs.getLong("id"),
                    rs.getString("username"),
                    rolMostrar,
                    ventasTexto
                };
                modeloTabla.addRow(fila);
            }
            
            labelTotalUsuarios.setText("Total: " + totalUsuarios + " usuarios");
            
        } catch (SQLException e) {
            UIUtils.mostrarError(this, "Error al cargar usuarios: " + e.getMessage());
        }
    }
    
    private void abrirDialogoCrearUsuario() {
        UsuarioDialog dialog = new UsuarioDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                 "Crear Usuario", true, null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmado()) {
            cargarUsuarios(); // Recargar la tabla
        }
    }
    
    private void eliminarUsuarioSeleccionado() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada == -1) {
            UIUtils.mostrarError(this, "Por favor, selecciona un usuario para eliminar.");
            return;
        }
        
        String nombreUsuario = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
        String ventasTexto = (String) modeloTabla.getValueAt(filaSeleccionada, 3);
        
        // Verificar si tiene ventas
        if (!ventasTexto.equals("0 ventas")) {
            UIUtils.mostrarError(this, 
                "No se puede eliminar el usuario '" + nombreUsuario + "' porque tiene ventas registradas.\n" +
                "Los usuarios con ventas se conservan para mantener el historial del negocio.");
            return;
        }
        
        if (UIUtils.confirmar(this, "¿Estás seguro de que deseas eliminar el usuario '" + nombreUsuario + "'?")) {
            Long usuarioId = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);
            
            String sql = "UPDATE usuarios SET activo = false WHERE id = ?";
            
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setLong(1, usuarioId);
                int filasAfectadas = pstmt.executeUpdate();
                
                if (filasAfectadas > 0) {
                    UIUtils.mostrarExito(this, "Usuario eliminado exitosamente.");
                    cargarUsuarios(); // Recargar la tabla
                } else {
                    UIUtils.mostrarError(this, "No se pudo eliminar el usuario.");
                }
                
            } catch (SQLException e) {
                UIUtils.mostrarError(this, "Error al eliminar usuario: " + e.getMessage());
            }
        }
    }
}
