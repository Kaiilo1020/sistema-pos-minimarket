package com.minimarket.ui.panels;

import com.minimarket.ui.handlers.UsuarioHandler;
import com.minimarket.ui.theme.EstilosApp;
import com.minimarket.ui.util.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel de gestión de usuarios - SOLO UI
 * Toda la lógica de negocio está delegada a UsuarioHandler
 */
public class UsuariosPanel extends JPanel {
    
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;
    private JButton btnCrearUsuario;
    private JButton btnEliminarUsuario;
    private JButton btnActualizar;
    private JLabel labelTotalUsuarios;
    
    // Handler que contiene toda la lógica
    private final UsuarioHandler usuarioHandler;
    
    public UsuariosPanel() {
        this.usuarioHandler = new UsuarioHandler();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        add(crearPanelBotonera(), BorderLayout.NORTH);
        add(crearTablaUsuarios(), BorderLayout.CENTER);
        add(crearPanelResumen(), BorderLayout.SOUTH);
        cargarUsuarios();
    }
    
    private JPanel crearPanelBotonera() {
        JPanel panelSuperior = UIUtils.configurarPanel(new BorderLayout(), 10);
        JPanel panelBotones = UIUtils.crearPanelBotones(FlowLayout.LEFT);
        
        btnCrearUsuario = new JButton("Crear Usuario");
        btnEliminarUsuario = new JButton("Eliminar Usuario");
        btnActualizar = new JButton("Actualizar");
        EstilosApp.estilizarBoton(btnCrearUsuario);
        EstilosApp.estilizarBotonError(btnEliminarUsuario);
        EstilosApp.estilizarBotonNeutro(btnActualizar);
        
        btnCrearUsuario.addActionListener(e -> abrirDialogoCrearUsuario());
        btnEliminarUsuario.addActionListener(e -> eliminarUsuarioSeleccionado());
        btnActualizar.addActionListener(e -> cargarUsuarios());
        
        panelBotones.add(btnCrearUsuario);
        panelBotones.add(btnEliminarUsuario);
        panelBotones.add(btnActualizar);
        panelSuperior.add(panelBotones, BorderLayout.WEST);
        return panelSuperior;
    }
    
    private JScrollPane crearTablaUsuarios() {
        String[] columnas = {"ID", "Usuario", "Rol", "Ventas Registradas"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaUsuarios = new JTable(modeloTabla);
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaUsuarios.setRowHeight(30);
        UIUtils.configurarTabla(tablaUsuarios);
        UIUtils.configurarColumnasTabla(tablaUsuarios, 50, 200, 150, 150);
        
        JScrollPane scrollPane = new JScrollPane(tablaUsuarios);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Lista de Usuarios"));
        return scrollPane;
    }
    
    private JPanel crearPanelResumen() {
        JPanel panelInferior = UIUtils.configurarPanel(new BorderLayout(), 10);
        JPanel panelInfo = UIUtils.crearPanelBotones(FlowLayout.CENTER);
        labelTotalUsuarios = new JLabel("Total: 0 usuarios");
        labelTotalUsuarios.setFont(UIUtils.BOLD_FONT);
        panelInfo.add(labelTotalUsuarios);
        
        JPanel panelNota = UIUtils.crearPanelBotones(FlowLayout.LEFT);
        JLabel labelNota = new JLabel("<html><b>Nota:</b> Solo se pueden eliminar usuarios que NO tengan ventas registradas.<br>" +
                "Si un trabajador tiene ventas, se conservan para el historial del negocio.</html>");
        labelNota.setFont(UIUtils.DEFAULT_FONT);
        labelNota.setForeground(Color.GRAY);
        panelNota.add(labelNota);
        
        panelInferior.add(panelInfo, BorderLayout.CENTER);
        panelInferior.add(panelNota, BorderLayout.SOUTH);
        return panelInferior;
    }
    
    /* ========================== DELEGACIÓN A HANDLER ========================== */
    
    private void cargarUsuarios() {
        try {
            var usuarios = usuarioHandler.cargarUsuarios();
            modeloTabla.setRowCount(0);
            
            for (com.minimarket.dao.UsuarioDAO.UsuarioInfo info : usuarios) {
                String rolMostrar = switch (info.rol) {
                    case "ADMINISTRADOR" -> "Administrador";
                    case "SUPERVISOR" -> "Supervisor";
                    case "CAJERO" -> "Trabajador";
                    default -> info.rol;
                };
                
                String ventasTexto = info.totalVentas + " venta" + (info.totalVentas != 1 ? "s" : "");
                
                Object[] fila = {
                    info.id,
                    info.username,
                    rolMostrar,
                    ventasTexto
                };
                modeloTabla.addRow(fila);
            }
            
            labelTotalUsuarios.setText("Total: " + usuarios.size() + " usuarios");
            
        } catch (Exception e) {
            UIUtils.mostrarError(this, "Error al cargar usuarios: " + e.getMessage());
        }
    }
    
    private void abrirDialogoCrearUsuario() {
        UsuarioDialog dialog = new UsuarioDialog((JFrame) SwingUtilities.getWindowAncestor(this), 
                                                 "Crear Usuario", true, null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmado()) {
            cargarUsuarios();
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
            try {
                usuarioHandler.eliminarUsuario(usuarioId, this);
                cargarUsuarios();
            } catch (RuntimeException e) {
                // El error ya fue mostrado por el handler
            }
        }
    }
}
