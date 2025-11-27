package com.minimarket.patterns.structural.decorator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

/**
 * Decorador que envía notificaciones por email
 * Útil para alertas que requieren notificación a supervisores o administradores
 */
public class DecoradorEmail extends DecoradorNotificacion {
    
    private List<String> destinatarios;
    private String asunto;
    
    public DecoradorEmail(NotificacionBase notificacion, List<String> destinatarios, String asunto) {
        super(notificacion);
        this.destinatarios = new ArrayList<>(destinatarios);
        this.asunto = asunto;
    }
    
    public DecoradorEmail(NotificacionBase notificacion, String destinatario) {
        super(notificacion);
        this.destinatarios = new ArrayList<>();
        this.destinatarios.add(destinatario);
        this.asunto = "Notificación del Sistema - " + notificacion.getTipo().getDescripcion();
    }
    
    @Override
    public void mostrar() {
        super.mostrar();
        enviarEmail();
    }
    
    @Override
    public String getContenido() {
        return super.getContenido() + " [📧 Email enviado]";
    }
    
    /**
     * Simula el envío de email
     */
    private void enviarEmail() {
        System.out.println("📧 Enviando email...");
        System.out.println("   Para: " + String.join(", ", destinatarios));
        System.out.println("   Asunto: " + asunto);
        System.out.println("   Contenido:");
        System.out.println("   " + generarContenidoEmail());
        System.out.println("✅ Email enviado exitosamente");
    }
    
    /**
     * Genera el contenido completo del email
     */
    private String generarContenidoEmail() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        
        StringBuilder contenido = new StringBuilder();
        contenido.append("NOTIFICACIÓN DEL SISTEMA DE VENTAS\n");
        contenido.append("=====================================\n\n");
        contenido.append("Tipo: ").append(notificacion.getTipo().getDescripcion()).append("\n");
        contenido.append("Fecha y Hora: ").append(LocalDateTime.now().format(formatter)).append("\n");
        contenido.append("Mensaje: ").append(notificacion.getContenido()).append("\n\n");
        
        // Agregar información adicional según el tipo
        switch (notificacion.getTipo()) {
            case ALERTA:
                contenido.append("⚠️ ACCIÓN REQUERIDA: Esta alerta requiere atención inmediata.\n");
                contenido.append("Por favor, revise el sistema y tome las medidas necesarias.\n");
                break;
            case ERROR:
                contenido.append("❌ ERROR CRÍTICO: Se ha detectado un error en el sistema.\n");
                contenido.append("Revise los logs y contacte al soporte técnico si es necesario.\n");
                break;
            case ADVERTENCIA:
                contenido.append("⚠️ ADVERTENCIA: Situación que requiere supervisión.\n");
                contenido.append("Monitoree la situación y tome acción preventiva si es necesario.\n");
                break;
        }
        
        contenido.append("\n");
        contenido.append("Este es un mensaje automático del Sistema de Ventas del Minimarket.\n");
        contenido.append("No responda a este email.\n");
        
        return contenido.toString();
    }
    
    /**
     * Agrega un destinatario adicional
     */
    public void agregarDestinatario(String email) {
        if (!destinatarios.contains(email)) {
            destinatarios.add(email);
        }
    }
    
    /**
     * Configura destinatarios según el tipo de notificación
     */
    public void configurarDestinatariosAutomaticos() {
        switch (notificacion.getTipo()) {
            case ALERTA:
            case ERROR:
                // Notificar a administradores y supervisores
                agregarDestinatario("admin@minimarket.com");
                agregarDestinatario("supervisor@minimarket.com");
                break;
            case ADVERTENCIA:
                // Notificar solo a supervisores
                agregarDestinatario("supervisor@minimarket.com");
                break;
            case INFO:
            case EXITO:
                // No enviar emails para notificaciones informativas
                break;
        }
    }
    
    public List<String> getDestinatarios() {
        return new ArrayList<>(destinatarios);
    }
    
    public String getAsunto() {
        return asunto;
    }
}
