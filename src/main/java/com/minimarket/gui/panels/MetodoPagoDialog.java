package com.minimarket.gui.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Diálogo para seleccionar método de pago
 */
public class MetodoPagoDialog extends JDialog {
    
    private JComboBox<String> comboMetodoPago;
    private JTextField campoReferencia;
    private boolean confirmado = false;
    private String metodoPagoSeleccionado;
    
    public MetodoPagoDialog(JFrame parent) {
        super(parent, "Método de Pago", true);
        initializeComponents();
        setLocationRelativeTo(parent);
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setSize(400, 200);
        
        // Panel principal
        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Método de pago
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        panelPrincipal.add(new JLabel("Método de Pago:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        comboMetodoPago = new JComboBox<>(new String[]{
            "EFECTIVO", "TARJETA_DEBITO", "TARJETA_CREDITO", 
            "YAPE", "PLIN", "TRANSFERENCIA"
        });
        panelPrincipal.add(comboMetodoPago, gbc);
        
        // Referencia (opcional)
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panelPrincipal.add(new JLabel("Referencia (opcional):"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        campoReferencia = new JTextField();
        campoReferencia.setToolTipText("Número de operación, voucher, etc.");
        panelPrincipal.add(campoReferencia, gbc);
        
        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnConfirmar = new JButton("Confirmar Venta");
        JButton btnCancelar = new JButton("Cancelar");
        
        btnConfirmar.setBackground(new Color(46, 125, 50));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFocusPainted(false);
        
        btnCancelar.setBackground(new Color(158, 158, 158));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        
        btnConfirmar.addActionListener(e -> {
            metodoPagoSeleccionado = (String) comboMetodoPago.getSelectedItem();
            confirmado = true;
            dispose();
        });
        
        btnCancelar.addActionListener(e -> dispose());
        
        panelBotones.add(btnConfirmar);
        panelBotones.add(btnCancelar);
        
        add(panelPrincipal, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }
    
    public boolean isConfirmado() {
        return confirmado;
    }
    
    public String getMetodoPagoSeleccionado() {
        return metodoPagoSeleccionado;
    }
    
    public String getReferencia() {
        return campoReferencia.getText().trim();
    }
}
