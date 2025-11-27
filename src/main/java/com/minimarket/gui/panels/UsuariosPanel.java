package com.minimarket.gui.panels;

import com.minimarket.patterns.creational.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * Panel de Usuarios/Vendedores - Gestión de usuarios del sistema
 * Replica la funcionalidad mostrada en la imagen
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
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(Color.WHITE);
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBotones.setBackground(Color.WHITE);
        
        btnCrearUsuario = new JButton("Crear Usuario");
        btnEliminarUsuario = new JButton("Eliminar Usuario");
        btnActualizar = new JButton("Actualizar");
        
        // Estilo de botones
        configurarBoton(btnCrearUsuario, new Color(46, 125, 50));
        configurarBoton(btnEliminarUsuario, new Color(211, 47, 47));
        configurarBoton(btnActualizar, new Color(117, 117, 117));
        
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
        tablaUsuarios.getTableHeader().setBackground(new Color(63, 81, 181));
        tablaUsuarios.getTableHeader().setForeground(Color.WHITE);
        tablaUsuarios.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        // Configurar ancho de columnas
        tablaUsuarios.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tablaUsuarios.getColumnModel().getColumn(1).setPreferredWidth(200); // Usuario
        tablaUsuarios.getColumnModel().getColumn(2).setPreferredWidth(150); // Rol
        tablaUsuarios.getColumnModel().getColumn(3).setPreferredWidth(150); // Ventas
        
        JScrollPane scrollPane = new JScrollPane(tablaUsuarios);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista de Usuarios"));
        
        // Panel inferior con información
        JPanel panelInferior = new JPanel(new BorderLayout());
        panelInferior.setBackground(Color.WHITE);
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Navegación (simulada)
        JPanel panelNavegacion = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelNavegacion.setBackground(Color.WHITE);
        
        JButton btnPrimera = new JButton("<< Primera");
        JButton btnAnterior = new JButton("< Anterior");
        labelTotalUsuarios = new JLabel("Total: 0 usuarios");
        JButton btnSiguiente = new JButton("Siguiente >");
        JButton btnUltima = new JButton("Última >>");
        
        // Estilo de botones de navegación
        configurarBotonNavegacion(btnPrimera);
        configurarBotonNavegacion(btnAnterior);
        configurarBotonNavegacion(btnSiguiente);
        configurarBotonNavegacion(btnUltima);
        
        labelTotalUsuarios.setFont(new Font("Arial", Font.BOLD, 12));
        
        panelNavegacion.add(btnPrimera);
        panelNavegacion.add(btnAnterior);
        panelNavegacion.add(labelTotalUsuarios);
        panelNavegacion.add(btnSiguiente);
        panelNavegacion.add(btnUltima);
        
        // Nota informativa
        JPanel panelNota = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelNota.setBackground(Color.WHITE);
        
        JLabel labelNota = new JLabel("<html><b>Nota:</b> Solo se pueden eliminar usuarios que NO tengan ventas registradas.<br>" +
                                     "Si un trabajador tiene ventas, se conservan para el historial del negocio.</html>");
        labelNota.setFont(new Font("Arial", Font.PLAIN, 11));
        labelNota.setForeground(new Color(100, 100, 100));
        
        panelNota.add(labelNota);
        
        panelInferior.add(panelNavegacion, BorderLayout.CENTER);
        panelInferior.add(panelNota, BorderLayout.SOUTH);
        
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    private void configurarBoton(JButton boton, Color color) {
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setFont(new Font("Arial", Font.BOLD, 11));
        boton.setPreferredSize(new Dimension(120, 35));
    }
    
    private void configurarBotonNavegacion(JButton boton) {
        boton.setBackground(Color.WHITE);
        boton.setForeground(new Color(63, 81, 181));
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createLineBorder(new Color(63, 81, 181)));
        boton.setFont(new Font("Arial", Font.PLAIN, 11));
        boton.setPreferredSize(new Dimension(80, 30));
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
            JOptionPane.showMessageDialog(this, 
                "Error al cargar usuarios: " + e.getMessage(), 
                "Error de Base de Datos", 
                JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, 
                "Por favor, selecciona un usuario para eliminar.", 
                "Selección Requerida", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String nombreUsuario = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
        String ventasTexto = (String) modeloTabla.getValueAt(filaSeleccionada, 3);
        
        // Verificar si tiene ventas
        if (!ventasTexto.equals("0 ventas")) {
            JOptionPane.showMessageDialog(this, 
                "No se puede eliminar el usuario '" + nombreUsuario + "' porque tiene ventas registradas.\n" +
                "Los usuarios con ventas se conservan para mantener el historial del negocio.", 
                "No se puede eliminar", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirmacion = JOptionPane.showConfirmDialog(this, 
            "¿Estás seguro de que deseas eliminar el usuario '" + nombreUsuario + "'?", 
            "Confirmar Eliminación", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            Long usuarioId = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);
            
            String sql = "UPDATE usuarios SET activo = false WHERE id = ?";
            
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setLong(1, usuarioId);
                int filasAfectadas = pstmt.executeUpdate();
                
                if (filasAfectadas > 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Usuario eliminado exitosamente.", 
                        "Eliminación Exitosa", 
                        JOptionPane.INFORMATION_MESSAGE);
                    cargarUsuarios(); // Recargar la tabla
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "No se pudo eliminar el usuario.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error al eliminar usuario: " + e.getMessage(), 
                    "Error de Base de Datos", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
