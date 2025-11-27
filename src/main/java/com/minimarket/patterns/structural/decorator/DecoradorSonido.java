package com.minimarket.patterns.structural.decorator;

/**
 * Decorador que agrega sonido a las notificaciones
 * Útil para alertas críticas que requieren atención inmediata
 */
public class DecoradorSonido extends DecoradorNotificacion {
    
    private String tipoSonido;
    
    public DecoradorSonido(NotificacionBase notificacion, String tipoSonido) {
        super(notificacion);
        this.tipoSonido = tipoSonido;
    }
    
    public DecoradorSonido(NotificacionBase notificacion) {
        super(notificacion);
        // Sonido por defecto según el tipo de notificación
        this.tipoSonido = determinarSonidoPorTipo(notificacion.getTipo());
    }
    
    @Override
    public void mostrar() {
        reproducirSonido();
        super.mostrar();
    }
    
    @Override
    public String getContenido() {
        return super.getContenido() + " [🔊 " + tipoSonido + "]";
    }
    
    /**
     * Simula la reproducción de sonido
     */
    private void reproducirSonido() {
        System.out.println("🔊 Reproduciendo sonido: " + tipoSonido);
        
        // Simular diferentes tipos de sonido
        switch (tipoSonido.toLowerCase()) {
            case "beep":
                System.out.println("   ♪ BEEP ♪");
                break;
            case "alerta":
                System.out.println("   ♪ BEEP BEEP BEEP ♪");
                break;
            case "error":
                System.out.println("   ♪ BUZZ BUZZ ♪");
                break;
            case "exito":
                System.out.println("   ♪ DING ♪");
                break;
            default:
                System.out.println("   ♪ NOTIFICATION SOUND ♪");
        }
    }
    
    /**
     * Determina el sonido apropiado según el tipo de notificación
     */
    private String determinarSonidoPorTipo(TipoNotificacion tipo) {
        switch (tipo) {
            case ERROR:
                return "error";
            case ALERTA:
                return "alerta";
            case EXITO:
                return "exito";
            case ADVERTENCIA:
                return "beep";
            case INFO:
            default:
                return "notification";
        }
    }
    
    public String getTipoSonido() {
        return tipoSonido;
    }
}
