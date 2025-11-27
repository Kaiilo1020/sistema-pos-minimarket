package com.minimarket.patterns.structural.decorator;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Decorador que registra notificaciones en archivo de log
 * Útil para auditoría y seguimiento de eventos del sistema
 */
public class DecoradorLog extends DecoradorNotificacion {
    
    private String archivoLog;
    private boolean incluirStackTrace;
    
    public DecoradorLog(NotificacionBase notificacion, String archivoLog) {
        super(notificacion);
        this.archivoLog = archivoLog != null ? archivoLog : "sistema_notificaciones.log";
        this.incluirStackTrace = false;
    }
    
    public DecoradorLog(NotificacionBase notificacion) {
        this(notificacion, "sistema_notificaciones.log");
    }
    
    @Override
    public void mostrar() {
        super.mostrar();
        registrarEnLog();
    }
    
    @Override
    public String getContenido() {
        return super.getContenido() + " [📝 Registrado en log]";
    }
    
    /**
     * Registra la notificación en el archivo de log
     */
    private void registrarEnLog() {
        try {
            String entradaLog = generarEntradaLog();
            escribirEnArchivo(entradaLog);
            System.out.println("📝 Notificación registrada en log: " + archivoLog);
        } catch (IOException e) {
            System.err.println("❌ Error al escribir en el archivo de log: " + e.getMessage());
        }
    }
    
    /**
     * Genera la entrada completa para el log
     */
    private String generarEntradaLog() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime ahora = LocalDateTime.now();
        
        StringBuilder entrada = new StringBuilder();
        
        // Encabezado con timestamp
        entrada.append("[").append(ahora.format(formatter)).append("] ");
        entrada.append("[").append(notificacion.getTipo().name()).append("] ");
        
        // Contenido de la notificación
        entrada.append(notificacion.getContenido());
        
        // Información adicional según el tipo
        switch (notificacion.getTipo()) {
            case ERROR:
                entrada.append(" | NIVEL: CRÍTICO");
                if (incluirStackTrace) {
                    entrada.append(" | STACK_TRACE: ").append(obtenerStackTrace());
                }
                break;
            case ALERTA:
                entrada.append(" | NIVEL: ALTO");
                break;
            case ADVERTENCIA:
                entrada.append(" | NIVEL: MEDIO");
                break;
            case INFO:
                entrada.append(" | NIVEL: BAJO");
                break;
            case EXITO:
                entrada.append(" | NIVEL: INFO");
                break;
        }
        
        // Información del sistema
        entrada.append(" | THREAD: ").append(Thread.currentThread().getName());
        entrada.append(" | MEMORIA: ").append(obtenerUsoMemoria());
        
        entrada.append("\n");
        
        return entrada.toString();
    }
    
    /**
     * Escribe la entrada en el archivo de log
     */
    private void escribirEnArchivo(String entrada) throws IOException {
        try (FileWriter writer = new FileWriter(archivoLog, true)) {
            writer.write(entrada);
            writer.flush();
        }
    }
    
    /**
     * Obtiene información del stack trace actual
     */
    private String obtenerStackTrace() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        if (stack.length > 3) {
            StackTraceElement elemento = stack[3]; // Evitar métodos internos del decorador
            return elemento.getClassName() + "." + elemento.getMethodName() + 
                   "(" + elemento.getFileName() + ":" + elemento.getLineNumber() + ")";
        }
        return "N/A";
    }
    
    /**
     * Obtiene información básica del uso de memoria
     */
    private String obtenerUsoMemoria() {
        Runtime runtime = Runtime.getRuntime();
        long memoriaUsada = runtime.totalMemory() - runtime.freeMemory();
        long memoriaTotal = runtime.totalMemory();
        
        return String.format("%.1f%%", (memoriaUsada * 100.0) / memoriaTotal);
    }
    
    /**
     * Configura si incluir stack trace en los logs
     */
    public DecoradorLog conStackTrace(boolean incluir) {
        this.incluirStackTrace = incluir;
        return this;
    }
    
    /**
     * Método estático para crear logs específicos por tipo
     */
    public static DecoradorLog crearLogError(NotificacionBase notificacion) {
        return new DecoradorLog(notificacion, "errores.log").conStackTrace(true);
    }
    
    public static DecoradorLog crearLogVentas(NotificacionBase notificacion) {
        return new DecoradorLog(notificacion, "ventas.log");
    }
    
    public static DecoradorLog crearLogInventario(NotificacionBase notificacion) {
        return new DecoradorLog(notificacion, "inventario.log");
    }
    
    public static DecoradorLog crearLogSeguridad(NotificacionBase notificacion) {
        return new DecoradorLog(notificacion, "seguridad.log").conStackTrace(true);
    }
    
    public String getArchivoLog() {
        return archivoLog;
    }
}
