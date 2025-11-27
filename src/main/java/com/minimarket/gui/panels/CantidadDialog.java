package com.minimarket.gui.panels;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo para seleccionar cantidad de productos
 */
public class CantidadDialog extends JDialog {
    
    private JSpinner spinnerCantidad;
    private boolean confirmado = false;
    private int cantidadSeleccionada = 1;
    
    public CantidadDialog(JFrame parent, String nombreProducto, int stockDisponible) {
        super(parent, "Seleccionar Cantidad", true);
        initializeComponents(nombreProducto, stockDisponible);
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents(String nombreProducto, int stockDisponible) {
        setLayout(new BorderLayout());
        setSize(450, 250);
        setResizable(false);
        
        // Panel principal
        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Título del producto (sin HTML para evitar saltos de línea)
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JLabel labelProducto = new JLabel(nombreProducto);
        labelProducto.setFont(new Font("Arial", Font.BOLD, 15));
        panelPrincipal.add(labelProducto, gbc);
        
        // Stock disponible
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JLabel labelStock = new JLabel("Stock disponible: " + stockDisponible + " unidades");
        labelStock.setFont(new Font("Arial", Font.PLAIN, 12));
        labelStock.setForeground(new Color(100, 100, 100));
        panelPrincipal.add(labelStock, gbc);
        
        // Cantidad
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.EAST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel labelCantidad = new JLabel("Cantidad:");
        labelCantidad.setFont(new Font("Arial", Font.BOLD, 13));
        panelPrincipal.add(labelCantidad, gbc);
        
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        spinnerCantidad = new JSpinner(new SpinnerNumberModel(1, 1, stockDisponible, 1));
        spinnerCantidad.setFont(new Font("Arial", Font.PLAIN, 13));
        spinnerCantidad.setPreferredSize(new Dimension(100, 28));
        ((JSpinner.DefaultEditor) spinnerCantidad.getEditor()).getTextField().setHorizontalAlignment(JTextField.CENTER);
        panelPrincipal.add(spinnerCantidad, gbc);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnAgregar = new JButton("Agregar al Carrito");
        
        // Usar estilo default de Swing pero con mejor tamaño
        btnCancelar.setFont(new Font("Arial", Font.PLAIN, 12));
        btnCancelar.setPreferredSize(new Dimension(100, 32));
        
        btnAgregar.setFont(new Font("Arial", Font.BOLD, 12));
        btnAgregar.setPreferredSize(new Dimension(150, 32));
        
        btnAgregar.addActionListener(e -> {
            cantidadSeleccionada = (Integer) spinnerCantidad.getValue();
            confirmado = true;
            dispose();
        });
        
        btnCancelar.addActionListener(e -> dispose());
        
        panelBotones.add(btnCancelar);
        panelBotones.add(btnAgregar);
        
        add(panelPrincipal, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
        
        // Configurar para que Enter confirme
        getRootPane().setDefaultButton(btnAgregar);
        
        // Seleccionar el texto del spinner
        SwingUtilities.invokeLater(() -> {
            ((JSpinner.DefaultEditor) spinnerCantidad.getEditor()).getTextField().selectAll();
            spinnerCantidad.requestFocus();
        });
    }
    
    public boolean isConfirmado() {
        return confirmado;
    }
    
    public int getCantidadSeleccionada() {
        return cantidadSeleccionada;
    }
}
